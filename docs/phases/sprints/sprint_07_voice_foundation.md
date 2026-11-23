# Sprint 7: Voice Input - Foundation
## Phase 2, Month 7, Weeks 1-2

**Sprint Duration:** 2 weeks
**Sprint Number:** 7 of 12 (Phase 2)
**Team:** Tech Lead, UI/UX Engineer, Backend Developers (2), DevOps Engineer

---

## 1. Sprint Overview

This sprint implements the foundation for voice input, including Android SpeechRecognizer integration, voice UI components, permission handling, and a basic on-device natural language parser. Users will be able to add items to their grocery list by speaking.

### Sprint Goal
> *"Enable users to add grocery items using voice input with a delightful, responsive UI experience."*

### Key Outcomes
- SpeechRecognizer integrated and working
- Voice input UI with real-time feedback
- Microphone permission handling
- Basic NL parser for single items
- Voice input as premium feature (gated)
- Accessibility considerations addressed

### Prerequisites from Sprint 6
- Premium feature gating system in place
- Premium subscription tier launched
- User authentication and sync working

---

## 2. Sprint Backlog

### 2.1 User Stories

#### US-7.1: SpeechRecognizer Integration
**As a** developer
**I want** Android SpeechRecognizer properly integrated
**So that** we can convert speech to text reliably

**Acceptance Criteria:**
- [ ] SpeechRecognizer initialized with context
- [ ] Supports offline recognition (if available)
- [ ] Handles partial results for real-time feedback
- [ ] Handles final results
- [ ] Error handling for all error codes
- [ ] Graceful fallback when speech not available
- [ ] Respects device language settings

**Story Points:** 8
**Assignee:** Tech Lead

**SpeechRecognizer Implementation:**
```kotlin
class VoiceInputManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    private val _voiceState = MutableStateFlow<VoiceState>(VoiceState.Idle)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    fun initialize() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _voiceState.value = VoiceState.Unavailable
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(createListener())
        }
    }

    fun startListening() {
        if (isListening) return

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        }

        _voiceState.value = VoiceState.Listening
        isListening = true
        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        isListening = false
        speechRecognizer?.stopListening()
        _voiceState.value = VoiceState.Processing
    }

    fun cancel() {
        isListening = false
        speechRecognizer?.cancel()
        _voiceState.value = VoiceState.Idle
    }

    private fun createListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _voiceState.value = VoiceState.Listening
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            matches?.firstOrNull()?.let { partial ->
                _voiceState.value = VoiceState.PartialResult(partial)
            }
        }

        override fun onResults(results: Bundle?) {
            isListening = false
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            matches?.firstOrNull()?.let { result ->
                _voiceState.value = VoiceState.Result(result)
            } ?: run {
                _voiceState.value = VoiceState.NoMatch
            }
        }

        override fun onError(error: Int) {
            isListening = false
            _voiceState.value = when (error) {
                SpeechRecognizer.ERROR_NO_MATCH -> VoiceState.NoMatch
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> VoiceState.Timeout
                SpeechRecognizer.ERROR_NETWORK,
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> VoiceState.NetworkError
                SpeechRecognizer.ERROR_AUDIO -> VoiceState.AudioError
                else -> VoiceState.Error(error)
            }
        }

        override fun onBeginningOfSpeech() {}
        override fun onEndOfSpeech() { _voiceState.value = VoiceState.Processing }
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    fun release() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}

sealed interface VoiceState {
    data object Idle : VoiceState
    data object Unavailable : VoiceState
    data object Listening : VoiceState
    data object Processing : VoiceState
    data class PartialResult(val text: String) : VoiceState
    data class Result(val text: String) : VoiceState
    data object NoMatch : VoiceState
    data object Timeout : VoiceState
    data object NetworkError : VoiceState
    data object AudioError : VoiceState
    data class Error(val code: Int) : VoiceState
}
```

---

#### US-7.2: Microphone Permission Handling
**As a** user
**I want** a clear microphone permission flow
**So that** I understand why the app needs access

**Acceptance Criteria:**
- [ ] Permission requested with clear rationale
- [ ] Handles permission granted
- [ ] Handles permission denied
- [ ] Handles "don't ask again" case
- [ ] Provides way to open app settings
- [ ] Graceful degradation without permission

**Story Points:** 5
**Assignee:** Tech Lead

