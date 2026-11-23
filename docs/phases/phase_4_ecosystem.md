# Phase 4: Ecosystem
## Restock Android Application

**Duration:** Months 16-24 (9 months)
**Sprints:** 18 sprints (2-week cycles)
**Team:** Full team maintained from Phase 3
**Full Team:** Tech Lead (1), UI/UX Engineer (1), Backend Devs (2), DevOps (1), ML Engineer (1), Android Dev (1)

---

## Table of Contents

1. [Phase Overview](#1-phase-overview)
2. [Strategic Goals](#2-strategic-goals)
3. [Team Structure & Responsibilities](#3-team-structure--responsibilities)
4. [Feature Breakdown](#4-feature-breakdown)
5. [Monthly Roadmap](#5-monthly-roadmap)
6. [Technical Decisions](#6-technical-decisions)
7. [Integration Specifications](#7-integration-specifications)
8. [Platform Expansion](#8-platform-expansion)
9. [Quality Assurance](#9-quality-assurance)
10. [Risk Management](#10-risk-management)
11. [Long-Term Vision](#11-long-term-vision)

---

## 1. Phase Overview

Phase 4 transforms Restock from a standalone app into an ecosystem player, integrating with smart homes, delivery services, and expanding across Android platforms. This phase establishes Restock as the central hub for household grocery management.

### Phase Vision
> *"Connect Restock to the broader smart home and retail ecosystem, making grocery management truly effortless."*

### Key Outcomes
- Google Assistant voice actions operational
- Home screen widget for quick access
- Instacart/Amazon Fresh ordering integration
- Budget tracking and spending analytics (Premium)
- Wear OS companion app
- Tablet-optimized experience
- 2,000,000 total users milestone
- 140,000 premium subscribers ($5.6M ARR)

### Prerequisites from Phase 3
- 500,000+ user base established
- 25,000+ premium subscribers
- Household sharing widely adopted
- Camera features stable and performing
- Backend infrastructure proven at scale

---

## 2. Strategic Goals

| Objective | Target | Measurement |
|-----------|--------|-------------|
| Total users | 2,000,000 | Play Console installs |
| Premium subscribers | 140,000 | Billing analytics |
| Premium conversion | 7% | Billing analytics |
| Assistant action usage | 30% of users | Usage analytics |
| Widget installs | 40% of users | Widget API tracking |
| Delivery orders placed | 10,000/month | Partner API tracking |
| Wear OS installs | 5% of users | Play Console |

### Revenue Targets

| Metric | Month 18 Target | Month 24 Target |
|--------|-----------------|-----------------|
| Premium subscribers | 70,000 | 140,000 |
| ARR | $2.8M | $5.6M |
| Affiliate revenue | $10,000/month | $50,000/month |
| Total revenue | $3M ARR | $6.2M ARR |

---

## 3. Team Structure & Responsibilities

### Maintained Team Structure

```
                         ┌──────────────────┐
                         │    Tech Lead     │
                         │   (Architect)    │
                         └────────┬─────────┘
                                  │
        ┌─────────────────────────┼─────────────────────────┐
        │                         │                         │
┌───────┴───────┐         ┌───────┴───────┐         ┌───────┴───────┐
│   Mobile      │         │    Backend    │         │  ML/DevOps    │
│   Team        │         │    Team       │         │    Team       │
│               │         │               │         │               │
│ • UI/UX (1)   │         │ • Backend (2) │         │ • ML Eng (1)  │
│ • Android (1) │         │               │         │ • DevOps (1)  │
└───────────────┘         └───────────────┘         └───────────────┘
```

### Role Assignments by Feature

| Feature | Tech Lead | UI/UX | Android Dev | Backend | ML Engineer | DevOps |
|---------|:---------:|:-----:|:-----------:|:-------:|:-----------:|:------:|
| Google Assistant | Actions setup | Voice UX | Fulfillment | Actions API | — | Deployment |
| Widget | Glance API | Widget design | Implementation | — | — | — |
| Delivery Integration | API design | Order flow | Integration | Partner APIs | — | Rate limiting |
| Budget Analytics | Data model | Dashboard UI | Charts | Aggregation | Insights ML | Data pipeline |
| Wear OS | — | Watch UX | WearOS app | Sync | — | — |
| Tablet UI | Architecture | Tablet layouts | Responsive | — | — | — |

### Potential Team Expansion (Month 20+)

Consider adding based on velocity needs:
- **Additional Android Developer** for Wear OS/Tablet parallel work
- **Data Engineer** for analytics pipeline
- **Partnership Manager** for delivery service relationships

---

## 4. Feature Breakdown

### 4.1 Smart Home Integration (Months 16-17)

#### Google Assistant Actions

**Capabilities:**
- "Hey Google, add milk to my Restock list"
- "Hey Google, what's on my grocery list?"
- "Hey Google, I bought the milk" (check off)
- "Hey Google, suggest something for dinner" (Premium)

**Technical Implementation:**

```yaml
# actions.yaml
actions:
  - name: ADD_TO_LIST
    fulfillment:
      conversationName: add_item
    intent:
      name: actions.intent.ADD_TO_LIST
      parameters:
        - name: item
          type: schema.org/Thing

  - name: GET_LIST
    fulfillment:
      conversationName: get_list
    intent:
      name: actions.intent.GET_LIST

  - name: CHECK_ITEM
    fulfillment:
      conversationName: check_item
    intent:
      name: actions.intent.COMPLETE_TASK
```

**Fulfillment Service:**
```kotlin
class AssistantFulfillmentService {
    suspend fun addItem(
        userId: String,
        itemName: String
    ): AssistantResponse {
        val result = addItemUseCase(userId, itemName)
        return AssistantResponse(
            speech = "Added $itemName to your Restock list",
            displayText = "✓ $itemName added"
        )
    }

    suspend fun getList(userId: String): AssistantResponse {
        val items = getCurrentListUseCase(userId)
        return AssistantResponse(
            speech = formatListForSpeech(items),
            displayText = formatListForDisplay(items),
            suggestions = listOf("Add item", "Check off item")
        )
    }
}
```

**Account Linking:**
- OAuth 2.0 account linking flow
- Secure token exchange
- Household support (select which list)

#### Home Screen Widget

**Widget Types:**

| Widget | Size | Features |
|--------|------|----------|
| Quick Add | 2x1 | Tap to add item, voice button |
| List Preview | 4x2 | Show top 5 items, quick check |
| Suggestions | 2x2 | Show top 3 suggestions, one-tap add |

**Implementation (Jetpack Glance):**

```kotlin
class RestockListWidget : GlanceAppWidget() {
    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        provideContent {
            val items = remember { getWidgetItems() }

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(RestockColors.Card)
                    .cornerRadius(16.dp)
                    .padding(12.dp)
            ) {
                Text(
                    text = "Grocery List",
                    style = TextStyle(fontWeight = FontWeight.Medium)
                )

                items.take(5).forEach { item ->
                    WidgetListItem(
                        item = item,
                        onCheck = { toggleItem(item.id) }
                    )
                }

                Button(
                    text = "Open Restock",
                    onClick = actionStartActivity<MainActivity>()
                )
            }
        }
    }
}
```

**Widget Update Strategy:**
- Update on list changes (via WorkManager)
- Periodic refresh every 30 minutes
- Update on user interaction

#### Android Auto Support (Stretch Goal)

**Capabilities:**
- Voice-only interaction while driving
- "Read my grocery list"
- "Add item to list"
- No visual UI (safety compliance)

---

### 4.2 Delivery Integration (Months 18-19)

#### Partner Services

| Service | Coverage | API Status | Priority |
|---------|----------|------------|----------|
| Instacart | US, Canada | Partner API available | P0 |
| Amazon Fresh | US | API limited | P1 |
| Walmart+ | US | Deep linking | P2 |
| Shipt | US | Partner API | P2 |

#### Instacart Integration (Primary)

**Flow:**
```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  Restock    │───►│   Convert   │───►│  Instacart  │───►│  Delivery   │
│  List       │    │   to Cart   │    │  Checkout   │    │  Scheduled  │
│             │    │             │    │             │    │             │
│  Items +    │    │  Product    │    │  Payment    │    │  Order      │
│  Quantities │    │  Matching   │    │  (in-app)   │    │  Tracking   │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

**API Integration:**
```kotlin
interface InstacartApi {
    @POST("v1/carts")
    suspend fun createCart(
        @Body items: List<InstacartItem>
    ): CartResponse

    @GET("v1/products/search")
    suspend fun searchProducts(
        @Query("query") query: String,
        @Query("store_id") storeId: String
    ): ProductSearchResponse

    @POST("v1/orders")
    suspend fun createOrder(
        @Body orderRequest: OrderRequest
    ): OrderResponse
}

data class InstacartItem(
    val productId: String,
    val quantity: Int,
    val substitutionPreference: SubstitutionPref
)
```

**Product Matching:**
```kotlin
class ProductMatchingService @Inject constructor(
    private val instacartApi: InstacartApi,
    private val matchingCache: MatchingCache,
) {
    suspend fun matchItem(
        restockItem: GroceryItem,
        storeId: String
    ): MatchResult {
        // Check cache first
        matchingCache.get(restockItem.normalizedName, storeId)?.let {
            return MatchResult.Cached(it)
        }

        // Search Instacart products
        val results = instacartApi.searchProducts(
            query = restockItem.name,
            storeId = storeId
        )

        // Rank by relevance
        val bestMatch = results.products
            .maxByOrNull { fuzzyScore(it.name, restockItem.name) }

        return bestMatch?.let {
            matchingCache.put(restockItem.normalizedName, storeId, it)
            MatchResult.Found(it)
        } ?: MatchResult.NotFound
    }
}
```

#### Price Comparison Engine

**Data Sources:**
- Instacart pricing
- Amazon Fresh pricing (where available)
- Historical price tracking

**Implementation:**
```kotlin
class PriceComparisonService @Inject constructor(
    private val instacartApi: InstacartApi,
    private val amazonFreshApi: AmazonFreshApi,
) {
    suspend fun compareCart(
        items: List<GroceryItem>,
        location: UserLocation
    ): PriceComparison {
        val instacartPrices = items.map { async { getInstacartPrice(it) } }
        val amazonPrices = items.map { async { getAmazonPrice(it) } }

        return PriceComparison(
            instacart = PriceResult(
                total = instacartPrices.awaitAll().sum(),
                deliveryFee = getInstacartDeliveryFee(location),
                items = instacartPrices.awaitAll()
            ),
            amazonFresh = PriceResult(
                total = amazonPrices.awaitAll().sum(),
                deliveryFee = getAmazonDeliveryFee(location),
                items = amazonPrices.awaitAll()
            )
        )
    }
}
```

#### Delivery UI

**Screens:**
- Service selection (which delivery service)
- Store selection (nearby stores)
- Cart review (matched products, substitutions)
- Price comparison view
- Checkout handoff (deep link or in-app)
- Order tracking (if API supports)

**Monetization:**
- Affiliate commission on orders (typically 5-10%)
- Premium feature (free tier: view prices only)

---

### 4.3 Budget & Analytics (Months 20-21) - Premium

#### Data Model

```kotlin
data class SpendingRecord(
    val id: String,
    val itemId: String,
    val amount: Money,
    val storeId: String?,
    val purchasedAt: Instant,
    val source: SpendingSource,  // MANUAL, RECEIPT_SCAN, DELIVERY
)

data class BudgetSettings(
    val userId: String,
    val weeklyBudget: Money?,
    val monthlyBudget: Money?,
    val categoryBudgets: Map<Category, Money>,
    val alertThreshold: Float,  // Alert at 80% of budget
)

data class SpendingInsight(
    val type: InsightType,
    val category: Category?,
    val message: String,
    val data: Map<String, Any>,
    val generatedAt: Instant,
)

enum class InsightType {
    SPENDING_TREND,
    CATEGORY_INCREASE,
    WASTE_DETECTED,
    SAVINGS_OPPORTUNITY,
    PRICE_INCREASE_ALERT,
}
```

#### Analytics Features

**Spending Tracking:**
- Manual price entry on purchase
- Auto-import from receipt scanning
- Auto-import from delivery orders
- Historical price trends per item

**Dashboard Views:**
- Weekly/monthly spending overview
- Category breakdown (produce, dairy, meat, etc.)
- Spending vs. budget progress
- Month-over-month comparison
- Year-over-year trends

**Budget Alerts:**
- Approaching budget limit (configurable threshold)
- Budget exceeded notification
- Unusual spending pattern alert

#### AI-Powered Insights (ML Engineer)

**Waste Reduction:**
```kotlin
// "You often buy spinach but purchase again before typical usage"
class WasteDetector @Inject constructor(
    private val purchaseRepository: PurchaseRepository,
    private val patternRepository: PatternRepository,
) {
    suspend fun detectPotentialWaste(): List<WasteInsight> {
        val patterns = patternRepository.getAllPatterns()

        return patterns.filter { pattern ->
            // Item is purchased frequently but pattern suggests over-buying
            val avgUsageInterval = getTypicalUsageInterval(pattern.itemId)
            pattern.avgIntervalDays < avgUsageInterval * 0.7
        }.map { pattern ->
            WasteInsight(
                itemId = pattern.itemId,
                message = "You might be buying ${pattern.itemName} more often than needed",
                suggestion = "Consider buying less or checking fridge before shopping"
            )
        }
    }
}
```

**Savings Opportunities:**
- Price drop alerts for frequently purchased items
- Bulk buying recommendations
- Store comparison suggestions

#### Analytics UI

**Dashboard Components:**
- Spending summary card (week/month)
- Interactive pie chart (categories)
- Line graph (spending over time)
- Budget progress bars
- Insights cards (swipeable)

**Design Principles:**
- Information at a glance
- Drill-down on tap
- No guilt-inducing language
- Focus on trends, not judgments

---

### 4.4 Platform Expansion (Months 22-24)

#### Wear OS Companion App

**Core Features:**
- View current list
- Quick check-off items
- Voice add ("OK Google, add milk")
- Suggestions glance tile
- Complication for item count

**Technical Approach:**
```kotlin
// Wear OS App Structure
:wear:app           // Main wear application
:wear:tiles         // Tiles and complications
:shared:domain      // Shared domain models
:shared:sync        // Data layer sync
```

**Data Sync:**
- Use Data Layer API for phone ↔ watch sync
- Local cache on watch for offline use
- Background sync every 15 minutes
- Immediate sync on phone list changes

**Wear OS UI:**

```kotlin
@Composable
fun WearListScreen(
    items: List<GroceryItem>,
    onItemCheck: (String) -> Unit,
) {
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        anchorType = ScalingLazyColumnAnchorType.ItemStart,
    ) {
        items(items) { item ->
            Chip(
                onClick = { onItemCheck(item.id) },
                label = { Text(item.name) },
                icon = {
                    Icon(
                        imageVector = if (item.isChecked)
                            Icons.Default.CheckCircle
                        else
                            Icons.Default.Circle,
                        contentDescription = null
                    )
                },
                colors = ChipDefaults.primaryChipColors()
            )
        }
    }
}
```

**Tile Implementation:**
```kotlin
class RestockTile : TileService() {
    override fun onTileRequest(requestParams: RequestParams) =
        Futures.immediateFuture(
            Tile.Builder()
                .setResourcesVersion(RESOURCES_VERSION)
                .setTimeline(
                    Timeline.Builder()
                        .addTimelineEntry(
                            TimelineEntry.Builder()
                                .setLayout(createListLayout())
                                .build()
                        )
                        .build()
                )
                .build()
        )
}
```

#### Tablet-Optimized UI

**Layout Strategy:**
- Adaptive layouts using WindowSizeClass
- Two-pane layout for large screens
- Master-detail pattern (list + item details)
- Extended navigation rail

**Implementation:**
```kotlin
@Composable
fun AdaptiveListScreen(
    windowSizeClass: WindowSizeClass,
    viewModel: ListViewModel,
) {
    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            // Phone: Single column
            PhoneListScreen(viewModel)
        }
        WindowWidthSizeClass.Medium -> {
            // Small tablet: List + suggestions side
            MediumTabletLayout(viewModel)
        }
        WindowWidthSizeClass.Expanded -> {
            // Large tablet: Full two-pane
            ExpandedTabletLayout(viewModel)
        }
    }
}

@Composable
fun ExpandedTabletLayout(viewModel: ListViewModel) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Navigation rail
        NavigationRail {
            // List, Meals, History, Settings
        }

        // Main list (60% width)
        Column(modifier = Modifier.weight(0.6f)) {
            SuggestionsSection()
            GroceryList()
        }

        // Detail panel (40% width)
        Column(modifier = Modifier.weight(0.4f)) {
            when (selectedItem) {
                null -> AnalyticsSummary()
                else -> ItemDetails(selectedItem)
            }
        }
    }
}
```

#### Chromebook Support

**Requirements:**
- Keyboard navigation support
- Mouse hover states
- Window resizing support
- Keyboard shortcuts (Ctrl+N new item, etc.)

**Implementation:**
```kotlin
// Keyboard shortcut handling
@Composable
fun ListScreen() {
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        // Global keyboard shortcuts
        onKeyEvent { event ->
            when {
                event.isCtrlPressed && event.key == Key.N -> {
                    focusAddItemField()
                    true
                }
                event.key == Key.Delete -> {
                    deleteSelectedItem()
                    true
                }
                else -> false
            }
        }
    }
}
```

#### Web PWA Companion (Stretch)

**Scope:**
- Read-only list view
- Quick add capability
- Sync with mobile app
- Works offline (service worker)

**Technical Approach:**
- Kotlin/JS with Compose for Web (experimental)
- OR React/Next.js with shared API

**Decision:** Evaluate Compose Multiplatform maturity; fallback to separate React PWA if needed.

---

## 5. Monthly Roadmap

### Month 16: Google Assistant Actions

**Week 1-2:**
- Actions on Google project setup
- Account linking implementation
- Basic add/get/check intents
- Fulfillment service development

**Week 3-4:**
- Multi-household support
- Error handling
- Testing and certification
- Documentation

**Team Focus:**
| Role | Focus |
|------|-------|
| Tech Lead | Actions architecture, fulfillment |
| UI/UX | Voice UX scripts, error messages |
| Backend (2) | Actions API, OAuth |
| DevOps | Actions deployment |

**Key Deliverable:** "Hey Google, add milk to Restock" works

---

### Month 17: Widget & Auto

**Week 1-2:**
- Jetpack Glance widget implementation
- Three widget sizes
- Widget configuration activity
- Update mechanism

**Week 3-4:**
- Android Auto voice integration
- Widget polish and testing
- Performance optimization

**Team Focus:**
| Role | Focus |
|------|-------|
| Tech Lead | Glance API, Auto integration |
| UI/UX | Widget designs (3 sizes) |
| Android Dev | Widget implementation |

**Key Deliverable:** Widgets available on Play Store

---

### Month 18: Delivery Partnerships

**Week 1-2:**
- Instacart partnership finalization
- API integration development
- Product matching algorithm
- Cart conversion flow

**Week 3-4:**
- Checkout flow UI
- Error handling
- Affiliate tracking setup
- Beta testing with partners

**Team Focus:**
| Role | Focus |
|------|-------|
| Tech Lead | API integration design |
| UI/UX | Order flow, cart review |
| Backend (2) | Partner APIs, matching |
| DevOps | Rate limiting, monitoring |

---

### Month 19: Delivery Completion

**Week 1-2:**
- Amazon Fresh integration
- Price comparison engine
- Order tracking (if available)

**Week 3-4:**
- Multi-service UI
- Delivery feature launch
- Analytics for orders

**Key Deliverable:** Order groceries through Restock

---

### Month 20: Budget Foundation

**Week 1-2:**
- Spending data model
- Manual price entry
- Receipt scan price import
- Delivery order import

**Week 3-4:**
- Budget settings UI
- Basic spending dashboard
- Category classification

**Team Focus:**
| Role | Focus |
|------|-------|
| Tech Lead | Data pipeline design |
| UI/UX | Dashboard design, charts |
| Backend (2) | Aggregation service |
| ML Engineer | Category classification |
| DevOps | Data warehouse setup |

---

### Month 21: Analytics & Insights

**Week 1-2:**
- Advanced dashboard views
- Budget alerts system
- Spending trends analysis

**Week 3-4:**
- AI-powered insights
- Waste detection
- Savings recommendations
- Analytics feature launch

**Key Deliverable:** Full budget tracking (Premium)

---

### Month 22: Wear OS Development

**Week 1-2:**
- Wear OS project setup
- Data Layer API integration
- Basic list view
- Check-off functionality

**Week 3-4:**
- Voice input on watch
- Tiles implementation
- Complications
- Testing on device matrix

**Team Focus:**
| Role | Focus |
|------|-------|
| Tech Lead | Data sync architecture |
| UI/UX | Watch UI/UX |
| Android Dev | Wear OS implementation |

---

### Month 23: Tablet & Chromebook

**Week 1-2:**
- WindowSizeClass implementation
- Two-pane layouts
- Navigation rail
- Responsive components

**Week 3-4:**
- Keyboard navigation
- Mouse interaction states
- Chromebook testing
- Large screen optimization

---

### Month 24: Polish & Launch

**Week 1-2:**
- Final testing across all platforms
- Performance optimization
- Bug fixes
- Documentation

**Week 3-4:**
- Staged rollout
- Marketing launch
- Phase 4 completion
- Future roadmap planning

**Key Deliverable:** Full ecosystem live, 2M users milestone

---

## 6. Technical Decisions

### Google Assistant Integration Method

**Decision:** Actions on Google with conversational actions

**Rationale:**
- Native Google ecosystem integration
- Supports multi-turn conversations
- Built-in account linking
- Handles voice recognition complexity

**Alternative Considered:** App Actions (simpler, in-app only)
- Rejected: Less flexibility, requires app to be open

### Widget Framework

**Decision:** Jetpack Glance

**Rationale:**
- Compose-based API (team familiarity)
- Modern, recommended approach
- Better performance than RemoteViews
- Easier maintenance

### Delivery Integration Approach

**Decision:** Direct API integration (Instacart first)

**Rationale:**
- Better user experience than deep linking
- Commission tracking capability
- Product matching control
- Price comparison possible

**Trade-off:**
- More development effort
- Partner relationship management needed

### Analytics Data Storage

**Decision:** Local aggregation + cloud backup

**Implementation:**
```kotlin
// Local: Real-time aggregates
@Entity(tableName = "spending_daily")
data class DailySpending(
    @PrimaryKey val date: LocalDate,
    val totalCents: Long,
    val categoryBreakdown: Map<Category, Long>,
)

// Cloud: Historical archive (encrypted)
data class MonthlyArchive(
    val month: YearMonth,
    val encryptedData: ByteArray,
)
```

**Rationale:**
- Privacy: Detailed data stays local
- Performance: Aggregates for quick dashboard
- Backup: Monthly archives for data recovery

---

## 7. Integration Specifications

### Google Assistant Webhook

```
POST /api/v1/assistant/webhook
Content-Type: application/json

Request (from Google):
{
    "handler": {
        "name": "add_item"
    },
    "intent": {
        "name": "ADD_TO_LIST",
        "params": {
            "item": {
                "resolved": "milk",
                "original": "some milk"
            }
        }
    },
    "user": {
        "accountLinkingStatus": "LINKED",
        "verifiedId": "user_abc123"
    }
}

Response:
{
    "prompt": {
        "firstSimple": {
            "speech": "Added milk to your grocery list.",
            "text": "✓ Added milk"
        },
        "suggestions": [
            {"title": "Add another item"},
            {"title": "Read my list"}
        ]
    }
}
```

### Instacart API

```
// Create cart from Restock list
POST /partner/v1/carts
Authorization: Bearer <partner_token>

Request:
{
    "store_id": "sprouts_123",
    "items": [
        {
            "product_id": "prod_abc",
            "quantity": 2,
            "unit": "each",
            "substitution": "best_match"
        }
    ],
    "user_context": {
        "restock_user_id": "user_xyz",
        "affiliate_code": "RESTOCK"
    }
}

Response:
{
    "cart_id": "cart_12345",
    "items": [...],
    "subtotal": 45.67,
    "estimated_delivery_fee": 5.99,
    "checkout_url": "https://instacart.com/checkout/cart_12345"
}
```

### Wear OS Data Sync

```kotlin
// Phone side: Send list updates
class WearDataSender @Inject constructor(
    private val dataClient: DataClient,
) {
    suspend fun sendListUpdate(items: List<GroceryItem>) {
        val request = PutDataMapRequest.create("/restock/list").apply {
            dataMap.putDataMapArrayList(
                "items",
                items.map { it.toDataMap() }.toArrayList()
            )
            dataMap.putLong("timestamp", System.currentTimeMillis())
        }
        dataClient.putDataItem(request.asPutDataRequest()).await()
    }
}

// Watch side: Receive updates
class WearDataReceiver : WearableListenerService() {
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            if (event.dataItem.uri.path == "/restock/list") {
                val items = DataMapItem.fromDataItem(event.dataItem)
                    .dataMap
                    .getDataMapArrayList("items")
                    ?.map { it.toGroceryItem() }

                updateLocalList(items)
            }
        }
    }
}
```

---

## 8. Platform Expansion

### Device Support Matrix (Phase 4 End)

| Platform | Form Factor | Status |
|----------|-------------|--------|
| Android Phone | 4.7" - 6.9" | Full support |
| Android Tablet | 7" - 12.9" | Optimized layout |
| Chromebook | Laptop/2-in-1 | Keyboard + mouse |
| Wear OS | Smartwatch | Companion app |
| Android Auto | Car display | Voice only |
| Google Assistant | Voice | Full actions |

### Responsive Design Breakpoints

| Size Class | Width | Layout |
|------------|-------|--------|
| Compact | < 600dp | Single column, bottom nav |
| Medium | 600-840dp | List + sidebar, nav rail |
| Expanded | > 840dp | Two-pane, full nav rail |

### Accessibility Across Platforms

| Platform | Considerations |
|----------|----------------|
| Phone | TalkBack, large text, high contrast |
| Tablet | Same + keyboard navigation |
| Wear OS | Limited text, large touch targets |
| Assistant | Voice-only UX, clear confirmations |
| Auto | Voice-only, no visual interaction |

---

## 9. Quality Assurance

### Testing Matrix

| Feature | Unit | Integration | E2E | Device Testing |
|---------|:----:|:-----------:|:---:|:--------------:|
| Assistant | 80% | Actions test | Simulator | Real devices |
| Widget | 70% | Glance test | Manual | Various launchers |
| Delivery | 80% | API mocks | Partner sandbox | Production |
| Analytics | 80% | Data pipeline | Dashboard flow | — |
| Wear OS | 70% | Data Layer | Full flow | Watch devices |
| Tablet | — | — | Layout tests | Various tablets |

### Performance Targets

| Platform | Metric | Target |
|----------|--------|--------|
| Widget | Update time | < 500ms |
| Assistant | Response time | < 2 seconds |
| Delivery | Cart creation | < 3 seconds |
| Wear OS | List load | < 1 second |
| Tablet | Layout switch | < 100ms |

### Partner Integration Testing

**Instacart:**
- Sandbox environment testing
- Product matching accuracy validation
- Checkout flow completion rate
- Error handling verification

**Google Assistant:**
- Actions Console simulator
- Multi-language testing (if supported)
- Account linking flow
- Error response handling

---

## 10. Risk Management

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Partner API changes | Medium | High | Version pinning, monitoring, fallback UI |
| Assistant certification delays | Medium | Medium | Early submission, buffer time |
| Wear OS low adoption | Medium | Low | Minimal investment, watch market share |
| Affiliate revenue below target | Medium | Medium | Multiple partners, direct value focus |
| Tablet/Chrome fragmentation | Low | Medium | Thorough device testing |
| Budget feature complexity | Medium | Medium | Phase features, start simple |

### Contingency Plans

**If Instacart partnership fails:**
1. Focus on deep linking to delivery apps
2. Build price comparison without ordering
3. Pursue alternative partnerships (Walmart, Shipt)

**If Assistant certification blocked:**
1. Address Google's feedback promptly
2. Simplify actions if needed
3. Focus on widget as voice alternative

**If analytics pipeline too complex:**
1. Launch with basic spending tracking
2. Add insights in subsequent releases
3. Consider third-party analytics service

---

## 11. Long-Term Vision

### Post-Phase 4 Opportunities

**Platform Expansion:**
- iOS app (evaluate KMP)
- Desktop app (Windows/Mac)
- Web app (full-featured PWA)

**Feature Expansion:**
- Store loyalty card integration
- Coupons and deals aggregation
- Meal kit partnership (HelloFresh, etc.)
- Smart pantry inventory tracking

**Business Expansion:**
- B2B: White-label for grocery chains
- API: Restock as a platform for other apps
- Data: Anonymized trend insights for CPG

### Technology Evolution

- **AI Advancement:** More sophisticated meal planning, dietary recommendations
- **Privacy Tech:** Zero-knowledge proofs for household sync
- **Edge Computing:** More powerful on-device ML

### Market Expansion

- International localization (EU, APAC)
- Regional delivery partnerships
- Currency and measurement localization

---

## Milestone Summary

| Milestone | Target | Success Criteria |
|-----------|--------|------------------|
| M1: Assistant Live | Month 16 | Voice actions certified |
| M2: Widgets Live | Month 17 | All 3 widgets on Play Store |
| M3: Delivery MVP | Month 18 | Instacart ordering works |
| M4: Multi-Delivery | Month 19 | 2+ delivery services |
| M5: Budget Launch | Month 21 | Full analytics (Premium) |
| M6: Wear OS Launch | Month 22 | Watch app on Play Store |
| M7: Tablet Optimized | Month 23 | Large screen layouts |
| M8: 2M Users | Month 24 | Total installs milestone |
| M9: $5M+ ARR | Month 24 | Revenue target achieved |

---

## Success Definition: End of Phase 4

> **Restock Ecosystem is complete when:**
>
> 1. Users can manage their grocery list via phone, tablet, watch, widget, or voice
> 2. Groceries can be ordered for delivery directly from the app
> 3. Spending is tracked and insights are generated automatically
> 4. 2,000,000 users trust Restock for their grocery management
> 5. 140,000 families pay for Premium features
> 6. The platform generates $5M+ in annual revenue
> 7. The foundation is set for international expansion

---

*Phase 4 Document v1.0 | January 2026*
