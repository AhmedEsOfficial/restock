# Sprint 8: Voice Input - Advanced Parsing
## Phase 2, Month 7, Weeks 3-4

**Sprint Duration:** 2 weeks
**Sprint Number:** 8 of 12 (Phase 2)
**Team:** Tech Lead, UI/UX Engineer, Backend Developers (2), DevOps Engineer

---

## 1. Sprint Overview

This sprint enhances voice input with multi-item parsing, quantity extraction, and refined voice feedback. By the end of this sprint, users will be able to say "milk, eggs, and 2 pounds of chicken" and have all items added correctly.

### Sprint Goal
> *"Enable natural multi-item voice input with intelligent quantity extraction and polished user feedback."*

### Key Outcomes
- Multi-item parsing working ("milk, eggs, and bread")
- Quantity extraction functional ("2 gallons of milk")
- Voice feedback refined and polished
- Accessibility improvements complete
- Voice input milestone achieved

### Prerequisites from Sprint 7
- Basic SpeechRecognizer integration complete
- Voice UI components built
- Basic NL parser working
- Permission handling implemented

---

## 2. Sprint Backlog

### 2.1 User Stories

#### US-8.1: Multi-Item Parsing
**As a** user
**I want** to add multiple items with one voice command
**So that** I can quickly build my shopping list

**Acceptance Criteria:**
- [ ] Parses comma-separated items ("milk, eggs, bread")
- [ ] Parses "and" conjunction ("milk and eggs")
- [ ] Parses mixed format ("milk, eggs, and bread")
- [ ] Handles up to 10 items in one utterance
- [ ] Each item normalized individually
- [ ] Preview shows all parsed items before adding
- [ ] User can remove individual items from preview

**Story Points:** 8
**Assignee:** Backend Developer 1

**Multi-Item Parser:**
```kotlin
class NaturalLanguageParser @Inject constructor() {

    // Updated parse function for multi-item support
    fun parse(input: String): ParseResult {
        val cleaned = input
            .lowercase()
            .removeFillerWords()
            .removeCommandPrefix()
            .handleCorrections()
            .normalizeWhitespace()
            .trim()

        if (cleaned.isEmpty()) {
            return ParseResult.Empty
        }

        // Check for multi-item patterns
        return if (containsMultipleItems(cleaned)) {
            parseItemList(cleaned)
        } else if (containsQuantity(cleaned)) {
            parseQuantifiedItem(cleaned)
        } else {
            ParseResult.SingleItem(
                name = cleaned.capitalizeWords(),
                originalInput = input
            )
        }
    }

    private fun containsMultipleItems(text: String): Boolean {
        return text.contains(",") ||
               text.contains(" and ") ||
               text.contains(" & ")
    }

    private fun parseItemList(text: String): ParseResult.MultipleItems {
        // Split by common delimiters
        val items = text
            .replace(" and ", ",")
            .replace(" & ", ",")
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { itemText ->
                // Each item might have quantity
                if (containsQuantity(itemText)) {
                    parseQuantifiedItem(itemText) as? ParseResult.QuantifiedItem
                        ?: ParseResult.SingleItem(itemText.capitalizeWords(), itemText)
                } else {
                    ParseResult.SingleItem(itemText.capitalizeWords(), itemText)
                }
            }
            .take(10) // Limit to 10 items

        return ParseResult.MultipleItems(items)
    }
}

sealed interface ParseResult {
    data object Empty : ParseResult

    data class SingleItem(
        val name: String,
        val originalInput: String
    ) : ParseResult

    data class QuantifiedItem(
        val name: String,
        val quantity: Double,
        val unit: String?,
        val originalInput: String
    ) : ParseResult

    data class MultipleItems(
        val items: List<ParseResult>  // List of SingleItem or QuantifiedItem
    ) : ParseResult
}
```

---

#### US-8.2: Quantity Extraction
**As a** user
**I want** quantities automatically extracted from my speech
**So that** I don't have to edit items afterward

**Acceptance Criteria:**
- [ ] Extracts numeric quantities ("2 milk" → 2x milk)
- [ ] Extracts word numbers ("two milk" → 2x milk)
- [ ] Extracts unit measurements ("2 gallons of milk")
- [ ] Common units recognized (lb, oz, gallon, dozen, etc.)
- [ ] Handles fractions ("half gallon")
- [ ] Falls back gracefully if parsing fails