**Permission Flow:**
```kotlin
@Composable
fun VoiceInputButton(
    voiceState: VoiceState,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    var showRationale by remember { mutableStateOf(false) }

    IconButton(
        onClick = {
            when {
                permissionState.status.isGranted -> {
                    if (voiceState is VoiceState.Listening) {
                        onStopListening()
                    } else {
                        onStartListening()
                    }
                }
                permissionState.status.shouldShowRationale -> {
                    showRationale = true
                }
                else -> {
                    permissionState.launchPermissionRequest()
                }
            }
        },
        modifier = modifier
    ) {
        Icon(
            imageVector = when (voiceState) {
                is VoiceState.Listening -> Icons.Default.Stop
                else -> Icons.Default.Mic
            },
            contentDescription = "Voice input"
        )
    }

    if (showRationale) {
        PermissionRationaleDialog(
            title = "Microphone Permission",
            message = "Restock needs microphone access to add items by voice. Your voice data is processed on-device and not stored.",
            onConfirm = {
                showRationale = false
                permissionState.launchPermissionRequest()
            },
            onDismiss = { showRationale = false }
        )
    }
}
```

---

#### US-7.3: Voice Input UI
**As a** user
**I want** clear visual feedback during voice input
**So that** I know the app is listening and what I said

**Acceptance Criteria:**
- [ ] Microphone button visible in input area
- [ ] Animated indicator when listening
- [ ] Real-time text display (partial results)
- [ ] Confirmation before adding item
- [ ] Cancel option during listening
- [ ] Success/error feedback animations
- [ ] Works with system dark mode

**Story Points:** 8
**Assignee:** UI/UX Engineer

**Voice Input UI States:**
```
Idle State:
┌─────────────────────────────────────────┐
│ Add an item...                      🎤 │
└─────────────────────────────────────────┘

Listening State:
┌─────────────────────────────────────────┐
│                                          │
│       [Pulsing Microphone Animation]     │
│                                          │
│       "Listening..."                     │
│                                          │
│              [Cancel]                    │
└─────────────────────────────────────────┘

Partial Result State:
┌─────────────────────────────────────────┐
│                                          │
│       [Microphone with audio waves]      │
│                                          │
│       "milk and eg..."                   │
│                                          │
│              [Cancel]                    │
└─────────────────────────────────────────┘

Result State:
┌─────────────────────────────────────────┐
│                                          │
│       "milk"                             │
│                                          │
│   [Add to List]        [Cancel]         │
│                                          │
└─────────────────────────────────────────┘
```

**Voice Input Composable:**
```kotlin
@Composable
fun VoiceInputSheet(
    voiceState: VoiceState,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onCancel: () -> Unit,
    onAddItem: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (voiceState) {
            is VoiceState.Idle -> {
                // Waiting to start
                PulsingMicrophoneButton(onClick = onStartListening)
                Text("Tap to speak", style = MaterialTheme.typography.bodyMedium)
            }

            is VoiceState.Listening -> {
                // Listening animation
                ListeningAnimation()
                Text("Listening...", style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = onCancel) { Text("Cancel") }
            }

            is VoiceState.PartialResult -> {
                // Show partial text
                ListeningAnimation(intensity = 0.5f)
                Text(
                    text = voiceState.text,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onCancel) { Text("Cancel") }
            }

            is VoiceState.Processing -> {
                CircularProgressIndicator()
                Text("Processing...", style = MaterialTheme.typography.bodyMedium)
            }

            is VoiceState.Result -> {
                Icon(Icons.Default.CheckCircle, null, tint = Success)
                Text(
                    text = voiceState.text,
                    style = MaterialTheme.typography.titleLarge
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(onClick = onCancel) { Text("Cancel") }
                    Button(onClick = { onAddItem(voiceState.text) }) { Text("Add to List") }
                }
            }

            is VoiceState.NoMatch,
            is VoiceState.Timeout -> {
                Icon(Icons.Default.MicOff, null, tint = Warning)
                Text("Didn't catch that. Try again?")
                Button(onClick = onStartListening) { Text("Try Again") }
            }

            is VoiceState.Error -> {
                Icon(Icons.Default.Error, null, tint = Error)
                Text("Something went wrong")
                TextButton(onClick = onCancel) { Text("Cancel") }
            }

            else -> {}
        }
    }
}

@Composable
fun ListeningAnimation(intensity: Float = 1f) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f * intensity,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        )
    )

    Icon(
        imageVector = Icons.Default.Mic,
        contentDescription = "Listening",
        modifier = Modifier
            .size(64.dp)
            .scale(scale),
        tint = MaterialTheme.colorScheme.primary
    )
}
```

