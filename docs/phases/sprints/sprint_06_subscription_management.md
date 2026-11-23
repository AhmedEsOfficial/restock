# Sprint 6: Premium Launch - Subscription Management
## Phase 2, Month 6, Weeks 3-4

**Sprint Duration:** 2 weeks
**Sprint Number:** 6 of 12 (Phase 2)
**Team:** Tech Lead, UI/UX Engineer, Backend Developers (2), DevOps Engineer

---

## 1. Sprint Overview

This sprint completes the premium tier launch by building the subscription management UI, implementing feature gating, and setting up analytics for conversion tracking. By the end of this sprint, the premium tier will be live and ready for paying subscribers.

### Sprint Goal
> *"Launch premium tier with polished subscription UI, robust feature gating, and conversion tracking analytics."*

### Key Outcomes
- Premium purchase screens implemented
- Subscription management UI complete
- Feature gating enforced throughout app
- Premium upsell placements integrated
- A/B testing infrastructure for upsells
- Analytics tracking premium conversion
- Premium tier launched to beta users

### Prerequisites from Sprint 5
- Google Play Billing integration complete
- Purchase flow working
- Backend verification operational
- Subscription state management ready

---

## 2. Sprint Backlog

### 2.1 User Stories

#### US-6.1: Premium Showcase Screen
**As a** free user
**I want** to see the benefits of premium
**So that** I can make an informed decision about upgrading

**Acceptance Criteria:**
- [ ] Displays all premium benefits clearly
- [ ] Shows monthly and annual pricing
- [ ] Highlights annual savings (33%)
- [ ] Purchase buttons for both options
- [ ] Loading state during purchase
- [ ] Success state with celebration
- [ ] Error state with retry option
- [ ] Accessible with TalkBack

**Story Points:** 8
**Assignee:** UI/UX Engineer

**UI Design:**
```
┌─────────────────────────────────────────┐
│              ← Back                      │
│                                          │
│         [Premium Crown Icon]             │
│                                          │
│        Upgrade to Premium                │
│                                          │
│  ┌─────────────────────────────────┐    │
│  │ ✓ Unlimited device sync         │    │
│  │ ✓ Voice input                   │    │
│  │ ✓ Natural language processing   │    │
│  │ ✓ Recipe import                 │    │
│  │ ✓ Priority support              │    │
│  └─────────────────────────────────┘    │
│                                          │
│  ┌─────────────────────────────────┐    │
│  │ Monthly           $4.99/month   │    │
│  │ [Subscribe Monthly]             │    │
│  └─────────────────────────────────┘    │
│                                          │
│  ┌─────────────────────────────────┐    │
│  │ Annual             $39.99/year  │    │
│  │ Save 33%! ($3.33/month)        │    │
│  │ [Subscribe Annually] ★ Best    │    │
│  └─────────────────────────────────┘    │
│                                          │
│  Terms of Service | Privacy Policy       │
│  Restore Purchases                       │
└─────────────────────────────────────────┘
```

---

#### US-6.2: Subscription Status Screen
**As a** premium subscriber
**I want** to see my subscription details
**So that** I can manage my subscription

**Acceptance Criteria:**
- [ ] Shows current subscription plan
- [ ] Shows renewal date
- [ ] Shows price
- [ ] Auto-renew status indicator
- [ ] Deep link to Play Store subscription management
- [ ] Handles grace period state
- [ ] Handles account hold state
- [ ] Handles canceled (active until) state

**Story Points:** 5
**Assignee:** UI/UX Engineer

**Subscription States UI:**
```kotlin
@Composable
fun SubscriptionStatusCard(
    subscription: PremiumSubscription,
    onManageClick: () -> Unit,
) {
    Card {
        Column {
            // Status badge
            when {
                subscription.isOnHold -> Badge("Payment Issue", color = Warning)
                subscription.isInGracePeriod -> Badge("Renewing Soon", color = Warning)
                !subscription.isAutoRenewing -> Badge("Cancels ${subscription.expiresAt.format()}", color = Muted)
                else -> Badge("Active", color = Success)
            }

            Text("Restock Premium")
            Text(subscription.productId.toDisplayName())

            if (subscription.isAutoRenewing) {
                Text("Renews ${subscription.expiresAt.format()}")
            } else {
                Text("Access until ${subscription.expiresAt.format()}")
            }

            Button(onClick = onManageClick) {
                Text("Manage Subscription")
            }
        }
    }
}
```

---

#### US-6.3: Feature Gating System
**As a** developer
**I want** a consistent feature gating system
**So that** premium features are properly restricted

