# Sprint 9: Advanced NL - LLM Integration
## Phase 2, Month 8, Weeks 1-2

**Sprint Duration:** 2 weeks
**Sprint Number:** 9 of 12 (Phase 2)
**Team:** Tech Lead, UI/UX Engineer, Backend Developers (2), DevOps Engineer

---

## 1. Sprint Overview

This sprint integrates cloud LLM capabilities (Claude/GPT) for advanced natural language understanding. Users will be able to say "I'm making tacos tonight" and receive intelligent suggestions for ingredients. This is a premium-only feature.

### Sprint Goal
> *"Enable intelligent meal-based suggestions through cloud LLM integration with context-aware understanding."*

### Key Outcomes
- Cloud LLM API integrated via backend proxy
- Intent recognition working ("I'm making tacos")
- Meal-based suggestions functional
- Context building for LLM prompts
- Graceful offline fallback
- LLM feature gated as premium

### Prerequisites from Sprint 8
- Voice input with multi-item parsing complete
- LLM API proxy infrastructure prepared
- Rate limiting in place
- Premium subscription system working

---

## 2. Sprint Backlog

### 2.1 User Stories

#### US-9.1: LLM API Integration (Backend)
**As a** backend developer
**I want** a secure LLM API proxy service
**So that** mobile clients can access AI capabilities safely

**Acceptance Criteria:**
- [ ] Backend endpoint proxies requests to LLM provider
- [ ] User context anonymized before sending
- [ ] API key secured (never exposed to client)
- [ ] Request/response logging for debugging (without PII)
- [ ] Timeout handling (max 10 seconds)
- [ ] Error handling with graceful degradation
- [ ] Rate limiting per user (50 requests/day)

**Story Points:** 8
**Assignee:** Backend Developer 1

**Backend LLM Service:**
```kotlin
class LlmService(
    private val anthropicClient: AnthropicClient,  // or OpenAIClient
    private val rateLimiter: RateLimiter,
) {
    suspend fun parseIntent(
        userId: String,
        text: String,
        context: UserContext
    ): LlmResult {
        // Check rate limit
        if (!rateLimiter.tryAcquire(userId, "llm_parse")) {
            return LlmResult.RateLimited
        }

        // Anonymize context
        val anonymizedContext = context.anonymize()

        // Build prompt
        val prompt = buildPrompt(text, anonymizedContext)

        // Call LLM API
        return try {
            withTimeout(10.seconds) {
                val response = anthropicClient.messages.create(
                    model = "claude-3-haiku-20240307",  // Fast, cost-effective
                    maxTokens = 500,
                    messages = listOf(
                        Message(role = "user", content = prompt)
                    )
                )

                parseLlmResponse(response.content)
            }
        } catch (e: TimeoutCancellationException) {
            LlmResult.Timeout
        } catch (e: Exception) {
            logger.error("LLM API error", e)
            LlmResult.Error(e.message ?: "Unknown error")
        }
    }

    private fun buildPrompt(text: String, context: AnonymizedContext): String = """
        You are a grocery shopping assistant. The user said: "$text"

        Context:
        - Current list items: ${context.currentListItems.joinToString(", ")}
        - Recent purchases: ${context.recentPurchases.joinToString(", ")}
        - Household size: ${context.householdSize}

        Analyze the user's intent and respond with a JSON object:
        {
            "intent": "meal_preparation" | "item_addition" | "question" | "unclear",
            "meal_name": "string or null",
            "suggested_items": [
                {"name": "item name", "quantity": 1, "unit": "optional", "confidence": 0.0-1.0}
            ],
            "already_have": ["items from context they likely have"],
            "response_text": "Brief friendly response"
        }

        Only suggest items directly needed for the meal. Be concise.
    """.trimIndent()

    private fun parseLlmResponse(content: String): LlmResult {
        return try {
            val json = Json.decodeFromString<LlmResponseJson>(content)
            LlmResult.Success(
                intent = json.intent,
                mealName = json.meal_name,
                suggestedItems = json.suggested_items.map { it.toSuggestedItem() },
                alreadyHave = json.already_have,
                responseText = json.response_text
            )
        } catch (e: Exception) {
            LlmResult.ParseError
        }
    }
}

sealed interface LlmResult {
    data class Success(
        val intent: String,
        val mealName: String?,
        val suggestedItems: List<SuggestedItem>,
        val alreadyHave: List<String>,
        val responseText: String
    ) : LlmResult
    data object RateLimited : LlmResult
    data object Timeout : LlmResult
    data class Error(val message: String) : LlmResult
    data object ParseError : LlmResult
}
```

