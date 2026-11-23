# Sprint 10: Advanced NL - Polish & Optimization
## Phase 2, Month 8, Weeks 3-4

**Sprint Duration:** 2 weeks
**Sprint Number:** 10 of 12 (Phase 2)
**Team:** Tech Lead, UI/UX Engineer, Backend Developers (2), DevOps Engineer

---

## 1. Sprint Overview

This sprint focuses on polishing the natural language features, improving suggestion quality, handling complex queries, and optimizing the AI experience. By the end of this sprint, the NL milestone should be achieved with the "I'm making tacos" use case working flawlessly.

### Sprint Goal
> *"Polish natural language features to production quality with improved accuracy, better handling of complex queries, and optimized user experience."*

### Key Outcomes
- Complex query handling improved
- Suggestion quality significantly enhanced
- Offline fallback improvements
- AI feature polish complete
- "I'm making tacos" milestone achieved
- Performance optimized

### Prerequisites from Sprint 9
- LLM integration functional
- AI suggestions UI complete
- Basic meal-based suggestions working
- Cost monitoring in place

---

## 2. Sprint Backlog

### 2.1 User Stories

#### US-10.1: Complex Query Handling
**As a** user
**I want** to ask complex food-related questions
**So that** the app helps me with meal planning

**Acceptance Criteria:**
- [ ] Handles "What should I cook this week?"
- [ ] Handles "Something quick for dinner"
- [ ] Handles "I have chicken and rice, what can I make?"
- [ ] Handles "We're having guests Saturday"
- [ ] Handles dietary restrictions ("vegetarian tacos")
- [ ] Graceful response for unrelated queries

**Story Points:** 8
**Assignee:** Backend Developer 2

**Enhanced Prompts:**
```kotlin
object AdvancedPrompts {

    val mealSuggestionPrompt = """
        You are a helpful grocery shopping assistant.

        Understand these query types:
        1. MEAL_PREPARATION: User mentions making a specific meal ("making tacos")
        2. MEAL_SUGGESTION: User asks what to cook ("what should I make")
        3. INGREDIENT_BASED: User mentions what they have ("I have chicken")
        4. OCCASION_BASED: User mentions an event ("dinner party")
        5. CONSTRAINT_BASED: User mentions restrictions ("vegetarian", "quick")
        6. SIMPLE_ADD: User just wants to add items

        For MEAL_SUGGESTION and INGREDIENT_BASED:
        - Suggest 2-3 meal ideas with brief descriptions
        - Let user pick, then provide ingredients

        For CONSTRAINT_BASED:
        - Respect dietary restrictions
        - Consider time constraints
        - Factor in skill level hints

        Always be concise and helpful.
    """.trimIndent()

    fun buildComplexQueryPrompt(
        text: String,
        context: AiContext,
        queryType: QueryType
    ): String {
        return when (queryType) {
            QueryType.MEAL_SUGGESTION -> buildMealSuggestionPrompt(text, context)
            QueryType.INGREDIENT_BASED -> buildIngredientBasedPrompt(text, context)
            QueryType.OCCASION_BASED -> buildOccasionPrompt(text, context)
            else -> buildStandardPrompt(text, context)
        }
    }
}
```

---

#### US-10.2: Suggestion Quality Tuning
**As a** user
**I want** accurate and relevant suggestions
**So that** I trust the AI recommendations

**Acceptance Criteria:**
- [ ] Core ingredients always suggested (high confidence)
- [ ] Optional items clearly marked
- [ ] Quantities appropriate for serving size
- [ ] Uncommon/specialty items lower confidence
- [ ] Feedback mechanism for wrong suggestions
- [ ] Learning from user patterns (future)

**Story Points:** 8
**Assignee:** Backend Developer 2

**Quality Improvements:**
```kotlin
class SuggestionQualityService @Inject constructor(
    private val itemRepository: ItemRepository,
    private val purchaseRepository: PurchaseRepository,
) {
    suspend fun refineSuggestions(
        suggestions: List<SuggestedItem>,
        context: AiContext
    ): List<SuggestedItem> {
        return suggestions.map { suggestion ->
            // Boost confidence for items user frequently buys
            val purchaseFrequency = purchaseRepository
                .getPurchaseFrequency(suggestion.name)

            // Lower confidence for specialty items
            val isSpecialty = isSpecialtyItem(suggestion.name)

            // Adjust confidence
            val adjustedConfidence = when {
                purchaseFrequency > 5 -> minOf(suggestion.confidence + 0.1f, 1f)
                isSpecialty -> maxOf(suggestion.confidence - 0.15f, 0.3f)
                else -> suggestion.confidence
            }

            suggestion.copy(confidence = adjustedConfidence)
        }.sortedByDescending { it.confidence }
    }

    private fun isSpecialtyItem(name: String): Boolean {
        val specialtyPatterns = listOf(
            "truffle", "saffron", "wagyu", "organic",
            "artisan", "imported", "specialty"
        )
        return specialtyPatterns.any { name.lowercase().contains(it) }
    }
}

// Feedback mechanism
class SuggestionFeedbackService @Inject constructor(
    private val feedbackRepository: FeedbackRepository
) {
    suspend fun recordFeedback(
        suggestionId: String,
        wasAccepted: Boolean,
        wasHelpful: Boolean?
    ) {
        feedbackRepository.saveFeedback(
            SuggestionFeedback(
                suggestionId = suggestionId,
                accepted = wasAccepted,
                helpful = wasHelpful,
                timestamp = Clock.System.now()
            )
        )
    }
}
```