**Acceptance Criteria:**
- [ ] FeatureGate composable for UI gating
- [ ] isPremiumFeature() extension for logic gating
- [ ] Graceful degradation for offline state
- [ ] Consistent upsell experience
- [ ] Easy to add new gated features
- [ ] Unit testable

**Story Points:** 8
**Assignee:** Tech Lead

**Gating Implementation:**
```kotlin
@Composable
fun PremiumFeatureGate(
    feature: PremiumFeature,
    subscriptionState: SubscriptionState,
    onUpgradeClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    when {
        subscriptionState is SubscriptionState.Premium -> content()
        subscriptionState is SubscriptionState.Loading -> LoadingPlaceholder()
        else -> PremiumUpsellCard(
            feature = feature,
            onUpgradeClick = onUpgradeClick
        )
    }
}

enum class PremiumFeature(
    val title: String,
    val description: String,
    val icon: ImageVector,
) {
    MULTI_DEVICE_SYNC(
        "Sync Across Devices",
        "Access your list on all your devices",
        Icons.Default.Sync
    ),
    VOICE_INPUT(
        "Voice Input",
        "Add items by speaking",
        Icons.Default.Mic
    ),
    NATURAL_LANGUAGE(
        "Smart Input",
        "Add multiple items naturally",
        Icons.Default.AutoAwesome
    ),
    RECIPE_IMPORT(
        "Recipe Import",
        "Import ingredients from recipe URLs",
        Icons.Default.MenuBook
    )
}

// Usage in composables
@Composable
fun VoiceInputButton(
    subscriptionState: SubscriptionState,
    onUpgradeClick: () -> Unit,
    onVoiceClick: () -> Unit,
) {
    PremiumFeatureGate(
        feature = PremiumFeature.VOICE_INPUT,
        subscriptionState = subscriptionState,
        onUpgradeClick = onUpgradeClick
    ) {
        IconButton(onClick = onVoiceClick) {
            Icon(Icons.Default.Mic, "Voice input")
        }
    }
}
```

---

#### US-6.4: Premium Upsell Cards
**As a** free user encountering a premium feature
**I want** to understand the value proposition
**So that** I can decide if premium is worth it

**Acceptance Criteria:**
- [ ] Upsell card shows specific feature benefit
- [ ] One-tap to premium showcase
- [ ] Dismiss option (optional, per placement)
- [ ] Visually distinct but not intrusive
- [ ] Tracks impression for analytics
- [ ] A/B testable variants

**Story Points:** 5
**Assignee:** UI/UX Engineer

**Upsell Card Component:**
```kotlin
@Composable
fun PremiumUpsellCard(
    feature: PremiumFeature,
    onUpgradeClick: () -> Unit,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    // Track impression
    LaunchedEffect(feature) {
        Analytics.trackPremiumUpsellImpression(feature)
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = feature.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(
                    text = feature.title,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = feature.description,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(onClick = {
                Analytics.trackPremiumUpsellTap(feature)
                onUpgradeClick()
            }) {
                Text("Upgrade")
            }

            onDismiss?.let {
                IconButton(onClick = it) {
                    Icon(Icons.Default.Close, "Dismiss")
                }
            }
        }
    }
}
```

---

#### US-6.5: Upsell Trigger Points
**As a** product manager
**I want** upsells at strategic moments
**So that** we maximize conversion without annoying users

**Acceptance Criteria:**
- [ ] Trigger when adding second device (sync limit)
- [ ] Trigger after 30 days of consistent use
- [ ] Trigger when pattern learning shows high value
- [ ] Trigger when tapping voice input (Phase 2+)
- [ ] Trigger when tapping recipe import (Phase 2+)
- [ ] Never interrupt core functionality
- [ ] Frequency capped (max 1/day)

**Story Points:** 8
**Assignee:** Backend Developer 1

