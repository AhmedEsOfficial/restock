# Sprint 9-10: Meal Tags & Polish
## Restock Android Application - Phase 1

**Duration:** Weeks 17-20 (4 weeks)
**Sprint Dates:** [TBD based on kickoff]
**Team:** Tech Lead/Senior Dev (1), UI/UX Engineer (1)
**Prerequisites:** Sprint 7-8 completed (suggestions feature operational)

---

## Table of Contents

1. [Sprint Overview](#1-sprint-overview)
2. [Sprint Goals](#2-sprint-goals)
3. [User Stories](#3-user-stories)
4. [Technical Tasks](#4-technical-tasks)
5. [Design Tasks](#5-design-tasks)
6. [Technical Implementation](#6-technical-implementation)
7. [UI Specifications](#7-ui-specifications)
8. [Performance Optimization](#8-performance-optimization)
9. [Accessibility Audit](#9-accessibility-audit)
10. [Testing Requirements](#10-testing-requirements)
11. [Beta Release Preparation](#11-beta-release-preparation)
12. [Acceptance Criteria](#12-acceptance-criteria)

---

## 1. Sprint Overview

This sprint adds the meal tagging feature and focuses heavily on polish, performance optimization, and accessibility. The meal tags provide lightweight categorization without requiring rigid meal planning. This sprint also prepares the app for beta testing.

### Sprint Theme
> *"Refine every detail until it delights."*

### Why This Matters
- Meal tags enable future meal-based suggestions
- Polish differentiates Restock from competitors
- Performance impacts retention
- Accessibility is required for Google Play feature eligibility
- Beta testing validates product-market fit

### Key Focus Areas
1. **Meal Tags Feature:** Lightweight, optional categorization
2. **Performance:** Optimize for 60fps, fast startup
3. **Accessibility:** Full TalkBack support, WCAG compliance
4. **Beta Release:** Internal/external testing ready

---

## 2. Sprint Goals

### Primary Goals

| Goal | Owner | Success Metric |
|------|-------|----------------|
| Meal tag data model & repository | Tech Lead | Tags persist and query correctly |
| Tag assignment UI | UI/UX | One-tap tag add/remove |
| Meals tab with grouped view | UI/UX | Items grouped by meal type |
| Tag association learning | Tech Lead | System suggests tags based on history |
| Performance optimization | Tech Lead | Cold start < 2s, 60fps scroll |
| Accessibility audit | UI/UX | TalkBack navigable, 48dp targets |
| Beta release | Both | App distributed to testers |

### Success Metrics

| Metric | Target |
|--------|--------|
| Cold start time | < 2 seconds |
| List scroll (500 items) | 60fps |
| Memory usage | < 150MB |
| Accessibility score | 100% (Accessibility Scanner) |
| Beta crash rate | < 1% |
| Beta tester feedback | Net positive |

---

## 3. User Stories

### US-9.1: Add Meal Tag to Item
**As a** user
**I want** to tag items with meal categories
**So that** I can organize my list by meals

**Acceptance Criteria:**
- [ ] Long-press item shows tag options
- [ ] Four categories: Breakfast, Lunch, Dinner, Snack
- [ ] Tap to add tag, tap again to remove
- [ ] Tag appears as colored chip on item card
- [ ] Items can have multiple tags

**Technical Notes:**
- Insert into meal_tags table
- Update frequency_count for learning

---

### US-9.2: View Meals Tab
**As a** user
**I want** to see items grouped by meal
**So that** I can plan shopping by meal type

**Acceptance Criteria:**
- [ ] "Meals" tab in bottom navigation
- [ ] Items grouped under meal headers
- [ ] Items without tags in "Untagged" section
- [ ] Tap item to check/uncheck
- [ ] Can add tags from this view

**Technical Notes:**
- Query items with meal_tags join
- Handle items with multiple tags (appear in multiple groups)

---

### US-9.3: Tag Association Learning
**As a** system
**I want** to learn which items go with which meals
**So that** I can suggest tags automatically

**Acceptance Criteria:**
- [ ] Track when items are tagged
- [ ] After 3+ tags, suggest meal for similar items
- [ ] User can accept or ignore suggestion
- [ ] Suggestions are subtle (not intrusive)

**Technical Notes:**
- Increment frequency_count in meal_tags
- Use word similarity for suggestions
- Examples: "eggs" → breakfast, "salad" → lunch

---

### US-9.4: Performance Optimization
**As a** user
**I want** the app to be fast and smooth
**So that** grocery management feels effortless

**Acceptance Criteria:**
- [ ] Cold start < 2 seconds
- [ ] Warm start < 500ms
- [ ] List scrolls at 60fps with 500 items
- [ ] No jank during animations
- [ ] Memory < 150MB typical usage

**Technical Notes:**
- Baseline Profiles
- Lazy loading optimization
- Database query optimization
- Compose recomposition reduction

---

### US-9.5: Accessibility Compliance
**As a** user with disabilities
**I want** to use the app with assistive technologies
**So that** I can manage my groceries independently

**Acceptance Criteria:**
- [ ] TalkBack navigates all screens
- [ ] Touch targets minimum 48dp
- [ ] Color contrast meets WCAG AA
- [ ] Custom actions for swipe gestures
- [ ] Content descriptions on all images/icons
- [ ] Dynamic text sizing supported

**Technical Notes:**
- Semantic Compose modifiers
- AccessibilityAction for swipes
- contentDescription on icons

---

### US-9.6: Beta Testing Release
**As a** product team
**I want** to release a beta version to testers
**So that** we can gather feedback before launch

**Acceptance Criteria:**
- [ ] Build deployed to Firebase App Distribution
- [ ] Testers can install and use app
- [ ] Crash reporting active
- [ ] Feedback mechanism available
- [ ] Beta testers recruited (20+)

**Technical Notes:**
- Firebase App Distribution setup
- Crashlytics integrated
- In-app feedback option (stretch)

---

## 4. Technical Tasks

### Tech Lead Tasks

#### Week 17: Meal Tags Data Layer

| Task ID | Task | Estimate | Priority |
|---------|------|----------|----------|
| T-9.1 | Create MealTagRepository interface | 2h | P0 |
| T-9.2 | Implement MealTagRepositoryImpl | 3h | P0 |
| T-9.3 | Create AddMealTagUseCase | 2h | P0 |
| T-9.4 | Create RemoveMealTagUseCase | 1h | P0 |
| T-9.5 | Create GetItemsByMealUseCase | 2h | P0 |
| T-9.6 | Implement tag frequency tracking | 2h | P0 |
| T-9.7 | Write meal tag unit tests | 3h | P0 |

#### Week 18: Tag Learning & Integration

| Task ID | Task | Estimate | Priority |
|---------|------|----------|----------|
| T-10.1 | Implement tag suggestion algorithm | 4h | P1 |
| T-10.2 | Create SuggestMealTagUseCase | 2h | P1 |
| T-10.3 | Integrate tags with list ViewModel | 3h | P0 |
| T-10.4 | Create MealsViewModel | 3h | P0 |
| T-10.5 | Add tags to suggestion cards (stretch) | 2h | P2 |
| T-10.6 | Integration testing | 3h | P0 |

#### Week 19: Performance Optimization

| Task ID | Task | Estimate | Priority |
|---------|------|----------|----------|
| T-11.1 | Set up Baseline Profiles | 4h | P0 |
| T-11.2 | Profile cold start, identify bottlenecks | 3h | P0 |
| T-11.3 | Optimize database queries | 3h | P0 |
| T-11.4 | Reduce unnecessary recompositions | 4h | P0 |
| T-11.5 | Implement lazy loading improvements | 2h | P0 |
| T-11.6 | Memory profiling and optimization | 3h | P0 |

#### Week 20: Beta Release & Polish

| Task ID | Task | Estimate | Priority |
|---------|------|----------|----------|
| T-12.1 | Firebase App Distribution setup | 2h | P0 |
| T-12.2 | Crashlytics configuration | 2h | P0 |
| T-12.3 | Build beta APK/AAB | 1h | P0 |
| T-12.4 | Deploy to testers | 1h | P0 |
| T-12.5 | Bug fixes from internal testing | 6h | P0 |
| T-12.6 | Code review final pass | 4h | P0 |
| T-12.7 | Documentation update | 2h | P1 |

---

## 5. Design Tasks

### UI/UX Engineer Tasks

#### Week 17: Meal Tags Design

| Task ID | Task | Estimate | Priority |
|---------|------|----------|----------|
| D-9.1 | Design meal tag chips | 3h | P0 |
| D-9.2 | Design tag selection sheet | 3h | P0 |
| D-9.3 | Design Meals tab layout | 4h | P0 |
| D-9.4 | Design meal section headers | 2h | P0 |
| D-9.5 | Create tag color system | 2h | P0 |
| D-9.6 | Figma prototypes | 3h | P0 |

#### Week 18: Meal Tags Implementation

| Task ID | Task | Estimate | Priority |
|---------|------|----------|----------|
| D-10.1 | Implement MealTagChip component | 3h | P0 |
| D-10.2 | Implement TagSelectionSheet | 4h | P0 |
| D-10.3 | Implement MealsScreen | 5h | P0 |
| D-10.4 | Implement meal section headers | 2h | P0 |
| D-10.5 | Add tags to GroceryItemCard | 2h | P0 |
| D-10.6 | Bottom navigation update | 2h | P0 |

#### Week 19: Accessibility & Polish

| Task ID | Task | Estimate | Priority |
|---------|------|----------|----------|
| D-11.1 | Accessibility Scanner audit | 3h | P0 |
| D-11.2 | Fix touch target issues | 3h | P0 |
| D-11.3 | Add content descriptions | 2h | P0 |
| D-11.4 | TalkBack testing and fixes | 4h | P0 |
| D-11.5 | Color contrast verification | 2h | P0 |
| D-11.6 | Dynamic text size testing | 2h | P0 |

#### Week 20: Final Polish & Beta

| Task ID | Task | Estimate | Priority |
|---------|------|----------|----------|
| D-12.1 | Animation polish pass | 3h | P0 |
| D-12.2 | Visual consistency audit | 3h | P0 |
| D-12.3 | Empty states refinement | 2h | P0 |
| D-12.4 | Error states design | 2h | P0 |
| D-12.5 | Beta feedback UI (stretch) | 3h | P2 |
| D-12.6 | User testing session | 4h | P0 |

---

## 6. Technical Implementation

### Meal Tag Data Model

```kotlin
// MealType.kt
enum class MealType(val displayName: String, val emoji: String) {
    BREAKFAST("Breakfast", "🌅"),
    LUNCH("Lunch", "☀️"),
    DINNER("Dinner", "🌙"),
    SNACK("Snack", "✨");

    companion object {
        fun fromString(value: String): MealType? =
            entries.find { it.name.equals(value, ignoreCase = true) }
    }
}

// MealTag.kt (domain model)
data class MealTag(
    val id: String,
    val itemId: String,
    val mealType: MealType,
    val frequencyCount: Int,
)

// ItemWithMealTags.kt
data class ItemWithMealTags(
    val item: CurrentListItem,
    val mealTags: List<MealType>,
)
```

### Repository Implementation

```kotlin
// MealTagRepository.kt
interface MealTagRepository {
    suspend fun addTag(itemId: String, mealType: MealType): Result<MealTag>
    suspend fun removeTag(itemId: String, mealType: MealType): Result<Unit>
    suspend fun getTagsForItem(itemId: String): List<MealType>
    suspend fun getItemsByMeal(mealType: MealType): List<GroceryItem>
    fun observeItemsWithTags(): Flow<List<ItemWithMealTags>>
    suspend fun suggestTag(itemName: String): MealType?
}

// MealTagRepositoryImpl.kt
class MealTagRepositoryImpl @Inject constructor(
    private val mealTagDao: MealTagDao,
    private val itemDao: ItemDao,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : MealTagRepository {

    override suspend fun addTag(itemId: String, mealType: MealType): Result<MealTag> =
        withContext(dispatcher) {
            runCatching {
                val existing = mealTagDao.getTag(itemId, mealType.name)

                if (existing != null) {
                    // Increment frequency
                    mealTagDao.incrementFrequency(itemId, mealType.name)
                    existing.copy(frequencyCount = existing.frequencyCount + 1).toDomain()
                } else {
                    // Create new tag
                    val entity = MealTagEntity(
                        id = UUID.randomUUID().toString(),
                        itemId = itemId,
                        mealType = mealType.name,
                        frequencyCount = 1,
                    )
                    mealTagDao.insert(entity)
                    entity.toDomain()
                }
            }
        }

    override suspend fun removeTag(itemId: String, mealType: MealType): Result<Unit> =
        withContext(dispatcher) {
            runCatching {
                mealTagDao.deleteTag(itemId, mealType.name)
            }
        }

    override suspend fun suggestTag(itemName: String): MealType? =
        withContext(dispatcher) {
            val normalized = itemName.normalizeItemName()

            // Check common breakfast items
            if (BREAKFAST_KEYWORDS.any { normalized.contains(it) }) {
                return@withContext MealType.BREAKFAST
            }

            // Check historical tags for similar items
            val historicalTag = mealTagDao.getMostFrequentTagForSimilarItem(normalized)
            if (historicalTag != null && historicalTag.frequencyCount >= 3) {
                return@withContext MealType.fromString(historicalTag.mealType)
            }

            null
        }

    companion object {
        private val BREAKFAST_KEYWORDS = listOf(
            "egg", "bacon", "cereal", "oatmeal", "pancake", "waffle",
            "toast", "yogurt", "juice", "coffee", "milk"
        )
    }
}
```

### Use Cases

```kotlin
// AddMealTagUseCase.kt
class AddMealTagUseCase @Inject constructor(
    private val mealTagRepository: MealTagRepository,
) {
    suspend operator fun invoke(itemId: String, mealType: MealType): Result<MealTag> =
        mealTagRepository.addTag(itemId, mealType)
}

// RemoveMealTagUseCase.kt
class RemoveMealTagUseCase @Inject constructor(
    private val mealTagRepository: MealTagRepository,
) {
    suspend operator fun invoke(itemId: String, mealType: MealType): Result<Unit> =
        mealTagRepository.removeTag(itemId, mealType)
}

// GetItemsByMealUseCase.kt
class GetItemsByMealUseCase @Inject constructor(
    private val mealTagRepository: MealTagRepository,
    private val currentListRepository: CurrentListRepository,
) {
    operator fun invoke(): Flow<Map<MealType?, List<CurrentListItem>>> =
        combine(
            currentListRepository.observeCurrentList(),
            mealTagRepository.observeItemsWithTags()
        ) { currentList, itemsWithTags ->
            val tagMap = itemsWithTags.associate { it.item.itemId to it.mealTags }

            currentList
                .filter { !it.isChecked }
                .flatMap { item ->
                    val tags = tagMap[item.itemId] ?: emptyList()
                    if (tags.isEmpty()) {
                        listOf(null to item)
                    } else {
                        tags.map { it to item }
                    }
                }
                .groupBy({ it.first }, { it.second })
        }
}
```

### Meals ViewModel

```kotlin
@HiltViewModel
class MealsViewModel @Inject constructor(
    private val getItemsByMealUseCase: GetItemsByMealUseCase,
    private val addMealTagUseCase: AddMealTagUseCase,
    private val removeMealTagUseCase: RemoveMealTagUseCase,
    private val checkItemUseCase: CheckItemUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MealsUiState())
    val uiState: StateFlow<MealsUiState> = _uiState.asStateFlow()

    init {
        observeItemsByMeal()
    }

    private fun observeItemsByMeal() {
        viewModelScope.launch {
            getItemsByMealUseCase()
                .collect { groupedItems ->
                    _uiState.update { state ->
                        state.copy(
                            breakfastItems = groupedItems[MealType.BREAKFAST] ?: emptyList(),
                            lunchItems = groupedItems[MealType.LUNCH] ?: emptyList(),
                            dinnerItems = groupedItems[MealType.DINNER] ?: emptyList(),
                            snackItems = groupedItems[MealType.SNACK] ?: emptyList(),
                            untaggedItems = groupedItems[null] ?: emptyList(),
                            isLoading = false,
                        )
                    }
                }
        }
    }

    fun onAction(action: MealsAction) {
        when (action) {
            is MealsAction.AddTag -> addTag(action.itemId, action.mealType)
            is MealsAction.RemoveTag -> removeTag(action.itemId, action.mealType)
            is MealsAction.ToggleItem -> toggleItem(action.id, action.isChecked)
        }
    }

    private fun addTag(itemId: String, mealType: MealType) {
        viewModelScope.launch {
            addMealTagUseCase(itemId, mealType)
        }
    }

    private fun removeTag(itemId: String, mealType: MealType) {
        viewModelScope.launch {
            removeMealTagUseCase(itemId, mealType)
        }
    }

    private fun toggleItem(id: String, isChecked: Boolean) {
        viewModelScope.launch {
            checkItemUseCase(id, isChecked)
        }
    }
}

data class MealsUiState(
    val breakfastItems: List<CurrentListItem> = emptyList(),
    val lunchItems: List<CurrentListItem> = emptyList(),
    val dinnerItems: List<CurrentListItem> = emptyList(),
    val snackItems: List<CurrentListItem> = emptyList(),
    val untaggedItems: List<CurrentListItem> = emptyList(),
    val isLoading: Boolean = true,
)

sealed interface MealsAction {
    data class AddTag(val itemId: String, val mealType: MealType) : MealsAction
    data class RemoveTag(val itemId: String, val mealType: MealType) : MealsAction
    data class ToggleItem(val id: String, val isChecked: Boolean) : MealsAction
}
```

---

## 7. UI Specifications

### Meal Tag Colors

| Meal | Color | Hex | Emoji |
|------|-------|-----|-------|
| Breakfast | Amber | `#F59E0B` | 🌅 |
| Lunch | Emerald | `#10B981` | ☀️ |
| Dinner | Indigo | `#6366F1` | 🌙 |
| Snack | Pink | `#EC4899` | ✨ |

### MealTagChip Component

```kotlin
@Composable
fun MealTagChip(
    mealType: MealType,
    onRemove: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val color = when (mealType) {
        MealType.BREAKFAST -> RestockColors.Breakfast
        MealType.LUNCH -> RestockColors.Lunch
        MealType.DINNER -> RestockColors.Dinner
        MealType.SNACK -> RestockColors.Snack
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = mealType.emoji,
                style = RestockTypography.labelSmall,
            )

            Spacer(Modifier.width(4.dp))

            Text(
                text = mealType.displayName,
                style = RestockTypography.labelSmall,
                color = color,
            )

            if (onRemove != null) {
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove ${mealType.displayName} tag",
                    tint = color,
                    modifier = Modifier
                        .size(14.dp)
                        .clickable { onRemove() }
                )
            }
        }
    }
}
```

### Tag Selection Sheet

```kotlin
@Composable
fun TagSelectionSheet(
    currentTags: List<MealType>,
    onTagToggle: (MealType) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = RestockColors.Card,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
        ) {
            Text(
                text = "Add Meal Tag",
                style = RestockTypography.titleLarge,
            )

            Spacer(Modifier.height(16.dp))

            MealType.entries.forEach { mealType ->
                val isSelected = mealType in currentTags
                val color = mealType.color

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onTagToggle(mealType) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) color.copy(alpha = 0.15f) else Color.Transparent,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) color else RestockColors.Muted.copy(alpha = 0.3f)
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = mealType.emoji,
                            style = RestockTypography.titleLarge,
                        )

                        Spacer(Modifier.width(12.dp))

                        Text(
                            text = mealType.displayName,
                            style = RestockTypography.bodyLarge,
                            color = if (isSelected) color else RestockColors.Primary,
                        )

                        Spacer(Modifier.weight(1f))

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = color,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
```

### Meals Screen Layout

```
┌─────────────────────────────────────────────────────────────┐
│  Meals                                                       │
│                                                              │
│  🌅 Breakfast (3)                                            │
│  ┌────────────────────────────────────────────────────────┐ │
│  │ ☐  Eggs                               [🌅 Breakfast]   │ │
│  │ ☐  Milk                               [🌅 Breakfast]   │ │
│  │ ☐  Cereal                             [🌅 Breakfast]   │ │
│  └────────────────────────────────────────────────────────┘ │
│                                                              │
│  ☀️ Lunch (2)                                                │
│  ┌────────────────────────────────────────────────────────┐ │
│  │ ☐  Bread                              [☀️ Lunch]       │ │
│  │ ☐  Deli meat                          [☀️ Lunch]       │ │
│  └────────────────────────────────────────────────────────┘ │
│                                                              │
│  🌙 Dinner (1)                                               │
│  ┌────────────────────────────────────────────────────────┐ │
│  │ ☐  Chicken breast                     [🌙 Dinner]      │ │
│  └────────────────────────────────────────────────────────┘ │
│                                                              │
│  Untagged (2)                                               │
│  ┌────────────────────────────────────────────────────────┐ │
│  │ ☐  Paper towels                                        │ │
│  │ ☐  Soap                                                │ │
│  └────────────────────────────────────────────────────────┘ │
│                                                              │
├──────────────────────────────────────────────────────────────┤
│  [List]          [Meals]          [History]                  │
└─────────────────────────────────────────────────────────────┘
```

---

## 8. Performance Optimization

### Baseline Profiles

```kotlin
// BaselineProfileGenerator.kt (in :benchmark module)
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generateBaselineProfile() {
        rule.collect(packageName = "com.restock") {
            // Cold start
            startActivityAndWait()

            // Common user journeys
            device.findObject(By.text("Add an item...")).click()
            device.pressBack()

            // Scroll list
            device.findObject(By.res("grocery_list")).scroll(Direction.DOWN, 1.0f)

            // Navigate to meals
            device.findObject(By.text("Meals")).click()
            device.wait(Until.hasObject(By.text("Breakfast")), 2000)
        }
    }
}
```

### Recomposition Optimization

```kotlin
// Use stable collections
@Immutable
data class StableGroceryList(
    val items: ImmutableList<CurrentListItem>,
)

// Use derivedStateOf for expensive computations
@Composable
fun GroceryListScreen(viewModel: ListViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val sortedItems by remember(uiState.uncheckedItems) {
        derivedStateOf {
            uiState.uncheckedItems.sortedBy { it.position }
        }
    }

    // Use key parameter for stable identity
    LazyColumn {
        items(
            items = sortedItems,
            key = { it.id }
        ) { item ->
            GroceryItemCard(
                item = item,
                modifier = Modifier.animateItem()
            )
        }
    }
}
```

### Database Query Optimization

```kotlin
// Add indices for common queries
@Entity(
    tableName = "current_list",
    indices = [
        Index(value = ["position"]),
        Index(value = ["is_checked"]),
        Index(value = ["item_id"]),
    ]
)

// Use efficient joins
@Query("""
    SELECT cl.*, i.name, i.normalized_name
    FROM current_list cl
    INNER JOIN items i ON cl.item_id = i.id
    WHERE cl.is_checked = 0
    ORDER BY cl.position ASC
""")
fun observeUncheckedItems(): Flow<List<CurrentListWithItem>>
```

### Performance Benchmarks

| Metric | Before | Target | Method |
|--------|--------|--------|--------|
| Cold start | TBD | < 2s | Baseline Profiles |
| Warm start | TBD | < 500ms | Activity optimization |
| List scroll | TBD | 60fps | LazyColumn optimization |
| Memory | TBD | < 150MB | Leak detection |

---

## 9. Accessibility Audit

### Accessibility Checklist

#### Touch Targets
- [ ] All clickable elements minimum 48dp × 48dp
- [ ] Adequate spacing between touch targets
- [ ] No overlapping touch targets

#### Screen Reader Support
- [ ] All images have contentDescription
- [ ] All buttons have descriptive labels
- [ ] Form fields have labels
- [ ] Lists are announced properly
- [ ] State changes are announced

#### Visual
- [ ] Text contrast ratio ≥ 4.5:1
- [ ] Non-text contrast ratio ≥ 3:1
- [ ] Not relying solely on color
- [ ] Support for large text (200% scaling)

#### Navigation
- [ ] Logical focus order
- [ ] Skip to main content (if applicable)
- [ ] Keyboard navigation works
- [ ] Custom actions for gestures

### Accessibility Fixes

```kotlin
// Ensure touch targets
@Composable
fun AccessibleIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp)  // Minimum touch target
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
        )
    }
}

// Custom actions for swipe gestures
@Composable
fun GroceryItemCard(
    item: CurrentListItem,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // ... existing implementation

    Box(
        modifier = modifier.semantics {
            // Add custom action for delete
            customActions = listOf(
                CustomAccessibilityAction("Delete") {
                    onDelete()
                    true
                }
            )
        }
    ) {
        // Card content
    }
}

// Announce state changes
LaunchedEffect(item.isChecked) {
    if (item.isChecked) {
        announceForAccessibility("${item.name} checked off")
    }
}
```

---

## 10. Testing Requirements

### Unit Tests

| Component | Test Cases | Coverage |
|-----------|------------|----------|
| MealTagRepository | Add, remove, query, suggest | 90% |
| AddMealTagUseCase | Success, duplicate, error | 90% |
| GetItemsByMealUseCase | Grouping, multiple tags | 90% |
| MealsViewModel | Actions, state updates | 85% |

### UI Tests

```kotlin
@Test
fun mealsScreen_showsItemsGroupedByMeal() {
    // Setup: Add items with tags
    composeTestRule.setContent {
        MealsScreen(viewModel = testViewModel)
    }

    composeTestRule.onNodeWithText("Breakfast").assertIsDisplayed()
    composeTestRule.onNodeWithText("Eggs").assertIsDisplayed()
}

@Test
fun tagSelectionSheet_togglesTag() {
    composeTestRule.setContent {
        TagSelectionSheet(
            currentTags = listOf(MealType.BREAKFAST),
            onTagToggle = { /* verify */ },
            onDismiss = {}
        )
    }

    composeTestRule.onNodeWithText("Lunch").performClick()
    // Verify onTagToggle called with LUNCH
}
```

### Accessibility Tests

```kotlin
@Test
fun allTouchTargets_meetMinimumSize() {
    composeTestRule.setContent {
        ListScreen(viewModel = testViewModel)
    }

    composeTestRule.onAllNodes(isClickable())
        .fetchSemanticsNodes()
        .forEach { node ->
            val bounds = node.boundsInRoot
            assertThat(bounds.width).isAtLeast(48.dp.toPx())
            assertThat(bounds.height).isAtLeast(48.dp.toPx())
        }
}

@Test
fun allImages_haveContentDescription() {
    composeTestRule.setContent {
        ListScreen(viewModel = testViewModel)
    }

    composeTestRule.onAllNodes(hasRole(Role.Image))
        .fetchSemanticsNodes()
        .forEach { node ->
            assertThat(node.config.getOrNull(SemanticsProperties.ContentDescription))
                .isNotNull()
        }
}
```

---

## 11. Beta Release Preparation

### Firebase App Distribution Setup

```kotlin
// build.gradle.kts (app module)
plugins {
    id("com.google.firebase.appdistribution")
}

android {
    buildTypes {
        create("beta") {
            initWith(getByName("release"))
            firebaseAppDistribution {
                artifactType = "APK"
                groups = "internal-testers"
                releaseNotes = "Beta release for internal testing"
            }
        }
    }
}
```

### Crashlytics Configuration

```kotlin
// RestockApplication.kt
@HiltAndroidApp
class RestockApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Crashlytics
        FirebaseCrashlytics.getInstance().apply {
            setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
            setCustomKey("build_type", BuildConfig.BUILD_TYPE)
        }
    }
}
```

### Beta Tester Recruitment

| Source | Target Count | Method |
|--------|--------------|--------|
| Team members | 5 | Direct invite |
| Friends & family | 10 | Email invite |
| Early signups | 10+ | Landing page |
| **Total** | **25+** | |

### Feedback Collection

- **In-app:** Shake to report (stretch goal)
- **Email:** beta-feedback@restock.app
- **Form:** Google Form link in app
- **Crashlytics:** Automatic crash reports

---

## 12. Acceptance Criteria

### Sprint Demo Checklist

#### Meal Tags
1. [ ] Long-press item → tag selection appears
2. [ ] Select "Breakfast" → tag chip appears on item
3. [ ] Select "Lunch" → both tags shown
4. [ ] Tap tag to remove
5. [ ] Navigate to Meals tab
6. [ ] Items grouped under correct headers
7. [ ] Untagged items in separate section

#### Performance
8. [ ] Cold start measured < 2 seconds
9. [ ] Scroll 500-item list at 60fps
10. [ ] Memory usage < 150MB

#### Accessibility
11. [ ] TalkBack navigates all screens
12. [ ] All touch targets ≥ 48dp
13. [ ] Accessibility Scanner shows no errors

#### Beta Release
14. [ ] Beta build deployed to Firebase
15. [ ] Testers can install from invite
16. [ ] Crashes appear in Crashlytics
17. [ ] At least 20 testers invited

### Technical Acceptance

- [ ] All unit tests pass
- [ ] No lint errors
- [ ] Baseline Profiles generated
- [ ] Code coverage maintained (>80%)

---

## Handoff to Sprint 11-12

### Completed Artifacts
- Meal tag feature complete
- Meals tab implemented
- Performance optimized
- Accessibility compliant
- Beta release deployed

### Ready for Sprint 11-12
- App feature-complete for launch
- Beta feedback collected
- Crashlytics monitoring active
- Ready for final polish and Play Store submission

### Beta Feedback to Address
- (Collect during beta period)
- (Prioritize for Sprint 11-12)

---

*Sprint 9-10 Document v1.0 | Phase 1 - Restock Android*