---

#### US-10.3: Multi-Turn Conversation
**As a** user
**I want** follow-up questions handled
**So that** I can refine my request

**Acceptance Criteria:**
- [ ] Remembers previous query context
- [ ] "Add those" after suggestion works
- [ ] "What else do I need?" continues context
- [ ] "Actually, make it vegetarian" modifies
- [ ] Context clears after 2 minutes of inactivity
- [ ] Clear indication of active context

**Story Points:** 8
**Assignee:** Tech Lead

**Conversation Context:**
```kotlin
class ConversationManager @Inject constructor() {
    private val contextTimeout = 2.minutes

    private var currentContext: ConversationContext? = null
    private var lastInteraction: Instant? = null

    fun getContext(): ConversationContext? {
        val last = lastInteraction ?: return null
        if (Clock.System.now() - last > contextTimeout) {
            clearContext()
            return null
        }
        return currentContext
    }

    fun updateContext(
        query: String,
        result: LlmParseResult,
        suggestions: List<SuggestedItem>
    ) {
        currentContext = ConversationContext(
            originalQuery = query,
            mealName = result.mealName,
            lastSuggestions = suggestions,
            turnCount = (currentContext?.turnCount ?: 0) + 1
        )
        lastInteraction = Clock.System.now()
    }

    fun clearContext() {
        currentContext = null
        lastInteraction = null
    }

    fun isFollowUp(text: String): Boolean {
        if (currentContext == null) return false

        val followUpPatterns = listOf(
            "add those", "add them", "add all",
            "what else", "anything else",
            "actually", "instead", "wait",
            "make it", "change to"
        )

        return followUpPatterns.any { text.lowercase().contains(it) }
    }
}

data class ConversationContext(
    val originalQuery: String,
    val mealName: String?,
    val lastSuggestions: List<SuggestedItem>,
    val turnCount: Int,
    val modifiers: List<String> = emptyList()  // "vegetarian", "quick", etc.
)
```

---

#### US-10.4: Offline Fallback Improvements
**As a** user without internet
**I want** smart suggestions even offline
**So that** the app is still useful

**Acceptance Criteria:**
- [ ] Common meals have offline ingredient lists
- [ ] On-device meal database (50+ common meals)
- [ ] Fuzzy matching for meal names
- [ ] Clear indication of offline mode
- [ ] Seamless transition when online returns

**Story Points:** 5
**Assignee:** Backend Developer 1

**Offline Meal Database:**
```kotlin
object OfflineMealDatabase {
    val meals: Map<String, OfflineMeal> = mapOf(
        "tacos" to OfflineMeal(
            name = "Tacos",
            aliases = listOf("taco", "tacos", "taco night"),
            coreIngredients = listOf(
                Ingredient("tortillas", 1.0, "pack"),
                Ingredient("ground beef", 1.0, "lb"),
                Ingredient("taco seasoning", 1.0, "packet"),
                Ingredient("cheese", 1.0, "cup", shredded = true)
            ),
            optionalIngredients = listOf(
                Ingredient("lettuce", 1.0, "head"),
                Ingredient("tomatoes", 2.0, null),
                Ingredient("salsa", 1.0, "jar"),
                Ingredient("sour cream", 1.0, "container")
            )
        ),
        "spaghetti" to OfflineMeal(
            name = "Spaghetti",
            aliases = listOf("spaghetti", "pasta", "spaghetti dinner"),
            coreIngredients = listOf(
                Ingredient("spaghetti", 1.0, "lb"),
                Ingredient("pasta sauce", 1.0, "jar"),
                Ingredient("ground beef", 1.0, "lb")
            ),
            optionalIngredients = listOf(
                Ingredient("parmesan cheese", 1.0, null),
                Ingredient("garlic bread", 1.0, "loaf"),
                Ingredient("salad mix", 1.0, "bag")
            )
        ),
        // ... 50+ more meals
    )

    fun findMeal(query: String): OfflineMeal? {
        val normalized = query.lowercase().trim()

        // Exact match
        meals.values.find { meal ->
            meal.aliases.any { it == normalized }
        }?.let { return it }

        // Fuzzy match
        return meals.values.find { meal ->
            meal.aliases.any { alias ->
                normalized.contains(alias) || alias.contains(normalized)
            }
        }
    }
}

class OfflineMealParser @Inject constructor() {
    fun parse(text: String): OfflineParseResult {
        // Detect meal intent
        val mealPatterns = listOf(
            Regex("(?:making|cooking|having|want)\\s+(.+?)(?:\\s+for|\\s+tonight|\$)", RegexOption.IGNORE_CASE),
            Regex("^(.+?)\\s+(?:for dinner|for lunch|tonight)", RegexOption.IGNORE_CASE)
        )

        for (pattern in mealPatterns) {
            val match = pattern.find(text)
            if (match != null) {
                val mealName = match.groupValues[1].trim()
                val meal = OfflineMealDatabase.findMeal(mealName)
                if (meal != null) {
                    return OfflineParseResult.MealFound(meal)
                }
            }
        }

        return OfflineParseResult.NoMealFound
    }
}
```