**Story Points:** 8
**Assignee:** Backend Developer 1

**Quantity Parser:**
```kotlin
class QuantityParser {

    private val numberWords = mapOf(
        "one" to 1.0, "two" to 2.0, "three" to 3.0, "four" to 4.0,
        "five" to 5.0, "six" to 6.0, "seven" to 7.0, "eight" to 8.0,
        "nine" to 9.0, "ten" to 10.0, "eleven" to 11.0, "twelve" to 12.0,
        "a" to 1.0, "an" to 1.0,
        "half" to 0.5, "quarter" to 0.25,
        "dozen" to 12.0, "couple" to 2.0, "few" to 3.0
    )

    private val units = setOf(
        // Weight
        "lb", "lbs", "pound", "pounds",
        "oz", "ounce", "ounces",
        "kg", "kilogram", "kilograms",
        "g", "gram", "grams",
        // Volume
        "gallon", "gallons", "gal",
        "quart", "quarts", "qt",
        "pint", "pints", "pt",
        "cup", "cups",
        "liter", "liters", "l",
        "ml", "milliliter", "milliliters",
        // Count
        "dozen", "pack", "packs",
        "bunch", "bunches",
        "bag", "bags",
        "box", "boxes",
        "can", "cans",
        "bottle", "bottles",
        "jar", "jars",
        "carton", "cartons"
    )

    private val unitNormalization = mapOf(
        "lb" to "lb", "lbs" to "lb", "pound" to "lb", "pounds" to "lb",
        "oz" to "oz", "ounce" to "oz", "ounces" to "oz",
        "gallon" to "gallon", "gallons" to "gallon", "gal" to "gallon",
        // ... more normalizations
    )

    fun parseQuantity(input: String): QuantityResult? {
        val text = input.lowercase().trim()

        // Pattern: [number] [unit] [of] [item]
        // Examples: "2 gallons of milk", "two pounds of chicken", "a dozen eggs"

        // Try numeric first
        val numericMatch = Regex("^(\\d+(?:\\.\\d+)?)")
            .find(text)

        if (numericMatch != null) {
            val quantity = numericMatch.value.toDouble()
            val remaining = text.removePrefix(numericMatch.value).trim()
            return parseUnitAndItem(quantity, remaining)
        }

        // Try word number
        for ((word, value) in numberWords) {
            if (text.startsWith("$word ")) {
                val remaining = text.removePrefix("$word ").trim()
                return parseUnitAndItem(value, remaining)
            }
        }

        return null
    }

    private fun parseUnitAndItem(quantity: Double, remaining: String): QuantityResult {
        var text = remaining

        // Check for unit
        var unit: String? = null
        for (u in units) {
            if (text.startsWith("$u ") || text.startsWith("$u of ")) {
                unit = unitNormalization[u] ?: u
                text = text
                    .removePrefix(u)
                    .removePrefix(" of ")
                    .removePrefix(" ")
                    .trim()
                break
            }
        }

        // Remaining text is the item name
        val itemName = text.capitalizeWords()

        return QuantityResult(
            quantity = quantity,
            unit = unit,
            itemName = itemName
        )
    }
}

data class QuantityResult(
    val quantity: Double,
    val unit: String?,
    val itemName: String
)
```

---

#### US-8.3: Multi-Item Preview UI
**As a** user
**I want** to see all parsed items before adding them
**So that** I can verify and correct if needed

**Acceptance Criteria:**
- [ ] Shows list of parsed items
- [ ] Each item shows name, quantity (if any), unit (if any)
- [ ] Remove individual items with X button
- [ ] Edit individual items inline (optional)
- [ ] "Add All" button adds all items
- [ ] "Cancel" discards all
- [ ] Smooth animations

**Story Points:** 5
**Assignee:** UI/UX Engineer

