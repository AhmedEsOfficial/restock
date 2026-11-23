# Sprint 5: Premium Launch - Billing Integration
## Phase 2, Month 6, Weeks 1-2

**Sprint Duration:** 2 weeks
**Sprint Number:** 5 of 12 (Phase 2)
**Team:** Tech Lead, UI/UX Engineer, Backend Developers (2), DevOps Engineer

---

## 1. Sprint Overview

This sprint implements the Google Play Billing integration for the premium subscription tier. By the end of this sprint, users will be able to purchase monthly or annual subscriptions, and the backend will verify and track subscription status.

### Sprint Goal
> *"Implement secure, reliable Google Play Billing integration with backend verification for premium subscriptions."*

### Key Outcomes
- Google Play Billing Library integrated
- Subscription products configured in Play Console
- Backend subscription verification operational
- Purchase flow working end-to-end
- Subscription state tracked per user
- Basic premium feature gating implemented

### Prerequisites from Sprint 4
- Cloud sync operational
- User authentication working
- Account management UI complete
- User tier already tracked in backend

---

## 2. Sprint Backlog

### 2.1 User Stories

#### US-5.1: Google Play Billing Setup
**As a** developer
**I want** Google Play Billing Library properly configured
**So that** we can process in-app purchases

**Acceptance Criteria:**
- [ ] BillingClient initialized with app context
- [ ] Pending purchases enabled
- [ ] Purchase listener configured
- [ ] Connection state managed (connect, disconnect, retry)
- [ ] Handles billing unavailable gracefully
- [ ] Supports Google Play billing library 6.0+

**Story Points:** 5
**Assignee:** Tech Lead

**BillingClient Setup:**
```kotlin
@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val subscriptionRepository: SubscriptionRepository,
) {
    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    private val _billingState = MutableStateFlow<BillingState>(BillingState.Disconnected)
    val billingState: StateFlow<BillingState> = _billingState.asStateFlow()

    private val purchasesUpdatedListener = PurchasesUpdatedListener { result, purchases ->
        when (result.responseCode) {
            BillingResponseCode.OK -> handlePurchases(purchases)
            BillingResponseCode.USER_CANCELED -> handleUserCanceled()
            else -> handleError(result)
        }
    }

    fun connect() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingResponseCode.OK) {
                    _billingState.value = BillingState.Connected
                    queryExistingPurchases()
                } else {
                    _billingState.value = BillingState.Error(result.debugMessage)
                }
            }

            override fun onBillingServiceDisconnected() {
                _billingState.value = BillingState.Disconnected
                // Retry with exponential backoff
            }
        })
    }
}
```

---

#### US-5.2: Subscription Products Configuration
**As a** product manager
**I want** subscription products defined in Play Console
**So that** users can subscribe to premium

**Acceptance Criteria:**
- [ ] Monthly subscription product created ($4.99/month)
- [ ] Annual subscription product created ($39.99/year)
- [ ] Base plans configured with pricing
- [ ] Free trial configured (7 days, optional - TBD)
- [ ] Grace period set (3 days)
- [ ] Account hold enabled
- [ ] Products activated for testing

**Story Points:** 3
**Assignee:** Tech Lead, Product

**Product IDs:**
```kotlin
object SubscriptionProducts {
    const val PREMIUM_MONTHLY = "restock_premium_monthly"
    const val PREMIUM_ANNUAL = "restock_premium_annual"

    const val MONTHLY_PRICE_USD = "$4.99"
    const val ANNUAL_PRICE_USD = "$39.99"
    const val ANNUAL_SAVINGS_PERCENT = "33%"
}
```

---

#### US-5.3: Query Available Products
**As a** user viewing premium options
**I want** to see accurate pricing for my region
**So that** I know exactly what I'll pay

**Acceptance Criteria:**
- [ ] Query product details from Play Store
- [ ] Display localized prices
- [ ] Handle product not found
- [ ] Cache product details (1 hour TTL)
- [ ] Show annual savings percentage

**Story Points:** 5
**Assignee:** Tech Lead

**API:**
```kotlin
suspend fun queryProductDetails(): Result<List<ProductDetails>> {
    val params = QueryProductDetailsParams.newBuilder()
        .setProductList(
            listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(PREMIUM_MONTHLY)
                    .setProductType(ProductType.SUBS)
                    .build(),
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(PREMIUM_ANNUAL)
                    .setProductType(ProductType.SUBS)
                    .build()
            )
        )
        .build()

    return suspendCoroutine { cont ->
        billingClient.queryProductDetailsAsync(params) { result, products ->
            if (result.responseCode == BillingResponseCode.OK) {
                cont.resume(Result.success(products))
            } else {
                cont.resume(Result.failure(BillingException(result)))
            }
        }
    }
}
```