---

#### US-9.2: AI Parse Endpoint
**As a** mobile app
**I want** an API endpoint for AI-powered text parsing
**So that** I can get intelligent suggestions

**Acceptance Criteria:**
- [ ] POST /api/v1/ai/parse endpoint
- [ ] Requires premium subscription token
- [ ] Accepts text and context
- [ ] Returns parsed intent and suggestions
- [ ] Includes token usage for tracking
- [ ] Handles all error cases

**Story Points:** 5
**Assignee:** Backend Developer 1

**API Specification:**
```
POST /api/v1/ai/parse
Authorization: Bearer <access_token>
X-Premium-Token: <subscription_token>
Content-Type: application/json

Request:
{
    "text": "I'm making tacos for 4 people tonight",
    "context": {
        "current_list": ["tortillas"],
        "recent_purchases": ["cheese", "lettuce", "tomatoes"],
        "household_size": 4,
        "meal_preferences": ["mexican", "quick meals"]
    }
}

Response (200 OK):
{
    "intent": "meal_preparation",
    "meal_name": "tacos",
    "servings": 4,
    "suggested_items": [
        {"name": "ground beef", "quantity": 1, "unit": "lb", "confidence": 0.95},
        {"name": "taco seasoning", "quantity": 1, "unit": "packet", "confidence": 0.90},
        {"name": "salsa", "quantity": 1, "unit": "jar", "confidence": 0.85},
        {"name": "sour cream", "quantity": 1, "confidence": 0.80}
    ],
    "already_have": ["tortillas", "cheese", "lettuce", "tomatoes"],
    "response_text": "Great choice! Here are the ingredients you'll need for tacos for 4.",
    "tokens_used": 450
}

Response (429 Too Many Requests):
{
    "error": "rate_limited",
    "message": "Daily AI request limit reached. Try again tomorrow.",
    "limit": 50,
    "used": 50,
    "resets_at": "2026-08-02T00:00:00Z"
}

Response (403 Forbidden):
{
    "error": "premium_required",
    "message": "AI features require a premium subscription"
}
```

---

#### US-9.3: Android LLM Repository
**As a** mobile developer
**I want** a clean LLM repository interface
**So that** the app can use AI features consistently

**Acceptance Criteria:**
- [ ] LlmRepository interface in domain layer
- [ ] Implementation calls backend API
- [ ] Handles offline gracefully
- [ ] Caches recent results
- [ ] Builds context from local data
- [ ] Tracks usage for UI feedback

**Story Points:** 8
**Assignee:** Tech Lead

**Repository Implementation:**
```kotlin
interface LlmRepository {
    suspend fun parseIntent(text: String): Result<LlmParseResult>
    fun getRemainingDailyRequests(): Flow<Int>
}

class LlmRepositoryImpl @Inject constructor(
    private val aiApi: AiApi,
    private val itemRepository: ItemRepository,
    private val purchaseRepository: PurchaseRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val connectivityObserver: ConnectivityObserver,
    private val cache: LlmCache,
) : LlmRepository {

    override suspend fun parseIntent(text: String): Result<LlmParseResult> {
        // Check connectivity
        if (!connectivityObserver.isConnected()) {
            return Result.failure(OfflineException())
        }

        // Check cache
        val cached = cache.get(text)
        if (cached != null) {
            return Result.success(cached)
        }

        // Build context
        val context = buildContext()

        // Call API
        return aiApi.parse(
            request = AiParseRequest(
                text = text,
                context = context
            )
        ).map { response ->
            val result = response.toParseResult()
            cache.put(text, result)
            result
        }
    }

    private suspend fun buildContext(): AiContext {
        val currentList = itemRepository.getCurrentListItems().first()
        val recentPurchases = purchaseRepository.getRecentPurchases(days = 30, limit = 50)
        val preferences = userPreferencesRepository.getMealPreferences()
        val householdSize = userPreferencesRepository.getHouseholdSize()

        return AiContext(
            currentList = currentList.map { it.name },
            recentPurchases = recentPurchases.map { it.itemName }.distinct(),
            householdSize = householdSize,
            mealPreferences = preferences
        )
    }
}

class LlmCache @Inject constructor() {
    private val cache = LruCache<String, LlmParseResult>(50)
    private val ttl = 1.hours

    fun get(key: String): LlmParseResult? {
        val entry = cache.get(key.hashCode().toString()) ?: return null
        return if (entry.timestamp + ttl > Clock.System.now()) entry.result else null
    }

    fun put(key: String, result: LlmParseResult) {
        cache.put(key.hashCode().toString(), CacheEntry(result, Clock.System.now()))
    }
}
```