**Multi-Item Preview UI:**
```kotlin
@Composable
fun MultiItemPreview(
    items: List<ParsedItem>,
    onRemoveItem: (Int) -> Unit,
    onAddAll: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "Adding ${items.size} items",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f, fill = false).heightIn(max = 300.dp)
        ) {
            itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
                ParsedItemRow(
                    item = item,
                    onRemove = { onRemoveItem(index) },
                    modifier = Modifier.animateItemPlacement()
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = onAddAll,
                modifier = Modifier.weight(1f)
            ) {
                Text("Add All")
            }
        }
    }
}

@Composable
fun ParsedItemRow(
    item: ParsedItem,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge
            )
            if (item.quantity != null || item.unit != null) {
                Text(
                    text = buildString {
                        item.quantity?.let { append(it.formatQuantity()) }
                        item.unit?.let { append(" $it") }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        IconButton(onClick = onRemove) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove ${item.name}"
            )
        }
    }
}
```

---

#### US-8.4: Voice Feedback Refinement
**As a** user
**I want** clear, satisfying feedback during voice input
**So that** I feel confident the app understood me

**Acceptance Criteria:**
- [ ] Haptic feedback when listening starts
- [ ] Sound effect option (on/off in settings)
- [ ] Smooth transitions between states
- [ ] Success animation when items added
- [ ] Error state clearly communicates issue
- [ ] Loading state doesn't feel stuck

**Story Points:** 5
**Assignee:** UI/UX Engineer

**Feedback Enhancements:**
```kotlin
@Composable
fun VoiceInputSheet(
    voiceState: VoiceState,
    parsedItems: List<ParsedItem>,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onCancel: () -> Unit,
    onAddItems: (List<ParsedItem>) -> Unit,
    onRemoveItem: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current

    // Haptic on state change
    LaunchedEffect(voiceState) {
        when (voiceState) {
            is VoiceState.Listening -> haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            is VoiceState.Result -> haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            else -> {}
        }
    }

    AnimatedContent(
        targetState = voiceState,
        transitionSpec = {
            fadeIn(tween(200)) togetherWith fadeOut(tween(200))
        }
    ) { state ->
        when (state) {
            is VoiceState.Idle -> IdleState(onStart = onStartListening)

            is VoiceState.Listening -> ListeningState(
                partialText = null,
                onCancel = onCancel
            )

            is VoiceState.PartialResult -> ListeningState(
                partialText = state.text,
                onCancel = onCancel
            )

            is VoiceState.Processing -> ProcessingState()

            is VoiceState.Result -> {
                if (parsedItems.size > 1) {
                    MultiItemPreview(
                        items = parsedItems,
                        onRemoveItem = onRemoveItem,
                        onAddAll = { onAddItems(parsedItems) },
                        onCancel = onCancel
                    )
                } else {
                    SingleItemConfirm(
                        item = parsedItems.firstOrNull(),
                        onAdd = { onAddItems(parsedItems) },
                        onCancel = onCancel
                    )
                }
            }

            is VoiceState.Error -> ErrorState(
                error = state,
                onRetry = onStartListening,
                onCancel = onCancel
            )

            else -> {}
        }
    }
}

@Composable
fun SuccessAnimation(
    itemCount: Int,
    onComplete: () -> Unit,
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.success_checkmark)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1
    )

    LaunchedEffect(progress) {
        if (progress == 1f) {
            delay(300)
            onComplete()
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(100.dp)
        )
        Text(
            text = if (itemCount == 1) "Item added!" else "$itemCount items added!",
            style = MaterialTheme.typography.titleMedium
        )
    }
}
```

---

#### US-8.5: Continuous Listening Mode (Optional)
**As a** power user
**I want** to add multiple items without tapping each time
**So that** I can quickly dictate my entire list

**Acceptance Criteria:**
- [ ] Toggle for continuous mode in voice UI
- [ ] Pauses between items detected
- [ ] "Done" command or button to finish
- [ ] Items added as confirmed (not batched)
- [ ] Clear visual feedback of continuous mode
- [ ] Times out after 5 seconds of silence

**Story Points:** 5
**Assignee:** Tech Lead

**Continuous Mode:**
```kotlin
class VoiceInputManager @Inject constructor(...) {

    var continuousMode = false

    private fun onSpeechEnd() {
        if (continuousMode) {
            // Process current result
            processCurrentResult()
            // Restart listening after short delay
            viewModelScope.launch {
                delay(500)
                startListening()
            }
        } else {
            _voiceState.value = VoiceState.Processing
        }
    }

    fun enableContinuousMode() {
        continuousMode = true
        startListening()
    }

    fun disableContinuousMode() {
        continuousMode = false
        cancel()
    }
}
```

---