---

#### US-5.4: Launch Purchase Flow
**As a** user
**I want** to purchase a premium subscription
**So that** I can access premium features

**Acceptance Criteria:**
- [ ] Launch billing flow from premium screen
- [ ] Support both monthly and annual options
- [ ] Handle successful purchase
- [ ] Handle user cancellation
- [ ] Handle errors with user-friendly messages
- [ ] Prevent duplicate purchases

**Story Points:** 8
**Assignee:** Tech Lead

**Purchase Flow:**
```kotlin
suspend fun launchPurchaseFlow(
    activity: Activity,
    productDetails: ProductDetails,
    selectedOfferToken: String
): Result<Unit> {
    val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
        .setProductDetails(productDetails)
        .setOfferToken(selectedOfferToken)
        .build()

    val billingFlowParams = BillingFlowParams.newBuilder()
        .setProductDetailsParamsList(listOf(productDetailsParams))
        .build()

    val result = billingClient.launchBillingFlow(activity, billingFlowParams)

    return if (result.responseCode == BillingResponseCode.OK) {
        Result.success(Unit)
    } else {
        Result.failure(BillingException(result))
    }
}
```

---

#### US-5.5: Process Purchases
**As a** system
**I want** to process completed purchases
**So that** users get access to premium features

**Acceptance Criteria:**
- [ ] Acknowledge purchases within 3 days (Google requirement)
- [ ] Verify purchase on backend before acknowledging
- [ ] Update local subscription state
- [ ] Handle already-owned scenarios
- [ ] Log purchase events for analytics

**Story Points:** 8
**Assignee:** Tech Lead, Backend Developer 1

**Purchase Processing:**
```kotlin
private suspend fun handlePurchases(purchases: List<Purchase>?) {
    purchases?.forEach { purchase ->
        when (purchase.purchaseState) {
            PurchaseState.PURCHASED -> {
                if (!purchase.isAcknowledged) {
                    // Verify on backend first
                    val verifyResult = subscriptionRepository.verifyPurchase(
                        purchaseToken = purchase.purchaseToken,
                        productId = purchase.products.first()
                    )

                    if (verifyResult.isSuccess) {
                        // Acknowledge with Google
                        acknowledgePurchase(purchase)
                        // Update local state
                        _subscriptionState.value = SubscriptionState.Premium
                    }
                }
            }
            PurchaseState.PENDING -> {
                // Inform user purchase is pending
                _purchaseEvents.emit(PurchaseEvent.Pending)
            }
        }
    }
}

private suspend fun acknowledgePurchase(purchase: Purchase) {
    val params = AcknowledgePurchaseParams.newBuilder()
        .setPurchaseToken(purchase.purchaseToken)
        .build()

    billingClient.acknowledgePurchase(params) { result ->
        if (result.responseCode != BillingResponseCode.OK) {
            // Log error, will retry on next app launch
        }
    }
}
```

---

#### US-5.6: Backend Purchase Verification
**As a** security measure
**I want** purchases verified on the server
**So that** we prevent fraud and receipt manipulation

**Acceptance Criteria:**
- [ ] Endpoint receives purchase token and product ID
- [ ] Verifies with Google Play Developer API
- [ ] Checks purchase not already used
- [ ] Updates user subscription status in database
- [ ] Returns verification result
- [ ] Handles verification failures gracefully

**Story Points:** 8
**Assignee:** Backend Developer 1

**API Specification:**
```
POST /api/v1/subscription/verify
Authorization: Bearer <access_token>
Content-Type: application/json

Request:
{
    "purchase_token": "google-play-purchase-token",
    "product_id": "restock_premium_monthly"
}

Response (200 OK):
{
    "status": "verified",
    "subscription": {
        "tier": "premium",
        "product_id": "restock_premium_monthly",
        "starts_at": "2026-06-01T10:00:00Z",
        "expires_at": "2026-07-01T10:00:00Z",
        "auto_renewing": true,
        "grace_period": false
    }
}

Response (400 Bad Request):
{
    "error": "invalid_purchase",
    "message": "Purchase token is invalid or already used"
}
```