---

#### US-9.4: Intent Recognition UI
**As a** user
**I want** the app to understand what I mean
**So that** it can suggest relevant items

**Acceptance Criteria:**
- [ ] Detects meal intent from voice/text input
- [ ] Shows loading state during AI processing
- [ ] Displays meal name and suggested items
- [ ] "Already have" items marked
- [ ] One-tap to add all missing items
- [ ] Individual item selection possible
- [ ] Shows AI response text

**Story Points:** 8
**Assignee:** UI/UX Engineer

**Intent Recognition UI:**
```kotlin
@Composable
fun AiSuggestionsSheet(
    result: LlmParseResult.MealSuggestion,
    onAddItems: (List<SuggestedItem>) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedItems by remember {
        mutableStateOf(result.suggestedItems.filter { it.confidence > 0.7 }.toSet())
    }

    Column(modifier = modifier.padding(16.dp)) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Default.Restaurant,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Making ${result.mealName}",
                style = MaterialTheme.typography.titleLarge
            )
        }

        // AI Response text
        Text(
            text = result.responseText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        // Suggested items
        if (result.suggestedItems.isNotEmpty()) {
            Text(
                text = "Suggested items",
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(Modifier.height(8.dp))

            result.suggestedItems.forEach { item ->
                SuggestedItemRow(
                    item = item,
                    isSelected = item in selectedItems,
                    onToggle = {
                        selectedItems = if (item in selectedItems) {
                            selectedItems - item
                        } else {
                            selectedItems + item
                        }
                    }
                )
            }
        }

        // Already have section
        if (result.alreadyHave.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "You already have",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = result.alreadyHave.joinToString(", "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Actions
        Spacer(Modifier.height(24.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = { onAddItems(selectedItems.toList()) },
                enabled = selectedItems.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) {
                Text("Add ${selectedItems.size} Items")
            }
        }
    }
}

@Composable
fun SuggestedItemRow(
    item: SuggestedItem,
    isSelected: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 8.dp)
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() }
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge
            )
            if (item.quantity != null || item.unit != null) {
                Text(
                    text = "${item.quantity ?: ""} ${item.unit ?: ""}".trim(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Confidence indicator
        ConfidenceBadge(item.confidence)
    }
}

@Composable
fun ConfidenceBadge(confidence: Float) {
    val color = when {
        confidence >= 0.9 -> MaterialTheme.colorScheme.primary
        confidence >= 0.7 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outline
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = "${(confidence * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
```

---

#### US-9.5: Meal-Based Suggestions
**As a** user planning a meal
**I want** the system to know common meal ingredients
**So that** I get helpful suggestions

**Acceptance Criteria:**
- [ ] LLM understands common meals (tacos, pasta, salad, etc.)
- [ ] Suggestions consider serving size
- [ ] Suggestions consider dietary context
- [ ] High confidence for core ingredients
- [ ] Lower confidence for optional items
- [ ] Reasonable number of suggestions (5-10)

**Story Points:** 5
**Assignee:** Backend Developer 2

**Prompt Engineering:**
```kotlin
object PromptTemplates {

    val mealParsingPrompt = """
        You are a grocery shopping assistant helping users plan meals.

        Guidelines:
        1. Focus on essential ingredients first (high confidence 0.9+)
        2. Add common accompaniments with medium confidence (0.7-0.9)
        3. Suggest optional items with lower confidence (0.5-0.7)
        4. Consider the serving size mentioned
        5. Don't suggest items the user already has
        6. Use common grocery store item names
        7. Include reasonable quantities

        Common meal mappings (use as baseline):
        - Tacos: tortillas, ground beef/chicken, taco seasoning, cheese, lettuce, tomatoes, salsa, sour cream
        - Pasta: pasta, sauce, parmesan, garlic bread, salad
        - Stir fry: rice, vegetables, protein, soy sauce, cooking oil
        - Breakfast: eggs, bacon/sausage, bread, butter, juice/coffee

        Respond ONLY with valid JSON.
    """.trimIndent()

    fun buildMealPrompt(text: String, context: AiContext): String {
        return """
            $mealParsingPrompt

            User input: "$text"

            Context:
            - Already on list: ${context.currentList.joinToString(", ").ifEmpty { "nothing" }}
            - Recent purchases (likely have): ${context.recentPurchases.take(20).joinToString(", ")}
            - Household size: ${context.householdSize} people

            Respond with JSON:
            {
                "intent": "meal_preparation",
                "meal_name": "meal name",
                "servings": number,
                "suggested_items": [
                    {"name": "item", "quantity": number, "unit": "string or null", "confidence": 0.0-1.0}
                ],
                "already_have": ["items from context"],
                "response_text": "Brief friendly response"
            }
        """.trimIndent()
    }
}
```