**Upsell Trigger Logic:**
```kotlin
class UpsellTriggerManager @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
    private val userStatsRepository: UserStatsRepository,
    private val dataStore: DataStore<Preferences>,
) {
    private val _upsellEvent = MutableSharedFlow<UpsellTrigger>()
    val upsellEvent: SharedFlow<UpsellTrigger> = _upsellEvent.asSharedFlow()

    suspend fun checkTriggers() {
        if (subscriptionRepository.isPremium()) return
        if (wasUpsellShownToday()) return

        val trigger = when {
            shouldShowDeviceLimitUpsell() -> UpsellTrigger.DEVICE_LIMIT
            shouldShow30DayUpsell() -> UpsellTrigger.CONSISTENT_USER
            shouldShowHighValueUpsell() -> UpsellTrigger.HIGH_VALUE_USER
            else -> null
        }

        trigger?.let {
            _upsellEvent.emit(it)
            recordUpsellShown(it)
        }
    }

    private suspend fun shouldShowDeviceLimitUpsell(): Boolean {
        val deviceCount = syncRepository.getLinkedDevicesCount()
        return deviceCount >= 1 && !dataStore.getBoolean(DEVICE_LIMIT_UPSELL_SHOWN)
    }

    private suspend fun shouldShow30DayUpsell(): Boolean {
        val firstUseDate = userStatsRepository.getFirstUseDate()
        val daysSinceFirstUse = ChronoUnit.DAYS.between(firstUseDate, LocalDate.now())
        return daysSinceFirstUse >= 30 && !dataStore.getBoolean(DAY_30_UPSELL_SHOWN)
    }

    private suspend fun shouldShowHighValueUpsell(): Boolean {
        val suggestionAcceptRate = patternRepository.getSuggestionAcceptanceRate()
        return suggestionAcceptRate > 0.6 && !dataStore.getBoolean(HIGH_VALUE_UPSELL_SHOWN)
    }
}

enum class UpsellTrigger {
    DEVICE_LIMIT,
    CONSISTENT_USER,
    HIGH_VALUE_USER,
    PREMIUM_FEATURE_TAP
}
```

---

#### US-6.6: Conversion Analytics
**As a** product manager
**I want** detailed conversion analytics
**So that** I can optimize the premium funnel

**Acceptance Criteria:**
- [ ] Track premium showcase views
- [ ] Track upsell impressions by placement
- [ ] Track upsell taps by placement
- [ ] Track purchase initiated
- [ ] Track purchase completed
- [ ] Track purchase failed (with reason)
- [ ] Track subscription cancellation
- [ ] Calculate conversion funnel metrics

**Story Points:** 5
**Assignee:** Backend Developer 2

**Analytics Events:**
```kotlin
object PremiumAnalytics {
    // Funnel events
    fun trackPremiumShowcaseView(source: String) =
        Analytics.logEvent("premium_showcase_view", "source" to source)

    fun trackUpsellImpression(trigger: UpsellTrigger, placement: String) =
        Analytics.logEvent("upsell_impression", "trigger" to trigger.name, "placement" to placement)

    fun trackUpsellTap(trigger: UpsellTrigger, placement: String) =
        Analytics.logEvent("upsell_tap", "trigger" to trigger.name, "placement" to placement)

    fun trackPurchaseInitiated(productId: String) =
        Analytics.logEvent("purchase_initiated", "product_id" to productId)

    fun trackPurchaseCompleted(productId: String, price: String) =
        Analytics.logEvent("purchase_completed", "product_id" to productId, "price" to price)

    fun trackPurchaseFailed(productId: String, reason: String) =
        Analytics.logEvent("purchase_failed", "product_id" to productId, "reason" to reason)

    fun trackSubscriptionCanceled(productId: String, daysSincePurchase: Int) =
        Analytics.logEvent("subscription_canceled", "product_id" to productId, "tenure_days" to daysSincePurchase)
}
```

---

#### US-6.7: A/B Testing Infrastructure
**As a** product manager
**I want** to A/B test premium upsell variants
**So that** we can optimize conversion

**Acceptance Criteria:**
- [ ] Firebase Remote Config integrated
- [ ] Experiment flags for upsell placements
- [ ] Experiment flags for upsell copy
- [ ] Experiment flags for pricing display
- [ ] User cohort assignment persisted
- [ ] Results trackable in analytics

**Story Points:** 5
**Assignee:** Backend Developer 2

**A/B Test Config:**
```kotlin
object PremiumExperiments {
    // Upsell copy variants
    suspend fun getUpsellVariant(): UpsellVariant {
        return when (remoteConfig.getString("upsell_copy_variant")) {
            "value_focused" -> UpsellVariant.VALUE_FOCUSED
            "feature_focused" -> UpsellVariant.FEATURE_FOCUSED
            "savings_focused" -> UpsellVariant.SAVINGS_FOCUSED
            else -> UpsellVariant.CONTROL
        }
    }

    // Annual pricing emphasis
    suspend fun shouldEmphasizeAnnual(): Boolean =
        remoteConfig.getBoolean("emphasize_annual_pricing")

    // Upsell frequency
    suspend fun getUpsellFrequencyDays(): Int =
        remoteConfig.getLong("upsell_frequency_days").toInt()
}

enum class UpsellVariant {
    CONTROL,           // "Upgrade to Premium"
    VALUE_FOCUSED,     // "Never forget groceries again"
    FEATURE_FOCUSED,   // "Unlock voice input & more"
    SAVINGS_FOCUSED    // "Save 33% with annual"
}
```

