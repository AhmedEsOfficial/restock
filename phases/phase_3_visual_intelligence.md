# Phase 3: Visual Intelligence
## Restock Android Application

**Duration:** Months 10-15 (6 months)
**Sprints:** 12 sprints (2-week cycles)
**Team Expansion:** ML Engineer (1), Additional Android Dev (1)
**Full Team:** Tech Lead (1), UI/UX Engineer (1), Backend Devs (2), DevOps (1), ML Engineer (1), Android Dev (1)

---

## Table of Contents

1. [Phase Overview](#1-phase-overview)
2. [Strategic Goals](#2-strategic-goals)
3. [Team Structure & Responsibilities](#3-team-structure--responsibilities)
4. [Feature Breakdown](#4-feature-breakdown)
5. [Monthly Roadmap](#5-monthly-roadmap)
6. [Technical Decisions](#6-technical-decisions)
7. [ML Model Specifications](#7-ml-model-specifications)
8. [Privacy & Security](#8-privacy--security)
9. [Quality Assurance](#9-quality-assurance)
10. [Risk Management](#10-risk-management)

---

## 1. Phase Overview

Phase 3 adds camera-based features for advanced input methods and introduces household sharing—the key premium conversion driver. This phase transitions Restock from a personal tool to a family platform while maintaining the privacy-first philosophy.

### Phase Vision
> *"Add visual AI features for even faster input and smarter inventory awareness, while enabling family collaboration."*

### Key Outcomes
- Household sharing operational (Premium)
- Barcode scanning for quick item addition
- Fridge scanning for inventory assessment (Premium)
- Receipt scanning for automatic check-off (Premium)
- 500,000 total users milestone
- 25,000 premium subscribers

### Prerequisites from Phase 2
- Cloud sync stable at scale
- Premium subscription infrastructure proven
- 100,000+ user base established
- Backend team fully operational
- LLM integration working

---

## 2. Strategic Goals

| Objective | Target | Measurement |
|-----------|--------|-------------|
| Total users | 500,000 | Play Console installs |
| Premium subscribers | 25,000 | Billing analytics |
| Premium conversion | 5-7% | Billing analytics |
| Household adoption | 40% of premium | Feature analytics |
| Barcode scan success | >95% | Scan completion rate |
| Fridge scan accuracy | >80% | Item detection accuracy |
| Receipt scan accuracy | >85% | Item match rate |

### Revenue Targets

| Metric | Month 12 Target | Month 15 Target |
|--------|-----------------|-----------------|
| Premium subscribers | 15,000 | 25,000 |
| ARR run rate | $600,000 | $1,000,000 |
| Conversion rate | 5% | 6% |

---

## 3. Team Structure & Responsibilities

### Full Team Structure (Phase 3)

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

### New Team Members

#### ML Engineer (1) - Starts Month 10
**Responsibilities:**
- Custom TFLite model development for fridge scanning
- Item detection and classification
- Model optimization for mobile (size < 5MB)
- Training data pipeline
- Model versioning and deployment

**Key Deliverables:**
- Fridge item detection model
- Receipt text recognition enhancement
- On-device inference optimization

#### Additional Android Developer (1) - Starts Month 10
**Responsibilities:**
- CameraX integration
- Barcode scanning implementation
- Camera preview UI
- Image processing pipeline
- Camera permission handling

**Key Deliverables:**
- Camera module implementation
- Barcode scanner feature
- Image capture optimization

### Role Assignments by Feature

| Feature | Tech Lead | UI/UX | Android Dev | Backend | ML Engineer | DevOps |
|---------|:---------:|:-----:|:-----------:|:-------:|:-----------:|:------:|
| Household Sharing | Architecture | UX flows | — | APIs | — | Real-time infra |
| Barcode Scanning | Integration | Camera UI | CameraX | Product DB | — | — |
| Fridge Scanning | Pipeline | Camera UI | CameraX | — | Detection model | Model serving |
| Receipt Scanning | OCR integration | Results UI | CameraX | — | Text enhancement | — |

### Communication Cadence
- **Daily:** Mobile standup (Tech Lead + UI/UX + Android Dev)
- **Daily:** Backend standup (Tech Lead + Backend + DevOps)
- **Weekly:** ML sync (Tech Lead + ML Engineer + Android Dev)
- **Weekly:** Full team sync (30 min)
- **Bi-weekly:** Sprint planning and retrospective

---

## 4. Feature Breakdown

### 4.1 Household Sharing (Months 10-11)

#### Data Model

```kotlin
data class Household(
    val id: String,
    val name: String,
    val createdAt: Instant,
    val ownerId: String,  // Creator with admin rights
)

data class HouseholdMember(
    val id: String,
    val householdId: String,
    val userId: String,
    val role: MemberRole,
    val joinedAt: Instant,
    val displayName: String,
)

enum class MemberRole {
    OWNER,      // Full control, can delete household
    ADMIN,      // Can manage members, can't delete household
    MEMBER,     // Standard access
    KID,        // Requests require approval
}

data class HouseholdInvite(
    val id: String,
    val householdId: String,
    val invitedEmail: String,
    val invitedByUserId: String,
    val expiresAt: Instant,
    val status: InviteStatus,
)
```

#### Real-Time Sync
**Requirements:**
- Changes visible within 2 seconds across devices
- Handle offline members gracefully
- Show "typing" indicator when someone is adding
- Activity attribution (who added/checked what)

**Implementation Options:**
| Option | Pros | Cons |
|--------|------|------|
| Firebase Realtime DB | Fast, built-in presence | Vendor lock-in |
| WebSocket (custom) | Full control, portable | More development time |
| Long-polling | Simple, reliable | Higher latency |

**Recommendation:** Firebase Realtime Database for MVP, evaluate custom WebSocket if scaling issues.

#### Member Management
- Invite via email or shareable link
- Accept/decline invite flow
- Remove members (owner/admin only)
- Leave household option
- Transfer ownership

#### Kid Account System
**Flow:**
1. Kid adds "Candy" to list
2. Item appears with "Pending Approval" badge
3. Parent gets notification
4. Parent approves → item added to list
5. Parent declines → item removed, kid notified

**Implementation:**
```kotlin
data class PendingRequest(
    val id: String,
    val itemName: String,
    val requestedBy: String,  // Kid user ID
    val requestedAt: Instant,
    val status: RequestStatus,
    val reviewedBy: String?,
    val reviewedAt: Instant?,
)
```

#### Household UI
- Household creation flow
- Member list with roles
- Invite member screen
- Pending requests view (for parents)
- Activity feed showing recent actions
- Settings: notification preferences, leave/delete

---

### 4.2 Barcode Scanning (Month 12)

#### Technical Stack
- **Camera:** CameraX (Jetpack)
- **Barcode Detection:** ML Kit Barcode Scanning
- **Product Database:** Open Food Facts API (free, community-driven)

#### Implementation

```kotlin
class BarcodeScannerManager @Inject constructor(
    private val productRepository: ProductRepository,
) {
    private val barcodeScanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E,
            )
            .build()
    )

    suspend fun processImage(imageProxy: ImageProxy): ScanResult {
        val inputImage = InputImage.fromMediaImage(
            imageProxy.image!!,
            imageProxy.imageInfo.rotationDegrees
        )

        val barcodes = barcodeScanner.process(inputImage).await()

        return barcodes.firstOrNull()?.let { barcode ->
            barcode.rawValue?.let { code ->
                lookupProduct(code)
            }
        } ?: ScanResult.NotFound
    }

    private suspend fun lookupProduct(barcode: String): ScanResult {
        return productRepository.findByBarcode(barcode)
            ?.let { ScanResult.Found(it) }
            ?: ScanResult.NotInDatabase(barcode)
    }
}
```

#### Product Database Strategy
1. **Primary:** Open Food Facts API (crowd-sourced, free)
2. **Fallback:** User can manually enter name
3. **Learning:** Store successful lookups locally

**API Integration:**
```kotlin
interface OpenFoodFactsApi {
    @GET("api/v0/product/{barcode}.json")
    suspend fun getProduct(
        @Path("barcode") barcode: String
    ): OpenFoodFactsResponse
}
```

#### Barcode UI
- Full-screen camera preview
- Viewfinder overlay with scan zone
- Auto-focus on scan zone
- Haptic feedback on successful scan
- Product preview card (name, image if available)
- "Add to list" button
- "Item not found" fallback to manual entry
- Flash toggle for low-light

---

### 4.3 Fridge Scanning (Months 13-14) - Premium

#### Technical Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Camera        │───►│   TFLite Model   │───►│   Suggestion    │
│   Capture       │    │   (On-Device)    │    │   Engine        │
│                 │    │                  │    │                 │
│   CameraX       │    │   Object         │    │   "Low/Missing" │
│   ImageProxy    │    │   Detection      │    │   Detection     │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                      │                       │
         │                      ▼                       ▼
         │              ┌──────────────────┐    ┌─────────────────┐
         │              │   Item           │    │   Suggestions   │
         │              │   Classification │    │   UI            │
         │              │                  │    │                 │
         │              │   Confidence     │    │   "You may be   │
         │              │   Scoring        │    │    low on..."   │
         └─────────────►└──────────────────┘    └─────────────────┘
                               │
                               ▼
                        [ALL ON-DEVICE]
                        Photos never uploaded
```

#### ML Model Requirements

| Specification | Target |
|---------------|--------|
| Model size | < 5 MB |
| Inference time | < 500ms on mid-range device |
| Detection classes | 50+ common fridge items |
| Minimum confidence | 0.7 for suggestions |
| Input resolution | 640x480 minimum |

#### Detection Classes (Initial)
```
Dairy: milk, eggs, butter, yogurt, cheese, cream
Produce: lettuce, tomatoes, carrots, onions, apples, oranges
Proteins: chicken, beef, fish, tofu
Beverages: juice, soda, water bottles, beer
Condiments: ketchup, mustard, mayo, salad dressing
Leftovers: containers (generic detection)
```

#### Inventory Assessment Algorithm
```kotlin
class FridgeAnalyzer @Inject constructor(
    private val tfliteModel: FridgeDetectionModel,
    private val inventoryRepository: InventoryRepository,
) {
    suspend fun analyze(image: Bitmap): FridgeAnalysisResult {
        // 1. Run object detection
        val detections = tfliteModel.detect(image)

        // 2. Filter by confidence
        val confident = detections.filter { it.confidence > 0.7 }

        // 3. Compare to typical inventory
        val typical = inventoryRepository.getTypicalFridgeContents()
        val missing = typical - confident.map { it.itemClass }

        // 4. Detect "low" items (small bounding box area)
        val low = confident.filter { isLowQuantity(it) }

        return FridgeAnalysisResult(
            detected = confident,
            missing = missing,
            low = low,
        )
    }
}
```

#### Fridge Scan UI
- Camera preview with guide overlay ("Align fridge contents")
- Capture button (or auto-capture after stabilization)
- Processing indicator
- Results screen:
  - Detected items with confidence badges
  - "Running low" items highlighted
  - "Usually have but not seen" section
- One-tap "Add missing items to list"
- "Scan again" option

#### Privacy Implementation
- **Critical:** Photos processed 100% on-device
- No photo upload ever (unless explicit share)
- No photo storage (processed, then discarded)
- Model runs in isolated process
- Clear privacy messaging in UI

---

### 4.4 Receipt Scanning (Month 15) - Premium

#### Technical Stack
- **OCR:** ML Kit Text Recognition
- **Enhancement:** Custom model for receipt-specific text
- **Matching:** Fuzzy string matching to current list

#### Receipt Processing Pipeline

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Capture   │───►│   OCR       │───►│   Parse     │───►│   Match     │
│   Photo     │    │   Extract   │    │   Lines     │    │   to List   │
│             │    │             │    │             │    │             │
│   CameraX   │    │   ML Kit    │    │   Extract   │    │   Fuzzy     │
│             │    │   Text      │    │   Items &   │    │   Match     │
│             │    │             │    │   Prices    │    │             │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

#### Receipt Parser
```kotlin
class ReceiptParser @Inject constructor() {
    fun parse(ocrText: String): ReceiptData {
        val lines = ocrText.lines()

        // 1. Identify store (header area)
        val store = extractStoreName(lines.take(5))

        // 2. Extract line items
        val items = lines
            .filter { it.matchesItemPattern() }
            .mapNotNull { parseLineItem(it) }

        // 3. Extract total
        val total = extractTotal(lines)

        return ReceiptData(store, items, total)
    }

    private fun parseLineItem(line: String): ReceiptLineItem? {
        // Pattern: "ITEM NAME          $X.XX"
        val regex = Regex("""(.+?)\s+\$?(\d+\.\d{2})$""")
        val match = regex.find(line) ?: return null

        return ReceiptLineItem(
            name = match.groupValues[1].trim(),
            price = match.groupValues[2].toDoubleOrNull()
        )
    }
}
```

#### List Matching
```kotlin
class ReceiptMatcher @Inject constructor(
    private val currentListRepository: CurrentListRepository,
) {
    suspend fun matchToList(receiptItems: List<ReceiptLineItem>): MatchResult {
        val currentList = currentListRepository.getCurrentItems()

        val matches = receiptItems.mapNotNull { receiptItem ->
            currentList
                .maxByOrNull { fuzzyScore(it.name, receiptItem.name) }
                ?.takeIf { fuzzyScore(it.name, receiptItem.name) > 0.7 }
                ?.let { Match(receiptItem, it) }
        }

        return MatchResult(
            matched = matches,
            unmatched = receiptItems - matches.map { it.receiptItem }.toSet()
        )
    }
}
```

#### Receipt UI
- Camera preview optimized for document capture
- Edge detection guide
- Auto-capture when receipt detected
- Processing indicator with progress
- Results screen:
  - Matched items (auto-checked)
  - Unmatched items (option to add to history)
  - Total and price display
- "Confirm check-off" button
- Price history building (silent, for future analytics)

---

## 5. Monthly Roadmap

### Month 10: Household Foundation

**Week 1-2:**
- ML Engineer & Android Dev onboarding
- Household data model design
- Real-time sync architecture
- Begin CameraX exploration

**Week 3-4:**
- Household API endpoints
- Member management backend
- Household creation UI
- Invite flow implementation

**Team Focus:**
| Role | Focus |
|------|-------|
| Tech Lead | Architecture, real-time decisions |
| UI/UX | Household flows, member UI |
| Android Dev | CameraX setup, permissions |
| Backend (2) | Household APIs, real-time |
| ML Engineer | Model research, training data |
| DevOps | Real-time infrastructure |

---

### Month 11: Household Completion

**Week 1-2:**
- Real-time sync implementation
- Activity attribution
- Kid account approval flow
- Household settings

**Week 3-4:**
- Notification system
- Offline handling
- Household beta testing
- Performance optimization

**Key Deliverable:** Household sharing fully operational

---

### Month 12: Barcode Scanner

**Week 1-2:**
- ML Kit barcode integration
- CameraX viewfinder
- Open Food Facts API integration
- Product lookup caching

**Week 3-4:**
- Barcode UI polish
- Error handling
- Low-light optimization
- Manual fallback flow

**Team Focus:**
| Role | Focus |
|------|-------|
| Tech Lead | ML Kit integration |
| UI/UX | Camera UI, product cards |
| Android Dev | CameraX, viewfinder |
| Backend (2) | Product database proxy |
| ML Engineer | Fridge model training |
| DevOps | Product DB caching |

**Key Deliverable:** Barcode scanning live for all users

---

### Month 13: Fridge Scan Model

**Week 1-2:**
- TFLite model training (ML Engineer lead)
- Training data preparation
- Initial model evaluation
- Android TFLite integration

**Week 3-4:**
- Model optimization (size, speed)
- On-device inference testing
- Camera UI for fridge scan
- Processing pipeline

**Team Focus:**
| Role | Focus |
|------|-------|
| Tech Lead | TFLite integration, pipeline |
| UI/UX | Fridge scan UI, results |
| Android Dev | Camera capture, model loading |
| ML Engineer | Model training, optimization |

---

### Month 14: Fridge Scan Feature

**Week 1-2:**
- Inventory assessment algorithm
- "Low/missing" detection
- Results UI implementation
- Suggestion generation

**Week 3-4:**
- Privacy verification
- Performance optimization
- Beta testing
- Accuracy refinement

**Key Deliverable:** Fridge scanning operational (Premium)

---

### Month 15: Receipt Scanner

**Week 1-2:**
- ML Kit text recognition
- Receipt parser development
- List matching algorithm
- Price extraction

**Week 3-4:**
- Receipt UI implementation
- Auto-check flow
- Price history storage
- Phase 3 completion review

**Team Focus:**
| Role | Focus |
|------|-------|
| Tech Lead | Parser, matching algorithm |
| UI/UX | Receipt UI, confirmation flow |
| Android Dev | Document capture optimization |
| ML Engineer | OCR enhancement |
| Backend (2) | Price history API |

**Key Deliverable:** Receipt scanning operational (Premium)

---

## 6. Technical Decisions

### Real-Time Sync for Households

**Decision:** Firebase Realtime Database for MVP

**Rationale:**
- Built-in presence detection
- Low-latency updates (< 100ms)
- Handles offline/online transitions
- Reduces development time by 4+ weeks

**Trade-offs:**
- Vendor lock-in concern
- Plan migration path to custom WebSocket if needed

### Object Detection Framework

**Decision:** TensorFlow Lite with custom-trained model

**Rationale:**
- Full control over model architecture
- On-device only (privacy)
- Can optimize specifically for fridge items
- Industry standard for mobile ML

**Alternative Considered:** ML Kit Object Detection
- Rejected: Limited to generic categories, can't customize for groceries

### Product Database

**Decision:** Open Food Facts as primary, with local caching

**Rationale:**
- Free and open source
- Large database (2M+ products)
- Community-maintained
- No API key required for basic usage

**Fallback Strategy:**
1. Check local cache first
2. Query Open Food Facts
3. Manual entry as final fallback

### CameraX Configuration

```kotlin
object CameraConfig {
    // For barcode scanning
    val barcodeConfig = ImageAnalysis.Builder()
        .setTargetResolution(Size(1280, 720))
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()

    // For fridge scanning
    val fridgeConfig = ImageCapture.Builder()
        .setTargetResolution(Size(1920, 1080))
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
        .build()

    // For receipt scanning
    val receiptConfig = ImageCapture.Builder()
        .setTargetResolution(Size(2048, 1536))  // Higher for OCR
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
        .build()
}
```

---

## 7. ML Model Specifications

### Fridge Detection Model

#### Architecture
**Base:** MobileNetV3-Small with SSD detection head

**Why MobileNetV3-Small:**
- Optimized for mobile (low latency)
- Small model size (< 3MB base)
- Good accuracy/speed trade-off

#### Training Data
**Sources:**
- Open Images Dataset (subset: food, beverages)
- Custom fridge photo collection (500+ images)
- Synthetic data augmentation

**Augmentation:**
- Rotation (±15°)
- Brightness variation
- Partial occlusion simulation
- Different fridge lighting conditions

#### Model Specifications

| Spec | Target |
|------|--------|
| Input size | 320x320 RGB |
| Output | Bounding boxes + class + confidence |
| Classes | 50 |
| Model size (quantized) | < 5 MB |
| Inference time (Pixel 6) | < 300ms |
| Inference time (low-end) | < 600ms |
| mAP@0.5 | > 0.75 |

#### Class List (50 items)
```
Dairy (10): milk, eggs, butter, cheese, yogurt, cream,
            cottage_cheese, sour_cream, cream_cheese, milk_alternative

Produce (15): lettuce, tomato, cucumber, carrot, onion,
              apple, orange, lemon, lime, grapes,
              pepper, celery, broccoli, spinach, berries

Proteins (8): chicken, beef, pork, fish, tofu,
              deli_meat, bacon, sausage

Beverages (7): juice, soda, water_bottle, beer, wine,
               sports_drink, iced_tea

Condiments (7): ketchup, mustard, mayo, salad_dressing,
                hot_sauce, soy_sauce, bbq_sauce

Other (3): leftovers_container, eggs_carton, bottle_generic
```

#### Model Deployment
- Package with app (avoid download on first use)
- Version in filename (fridge_model_v1.tflite)
- Background update check via Remote Config
- A/B testing capability for model versions

---

## 8. Privacy & Security

### Core Privacy Principles

| Principle | Implementation |
|-----------|----------------|
| On-device processing | All camera ML runs locally |
| No photo upload | Photos processed in memory, never saved |
| User consent | Clear permissions with explanations |
| Data minimization | Only store detection results, not images |

### Fridge Scan Privacy

```kotlin
class FridgeScanProcessor @Inject constructor(
    private val model: FridgeDetectionModel,
) {
    suspend fun processImage(bitmap: Bitmap): FridgeResults {
        // Process entirely on device
        val results = model.detect(bitmap)

        // Clear bitmap from memory immediately
        bitmap.recycle()

        // Only detection results are retained
        // No image data ever leaves device
        return results
    }
}
```

### Household Data Privacy

| Data Type | Encryption | Visibility |
|-----------|------------|------------|
| List items | E2E encrypted | Household members only |
| Member info | E2E encrypted | Household members only |
| Activity log | E2E encrypted | Household members only |
| Invites | Server-side encrypted | Inviter + invitee only |

### Permission Handling

```kotlin
val requiredPermissions = listOf(
    Manifest.permission.CAMERA,  // For scanning features
)

// Clear, honest permission rationale
val cameraRationale = """
Restock needs camera access for:
• Scanning barcodes to quickly add products
• Scanning your fridge to suggest items you may need
• Scanning receipts to check off purchased items

All photos are processed on your device and never uploaded.
""".trimIndent()
```

---

## 9. Quality Assurance

### Testing Requirements

| Feature | Unit Test | Integration | UI Test | Manual |
|---------|:---------:|:-----------:|:-------:|:------:|
| Household | 80% | Sync flow | Member management | Multi-device |
| Barcode | 80% | ML Kit | Scan flow | Various products |
| Fridge Scan | 80% | TFLite | Full flow | Real fridges |
| Receipt | 80% | OCR + Match | Scan flow | Various receipts |

### ML Model Testing

**Fridge Model Validation:**
- Test set: 500 images (not in training)
- Per-class precision/recall metrics
- Edge cases: empty fridge, cluttered fridge, unusual lighting
- Device matrix: low-end to flagship

**Acceptance Criteria:**
- mAP@0.5 > 0.75 on test set
- No class with recall < 0.6
- Inference time < 600ms on Pixel 4a

### Performance Benchmarks

| Feature | Metric | Target |
|---------|--------|--------|
| Barcode scan | Time to result | < 1 second |
| Fridge scan | Processing time | < 2 seconds |
| Receipt scan | Processing time | < 5 seconds |
| Household sync | Update visibility | < 2 seconds |
| Camera launch | Time to preview | < 1 second |

### Device Testing Matrix

| Device Category | Examples | Priority |
|-----------------|----------|----------|
| Flagship | Pixel 8, Samsung S24 | P0 |
| Mid-range | Pixel 6a, Samsung A54 | P0 |
| Budget | Pixel 4a, Samsung A14 | P1 |
| Low-end | Older devices, low RAM | P2 |

---

## 10. Risk Management

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Fridge model accuracy < target | Medium | High | Conservative confidence threshold, easy feedback mechanism |
| Open Food Facts gaps | Medium | Medium | Build local cache, allow manual entry, consider paid DB |
| Household sync conflicts | Medium | Medium | Clear conflict UI, last-writer-wins with attribution |
| Camera permission rejection | Low | High | Clear rationale, graceful degradation |
| ML model size too large | Low | Medium | Aggressive quantization, on-demand download option |
| Real-time sync costs | Medium | Medium | Monitor usage, rate limiting if needed |

### Fallback Strategies

**If fridge scanning accuracy < 70%:**
1. Increase confidence threshold (show fewer, more accurate results)
2. Add "Help improve" feedback button
3. Collect edge cases for model retraining
4. Consider hybrid cloud approach (with clear consent)

**If barcode database coverage < 80%:**
1. Partner with commercial database (UPC Database API)
2. Implement community contribution (user-submitted products)
3. AI-based product name inference from barcode patterns

**If household sync latency > 5 seconds:**
1. Investigate network conditions
2. Implement optimistic UI updates
3. Consider switching to custom WebSocket
4. Add sync status indicator

---

## Milestone Summary

| Milestone | Target | Success Criteria |
|-----------|--------|------------------|
| M1: Household MVP | Month 10.5 | Basic sharing works |
| M2: Household Complete | Month 11 | All features, beta tested |
| M3: Barcode Launch | Month 12 | 95% scan success |
| M4: Fridge Model | Month 13 | mAP > 0.75 |
| M5: Fridge Feature | Month 14 | 80% user satisfaction |
| M6: Receipt Launch | Month 15 | 85% match accuracy |
| M7: 500K Users | Month 15 | Total installs |
| M8: 25K Premium | Month 15 | Paying subscribers |

---

## Transition to Phase 4

### Prerequisites for Phase 4
- [ ] 500,000 total users achieved
- [ ] 25,000 premium subscribers
- [ ] Household sharing adoption > 40% of premium
- [ ] All camera features stable
- [ ] ML models performing at target accuracy

### Phase 4 Preparation
- Begin Google Assistant Action development planning
- Research Instacart/Amazon Fresh API access
- Design budget tracking data model
- Evaluate Wear OS development requirements

---

*Phase 3 Document v1.0 | January 2026*