---

#### US-9.6: Offline Fallback
**As a** user without internet
**I want** the app to still work
**So that** I'm not blocked from using it

**Acceptance Criteria:**
- [ ] Detects offline state
- [ ] Falls back to on-device parser
- [ ] Clear message that AI features unavailable
- [ ] Basic intent recognition still works
- [ ] No crash or error state

**Story Points:** 3
**Assignee:** Tech Lead

**Fallback Logic:**
```kotlin
class NaturalLanguageService @Inject constructor(
    private val onDeviceParser: NaturalLanguageParser,
    private val llmRepository: LlmRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val connectivityObserver: ConnectivityObserver,
) {
    suspend fun parse(text: String): NlResult {
        // Check if premium and online
        val isPremium = subscriptionRepository.isPremium()
        val isOnline = connectivityObserver.isConnected()

        return when {
            !isPremium -> {
                // Free tier: on-device only
                NlResult.OnDevice(onDeviceParser.parse(text))
            }
            !isOnline -> {
                // Premium but offline: on-device with message
                NlResult.OfflineFallback(
                    result = onDeviceParser.parse(text),
                    message = "AI features require internet. Using basic parsing."
                )
            }
            else -> {
                // Premium and online: try LLM
                llmRepository.parseIntent(text)
                    .map { NlResult.Ai(it) }
                    .getOrElse { error ->
                        // LLM failed: fall back
                        NlResult.AiFallback(
                            result = onDeviceParser.parse(text),
                            message = "AI unavailable. Using basic parsing."
                        )
                    }
            }
        }
    }
}

sealed interface NlResult {
    data class OnDevice(val result: ParseResult) : NlResult
    data class OfflineFallback(val result: ParseResult, val message: String) : NlResult
    data class Ai(val result: LlmParseResult) : NlResult
    data class AiFallback(val result: ParseResult, val message: String) : NlResult
}
```

---

### 2.2 Technical Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies |
|---------|------|----------|------------|--------------|
| T-9.1 | Create Anthropic/OpenAI client wrapper | Backend Dev 1 | 8 | - |
| T-9.2 | Implement LlmService with prompts | Backend Dev 1 | 10 | T-9.1 |
| T-9.3 | Create /ai/parse endpoint | Backend Dev 1 | 6 | T-9.2 |
| T-9.4 | Implement per-user rate limiting | Backend Dev 1 | 4 | - |
| T-9.5 | Create LlmRepository interface | Tech Lead | 2 | - |
| T-9.6 | Implement LlmRepositoryImpl | Tech Lead | 8 | T-9.3, T-9.5 |
| T-9.7 | Implement context building | Tech Lead | 6 | T-9.6 |
| T-9.8 | Implement LLM response caching | Tech Lead | 4 | T-9.6 |
| T-9.9 | Create NaturalLanguageService | Tech Lead | 6 | T-9.6 |
| T-9.10 | Design AI suggestions UI | UI/UX | 8 | - |
| T-9.11 | Build AiSuggestionsSheet | UI/UX | 10 | T-9.10 |
| T-9.12 | Build SuggestedItemRow component | UI/UX | 4 | - |
| T-9.13 | Create AI loading state UI | UI/UX | 4 | - |
| T-9.14 | Implement prompt templates | Backend Dev 2 | 6 | - |
| T-9.15 | Test and tune meal prompts | Backend Dev 2 | 8 | T-9.2, T-9.14 |
| T-9.16 | Implement offline fallback | Tech Lead | 4 | T-9.9 |
| T-9.17 | Create AiViewModel | Backend Dev 2 | 6 | T-9.9 |
| T-9.18 | Integrate AI into voice flow | Backend Dev 2 | 6 | T-9.11, T-9.17 |
| T-9.19 | Write LLM service tests (mock) | Backend Dev 1 | 6 | T-9.2 |
| T-9.20 | Write integration tests | Backend Dev 2 | 6 | All |
| T-9.21 | Set up LLM cost monitoring | DevOps | 4 | T-9.3 |

---

## 3. Team Assignments

### Tech Lead
**Focus:** Android LLM integration
- Create LlmRepository
- Implement context building
- Implement response caching
- Create NaturalLanguageService
- Implement offline fallback

### UI/UX Engineer
**Focus:** AI suggestions UI
- Design AI suggestions interface
- Build AiSuggestionsSheet
- Build SuggestedItemRow
- Create loading/error states
- Ensure accessibility