---

#### US-6.8: Premium Badge UI
**As a** premium subscriber
**I want** to see my premium status in the app
**So that** I feel recognized for my support

**Acceptance Criteria:**
- [ ] Premium badge in settings/account area
- [ ] Optional premium indicator in app (subtle)
- [ ] Badge shows subscription type (monthly/annual)
- [ ] Accessible

**Story Points:** 3
**Assignee:** UI/UX Engineer

---

#### US-6.9: Beta Test Premium
**As a** QA engineer
**I want** to thoroughly test premium features
**So that** we launch with confidence

**Acceptance Criteria:**
- [ ] Internal testing track configured
- [ ] Test license accounts working
- [ ] All purchase flows tested
- [ ] All subscription states tested
- [ ] All edge cases documented
- [ ] No critical bugs remaining

**Story Points:** 8
**Assignee:** Backend Developer 2, Tech Lead

---

### 2.2 Technical Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies |
|---------|------|----------|------------|--------------|
| T-6.1 | Build PremiumShowcaseScreen | UI/UX | 12 | - |
| T-6.2 | Build SubscriptionStatusScreen | UI/UX | 8 | - |
| T-6.3 | Create PremiumFeatureGate composable | Tech Lead | 4 | - |
| T-6.4 | Create PremiumUpsellCard component | UI/UX | 4 | - |
| T-6.5 | Implement FeatureGate in existing screens | Tech Lead | 6 | T-6.3 |
| T-6.6 | Create UpsellTriggerManager | Backend Dev 1 | 8 | - |
| T-6.7 | Integrate upsell triggers into app flow | Backend Dev 1 | 6 | T-6.6 |
| T-6.8 | Implement conversion analytics events | Backend Dev 2 | 4 | - |
| T-6.9 | Set up Firebase Remote Config | Backend Dev 2 | 4 | - |
| T-6.10 | Create A/B test flag system | Backend Dev 2 | 6 | T-6.9 |
| T-6.11 | Create PremiumBadge component | UI/UX | 2 | - |
| T-6.12 | Create PremiumViewModel | Backend Dev 1 | 6 | - |
| T-6.13 | Wire up premium purchase flow | Tech Lead | 6 | T-6.1, T-6.12 |
| T-6.14 | Deep link to Play Store subscription | Tech Lead | 2 | - |
| T-6.15 | Add premium settings section | UI/UX | 4 | - |
| T-6.16 | Create premium feature test matrix | Backend Dev 2 | 4 | - |
| T-6.17 | Execute beta testing plan | Backend Dev 2 | 10 | All |
| T-6.18 | Fix beta testing bugs | All | 10 | T-6.17 |
| T-6.19 | Create conversion dashboard | DevOps | 6 | T-6.8 |
| T-6.20 | Write feature gating tests | Tech Lead | 4 | T-6.3 |
| T-6.21 | Write upsell trigger tests | Backend Dev 1 | 4 | T-6.6 |

---

## 3. Team Assignments

### Tech Lead
**Focus:** Feature gating, purchase flow integration
- Create PremiumFeatureGate system
- Implement gating in existing screens
- Wire up purchase flow with UI
- Implement Play Store deep link
- Code review and architecture guidance

### UI/UX Engineer
**Focus:** Premium UI screens
- Build PremiumShowcaseScreen
- Build SubscriptionStatusScreen
- Create PremiumUpsellCard
- Create PremiumBadge
- Add premium settings section

### Backend Developer 1
**Focus:** Upsell logic and ViewModels
- Create UpsellTriggerManager
- Integrate triggers into app flow
- Create PremiumViewModel
- Write trigger unit tests

### Backend Developer 2
**Focus:** Analytics and A/B testing
- Implement conversion analytics
- Set up Firebase Remote Config
- Create A/B test infrastructure
- Execute beta testing
- Create test documentation

### DevOps Engineer
**Focus:** Analytics and monitoring
- Set up conversion analytics dashboard
- Configure Remote Config infrastructure
- Monitor beta performance
- Alert setup for billing issues

---

## 4. Dependencies

### External Dependencies
| Dependency | Owner | Status | Risk |
|------------|-------|--------|------|
| Firebase Remote Config | DevOps | Required | Low |
| Play Store internal testing | Tech Lead | Required | Low |
| License tester accounts | Tech Lead | Required | Low |