**Backend Implementation:**
```kotlin
class SubscriptionVerificationService(
    private val playDeveloperApi: AndroidPublisher,
    private val subscriptionRepository: SubscriptionRepository,
) {
    suspend fun verifyPurchase(
        userId: String,
        purchaseToken: String,
        productId: String
    ): VerificationResult {
        // Query Google Play Developer API
        val subscription = playDeveloperApi
            .purchases()
            .subscriptions()
            .get(PACKAGE_NAME, productId, purchaseToken)
            .execute()

        // Validate subscription state
        if (subscription.paymentState != 1) { // Not paid
            return VerificationResult.Failed("Payment not completed")
        }

        // Check not already used by another user
        val existingUse = subscriptionRepository.findByPurchaseToken(purchaseToken)
        if (existingUse != null && existingUse.userId != userId) {
            return VerificationResult.Failed("Purchase token already used")
        }

        // Update user subscription
        subscriptionRepository.updateSubscription(
            userId = userId,
            tier = SubscriptionTier.PREMIUM,
            productId = productId,
            purchaseToken = purchaseToken,
            expiresAt = Instant.ofEpochMilli(subscription.expiryTimeMillis)
        )

        return VerificationResult.Success(subscription)
    }
}
```

---

#### US-5.7: Webhook for Subscription Events
**As a** backend service
**I want** to receive Google Play real-time notifications
**So that** we can track subscription state changes

**Acceptance Criteria:**
- [ ] Pub/Sub topic configured for RTDN
- [ ] Webhook endpoint receives notifications
- [ ] Handles SUBSCRIPTION_RENEWED
- [ ] Handles SUBSCRIPTION_CANCELED
- [ ] Handles SUBSCRIPTION_EXPIRED
- [ ] Handles SUBSCRIPTION_RECOVERED (from grace period)
- [ ] Handles SUBSCRIPTION_ON_HOLD
- [ ] Updates database on each event
- [ ] Idempotent processing

**Story Points:** 8
**Assignee:** Backend Developer 2

**Notification Types:**
```kotlin
enum class SubscriptionNotificationType(val value: Int) {
    RECOVERED(1),        // Recovered from account hold
    RENEWED(2),          // Renewed successfully
    CANCELED(3),         // Canceled by user
    PURCHASED(4),        // New purchase
    ON_HOLD(5),          // Account hold started
    IN_GRACE_PERIOD(6),  // In grace period
    RESTARTED(7),        // Resubscribed
    PRICE_CHANGE_CONFIRMED(8),
    DEFERRED(9),         // Pending purchase
    PAUSED(10),          // Subscription paused
    PAUSE_SCHEDULE_CHANGED(11),
    REVOKED(12),         // Subscription revoked
    EXPIRED(13)          // Subscription expired
}
```

**Webhook Handler:**
```kotlin
suspend fun handleNotification(notification: DeveloperNotification) {
    val subscriptionNotification = notification.subscriptionNotification
        ?: return // Not a subscription notification

    val purchaseToken = subscriptionNotification.purchaseToken
    val notificationType = subscriptionNotification.notificationType

    when (notificationType) {
        SubscriptionNotificationType.RENEWED.value -> {
            refreshSubscriptionFromGoogle(purchaseToken)
        }
        SubscriptionNotificationType.CANCELED.value -> {
            markSubscriptionCanceled(purchaseToken)
        }
        SubscriptionNotificationType.EXPIRED.value -> {
            expireSubscription(purchaseToken)
        }
        SubscriptionNotificationType.ON_HOLD.value -> {
            markOnHold(purchaseToken)
        }
        SubscriptionNotificationType.RECOVERED.value -> {
            refreshSubscriptionFromGoogle(purchaseToken)
        }
        // Handle other types...
    }
}
```

---

#### US-5.8: Restore Purchases
**As a** user who reinstalled the app
**I want** to restore my existing subscription
**So that** I don't lose access to premium features

**Acceptance Criteria:**
- [ ] Query existing purchases on app launch (logged in)
- [ ] Query existing purchases on "Restore" button tap
- [ ] Verify purchases on backend
- [ ] Update local subscription state
- [ ] Handle no purchases found
- [ ] Works across devices (same Google account)

**Story Points:** 5
**Assignee:** Tech Lead

**Restore Flow:**
```kotlin
suspend fun restorePurchases(): Result<SubscriptionState> {
    val params = QueryPurchasesParams.newBuilder()
        .setProductType(ProductType.SUBS)
        .build()

    return suspendCoroutine { cont ->
        billingClient.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingResponseCode.OK) {
                val activePurchase = purchases.find { purchase ->
                    purchase.purchaseState == PurchaseState.PURCHASED &&
                    purchase.isAutoRenewing
                }

                if (activePurchase != null) {
                    // Verify on backend
                    verifyAndUpdateState(activePurchase)
                    cont.resume(Result.success(SubscriptionState.Premium))
                } else {
                    cont.resume(Result.success(SubscriptionState.Free))
                }
            } else {
                cont.resume(Result.failure(BillingException(result)))
            }
        }
    }
}
```