#### US-8.6: Voice Analytics Enhancement
**As a** product manager
**I want** detailed voice input analytics
**So that** I can improve the feature

**Acceptance Criteria:**
- [ ] Track items parsed per utterance
- [ ] Track quantity extraction success rate
- [ ] Track multi-item usage rate
- [ ] Track common parsing errors
- [ ] Track voice input vs manual input ratio
- [ ] Track continuous mode usage

**Story Points:** 3
**Assignee:** Backend Developer 2

**Analytics Events:**
```kotlin
object VoiceAnalytics {
    fun trackVoiceInput(
        itemCount: Int,
        hasQuantity: Boolean,
        parsingDuration: Long,
        recognitionConfidence: Float?
    ) = Analytics.logEvent(
        "voice_input_parsed",
        mapOf(
            "item_count" to itemCount,
            "has_quantity" to hasQuantity,
            "parsing_ms" to parsingDuration,
            "confidence" to (recognitionConfidence ?: -1f)
        )
    )

    fun trackVoiceError(errorType: String, inputText: String?) =
        Analytics.logEvent(
            "voice_input_error",
            mapOf("error_type" to errorType, "input_length" to (inputText?.length ?: 0))
        )

    fun trackContinuousModeSession(
        duration: Long,
        itemsAdded: Int
    ) = Analytics.logEvent(
        "voice_continuous_session",
        mapOf("duration_ms" to duration, "items_added" to itemsAdded)
    )
}
```

---

### 2.2 Technical Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies |
|---------|------|----------|------------|--------------|
| T-8.1 | Implement multi-item parsing logic | Backend Dev 1 | 8 | - |
| T-8.2 | Create QuantityParser class | Backend Dev 1 | 10 | - |
| T-8.3 | Write quantity parser unit tests | Backend Dev 1 | 6 | T-8.2 |
| T-8.4 | Update ParseResult to support multiple items | Backend Dev 1 | 2 | T-8.1 |
| T-8.5 | Build MultiItemPreview composable | UI/UX | 8 | - |
| T-8.6 | Build ParsedItemRow composable | UI/UX | 3 | - |
| T-8.7 | Add animations to voice feedback | UI/UX | 6 | - |
| T-8.8 | Integrate Lottie success animation | UI/UX | 4 | - |
| T-8.9 | Add haptic feedback | UI/UX | 2 | - |
| T-8.10 | Implement continuous listening mode | Tech Lead | 8 | - |
| T-8.11 | Update VoiceInputViewModel for multi-item | Backend Dev 1 | 6 | T-8.1, T-8.4 |
| T-8.12 | Create AddMultipleItemsUseCase | Backend Dev 1 | 4 | - |
| T-8.13 | Implement voice analytics events | Backend Dev 2 | 4 | - |
| T-8.14 | Create voice input settings section | UI/UX | 4 | - |
| T-8.15 | Accessibility audit for multi-item | UI/UX | 4 | T-8.5 |
| T-8.16 | Backend: LLM API rate limiting | Backend Dev 2 | 6 | - |
| T-8.17 | Backend: LLM response caching | Backend Dev 2 | 4 | - |
| T-8.18 | Write multi-item parsing tests | Backend Dev 1 | 4 | T-8.1 |
| T-8.19 | Integration testing voice flow | Backend Dev 2 | 8 | All |
| T-8.20 | Device testing matrix | Backend Dev 2 | 6 | All |

---

## 3. Team Assignments

### Tech Lead
**Focus:** Continuous listening mode
- Implement continuous listening
- Silence detection
- Code review all changes
- Integration support

### UI/UX Engineer
**Focus:** Multi-item UI and polish
- Build MultiItemPreview
- Add success animation (Lottie)
- Haptic feedback
- Voice settings section
- Accessibility audit

### Backend Developer 1
**Focus:** NL parser enhancements
- Implement multi-item parsing
- Create QuantityParser
- Update VoiceInputViewModel
- Create AddMultipleItemsUseCase
- Write comprehensive tests

### Backend Developer 2
**Focus:** Analytics and backend
- Implement voice analytics
- LLM API rate limiting
- LLM response caching
- Integration testing
- Device testing

### DevOps Engineer
**Focus:** Monitoring and infrastructure
- LLM API monitoring setup
- Rate limiting infrastructure
- Voice feature monitoring
- Alert configuration