---

#### US-10.5: AI Experience Polish
**As a** user
**I want** a smooth, delightful AI experience
**So that** I enjoy using the feature

**Acceptance Criteria:**
- [ ] Loading states feel snappy (skeleton UI)
- [ ] Error messages are friendly and actionable
- [ ] Success feels satisfying (animation)
- [ ] Retry is easy and obvious
- [ ] AI branding subtle but present
- [ ] Help/tips for new users

**Story Points:** 5
**Assignee:** UI/UX Engineer

**Polish Improvements:**
```kotlin
@Composable
fun AiLoadingState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(24.dp)
    ) {
        // Animated dots or subtle brain icon
        AiThinkingAnimation()

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Thinking...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Optional: Rotating helpful tips
        AiTip()
    }
}

@Composable
fun AiThinkingAnimation() {
    val infiniteTransition = rememberInfiniteTransition()
    val dots = remember { listOf(0, 1, 2) }

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        dots.forEach { index ->
            val delay = index * 200
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = delay),
                    repeatMode = RepeatMode.Reverse
                )
            )

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = alpha),
                        CircleShape
                    )
            )
        }
    }
}

@Composable
fun AiTip() {
    val tips = listOf(
        "Try: \"I'm making tacos tonight\"",
        "Try: \"Quick dinner for two\"",
        "Try: \"What goes with chicken?\""
    )

    var currentTip by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            currentTip = (currentTip + 1) % tips.size
        }
    }

    AnimatedContent(targetState = currentTip) { index ->
        Text(
            text = tips[index],
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun AiErrorState(
    error: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(24.dp)
    ) {
        Icon(
            Icons.Default.CloudOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Couldn't get suggestions",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = error,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
            Button(onClick = onRetry) {
                Icon(Icons.Default.Refresh, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Try Again")
            }
        }
    }
}
```

---

#### US-10.6: Performance Optimization
**As a** user
**I want** fast AI responses
**So that** I don't wait around

**Acceptance Criteria:**
- [ ] Response time < 3 seconds (p95)
- [ ] Caching reduces repeat queries to < 500ms
- [ ] Parallel context building
- [ ] Optimized prompts (fewer tokens)
- [ ] Prefetch suggestions when likely

**Story Points:** 5
**Assignee:** Tech Lead

**Optimizations:**
```kotlin
class AiPerformanceOptimizer @Inject constructor(
    private val contextBuilder: ContextBuilder,
    private val llmCache: LlmCache,
    private val mealPredictor: MealPredictor,
) {
    // Prefetch likely meals based on time of day
    suspend fun prefetchSuggestions() {
        val likelyMeals = mealPredictor.getLikelyMeals()
        likelyMeals.take(3).forEach { meal ->
            val cacheKey = "meal:${meal.lowercase()}"
            if (!llmCache.has(cacheKey)) {
                // Prefetch in background
                supervisorScope {
                    launch {
                        // Build prompt and cache result
                    }
                }
            }
        }
    }

    // Parallel context building
    suspend fun buildContextFast(): AiContext = coroutineScope {
        val currentListDeferred = async { getCurrentList() }
        val recentPurchasesDeferred = async { getRecentPurchases() }
        val preferencesDeferred = async { getPreferences() }

        AiContext(
            currentList = currentListDeferred.await(),
            recentPurchases = recentPurchasesDeferred.await(),
            preferences = preferencesDeferred.await()
        )
    }

    // Optimize prompt length
    fun optimizePrompt(prompt: String): String {
        return prompt
            .replace(Regex("\\s+"), " ")  // Remove extra whitespace
            .take(2000)  // Limit context length
    }
}

class MealPredictor @Inject constructor() {
    fun getLikelyMeals(): List<String> {
        val hour = LocalTime.now().hour
        return when (hour) {
            in 6..10 -> listOf("breakfast", "eggs", "pancakes")
            in 11..14 -> listOf("lunch", "sandwich", "salad")
            in 17..21 -> listOf("dinner", "tacos", "pasta", "stir fry")
            else -> listOf("snack")
        }
    }
}
```