### Internal Dependencies
| Dependency | From | To | Notes |
|------------|------|-----|-------|
| BillingManager | Sprint 5 | Purchase flow | Complete |
| SubscriptionRepository | Sprint 5 | Feature gating | Complete |
| Subscription state | Sprint 5 | All premium UI | Complete |

---

## 5. Definition of Done

### Sprint Level
- [ ] Premium showcase screen complete and polished
- [ ] Subscription status screen functional
- [ ] Feature gating implemented for all premium features
- [ ] Upsell cards at all trigger points
- [ ] A/B testing infrastructure ready
- [ ] Conversion analytics tracking all events
- [ ] Beta testing complete with no P0/P1 bugs
- [ ] Premium tier ready for public launch

### Story Level
- [ ] Acceptance criteria met
- [ ] UI matches designs
- [ ] Accessibility verified
- [ ] Analytics events firing
- [ ] Code reviewed

---

## 6. Risk Assessment

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Poor conversion rate | Medium | High | A/B test upsell variants; iterate quickly |
| Upsells feel annoying | Medium | High | Frequency caps; easy dismissal; user feedback |
| Feature gating bugs | Low | High | Thorough testing; graceful fallbacks |
| Analytics data quality | Medium | Medium | Validate events in QA; documentation |
| Beta testing finds major issues | Medium | Medium | Buffer time for fixes; staged rollout |

---

## 7. Testing Requirements

### Unit Tests
- PremiumFeatureGate logic
- UpsellTriggerManager conditions
- Subscription state handling
- A/B test flag resolution

### UI Tests
- Premium showcase interaction
- Purchase flow states
- Upsell card display and tap
- Feature gate behavior

### Integration Tests
- End-to-end purchase flow
- Analytics event firing
- Remote config fetching

### Manual Testing
- All premium features work for subscribers
- All premium features blocked for free users
- All upsell placements work
- All subscription states display correctly

### Test Coverage Targets
| Component | Target |
|-----------|--------|
| Feature gating | 90% |
| Upsell triggers | 85% |
| Premium UI | Key flows |

---

## 8. Success Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| Premium showcase view rate | >10% of MAU | Analytics |
| Upsell → Showcase conversion | >20% | Analytics |
| Showcase → Purchase conversion | >5% | Analytics |
| Overall premium conversion | 3-5% | Billing data |
| User satisfaction (premium) | >4.5 stars | App reviews |
| Zero premium-related crashes | 0 | Crashlytics |

---

## 9. Sprint Ceremonies

| Ceremony | Day | Duration | Attendees |
|----------|-----|----------|-----------|
| Sprint Planning | Day 1 | 2 hours | All |
| Daily Standup | Daily | 15 min | All |
| Tech Sync | Day 5, 10 | 1 hour | Tech Lead, Backend, DevOps |
| Design Review | Day 4 | 30 min | Tech Lead, UI/UX |
| Beta Testing Kickoff | Day 7 | 1 hour | All |
| Sprint Review | Day 10 | 1 hour | All + stakeholders |
| Retrospective | Day 10 | 45 min | All |

---

## 10. Deliverables Checklist

- [ ] PremiumShowcaseScreen
- [ ] SubscriptionStatusScreen
- [ ] PremiumFeatureGate composable
- [ ] PremiumUpsellCard component
- [ ] UpsellTriggerManager
- [ ] Conversion analytics implementation
- [ ] A/B testing infrastructure
- [ ] PremiumBadge component
- [ ] Premium settings section
- [ ] Beta testing report
- [ ] Conversion analytics dashboard
- [ ] A/B test configuration guide

---

## 11. Milestone Achievement

### Premium Launch Milestone Complete
At the end of this sprint, the **M2: Premium Launch** milestone from Phase 2 should be achieved:

- [ ] Subscriptions active and processing payments
- [ ] Feature gating enforced
- [ ] Premium users have full access
- [ ] Free users see upsells at appropriate times
- [ ] Conversion tracking operational
- [ ] Ready for public premium launch

---

## 12. Handoff to Sprint 7

### Completed Items for Next Sprint
- Premium tier fully launched
- Feature gating infrastructure in place
- Analytics tracking conversion

### Prepared Items for Next Sprint
- Ready to implement voice input (premium feature)
- Feature gating ready for new features
- Premium users available for early access

---

*Sprint 6 Document v1.0 | Phase 2: Growth & Intelligence | Restock Android*