---

#### US-5.9: Subscription State Management
**As a** developer
**I want** a single source of truth for subscription state
**So that** the app consistently knows user tier

**Acceptance Criteria:**
- [ ] SubscriptionRepository tracks current state
- [ ] State persisted locally for offline access
- [ ] State synced with backend on app launch
- [ ] State updated on purchase/webhook events
- [ ] StateFlow exposes current subscription
- [ ] Includes expiration date and grace period info

**Story Points:** 5
**Assignee:** Backend Developer 1

**Subscription State:**
```kotlin
sealed interface SubscriptionState {
    data object Loading : SubscriptionState
    data object Free : SubscriptionState
    data class Premium(
        val productId: String,
        val expiresAt: Instant,
        val isAutoRenewing: Boolean,
        val isInGracePeriod: Boolean = false,
        val isOnHold: Boolean = false
    ) : SubscriptionState
    data class Expired(val expiredAt: Instant) : SubscriptionState
}

@Singleton
class SubscriptionRepository @Inject constructor(
    private val subscriptionApi: SubscriptionApi,
    private val dataStore: DataStore<Preferences>,
    private val billingManager: BillingManager,
) {
    private val _subscriptionState = MutableStateFlow<SubscriptionState>(SubscriptionState.Loading)
    val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState.asStateFlow()

    suspend fun refreshSubscriptionState() {
        // First check local cache
        val cached = dataStore.data.first()[SUBSCRIPTION_KEY]
        if (cached != null) {
            _subscriptionState.value = parseSubscription(cached)
        }

        // Then verify with backend
        subscriptionApi.getSubscription()
            .onSuccess { subscription ->
                _subscriptionState.value = subscription.toState()
                cacheSubscription(subscription)
            }
            .onFailure {
                // Keep cached state, log error
            }
    }
}
```

---

### 2.2 Technical Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies |
|---------|------|----------|------------|--------------|
| T-5.1 | Add billing library to dependencies | Tech Lead | 1 | - |
| T-5.2 | Create BillingManager singleton | Tech Lead | 8 | T-5.1 |
| T-5.3 | Create subscription products in Play Console | Tech Lead | 4 | - |
| T-5.4 | Implement queryProductDetails | Tech Lead | 4 | T-5.2 |
| T-5.5 | Implement launchPurchaseFlow | Tech Lead | 6 | T-5.2 |
| T-5.6 | Implement purchase processing | Tech Lead | 8 | T-5.5 |
| T-5.7 | Create subscriptions table (backend) | Backend Dev 1 | 3 | - |
| T-5.8 | Implement Google Play Developer API client | Backend Dev 1 | 8 | - |
| T-5.9 | Create /subscription/verify endpoint | Backend Dev 1 | 8 | T-5.7, T-5.8 |
| T-5.10 | Create /subscription endpoint (GET status) | Backend Dev 1 | 4 | T-5.7 |
| T-5.11 | Set up Pub/Sub for RTDN | DevOps | 4 | - |
| T-5.12 | Create webhook endpoint | Backend Dev 2 | 8 | T-5.7, T-5.11 |
| T-5.13 | Implement webhook handlers | Backend Dev 2 | 8 | T-5.12 |
| T-5.14 | Implement restorePurchases | Tech Lead | 4 | T-5.2 |
| T-5.15 | Create SubscriptionRepository (Android) | Backend Dev 1 | 6 | T-5.9, T-5.10 |
| T-5.16 | Create SubscriptionViewModel | Backend Dev 1 | 4 | T-5.15 |
| T-5.17 | Design premium purchase UI | UI/UX | 10 | - |
| T-5.18 | Design subscription status UI | UI/UX | 6 | - |
| T-5.19 | Write BillingManager unit tests | Tech Lead | 6 | T-5.2 |
| T-5.20 | Write backend verification tests | Backend Dev 1 | 6 | T-5.9 |
| T-5.21 | Write webhook handler tests | Backend Dev 2 | 6 | T-5.13 |
| T-5.22 | Set up webhook monitoring | DevOps | 4 | T-5.12 |
| T-5.23 | Create test purchase documentation | Tech Lead | 2 | All |

---

## 3. Team Assignments

### Tech Lead
**Focus:** Android billing integration
- Set up Google Play Billing library
- Create BillingManager
- Implement purchase flow
- Implement restore purchases
- Configure products in Play Console
- Write billing unit tests

### UI/UX Engineer
**Focus:** Premium purchase UI design
- Design premium showcase screen
- Design pricing comparison
- Design purchase success/error states
- Design subscription status displays
- Prepare for Sprint 6 UI implementation