---

#### US-7.4: Basic NL Parser (On-Device)
**As a** user
**I want** my spoken input understood naturally
**So that** I don't have to speak like a robot

**Acceptance Criteria:**
- [ ] Normalizes item names (capitalization, whitespace)
- [ ] Removes filler words ("um", "uh", "like")
- [ ] Handles common corrections ("I mean", "actually")
- [ ] Strips leading "add" or similar commands
- [ ] Handles singular/plural normalization
- [ ] Fast: < 50ms for typical input

**Story Points:** 5
**Assignee:** Backend Developer 1

**Basic Parser Implementation:**
```kotlin
class NaturalLanguageParser @Inject constructor() {

    private val fillerWords = setOf("um", "uh", "like", "you know", "basically", "actually")
    private val commandPrefixes = listOf("add", "put", "get", "buy", "i need", "we need", "get me", "pick up")
    private val correctionPhrases = listOf("i mean", "actually", "no wait", "sorry")

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

        return ParseResult.SingleItem(
            name = cleaned.capitalizeWords(),
            originalInput = input
        )
    }

    private fun String.removeFillerWords(): String {
        var result = this
        fillerWords.forEach { filler ->
            result = result.replace(Regex("\\b$filler\\b"), " ")
        }
        return result
    }

    private fun String.removeCommandPrefix(): String {
        commandPrefixes.forEach { prefix ->
            if (this.startsWith(prefix)) {
                return this.removePrefix(prefix).trimStart()
            }
        }
        return this
    }

    private fun String.handleCorrections(): String {
        correctionPhrases.forEach { phrase ->
            val index = this.lastIndexOf(phrase)
            if (index != -1) {
                return this.substring(index + phrase.length).trim()
            }
        }
        return this
    }

    private fun String.normalizeWhitespace(): String =
        this.replace(Regex("\\s+"), " ")

    private fun String.capitalizeWords(): String =
        this.split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
}

sealed interface ParseResult {
    data object Empty : ParseResult
    data class SingleItem(
        val name: String,
        val originalInput: String
    ) : ParseResult
}
```

---

#### US-7.5: Voice Input ViewModel
**As a** developer
**I want** a clean ViewModel for voice input
**So that** the UI layer stays simple

**Acceptance Criteria:**
- [ ] Manages VoiceInputManager lifecycle
- [ ] Exposes voice state
- [ ] Handles add item action
- [ ] Integrates with premium gating
- [ ] Handles permission state
- [ ] Logs analytics events

**Story Points:** 5
**Assignee:** Backend Developer 1

**ViewModel Implementation:**
```kotlin
@HiltViewModel
class VoiceInputViewModel @Inject constructor(
    private val voiceInputManager: VoiceInputManager,
    private val addItemUseCase: AddItemUseCase,
    private val nlParser: NaturalLanguageParser,
    private val subscriptionRepository: SubscriptionRepository,
) : ViewModel() {

    val voiceState: StateFlow<VoiceState> = voiceInputManager.voiceState
    val isPremium: StateFlow<Boolean> = subscriptionRepository.subscriptionState
        .map { it is SubscriptionState.Premium }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _events = Channel<VoiceInputEvent>(Channel.BUFFERED)
    val events: Flow<VoiceInputEvent> = _events.receiveAsFlow()

    init {
        voiceInputManager.initialize()
    }

    fun startListening() {
        if (!isPremium.value) {
            viewModelScope.launch {
                _events.send(VoiceInputEvent.PremiumRequired)
            }
            return
        }
        Analytics.trackVoiceInputStart()
        voiceInputManager.startListening()
    }

    fun stopListening() {
        voiceInputManager.stopListening()
    }

    fun cancel() {
        voiceInputManager.cancel()
    }

    fun addItem(spokenText: String) {
        viewModelScope.launch {
            val parseResult = nlParser.parse(spokenText)

            when (parseResult) {
                is ParseResult.SingleItem -> {
                    addItemUseCase(parseResult.name)
                        .onSuccess {
                            Analytics.trackVoiceInputSuccess(parseResult.name)
                            _events.send(VoiceInputEvent.ItemAdded(parseResult.name))
                        }
                        .onFailure {
                            _events.send(VoiceInputEvent.Error(it.message ?: "Failed to add item"))
                        }
                }
                ParseResult.Empty -> {
                    _events.send(VoiceInputEvent.Error("Couldn't understand input"))
                }
            }

            voiceInputManager.cancel() // Reset to idle
        }
    }

    override fun onCleared() {
        voiceInputManager.release()
    }
}

sealed interface VoiceInputEvent {
    data class ItemAdded(val name: String) : VoiceInputEvent
    data class Error(val message: String) : VoiceInputEvent
    data object PremiumRequired : VoiceInputEvent
}
```