### Backend Developer 1
**Focus:** LLM backend service
- Create LLM client wrapper
- Implement LlmService
- Create /ai/parse endpoint
- Implement rate limiting
- Write service tests

### Backend Developer 2
**Focus:** Prompts and integration
- Design prompt templates
- Test and tune meal prompts
- Create AiViewModel
- Integrate AI into voice flow
- Write integration tests

### DevOps Engineer
**Focus:** LLM infrastructure
- Set up LLM cost monitoring
- Configure rate limit storage
- Alert on high LLM costs
- Monitor API latency

---

## 4. Dependencies

### External Dependencies
| Dependency | Owner | Status | Risk |
|------------|-------|--------|------|
| Anthropic API key | Tech Lead | Required | Medium |
| OpenAI API key (backup) | Tech Lead | Optional | Low |
| LLM budget approval | Product | Required | Medium |

### Internal Dependencies
| Dependency | From | To | Notes |
|------------|------|-----|-------|
| Voice input | Sprint 8 | AI integration | Input source |
| Premium subscription | Sprint 6 | AI gating | Access control |
| On-device parser | Sprint 8 | Fallback | Offline support |

---

## 5. Definition of Done

### Sprint Level
- [ ] LLM backend service operational
- [ ] AI parse endpoint working
- [ ] Meal-based suggestions accurate
- [ ] AI suggestions UI complete
- [ ] Offline fallback working
- [ ] Rate limiting enforced
- [ ] Cost monitoring in place
- [ ] 80% test coverage for new code

### Story Level
- [ ] Acceptance criteria met
- [ ] Prompts tested with various inputs
- [ ] UI matches designs
- [ ] Code reviewed
- [ ] Cost impact documented

---

## 6. Risk Assessment

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| LLM costs exceed budget | Medium | High | Rate limiting; usage monitoring; alerts |
| LLM response quality poor | Medium | High | Prompt engineering; fallback; user feedback |
| LLM latency too high | Medium | Medium | Caching; timeout; loading states |
| API rate limits hit | Low | Medium | Queue requests; graceful degradation |
| Prompt injection attacks | Low | Medium | Input sanitization; output validation |

---

## 7. Testing Requirements

### Unit Tests
- LLM response parsing
- Context building
- Cache operations
- Rate limiting logic
- Fallback logic

### Integration Tests
- Full AI flow (mock LLM)
- Rate limit behavior
- Offline fallback
- Premium gating

### Manual Tests
- Various meal inputs
- Edge cases ("weird food")
- Response quality review
- Latency testing

### Test Coverage Targets
| Component | Target |
|-----------|--------|
| LlmService | 80% |
| LlmRepository | 85% |
| NaturalLanguageService | 90% |
| AI UI components | Key flows |

---

## 8. Success Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| AI suggestion acceptance | >60% | Analytics |
| AI response latency (p95) | <3 seconds | Monitoring |
| AI error rate | <5% | Monitoring |
| Cost per AI request | <$0.01 | LLM dashboard |
| Premium user AI usage | >30% | Analytics |

---

## 9. Sprint Ceremonies

| Ceremony | Day | Duration | Attendees |
|----------|-----|----------|-----------|
| Sprint Planning | Day 1 | 2 hours | All |
| Daily Standup | Daily | 15 min | All |
| Tech Sync | Day 5, 10 | 1 hour | Tech Lead, Backend, DevOps |
| Prompt Review | Day 6 | 1 hour | Tech Lead, Backend |
| AI Demo | Day 9 | 30 min | All |
| Sprint Review | Day 10 | 1 hour | All + stakeholders |
| Retrospective | Day 10 | 45 min | All |

---

## 10. Deliverables Checklist

- [ ] LLM service implementation
- [ ] AI parse endpoint
- [ ] Rate limiting implementation
- [ ] LlmRepository implementation
- [ ] Context building system
- [ ] Response caching
- [ ] NaturalLanguageService
- [ ] AiSuggestionsSheet UI
- [ ] Offline fallback
- [ ] Prompt templates documentation
- [ ] Cost monitoring dashboard
- [ ] Test suite

---

## 11. Handoff to Sprint 10

### Completed Items for Next Sprint
- LLM integration functional
- Meal-based suggestions working
- Basic AI capabilities in place

### Prepared Items for Next Sprint
- Ready for quality tuning
- Ready for complex query handling
- Ready for premium NL polish

---

*Sprint 9 Document v1.0 | Phase 2: Growth & Intelligence | Restock Android*