---

### 2.2 Technical Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies |
|---------|------|----------|------------|--------------|
| T-10.1 | Implement complex query prompts | Backend Dev 2 | 8 | - |
| T-10.2 | Test complex queries with LLM | Backend Dev 2 | 6 | T-10.1 |
| T-10.3 | Create SuggestionQualityService | Backend Dev 2 | 6 | - |
| T-10.4 | Implement feedback mechanism | Backend Dev 2 | 4 | - |
| T-10.5 | Implement ConversationManager | Tech Lead | 8 | - |
| T-10.6 | Integrate conversation context | Tech Lead | 6 | T-10.5 |
| T-10.7 | Create offline meal database | Backend Dev 1 | 8 | - |
| T-10.8 | Implement OfflineMealParser | Backend Dev 1 | 6 | T-10.7 |
| T-10.9 | Enhance offline fallback | Backend Dev 1 | 4 | T-10.8 |
| T-10.10 | Design AI loading animation | UI/UX | 4 | - |
| T-10.11 | Implement AiThinkingAnimation | UI/UX | 3 | T-10.10 |
| T-10.12 | Create AiErrorState | UI/UX | 3 | - |
| T-10.13 | Add AI tips rotation | UI/UX | 2 | - |
| T-10.14 | Polish success animation | UI/UX | 4 | - |
| T-10.15 | Implement parallel context building | Tech Lead | 4 | - |
| T-10.16 | Implement prefetch suggestions | Tech Lead | 6 | - |
| T-10.17 | Optimize prompt length | Backend Dev 1 | 2 | - |
| T-10.18 | End-to-end NL testing | Backend Dev 2 | 8 | All |
| T-10.19 | Performance benchmarking | Tech Lead | 4 | All |
| T-10.20 | User acceptance testing | UI/UX | 6 | All |

---

## 3. Team Assignments

### Tech Lead
**Focus:** Conversation management, performance
- Implement ConversationManager
- Integrate conversation context
- Parallel context building
- Implement prefetch suggestions
- Performance benchmarking

### UI/UX Engineer
**Focus:** AI experience polish
- Design and implement loading animation
- Create error state UI
- Polish success animation
- Add AI tips
- User acceptance testing

### Backend Developer 1
**Focus:** Offline improvements
- Create offline meal database
- Implement OfflineMealParser
- Enhance offline fallback
- Optimize prompt length

### Backend Developer 2
**Focus:** Query handling and quality
- Implement complex query prompts
- Test and tune prompts
- Create SuggestionQualityService
- Implement feedback mechanism
- End-to-end testing

### DevOps Engineer
**Focus:** Monitoring and optimization
- Performance monitoring
- LLM cost optimization
- Alert tuning
- Cache hit rate monitoring

---

## 4. Definition of Done

### Sprint Level
- [ ] Complex queries handled appropriately
- [ ] Suggestion quality noticeably improved
- [ ] Conversation context working
- [ ] Offline fallback enhanced with meal database
- [ ] AI UI fully polished
- [ ] Response time < 3 seconds (p95)
- [ ] "I'm making tacos" works perfectly
- [ ] NL milestone achieved

### Story Level
- [ ] Acceptance criteria met
- [ ] Quality verified with test users
- [ ] Performance benchmarks met
- [ ] Code reviewed

---

## 5. Success Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| Complex query success rate | >70% | Manual review |
| Suggestion acceptance rate | >65% | Analytics |
| Response time (p95) | <3 seconds | Monitoring |
| Cache hit rate | >30% | Monitoring |
| User satisfaction | >4.0 | Feedback |

---

## 6. Milestone Achievement

### NL Complete Milestone
At the end of this sprint, the **M4: NL Complete** milestone from Phase 2 should be achieved:

- [ ] "I'm making tacos" works correctly
- [ ] Complex queries handled
- [ ] Suggestion quality high
- [ ] Offline fallback improved
- [ ] AI experience polished
- [ ] Performance optimized

---

## 7. Handoff to Sprint 11

### Completed Items for Next Sprint
- Natural language features complete
- AI infrastructure stable
- Quality and performance optimized

### Prepared Items for Next Sprint
- Ready for recipe URL parsing
- LLM infrastructure can handle recipe prompts
- Context building extensible for recipes

---

*Sprint 10 Document v1.0 | Phase 2: Growth & Intelligence | Restock Android*