---

#### US-7.6: Voice Accessibility
**As a** user with motor disabilities
**I want** voice input to be fully accessible
**So that** I can use the app hands-free

**Acceptance Criteria:**
- [ ] Voice button has proper content description
- [ ] Listening state announced
- [ ] Results announced
- [ ] Works with external switches
- [ ] No critical timing requirements
- [ ] Clear audio feedback (optional haptics)

**Story Points:** 3
**Assignee:** UI/UX Engineer

---

### 2.2 Technical Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies |
|---------|------|----------|------------|--------------|
| T-7.1 | Create VoiceInputManager class | Tech Lead | 10 | - |
| T-7.2 | Implement SpeechRecognizer callbacks | Tech Lead | 6 | T-7.1 |
| T-7.3 | Handle offline recognition mode | Tech Lead | 4 | T-7.1 |
| T-7.4 | Create permission handling composable | Tech Lead | 4 | - |
| T-7.5 | Create PermissionRationaleDialog | Tech Lead | 2 | - |
| T-7.6 | Design voice input UI states | UI/UX | 6 | - |
| T-7.7 | Build VoiceInputSheet composable | UI/UX | 10 | T-7.6 |
| T-7.8 | Create ListeningAnimation | UI/UX | 4 | - |
| T-7.9 | Create PulsingMicrophoneButton | UI/UX | 3 | - |
| T-7.10 | Build success/error feedback animations | UI/UX | 4 | - |
| T-7.11 | Implement NaturalLanguageParser | Backend Dev 1 | 8 | - |
| T-7.12 | Write NL parser unit tests | Backend Dev 1 | 4 | T-7.11 |
| T-7.13 | Create VoiceInputViewModel | Backend Dev 1 | 6 | T-7.1, T-7.11 |
| T-7.14 | Integrate voice input into list screen | Backend Dev 1 | 4 | T-7.7, T-7.13 |
| T-7.15 | Add premium gating to voice input | Backend Dev 1 | 2 | T-7.13 |
| T-7.16 | Implement voice analytics events | Backend Dev 2 | 4 | - |
| T-7.17 | Accessibility audit for voice UI | UI/UX | 4 | T-7.7 |
| T-7.18 | Backend: set up LLM API proxy (prep) | Backend Dev 2 | 8 | - |
| T-7.19 | Write VoiceInputManager tests | Tech Lead | 6 | T-7.1 |
| T-7.20 | Write VoiceInputViewModel tests | Backend Dev 1 | 4 | T-7.13 |
| T-7.21 | Manual voice testing on devices | Backend Dev 2 | 6 | All |

---

## 3. Team Assignments

### Tech Lead
**Focus:** SpeechRecognizer integration
- Create VoiceInputManager
- Implement all recognition callbacks
- Handle offline mode
- Create permission handling
- Write integration tests

### UI/UX Engineer
**Focus:** Voice input UI
- Design voice input states
- Build VoiceInputSheet
- Create animations (listening, success, error)
- Ensure accessibility compliance
- Dark mode support

### Backend Developer 1
**Focus:** NL parser and ViewModel
- Implement NaturalLanguageParser
- Create VoiceInputViewModel
- Integrate voice into list screen
- Add premium gating
- Write unit tests

### Backend Developer 2
**Focus:** Analytics and backend prep
- Implement voice analytics events
- Set up LLM API proxy infrastructure (for Sprint 9)
- Manual testing on various devices
- Document voice input behavior

### DevOps Engineer
**Focus:** Infrastructure preparation
- API proxy infrastructure setup
- Rate limiting preparation
- Monitoring for voice features

