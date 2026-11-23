# Phase 2: Growth & Intelligence
## Restock Android Application

**Duration:** Months 4-9 (6 months)
**Sprints:** 12 sprints (2-week cycles)
**Team Expansion:** Requires Backend Developer (2), DevOps Engineer (1) in addition to existing team
**Current Team:** Tech Lead/Senior Dev (1), UI/UX Engineer (1)

---

## Table of Contents

1. [Phase Overview](#1-phase-overview)
2. [Strategic Goals](#2-strategic-goals)
3. [Team Structure & Responsibilities](#3-team-structure--responsibilities)
4. [Feature Breakdown](#4-feature-breakdown)
5. [Monthly Roadmap](#5-monthly-roadmap)
6. [Technical Decisions](#6-technical-decisions)
7. [API Specifications](#7-api-specifications)
8. [Premium Tier Implementation](#8-premium-tier-implementation)
9. [Quality Assurance](#9-quality-assurance)
10. [Risk Management](#10-risk-management)

---

## 1. Phase Overview

Phase 2 transforms Restock from a standalone app into a connected platform with cloud synchronization, premium monetization, and natural language intelligence. This phase establishes the business model and prepares for advanced AI features.

### Phase Vision
> *"Expand platform reach and introduce natural language features while building sustainable revenue."*

### Key Outcomes
- Cloud sync operational across unlimited devices
- Premium subscription tier launched and generating revenue
- Voice input and natural language processing working
- Recipe URL import functional
- 100,000 total users milestone achieved

### Prerequisites from Phase 1
- Core list functionality stable
- Pattern learning engine operational
- >50% suggestion acceptance rate validated
- Google Play Store presence established

---

## 2. Strategic Goals

| Objective | Target | Measurement |
|-----------|--------|-------------|
| Total users | 100,000 | Play Console installs |
| Premium conversion | 3-5% | Billing analytics |
| Monthly Active Users (MAU) | 40,000+ | Firebase Analytics |
| Day 30 retention | 20%+ | Cohort analysis |
| NL input accuracy | >90% | Error rate tracking |
| Recipe parse success | >85% | Parse completion rate |
| Cloud sync reliability | 99.9% uptime | Backend monitoring |

### Revenue Targets

| Metric | Month 6 Target | Month 9 Target |
|--------|----------------|----------------|
| Premium subscribers | 1,500 | 4,000 |
| ARR run rate | $60,000 | $160,000 |
| Conversion rate | 3% | 4% |

---

## 3. Team Structure & Responsibilities

### Existing Team

#### Tech Lead / Senior Developer
**Phase 2 Focus:**
- Cloud architecture design
- End-to-end encryption implementation
- Natural language parser (on-device)
- Voice input integration
- Performance optimization for sync

**Key Deliverables:**
- Sync engine with conflict resolution
- NL parsing system
- Voice recognition integration
- Premium feature gating

#### UI/UX Engineer
**Phase 2 Focus:**
- Account management flows
- Premium upgrade experience
- Voice input UI
- Recipe import interface
- Settings expansion

**Key Deliverables:**
- Subscription management screens
- Voice input affordances
- Recipe preview/confirm flow
- Premium vs. free UI states

### New Team Members (Month 4+)

#### Backend Developers (2)
**Responsibilities:**
- Cloud infrastructure setup (GCP/AWS)
- RESTful API development
- User authentication system
- Real-time sync service
- Database design (PostgreSQL/Firestore)
- API rate limiting and security

**Reporting:** To Tech Lead

#### DevOps Engineer (1)
**Responsibilities:**
- CI/CD pipeline expansion
- Cloud infrastructure management
- Monitoring and alerting setup
- Security compliance
- Disaster recovery planning
- Cost optimization

**Reporting:** To Tech Lead

### Team Collaboration Model

```
                    ┌──────────────┐
                    │  Tech Lead   │
                    │ (Architect)  │
                    └──────┬───────┘
                           │
       ┌───────────────────┼───────────────────┐
       │                   │                   │
┌──────┴──────┐    ┌───────┴───────┐   ┌──────┴──────┐
│  UI/UX      │    │   Backend     │   │   DevOps    │
│  Engineer   │    │   Devs (2)    │   │   Engineer  │
│             │    │               │   │             │
│ - Mobile UI │    │ - APIs        │   │ - Infra     │
│ - UX flows  │    │ - Auth        │   │ - CI/CD     │
│ - Premium   │    │ - Sync        │   │ - Monitoring│
└─────────────┘    └───────────────┘   └─────────────┘
```

### Communication Cadence
- **Daily:** Standup (15 min, all team)
- **Weekly:** Tech sync (1 hr, Tech Lead + Backend + DevOps)
- **Weekly:** Design review (30 min, Tech Lead + UI/UX)
- **Bi-weekly:** Sprint planning and retrospective
- **Monthly:** Architecture review

---

## 4. Feature Breakdown

### 4.1 Cloud Infrastructure (Months 4-5)

#### User Authentication
**Requirements:**
- Email/password registration
- Social login (Google, Apple)
- Password reset flow
- Session management with secure token refresh
- Biometric authentication option (on-device)
- Rate limiting on auth endpoints

**Implementation Decisions:**
- Use Firebase Auth OR custom OAuth 2.0/OIDC (evaluate first 2 weeks)
- JWT tokens with 1-hour expiry, refresh tokens with 30-day expiry
- Tokens stored in Android Keystore

#### Cloud Sync Architecture
**Requirements:**
- End-to-end encryption using user-derived keys
- Conflict resolution: Last-write-wins with timestamp comparison
- Delta sync for bandwidth efficiency
- Offline-first with background sync
- Multi-device support (Premium: unlimited, Free: 1 device)

**Data Sync Strategy:**
```
┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│   Device A  │◄───────►│    Cloud    │◄───────►│   Device B  │
│             │         │    Sync     │         │             │
│  Local DB   │   E2E   │   Service   │   E2E   │  Local DB   │
│  (SQLite)   │ Encrypt │ (Firestore/ │ Encrypt │  (SQLite)   │
│             │         │  PostgreSQL)│         │             │
└─────────────┘         └─────────────┘         └─────────────┘
```

**Conflict Resolution:**
1. Each change includes `client_id` and `timestamp`
2. Server compares timestamps for same entity
3. Latest timestamp wins
4. Conflicts logged for potential user resolution

#### Account Management UI
- Profile settings screen
- Linked devices view
- Sign out / sign out all devices
- Delete account (with data export option)
- Privacy settings

---

### 4.2 Premium Subscription (Month 6)

#### Google Play Billing Integration
**Requirements:**
- Subscription products: Monthly ($4.99), Annual ($39.99)
- Subscription acknowledgment within 3 days
- Grace period handling (3 days)
- Account hold handling
- Restore purchases flow
- Subscription management deep link

**Implementation:**
```kotlin
// BillingClient setup
class BillingManager @Inject constructor(
    private val context: Context,
    private val subscriptionRepository: SubscriptionRepository,
) {
    private val billingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    // Query available products
    // Launch purchase flow
    // Verify purchases on backend
    // Handle subscription state changes
}
```

#### Premium Feature Gating
**Free Tier:**
- Basic list functionality
- Pattern learning & suggestions
- Meal tags
- Offline mode
- Cloud sync: 1 device only

**Premium Tier:**
- Cloud sync: Unlimited devices
- Natural language input
- Recipe import & parsing
- Priority support

**Gating Strategy:**
```kotlin
@Composable
fun PremiumGatedFeature(
    isPremium: Boolean,
    onUpgradeClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    if (isPremium) {
        content()
    } else {
        PremiumUpsellCard(onUpgradeClick)
    }
}
```

#### Subscription Management UI
- Premium benefits showcase
- Pricing display with annual savings highlight
- Purchase flow (one-tap where possible)
- Subscription status screen
- Cancellation flow (redirect to Play Store)
- Trial period UI (if implemented)

---

### 4.3 Natural Language Input (Months 7-8)

#### Voice Input Integration
**Requirements:**
- Microphone button in add item area
- Use Android `SpeechRecognizer` API
- Support continuous recognition for multiple items
- Visual feedback during listening
- Handle permissions gracefully

**Implementation:**
```kotlin
class VoiceInputManager @Inject constructor() {
    private val speechRecognizer: SpeechRecognizer

    fun startListening(
        onPartialResult: (String) -> Unit,
        onFinalResult: (String) -> Unit,
        onError: (Int) -> Unit,
    )

    fun stopListening()
}
```

#### Natural Language Parser (On-Device)
**Capabilities:**
- Multi-item parsing: "milk, eggs, and bread" → 3 items
- Quantity extraction: "2 gallons of milk" → milk, quantity: 2, unit: gallon
- Basic intent recognition: "I need breakfast stuff" → suggest breakfast-tagged items

**Parser Rules:**
```kotlin
// On-device rule-based parser
class NaturalLanguageParser {
    fun parse(input: String): ParseResult {
        return when {
            input.containsMultipleItems() -> parseItemList(input)
            input.containsQuantity() -> parseQuantifiedItem(input)
            input.containsIntent() -> parseIntent(input)
            else -> ParseResult.SingleItem(input.normalizeItemName())
        }
    }
}
```

#### Advanced NL (Cloud LLM - Premium Only)
**Use Cases:**
- Complex queries: "What should I cook this week?"
- Meal suggestions based on inventory
- Recipe interpretation from description

**Implementation:**
- Use Claude/GPT API via backend proxy
- User data anonymized before sending
- Results cached where possible
- Graceful fallback when offline

**API Contract:**
```json
POST /api/v1/ai/parse
{
    "text": "I'm making tacos tonight",
    "context": {
        "current_list": ["..."],
        "recent_purchases": ["..."],
        "meal_preferences": ["..."]
    }
}

Response:
{
    "intent": "meal_preparation",
    "meal": "tacos",
    "suggested_items": [
        {"name": "tortillas", "confidence": 0.95},
        {"name": "ground beef", "confidence": 0.90},
        {"name": "cheese", "confidence": 0.85}
    ]
}
```

---

### 4.4 Recipe Integration (Month 9)

#### URL Parsing Service
**Requirements:**
- Accept recipe URLs from popular sites
- Extract structured recipe data
- Parse ingredient list into discrete items
- Handle various measurement formats
- Graceful failure for unsupported sites

**Supported Sites (Initial):**
- AllRecipes
- Food Network
- Serious Eats
- NYT Cooking
- Generic recipe schema (schema.org)

#### Ingredient Extraction AI
**Pipeline:**
```
URL → Fetch Page → Extract Schema/HTML →
Parse Ingredients → Normalize Names →
Check "Already Have" → Present to User
```

**Ingredient Normalization:**
```kotlin
// "1 cup all-purpose flour, sifted" → GroceryItem("flour")
// "2 large eggs, beaten" → GroceryItem("eggs", quantity = 2)
// "Salt and pepper to taste" → GroceryItem("salt"), GroceryItem("pepper")
```

#### Recipe Storage
**Data Model:**
```kotlin
data class Recipe(
    val id: String,
    val name: String,
    val sourceUrl: String,
    val ingredients: List<RecipeIngredient>,
    val servings: Int,
    val savedAt: Instant,
)

data class RecipeIngredient(
    val itemId: String?,  // Linked grocery item if exists
    val rawText: String,
    val normalizedName: String,
    val quantity: Double?,
    val unit: String?,
)
```

#### Recipe UI
- URL paste/share input
- Recipe preview with editable ingredients
- "Already have" badges on owned items
- One-tap "Add missing items to list"
- Saved recipes library (Premium)
- Quick re-shop from saved recipes

---

## 5. Monthly Roadmap

### Month 4: Cloud Foundation

**Week 1-2:**
- Backend team onboarding
- Cloud infrastructure evaluation (GCP vs AWS)
- Auth system architecture design
- DevOps: CI/CD expansion for backend

**Week 3-4:**
- Firebase Auth / custom auth implementation
- User registration and login APIs
- Basic backend deployment
- DevOps: Staging environment setup

**Team Focus:**
| Role | Focus |
|------|-------|
| Tech Lead | Architecture decisions, encryption design |
| UI/UX | Account flows wireframes, settings redesign |
| Backend (2) | Auth system, user service |
| DevOps | Infrastructure setup |

---

### Month 5: Sync Implementation

**Week 1-2:**
- Sync API endpoints development
- E2E encryption implementation
- Delta sync algorithm
- Conflict resolution logic

**Week 3-4:**
- Android sync integration
- Background sync with WorkManager
- Multi-device testing
- Account management UI completion

**Team Focus:**
| Role | Focus |
|------|-------|
| Tech Lead | Encryption implementation, sync engine (Android) |
| UI/UX | Account settings UI, sync status indicators |
| Backend (2) | Sync service, conflict resolution |
| DevOps | Database setup, monitoring |

**Key Deliverable:** Cloud sync fully operational

---

### Month 6: Premium Launch

**Week 1-2:**
- Google Play Billing integration
- Subscription verification backend
- Feature gating implementation
- Premium UI development

**Week 3-4:**
- Subscription management screens
- A/B test premium upsell placements
- Analytics for conversion tracking
- Beta test premium features

**Team Focus:**
| Role | Focus |
|------|-------|
| Tech Lead | Billing integration, gating logic |
| UI/UX | Premium showcase, upsell cards |
| Backend (2) | Subscription verification, webhook handling |
| DevOps | Billing webhook infrastructure |

**Key Deliverable:** Premium tier live with paying subscribers

---

### Month 7: Voice Input

**Week 1-2:**
- SpeechRecognizer integration
- Voice UI components
- Permission handling
- Basic NL parser (on-device)

**Week 3-4:**
- Multi-item voice parsing
- Quantity extraction
- Voice feedback refinement
- Accessibility for voice features

**Team Focus:**
| Role | Focus |
|------|-------|
| Tech Lead | Voice recognition, NL parser |
| UI/UX | Voice UI, feedback animations |
| Backend (2) | LLM API proxy setup, caching |
| DevOps | API rate limiting, monitoring |

**Key Deliverable:** Voice input working for basic item addition

---

### Month 8: Advanced NL

**Week 1-2:**
- Cloud LLM integration (Claude/GPT)
- Intent recognition system
- Meal-based suggestions
- Context building for LLM

**Week 3-4:**
- Complex query handling
- Suggestion quality tuning
- Offline fallback improvements
- Premium NL feature polish

**Team Focus:**
| Role | Focus |
|------|-------|
| Tech Lead | LLM integration, context system |
| UI/UX | Suggestion presentation, loading states |
| Backend (2) | AI endpoint, context aggregation |
| DevOps | LLM cost monitoring |

**Key Deliverable:** "I'm making tacos" works correctly

---

### Month 9: Recipe Integration

**Week 1-2:**
- URL fetching service
- Recipe schema parser
- Ingredient extraction
- "Already have" detection

**Week 3-4:**
- Recipe storage system
- One-tap recipe shopping
- Saved recipes library
- Phase 2 completion and review

**Team Focus:**
| Role | Focus |
|------|-------|
| Tech Lead | Ingredient normalization, recipe storage |
| UI/UX | Recipe import flow, library UI |
| Backend (2) | URL parsing service, recipe API |
| DevOps | Recipe service scaling |

**Key Deliverable:** Recipe URL import fully functional

---

## 6. Technical Decisions

### Cloud Provider Selection

**Evaluation Criteria:**
| Criteria | Weight | GCP | AWS |
|----------|--------|-----|-----|
| Firebase integration | High | Native | Amplify (less mature) |
| Cost at scale | Medium | Similar | Similar |
| Team expertise | Medium | Evaluate | Evaluate |
| Compliance | Low | Both adequate | Both adequate |

**Recommendation:** Start with Firebase/GCP for faster iteration, evaluate AWS for Phase 3+ scale needs.

### Authentication Strategy

**Decision:** Firebase Auth for initial launch

**Rationale:**
- Fast implementation (2-3 weeks vs 6+ weeks custom)
- Built-in social login
- Handles edge cases (email verification, password reset)
- Can migrate to custom later if needed

### Encryption Approach

**Decision:** Client-side encryption with user-derived keys

**Implementation:**
```kotlin
// Key derivation from user password
val key = Argon2.hash(
    password = userPassword,
    salt = uniqueUserSalt,
    iterations = 3,
    memory = 64 * 1024,
    outputLength = 32
)

// Encrypt data before sending to server
val encryptedData = AES256GCM.encrypt(
    data = groceryData.toJson(),
    key = derivedKey
)
```

**Key Points:**
- Server never sees plaintext grocery data
- User controls encryption key (derived from password)
- Enables zero-knowledge architecture
- Trade-off: Password recovery requires data loss warning

### LLM Provider Selection

**Evaluation:**
| Provider | Pros | Cons |
|----------|------|------|
| Claude (Anthropic) | High quality, good at parsing | Newer, less battle-tested |
| GPT-4 (OpenAI) | Well-documented, reliable | Higher cost, rate limits |
| Gemini (Google) | On-device option, cost-effective | Quality varies |

**Recommendation:** Claude API for cloud processing, with Gemini Nano evaluation for on-device enhancement.

---

## 7. API Specifications

### Sync Endpoint

```
POST /api/v1/sync
Authorization: Bearer <token>

Request:
{
    "client_id": "uuid",
    "last_sync_at": "2026-03-15T10:30:00Z",
    "changes": [
        {
            "table": "items",
            "operation": "INSERT",
            "id": "item-uuid-123",
            "data": {
                "name": "encrypted_base64...",
                "normalized_name": "encrypted_base64...",
                "created_at": 1710498600000
            },
            "timestamp": "2026-03-15T10:25:00Z"
        }
    ]
}

Response:
{
    "status": "success",
    "server_changes": [...],
    "conflicts": [
        {
            "id": "item-uuid-456",
            "client_version": {...},
            "server_version": {...},
            "resolution": "server_wins"
        }
    ],
    "sync_token": "opaque_sync_token_string"
}
```

### AI Parse Endpoint

```
POST /api/v1/ai/parse
Authorization: Bearer <token>
X-Premium-Token: <subscription_token>

Request:
{
    "text": "I'm making tacos for 4 people tonight",
    "context": {
        "current_list": ["tortillas"],
        "recent_purchases": ["cheese", "lettuce"],
        "household_size": 4
    }
}

Response:
{
    "intent": "meal_preparation",
    "meal_name": "tacos",
    "servings": 4,
    "suggested_items": [
        {"name": "ground beef", "quantity": 1, "unit": "lb", "confidence": 0.95},
        {"name": "taco seasoning", "quantity": 1, "unit": "packet", "confidence": 0.90},
        {"name": "salsa", "confidence": 0.85},
        {"name": "sour cream", "confidence": 0.80}
    ],
    "already_have": ["tortillas", "cheese", "lettuce"],
    "tokens_used": 450
}
```

### Recipe Import Endpoint

```
POST /api/v1/recipe/import
Authorization: Bearer <token>
X-Premium-Token: <subscription_token>

Request:
{
    "url": "https://www.allrecipes.com/recipe/123/amazing-tacos"
}

Response:
{
    "success": true,
    "recipe": {
        "name": "Amazing Tacos",
        "source_url": "https://www.allrecipes.com/recipe/123/amazing-tacos",
        "servings": 6,
        "ingredients": [
            {
                "raw_text": "1 pound ground beef",
                "normalized_name": "ground beef",
                "quantity": 1,
                "unit": "pound"
            },
            {
                "raw_text": "1 packet taco seasoning mix",
                "normalized_name": "taco seasoning",
                "quantity": 1,
                "unit": "packet"
            }
        ]
    }
}
```

---

## 8. Premium Tier Implementation

### Feature Comparison

| Feature | Free | Premium |
|---------|:----:|:-------:|
| Basic list | ✓ | ✓ |
| Pattern learning | ✓ | ✓ |
| Meal tags | ✓ | ✓ |
| Offline mode | ✓ | ✓ |
| Cloud sync devices | 1 | Unlimited |
| Voice input | ✗ | ✓ |
| Natural language AI | ✗ | ✓ |
| Recipe import | ✗ | ✓ |
| Priority support | ✗ | ✓ |

### Pricing Implementation

```kotlin
object PremiumPricing {
    const val MONTHLY_PRODUCT_ID = "restock_premium_monthly"
    const val ANNUAL_PRODUCT_ID = "restock_premium_annual"

    const val MONTHLY_PRICE = "$4.99"
    const val ANNUAL_PRICE = "$39.99"
    const val ANNUAL_SAVINGS = "33%"
}
```

### Upsell Strategy

**Trigger Points:**
1. When user tries to use premium feature
2. After 30 days of consistent use
3. When pattern learning shows high value
4. When user adds second device

**Upsell UI Principles:**
- Show value, not just paywall
- Highlight specific benefit being missed
- One-tap purchase where possible
- Never interrupt core functionality

---

## 9. Quality Assurance

### Testing Requirements

| Test Type | Coverage Target | Focus Areas |
|-----------|-----------------|-------------|
| Unit (Backend) | 80% | API handlers, sync logic, encryption |
| Unit (Android) | 80% | ViewModels, Use Cases, Parsers |
| Integration | 60% | Sync flow, auth flow, billing |
| E2E | Critical paths | Full sync, premium purchase, recipe import |

### Performance Benchmarks

| Metric | Target |
|--------|--------|
| Sync latency (100 items) | < 2 seconds |
| Voice recognition start | < 500ms |
| Recipe parse | < 5 seconds |
| API response time (p95) | < 500ms |
| App cold start (with sync) | < 3 seconds |

### Security Review

**Pre-Launch Checklist:**
- [ ] Penetration testing on backend
- [ ] Encryption implementation review
- [ ] Auth flow security audit
- [ ] API rate limiting tested
- [ ] OWASP MASVS compliance check
- [ ] Data privacy review

---

## 10. Risk Management

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Premium conversion too low | Medium | High | A/B test pricing, focus on household sharing (Phase 3) as key driver |
| LLM costs exceed revenue | Medium | High | On-device models where possible, usage caps, monitor unit economics |
| Sync conflicts frustrate users | Medium | Medium | Clear conflict UI, automatic resolution preference |
| Backend scaling issues | Low | High | Load testing, auto-scaling, monitoring alerts |
| Voice recognition accuracy | Medium | Medium | Clear feedback UI, easy manual correction |
| Recipe parsing failures | Medium | Low | Graceful degradation, manual editing option |

### Contingency Plans

**If premium conversion < 2%:**
- Extend free trial period
- Add more compelling premium features
- Consider lifetime pricing option

**If LLM costs unsustainable:**
- Implement stricter rate limiting
- Shift more processing to on-device
- Negotiate volume pricing with provider

---

## Milestone Summary

| Milestone | Target | Success Criteria |
|-----------|--------|------------------|
| M1: Cloud Sync | Month 5 | Multi-device sync operational |
| M2: Premium Launch | Month 6 | Subscriptions active, payments processing |
| M3: Voice Input | Month 7 | Voice adds items successfully |
| M4: NL Complete | Month 8 | "I'm making tacos" works |
| M5: Recipe Import | Month 9 | URL paste imports ingredients |
| M6: 100K Users | Month 9 | Total installs reach 100,000 |

---

## Transition to Phase 3

### Prerequisites for Phase 3
- [ ] 100,000 total users achieved
- [ ] Premium conversion rate > 3%
- [ ] Cloud infrastructure stable and scalable
- [ ] Backend team fully operational
- [ ] ML Engineer hire initiated

### Phase 3 Preparation
- Begin ML Engineer recruitment (Month 8)
- Evaluate TFLite model requirements for fridge scanning
- Design household sharing architecture
- Plan CameraX integration

---

*Phase 2 Document v1.0 | January 2026*