### Backend Developer 1
**Focus:** Backend verification and subscription API
- Create subscriptions database table
- Implement Google Play Developer API client
- Create verification endpoint
- Create subscription status endpoint
- Create SubscriptionRepository (Android)
- Write backend tests

### Backend Developer 2
**Focus:** Webhook integration
- Set up webhook endpoint
- Implement all notification type handlers
- Ensure idempotent processing
- Write webhook tests
- Handle edge cases

### DevOps Engineer
**Focus:** Infrastructure for billing
- Set up Cloud Pub/Sub for RTDN
- Configure webhook endpoint infrastructure
- Set up monitoring for billing events
- Secure webhook endpoint

---

## 4. Dependencies

### External Dependencies
| Dependency | Owner | Status | Risk |
|------------|-------|--------|------|
| Google Play Console access | Product | Required | Medium |
| Google Play Developer API credentials | DevOps | Required | Medium |
| Cloud Pub/Sub setup | DevOps | Required | Low |
| Test license accounts | Tech Lead | Required | Low |

### Internal Dependencies
| Dependency | From | To | Notes |
|------------|------|-----|-------|
| User authentication | Sprint 2 | All billing | JWT required |
| User database | Sprint 2 | Subscriptions table | FK relationship |

---

## 5. Definition of Done

### Sprint Level
- [ ] User can purchase monthly subscription
- [ ] User can purchase annual subscription
- [ ] Purchases verified on backend
- [ ] Subscription state tracked accurately
- [ ] Webhook receives and processes events
- [ ] Restore purchases works
- [ ] Test purchases working in internal testing
- [ ] All billing edge cases handled
- [ ] 80% test coverage for billing code

### Story Level
- [ ] Acceptance criteria met
- [ ] Unit tests passing
- [ ] Integration tests passing (with test purchases)
- [ ] Code reviewed
- [ ] Security review for payment code

---

## 6. Risk Assessment

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Play Console approval delays | Medium | High | Submit early; follow guidelines |
| Billing library bugs | Low | High | Use stable version; monitor issues |
| Webhook delivery failures | Medium | Medium | Idempotent handlers; retry logic |
| Purchase verification failures | Low | High | Graceful degradation; manual review |
| Test environment limitations | Medium | Medium | Use license testers; document process |

---

## 7. Testing Requirements

### Unit Tests
- BillingManager connection handling
- Purchase processing logic
- Subscription state transitions
- Backend verification logic
- Webhook handlers

### Integration Tests
- Full purchase flow (test mode)
- Restore purchases
- Subscription status API
- Webhook end-to-end

### Manual Testing
- Test purchases with license testers
- Subscription renewal (sandbox)
- Cancellation flow
- Grace period behavior

### Test Coverage Targets
| Component | Target |
|-----------|--------|
| BillingManager | 80% |
| SubscriptionRepository | 85% |
| Backend verification | 90% |
| Webhook handlers | 90% |

---

## 8. Success Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| Purchase success rate | >95% | Analytics |
| Verification latency | <500ms | API metrics |
| Webhook processing time | <1 second | Monitoring |
| Restore purchase success | >99% | Analytics |
| Zero billing-related crashes | 0 | Crashlytics |

---

## 9. Sprint Ceremonies

| Ceremony | Day | Duration | Attendees |
|----------|-----|----------|-----------|
| Sprint Planning | Day 1 | 2 hours | All |
| Daily Standup | Daily | 15 min | All |
| Tech Sync | Day 5, 10 | 1 hour | Tech Lead, Backend, DevOps |
| Billing Review | Day 7 | 1 hour | Tech Lead, Backend |
| Sprint Review | Day 10 | 1 hour | All + stakeholders |
| Retrospective | Day 10 | 45 min | All |

---

## 10. Deliverables Checklist

- [ ] Google Play Billing integration
- [ ] Subscription products in Play Console
- [ ] BillingManager implementation
- [ ] Backend verification endpoint
- [ ] Backend subscription status endpoint
- [ ] Webhook endpoint and handlers
- [ ] SubscriptionRepository (Android)
- [ ] Premium UI designs
- [ ] Test purchase documentation
- [ ] Billing monitoring dashboard
- [ ] Unit and integration tests

---

## 11. Handoff to Sprint 6

### Completed Items for Next Sprint
- Billing infrastructure complete
- Purchases can be made and verified
- Subscription state tracked
- Webhook processing operational

### Prepared Items for Next Sprint
- Ready to build premium UI screens
- Ready to implement feature gating
- Ready for A/B testing premium placements

---

*Sprint 5 Document v1.0 | Phase 2: Growth & Intelligence | Restock Android*