---

## 4. Dependencies

### External Dependencies
| Dependency | Owner | Status | Risk |
|------------|-------|--------|------|
| Android SpeechRecognizer | System | Available | Low |
| Offline speech model | Device | Optional | Medium |
| Accompanist permissions | Gradle | Available | Low |

### Internal Dependencies
| Dependency | From | To | Notes |
|------------|------|-----|-------|
| Premium gating | Sprint 6 | Voice input | Feature gate |
| Add item use case | Phase 1 | Voice add | Reuse |
| Analytics | Sprint 6 | Voice events | Events |

---

## 5. Definition of Done

### Sprint Level
- [ ] User can add items by voice
- [ ] Voice UI provides clear feedback
- [ ] Permission handling works correctly
- [ ] Basic NL parser cleans input
- [ ] Voice gated as premium feature
- [ ] Works offline (basic recognition)
- [ ] Accessible with TalkBack
- [ ] 80% test coverage for voice code

### Story Level
- [ ] Acceptance criteria met
- [ ] UI matches designs
- [ ] Tested on 3+ device types
- [ ] Code reviewed
- [ ] Analytics events firing

---

## 6. Risk Assessment

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Speech recognition accuracy varies | High | Medium | Clear feedback; easy retry |
| Offline recognition not available | Medium | Low | Fall back to online; inform user |
| Permission rejection rate high | Medium | Medium | Clear rationale; non-blocking |
| NL parser misunderstands | High | Low | Show result; allow edit |
| Device compatibility issues | Medium | Medium | Test widely; graceful fallback |

---

## 7. Testing Requirements

### Unit Tests
- VoiceInputManager state transitions
- NaturalLanguageParser all cases
- VoiceInputViewModel logic
- Permission state handling

### Integration Tests
- Voice recognition mock
- End-to-end add item flow

### Manual Tests
- Real device speech recognition
- Various accents/speaking styles
- Offline mode
- Permission flows
- Accessibility with TalkBack

### Test Coverage Targets
| Component | Target |
|-----------|--------|
| VoiceInputManager | 80% |
| NaturalLanguageParser | 90% |
| VoiceInputViewModel | 85% |

---

## 8. Success Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| Voice recognition success rate | >85% | Analytics |
| Voice add completion rate | >70% | Analytics |
| Voice input latency | <2 seconds | Performance |
| Permission grant rate | >60% | Analytics |
| User satisfaction (voice) | >4.0 | Feedback |

---

## 9. Sprint Ceremonies

| Ceremony | Day | Duration | Attendees |
|----------|-----|----------|-----------|
| Sprint Planning | Day 1 | 2 hours | All |
| Daily Standup | Daily | 15 min | All |
| Tech Sync | Day 5, 10 | 1 hour | Tech Lead, Backend, DevOps |
| Design Review | Day 4 | 30 min | Tech Lead, UI/UX |
| Voice Demo | Day 8 | 30 min | All |
| Sprint Review | Day 10 | 1 hour | All + stakeholders |
| Retrospective | Day 10 | 45 min | All |

---

## 10. Deliverables Checklist

- [ ] VoiceInputManager implementation
- [ ] Permission handling flow
- [ ] VoiceInputSheet UI component
- [ ] Voice animations (listening, success, error)
- [ ] NaturalLanguageParser implementation
- [ ] VoiceInputViewModel
- [ ] Voice analytics events
- [ ] Premium gating for voice
- [ ] Accessibility verification report
- [ ] Voice testing documentation
- [ ] LLM API proxy infrastructure (prep)

---

## 11. Milestone Progress

### Voice Input Milestone (Partial)
This sprint achieves basic voice input:

- [x] Voice adds single items
- [x] Voice UI provides feedback
- [ ] Multi-item parsing (Sprint 8)
- [ ] Quantity extraction (Sprint 8)

---

## 12. Handoff to Sprint 8

### Completed Items for Next Sprint
- Basic voice input working
- NL parser foundation ready
- Voice UI components complete
- LLM proxy infrastructure ready

### Prepared Items for Next Sprint
- Ready to add multi-item parsing
- Ready to add quantity extraction
- Ready to polish voice feedback

---

*Sprint 7 Document v1.0 | Phase 2: Growth & Intelligence | Restock Android*