---

## 4. Dependencies

### External Dependencies
| Dependency | Owner | Status | Risk |
|------------|-------|--------|------|
| Lottie animations | UI/UX | In progress | Low |
| Unit conversion data | Backend Dev 1 | Create | Low |

### Internal Dependencies
| Dependency | From | To | Notes |
|------------|------|-----|-------|
| VoiceInputManager | Sprint 7 | Multi-item | Extend |
| Basic NL parser | Sprint 7 | Quantity parser | Extend |
| Premium gating | Sprint 6 | Voice features | Active |

---

## 5. Definition of Done

### Sprint Level
- [ ] Multi-item parsing works reliably
- [ ] Quantity extraction works for common patterns
- [ ] Multi-item preview UI complete
- [ ] Voice feedback polished with animations
- [ ] Continuous mode functional (optional)
- [ ] Voice analytics tracking
- [ ] 85% test coverage for parsers
- [ ] Voice input milestone achieved

### Story Level
- [ ] Acceptance criteria met
- [ ] Unit tests passing
- [ ] Edge cases handled
- [ ] Code reviewed
- [ ] Accessibility verified

---

## 6. Risk Assessment

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Quantity parsing edge cases | High | Medium | Extensive test suite; fallback to manual |
| Continuous mode battery drain | Medium | Medium | Timeout limits; user control |
| Complex utterances fail | Medium | Medium | Clear error handling; edit option |
| Performance with many items | Low | Medium | Limit to 10 items; optimize |

---

## 7. Testing Requirements

### Unit Tests
- Multi-item parsing (all patterns)
- Quantity extraction (numbers, words, fractions)
- Unit recognition and normalization
- Edge cases (empty, single, many items)

### Integration Tests
- Full voice → parse → add flow
- Multi-item flow with quantities
- Continuous mode session

### Manual Tests
- Real speech with natural variations
- Various accents
- Complex quantities
- Continuous mode reliability

### Test Coverage Targets
| Component | Target |
|-----------|--------|
| NaturalLanguageParser | 90% |
| QuantityParser | 95% |
| VoiceInputViewModel | 85% |
| Multi-item UI | Key flows |

---

## 8. Success Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| Multi-item parsing accuracy | >90% | Analytics |
| Quantity extraction accuracy | >85% | Analytics |
| Items per voice session | >2 avg | Analytics |
| Voice feature satisfaction | >4.0 | User feedback |
| Voice input adoption (premium) | >40% | Analytics |

---

## 9. Sprint Ceremonies

| Ceremony | Day | Duration | Attendees |
|----------|-----|----------|-----------|
| Sprint Planning | Day 1 | 2 hours | All |
| Daily Standup | Daily | 15 min | All |
| Tech Sync | Day 5, 10 | 1 hour | Tech Lead, Backend, DevOps |
| Parser Review | Day 6 | 1 hour | Tech Lead, Backend Dev 1 |
| Voice Demo | Day 9 | 30 min | All |
| Sprint Review | Day 10 | 1 hour | All + stakeholders |
| Retrospective | Day 10 | 45 min | All |

---

## 10. Deliverables Checklist

- [ ] Multi-item parsing implementation
- [ ] QuantityParser implementation
- [ ] MultiItemPreview UI
- [ ] Success/feedback animations
- [ ] Continuous listening mode
- [ ] Voice settings section
- [ ] Voice analytics events
- [ ] LLM rate limiting infrastructure
- [ ] Comprehensive test suite
- [ ] Device testing report
- [ ] Updated voice documentation

---

## 11. Milestone Achievement

### Voice Input Milestone Complete
At the end of this sprint, the **M3: Voice Input** milestone from Phase 2 should be achieved:

- [ ] Voice adds items successfully
- [ ] Multi-item parsing works
- [ ] Quantity extraction works
- [ ] Voice UI polished
- [ ] Accessible with TalkBack
- [ ] Premium feature gated

---

## 12. Handoff to Sprint 9

### Completed Items for Next Sprint
- Voice input fully functional
- NL parser foundation solid
- LLM infrastructure prepared

### Prepared Items for Next Sprint
- Ready for cloud LLM integration
- Ready for intent recognition
- Ready for meal-based suggestions

---

*Sprint 8 Document v1.0 | Phase 2: Growth & Intelligence | Restock Android*
