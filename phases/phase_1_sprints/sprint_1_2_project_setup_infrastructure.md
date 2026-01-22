# Sprint 1-2: Project Setup & Core Infrastructure
## Restock Android Application - Phase 1

**Duration:** Weeks 1-4 (4 weeks)
**Sprint Dates:** [TBD based on kickoff]
**Team:** Tech Lead/Senior Dev (1), UI/UX Engineer (1)

---

## Table of Contents

1. [Sprint Overview](#1-sprint-overview)
2. [Sprint Goals](#2-sprint-goals)
3. [User Stories](#3-user-stories)
4. [Technical Tasks](#4-technical-tasks)
5. [Design Tasks](#5-design-tasks)
6. [Definition of Done](#6-definition-of-done)
7. [Daily Breakdown](#7-daily-breakdown)
8. [Dependencies & Blockers](#8-dependencies--blockers)
9. [Technical Specifications](#9-technical-specifications)
10. [Testing Requirements](#10-testing-requirements)
11. [Sprint Ceremonies](#11-sprint-ceremonies)
12. [Risks & Mitigation](#12-risks--mitigation)
13. [Acceptance Criteria](#13-acceptance-criteria)

---

## 1. Sprint Overview

This sprint establishes the technical foundation for the entire Restock application. No user-facing features will be delivered, but the architecture, build system, dependency injection, database, and design system will be fully operational. This foundation enables all subsequent sprints to focus on feature delivery.

### Sprint Theme
> *"Build the foundation right so everything else is fast."*

### Why This Matters
- Architectural decisions made now affect the entire project
- Poor foundation = technical debt that compounds
- Design system consistency starts here
- CI/CD prevents integration problems later

---

## 2. Sprint Goals

### Primary Goals

| Goal | Owner | Success Metric |
|------|-------|----------------|
| Multi-module project structure operational | Tech Lead | `./gradlew build` succeeds |
| Hilt DI configured across all modules | Tech Lead | ViewModels inject dependencies |
| Room database with all core entities | Tech Lead | All DAOs compile and test |
| CI/CD pipeline running | Tech Lead | PRs trigger automated builds |
| Design system foundation complete | UI/UX | Theme applied, 5+ components |
| Base ViewModel pattern established | Tech Lead | Template ViewModel documented |

### Stretch Goals

| Goal | Owner | Condition |
|------|-------|-----------|
| Baseline profile generation setup | Tech Lead | If primary goals complete by day 15 |
| Dark theme variant | UI/UX | If primary goals complete by day 15 |
| Accessibility scanner integration | UI/UX | If primary goals complete by day 15 |

---

## 3. User Stories

### US-1.1: Developer Project Setup
**As a** developer
**I want** a well-structured multi-module project
**So that** I can work on features independently without build conflicts

**Acceptance Criteria:**
- [ ] Project compiles with `./gradlew build`
- [ ] Module dependencies are acyclic
- [ ] Build time < 2 minutes (clean build)
- [ ] All modules have consistent Kotlin and Android versions

---

### US-1.2: Dependency Injection Setup
**As a** developer
**I want** Hilt configured for dependency injection
**So that** I can easily test components and manage dependencies

**Acceptance Criteria:**
- [ ] `@HiltAndroidApp` on Application class
- [ ] Sample ViewModel with injected repository compiles
- [ ] Test can use `@HiltAndroidTest`
- [ ] Module-specific DI modules exist

---

### US-1.3: Database Foundation
**As a** developer
**I want** Room database with all core entities defined
**So that** I can persist and query grocery data

**Acceptance Criteria:**
- [ ] All 5 core tables defined (items, current_list, purchases, patterns, meal_tags)
- [ ] All DAOs with basic CRUD operations
- [ ] Database version 1 with no pending migrations
- [ ] Unit tests for each DAO

---

### US-1.4: Design System Foundation
**As a** designer/developer
**I want** a consistent design system implemented
**So that** all UI components share the same visual language

**Acceptance Criteria:**
- [ ] Color palette defined and applied
- [ ] Typography scale implemented
- [ ] Spacing system established
- [ ] 5+ reusable components created with previews
- [ ] Theme supports light mode (dark mode stretch)

---

### US-1.5: CI/CD Pipeline
**As a** developer
**I want** automated builds and tests on every PR
**So that** code quality is maintained consistently

**Acceptance Criteria:**
- [ ] GitHub Actions workflow runs on PR
- [ ] Lint checks pass before merge
- [ ] Unit tests run and report results
- [ ] Build artifacts generated for review builds

---

## 4. Technical Tasks

### Tech Lead Tasks

#### Week 1: Project Structure & Build System

| Task ID | Task | Estimate | Priority |
|---------|------|----------|----------|
| T-1.1 | Create multi-module project structure | 4h | P0 |
| T-1.2 | Configure Gradle with version catalogs | 3h | P0 |
| T-1.3 | Set up KSP for annotation processing | 2h | P0 |
| T-1.4 | Configure module dependencies | 2h | P0 |
| T-1.5 | Set up Hilt in :app module | 3h | P0 |
| T-1.6 | Create Hilt modules for each layer | 4h | P0 |
| T-1.7 | Configure Detekt static analysis | 2h | P1 |
| T-1.8 | Configure Ktlint formatting | 1h | P1 |
| T-1.9 | Create .editorconfig for consistency | 0.5h | P1 |
| T-1.10 | Set up GitHub repository with branch protection | 1h | P0 |

#### Week 2: Database & Repository Layer

| Task ID | Task | Estimate | Priority |
|---------|------|----------|----------|
| T-2.1 | Create RestockDatabase class | 2h | P0 |
| T-2.2 | Implement ItemEntity and ItemDao | 3h | P0 |
| T-2.3 | Implement CurrentListEntity and CurrentListDao | 3h | P0 |
| T-2.4 | Implement PurchaseEntity and PurchaseDao | 2h | P0 |
| T-2.5 | Implement PatternEntity and PatternDao | 2h | P0 |
| T-2.6 | Implement MealTagEntity and MealTagDao | 2h | P0 |
| T-2.7 | Create database type converters | 1h | P0 |
| T-2.8 | Add database indices for performance | 1h | P1 |
| T-2.9 | Write DAO unit tests | 4h | P0 |
| T-2.10 | Create repository interfaces in domain layer | 3h | P0 |

#### Week 3: Architecture Patterns & CI/CD

| Task ID | Task | Estimate | Priority |
|---------|------|----------|----------|
| T-3.1 | Create BaseViewModel with UDF pattern | 3h | P0 |
| T-3.2 | Create UiState sealed interface pattern | 2h | P0 |
| T-3.3 | Create Action sealed interface pattern | 1h | P0 |
| T-3.4 | Create Event channel pattern for one-shots | 2h | P0 |
| T-3.5 | Set up GitHub Actions workflow | 3h | P0 |
| T-3.6 | Configure lint checks in CI | 1h | P0 |
| T-3.7 | Configure test execution in CI | 2h | P0 |
| T-3.8 | Set up build artifact storage | 1h | P1 |
| T-3.9 | Create PR template | 0.5h | P2 |
| T-3.10 | Document architecture decisions | 2h | P1 |

#### Week 4: Integration & Polish

| Task ID | Task | Estimate | Priority |
|---------|------|----------|----------|
| T-4.1 | Create sample feature module structure | 2h | P0 |
| T-4.2 | Implement sample ViewModel with Room | 3h | P0 |
| T-4.3 | Verify Hilt injection end-to-end | 2h | P0 |
| T-4.4 | Create DataStore setup for preferences | 2h | P1 |
| T-4.5 | Configure ProGuard rules | 2h | P1 |
| T-4.6 | Set up debug/release build variants | 1h | P0 |
| T-4.7 | Create architecture documentation | 3h | P1 |
| T-4.8 | Code review all infrastructure code | 4h | P0 |
| T-4.9 | Fix issues from code review | 4h | P0 |
| T-4.10 | Sprint retrospective preparation | 1h | P0 |

---

## 5. Design Tasks

### UI/UX Engineer Tasks

#### Week 1: Design System Specification

| Task ID | Task | Estimate | Priority |
|---------|------|----------|----------|
| D-1.1 | Finalize color palette in Figma | 3h | P0 |
| D-1.2 | Define typography scale | 2h | P0 |
| D-1.3 | Create spacing system documentation | 1h | P0 |
| D-1.4 | Design component specifications | 4h | P0 |
| D-1.5 | Create icon set requirements | 2h | P1 |
| D-1.6 | Define elevation/shadow system | 1h | P1 |
| D-1.7 | Document interaction patterns | 2h | P1 |
| D-1.8 | Create design system Figma library | 4h | P0 |

#### Week 2: Theme Implementation

| Task ID | Task | Estimate | Priority |
|---------|------|----------|----------|
| D-2.1 | Implement RestockColors object | 2h | P0 |
| D-2.2 | Implement RestockTypography | 2h | P0 |
| D-2.3 | Implement RestockSpacing object | 1h | P0 |
| D-2.4 | Implement RestockShapes | 1h | P0 |
| D-2.5 | Create RestockTheme composable | 3h | P0 |
| D-2.6 | Set up preview configurations | 2h | P0 |
| D-2.7 | Create theme documentation | 2h | P1 |
| D-2.8 | Verify theme on multiple screen sizes | 2h | P0 |

#### Week 3: Core Components

| Task ID | Task | Estimate | Priority |
|---------|------|----------|----------|
| D-3.1 | Implement RestockButton (3 variants) | 4h | P0 |
| D-3.2 | Implement RestockCard component | 3h | P0 |
| D-3.3 | Implement RestockTextField | 4h | P0 |
| D-3.4 | Implement RestockCheckbox | 2h | P0 |
| D-3.5 | Implement RestockChip (meal tags) | 2h | P0 |
| D-3.6 | Create component preview gallery | 2h | P1 |
| D-3.7 | Document component usage | 2h | P1 |

#### Week 4: Additional Components & Polish

| Task ID | Task | Estimate | Priority |
|---------|------|----------|----------|
| D-4.1 | Implement RestockSnackbar | 2h | P0 |
| D-4.2 | Implement RestockBottomSheet | 3h | P1 |
| D-4.3 | Implement RestockDialog | 2h | P1 |
| D-4.4 | Implement LoadingIndicator | 1h | P0 |
| D-4.5 | Implement EmptyState component | 2h | P0 |
| D-4.6 | Create animation specifications | 3h | P1 |
| D-4.7 | Accessibility audit of components | 3h | P0 |
| D-4.8 | Fix accessibility issues | 3h | P0 |
| D-4.9 | Final component review | 2h | P0 |

---

## 6. Definition of Done

### Sprint-Level DoD

- [ ] All P0 tasks completed
- [ ] All unit tests pass
- [ ] CI pipeline green on main branch
- [ ] Code review completed for all PRs
- [ ] No critical lint warnings
- [ ] Architecture documentation updated
- [ ] Design system documented

### Task-Level DoD

- [ ] Code compiles without errors
- [ ] Unit tests written (where applicable)
- [ ] Code reviewed by peer
- [ ] Documentation updated
- [ ] No hardcoded strings (use resources)
- [ ] Follows established patterns

---

## 7. Daily Breakdown

### Week 1

| Day | Tech Lead | UI/UX Engineer |
|-----|-----------|----------------|
| Mon | T-1.1, T-1.2: Project structure, Gradle setup | D-1.1: Color palette finalization |
| Tue | T-1.3, T-1.4: KSP, module dependencies | D-1.2, D-1.3: Typography, spacing |
| Wed | T-1.5, T-1.6: Hilt setup | D-1.4: Component specifications |
| Thu | T-1.7, T-1.8, T-1.9: Code quality tools | D-1.5, D-1.6: Icons, elevation |
| Fri | T-1.10: GitHub setup, Week 1 review | D-1.7, D-1.8: Interaction patterns, Figma library |

### Week 2

| Day | Tech Lead | UI/UX Engineer |
|-----|-----------|----------------|
| Mon | T-2.1, T-2.2: Database, ItemEntity | D-2.1, D-2.2: Colors, Typography impl |
| Tue | T-2.3: CurrentListEntity | D-2.3, D-2.4: Spacing, Shapes impl |
| Wed | T-2.4, T-2.5: Purchase, Pattern entities | D-2.5: RestockTheme composable |
| Thu | T-2.6, T-2.7: MealTag, type converters | D-2.6, D-2.7: Previews, documentation |
| Fri | T-2.8, T-2.9, T-2.10: Indices, tests, interfaces | D-2.8: Multi-screen verification |

### Week 3

| Day | Tech Lead | UI/UX Engineer |
|-----|-----------|----------------|
| Mon | T-3.1, T-3.2: BaseViewModel, UiState | D-3.1: RestockButton |
| Tue | T-3.3, T-3.4: Action, Event patterns | D-3.2: RestockCard |
| Wed | T-3.5: GitHub Actions workflow | D-3.3: RestockTextField |
| Thu | T-3.6, T-3.7: CI lint and tests | D-3.4, D-3.5: Checkbox, Chip |
| Fri | T-3.8, T-3.9, T-3.10: Artifacts, PR template, docs | D-3.6, D-3.7: Preview gallery, docs |

### Week 4

| Day | Tech Lead | UI/UX Engineer |
|-----|-----------|----------------|
| Mon | T-4.1, T-4.2: Sample feature module | D-4.1, D-4.2: Snackbar, BottomSheet |
| Tue | T-4.3, T-4.4: Hilt verification, DataStore | D-4.3, D-4.4: Dialog, LoadingIndicator |
| Wed | T-4.5, T-4.6: ProGuard, build variants | D-4.5, D-4.6: EmptyState, animations |
| Thu | T-4.7, T-4.8: Documentation, code review | D-4.7, D-4.8: Accessibility audit, fixes |
| Fri | T-4.9, T-4.10: Fix issues, retrospective | D-4.9: Final review, retrospective |

---

## 8. Dependencies & Blockers

### External Dependencies

| Dependency | Impact | Mitigation |
|------------|--------|------------|
| GitHub repository access | Blocks T-1.10 | Request access day 1 |
| Figma license | Blocks D-1.* | Ensure license active |
| Android Studio version | Blocks all | Standardize on Arctic Fox+ |
| Gradle version compatibility | Blocks build | Test Gradle 8.2+ early |

### Internal Dependencies

```
T-1.1 (Project structure)
  └── T-1.2 (Gradle) ── T-1.3 (KSP) ── T-1.5 (Hilt)
                                          └── T-2.1 (Database)
                                                └── T-2.2-T-2.6 (Entities)
                                                      └── T-2.10 (Repos)

D-1.1-D-1.3 (Specs)
  └── D-2.1-D-2.5 (Theme)
        └── D-3.1-D-3.5 (Components)
```

### Potential Blockers

| Blocker | Probability | Impact | Resolution |
|---------|-------------|--------|------------|
| KSP/Hilt compatibility issues | Medium | High | Use tested version combination |
| Room migration complexity | Low | Medium | Start with version 1, no migrations |
| Compose version conflicts | Medium | Medium | Pin all Compose BOM versions |

---

## 9. Technical Specifications

### Module Structure

```
restock/
├── app/                          # Application entry point
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   └── kotlin/
│   │       └── com/restock/
│   │           ├── RestockApplication.kt
│   │           └── di/
│   │               └── AppModule.kt
│   └── build.gradle.kts
│
├── core/
│   ├── ui/                       # Design system
│   │   ├── src/main/kotlin/
│   │   │   └── com/restock/core/ui/
│   │   │       ├── theme/
│   │   │       │   ├── Color.kt
│   │   │       │   ├── Type.kt
│   │   │       │   ├── Shape.kt
│   │   │       │   ├── Spacing.kt
│   │   │       │   └── Theme.kt
│   │   │       └── components/
│   │   │           ├── RestockButton.kt
│   │   │           ├── RestockCard.kt
│   │   │           ├── RestockTextField.kt
│   │   │           ├── RestockCheckbox.kt
│   │   │           └── RestockChip.kt
│   │   └── build.gradle.kts
│   │
│   ├── data/                     # Data layer
│   │   ├── src/main/kotlin/
│   │   │   └── com/restock/core/data/
│   │   │       ├── database/
│   │   │       │   ├── RestockDatabase.kt
│   │   │       │   ├── entity/
│   │   │       │   │   ├── ItemEntity.kt
│   │   │       │   │   ├── CurrentListEntity.kt
│   │   │       │   │   ├── PurchaseEntity.kt
│   │   │       │   │   ├── PatternEntity.kt
│   │   │       │   │   └── MealTagEntity.kt
│   │   │       │   ├── dao/
│   │   │       │   │   ├── ItemDao.kt
│   │   │       │   │   ├── CurrentListDao.kt
│   │   │       │   │   ├── PurchaseDao.kt
│   │   │       │   │   ├── PatternDao.kt
│   │   │       │   │   └── MealTagDao.kt
│   │   │       │   └── converter/
│   │   │       │       └── Converters.kt
│   │   │       ├── repository/
│   │   │       │   └── (implementations in later sprints)
│   │   │       └── di/
│   │   │           └── DataModule.kt
│   │   └── build.gradle.kts
│   │
│   ├── domain/                   # Domain layer
│   │   ├── src/main/kotlin/
│   │   │   └── com/restock/core/domain/
│   │   │       ├── model/
│   │   │       │   ├── GroceryItem.kt
│   │   │       │   ├── CurrentListItem.kt
│   │   │       │   ├── Purchase.kt
│   │   │       │   ├── Pattern.kt
│   │   │       │   └── MealTag.kt
│   │   │       └── repository/
│   │   │           ├── ItemRepository.kt
│   │   │           ├── CurrentListRepository.kt
│   │   │           ├── PurchaseRepository.kt
│   │   │           ├── PatternRepository.kt
│   │   │           └── MealTagRepository.kt
│   │   └── build.gradle.kts
│   │
│   └── ml/                       # ML layer (placeholder)
│       └── build.gradle.kts
│
├── feature/                      # Feature modules (placeholders)
│   ├── list/
│   ├── suggestions/
│   ├── meals/
│   ├── history/
│   └── settings/
│
├── gradle/
│   └── libs.versions.toml        # Version catalog
│
├── build.gradle.kts              # Root build file
└── settings.gradle.kts           # Module settings
```

### Version Catalog (libs.versions.toml)

```toml
[versions]
kotlin = "2.0.0"
agp = "8.2.0"
ksp = "2.0.0-1.0.21"

# AndroidX
core-ktx = "1.12.0"
lifecycle = "2.7.0"
activity-compose = "1.8.2"

# Compose
compose-bom = "2024.02.00"
compose-compiler = "1.5.10"

# Hilt
hilt = "2.50"
hilt-navigation-compose = "1.2.0"

# Room
room = "2.6.1"

# DataStore
datastore = "1.0.0"

# Coroutines
coroutines = "1.8.0"

# Testing
junit = "5.10.1"
mockk = "1.13.9"
turbine = "1.0.0"

# Code Quality
detekt = "1.23.4"
ktlint = "12.1.0"

[libraries]
# AndroidX Core
androidx-core-ktx = { module = "androidx.core:core-ktx", version.ref = "core-ktx" }
androidx-lifecycle-runtime = { module = "androidx.lifecycle:lifecycle-runtime-ktx", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel = { module = "androidx.lifecycle:lifecycle-viewmodel-ktx", version.ref = "lifecycle" }
androidx-activity-compose = { module = "androidx.activity:activity-compose", version.ref = "activity-compose" }

# Compose
compose-bom = { module = "androidx.compose:compose-bom", version.ref = "compose-bom" }
compose-ui = { module = "androidx.compose.ui:ui" }
compose-ui-graphics = { module = "androidx.compose.ui:ui-graphics" }
compose-ui-tooling = { module = "androidx.compose.ui:ui-tooling" }
compose-ui-tooling-preview = { module = "androidx.compose.ui:ui-tooling-preview" }
compose-material3 = { module = "androidx.compose.material3:material3" }

# Hilt
hilt-android = { module = "com.google.dagger:hilt-android", version.ref = "hilt" }
hilt-compiler = { module = "com.google.dagger:hilt-compiler", version.ref = "hilt" }
hilt-navigation-compose = { module = "androidx.hilt:hilt-navigation-compose", version.ref = "hilt-navigation-compose" }

# Room
room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
room-ktx = { module = "androidx.room:room-ktx", version.ref = "room" }
room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }

# DataStore
datastore-preferences = { module = "androidx.datastore:datastore-preferences", version.ref = "datastore" }

# Coroutines
coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }
coroutines-android = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-android", version.ref = "coroutines" }

# Testing
junit-jupiter = { module = "org.junit.jupiter:junit-jupiter", version.ref = "junit" }
mockk = { module = "io.mockk:mockk", version.ref = "mockk" }
turbine = { module = "app.cash.turbine:turbine", version.ref = "turbine" }
coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines" }
room-testing = { module = "androidx.room:room-testing", version.ref = "room" }

[bundles]
compose = ["compose-ui", "compose-ui-graphics", "compose-ui-tooling-preview", "compose-material3"]
room = ["room-runtime", "room-ktx"]
testing = ["junit-jupiter", "mockk", "turbine", "coroutines-test"]

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
detekt = { id = "io.gitlab.arturbosch.detekt", version.ref = "detekt" }
ktlint = { id = "org.jlleitschuh.gradle.ktlint", version.ref = "ktlint" }
```

### Database Schema Implementation

```kotlin
// ItemEntity.kt
@Entity(
    tableName = "items",
    indices = [Index(value = ["normalized_name"], unique = true)]
)
data class ItemEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "normalized_name")
    val normalizedName: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
)

// ItemDao.kt
@Dao
interface ItemDao {
    @Query("SELECT * FROM items WHERE is_active = 1 ORDER BY name ASC")
    fun observeActiveItems(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getById(id: String): ItemEntity?

    @Query("SELECT * FROM items WHERE normalized_name = :normalizedName LIMIT 1")
    suspend fun findByNormalizedName(normalizedName: String): ItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ItemEntity>)

    @Update
    suspend fun update(item: ItemEntity)

    @Query("UPDATE items SET is_active = 0 WHERE id = :id")
    suspend fun softDelete(id: String)

    @Query("DELETE FROM items WHERE id = :id")
    suspend fun hardDelete(id: String)
}
```

### BaseViewModel Pattern

```kotlin
// BaseViewModel.kt
abstract class BaseViewModel<State : Any, Action : Any, Event : Any>(
    initialState: State
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<State> = _uiState.asStateFlow()

    private val _events = Channel<Event>(Channel.BUFFERED)
    val events: Flow<Event> = _events.receiveAsFlow()

    protected val currentState: State
        get() = _uiState.value

    protected fun updateState(reducer: State.() -> State) {
        _uiState.update { it.reducer() }
    }

    protected fun sendEvent(event: Event) {
        viewModelScope.launch {
            _events.send(event)
        }
    }

    abstract fun onAction(action: Action)
}

// Usage example
sealed interface ListUiState {
    data object Loading : ListUiState
    data class Success(val items: List<GroceryItem>) : ListUiState
    data class Error(val message: String) : ListUiState
}

sealed interface ListAction {
    data class AddItem(val name: String) : ListAction
    data class CheckItem(val id: String) : ListAction
    data class DeleteItem(val id: String) : ListAction
}

sealed interface ListEvent {
    data object ItemAdded : ListEvent
    data class Error(val message: String) : ListEvent
}
```

---

## 10. Testing Requirements

### Unit Test Coverage

| Component | Minimum Coverage | Focus Areas |
|-----------|------------------|-------------|
| DAOs | 90% | CRUD operations, queries |
| Entities | 80% | Mapping, validation |
| BaseViewModel | 90% | State updates, events |
| Theme | N/A | Preview-based testing |
| Components | N/A | Preview + screenshot tests |

### DAO Test Example

```kotlin
@RunWith(AndroidJUnit4::class)
class ItemDaoTest {

    private lateinit var database: RestockDatabase
    private lateinit var itemDao: ItemDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RestockDatabase::class.java
        ).build()
        itemDao = database.itemDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveItem() = runTest {
        val item = ItemEntity(
            id = "test-1",
            name = "Milk",
            normalizedName = "milk",
            createdAt = System.currentTimeMillis(),
        )

        itemDao.insert(item)

        val retrieved = itemDao.getById("test-1")
        assertThat(retrieved).isEqualTo(item)
    }

    @Test
    fun findByNormalizedName() = runTest {
        val item = ItemEntity(
            id = "test-1",
            name = "Organic Milk",
            normalizedName = "milk",
            createdAt = System.currentTimeMillis(),
        )

        itemDao.insert(item)

        val found = itemDao.findByNormalizedName("milk")
        assertThat(found?.id).isEqualTo("test-1")
    }

    @Test
    fun observeActiveItemsExcludesInactive() = runTest {
        val active = ItemEntity("1", "Milk", "milk", 1000L, isActive = true)
        val inactive = ItemEntity("2", "Eggs", "eggs", 1000L, isActive = false)

        itemDao.insertAll(listOf(active, inactive))

        itemDao.observeActiveItems().test {
            val items = awaitItem()
            assertThat(items).hasSize(1)
            assertThat(items[0].id).isEqualTo("1")
        }
    }
}
```

### CI Test Configuration

```yaml
# .github/workflows/test.yml
name: Tests

on:
  pull_request:
    branches: [main, develop]

jobs:
  unit-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Run unit tests
        run: ./gradlew testDebugUnitTest

      - name: Upload test results
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: test-results
          path: '**/build/test-results/**/*.xml'
```

---

## 11. Sprint Ceremonies

### Sprint Planning (Day 1, 2 hours)

**Agenda:**
1. Review sprint goals (15 min)
2. Walk through user stories (30 min)
3. Task breakdown and estimation (45 min)
4. Capacity planning (15 min)
5. Commitment and questions (15 min)

### Daily Standups (15 min each)

**Format:**
- What I completed yesterday
- What I'm working on today
- Any blockers

**Schedule:** 9:30 AM daily

### Mid-Sprint Review (Day 10, 30 min)

**Agenda:**
1. Progress check against goals
2. Identify at-risk items
3. Adjust priorities if needed
4. Blocker escalation

### Sprint Review (Day 19, 1 hour)

**Agenda:**
1. Demo completed work (30 min)
2. Stakeholder feedback (15 min)
3. Metrics review (15 min)

### Sprint Retrospective (Day 20, 1 hour)

**Format:** Start/Stop/Continue

**Questions:**
- What went well?
- What could be improved?
- What will we commit to changing?

---

## 12. Risks & Mitigation

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Gradle/KSP version conflicts | Medium | High | Test version matrix early |
| Hilt configuration complexity | Medium | Medium | Follow official samples |
| Room schema mistakes | Low | High | Review schema before implementation |
| Design system scope creep | Medium | Medium | Strict component list, defer extras |
| CI setup delays | Low | Medium | Use proven GitHub Actions templates |

### Contingency Plans

**If Gradle issues persist:**
- Fallback to known-good version combination
- Consult Android team samples
- Consider AGP downgrade if necessary

**If behind schedule:**
- Cut stretch goals immediately
- Reduce component count (minimum 5)
- Defer DataStore to Sprint 3-4

---

## 13. Acceptance Criteria

### Sprint Completion Checklist

#### Technical Foundation
- [ ] `./gradlew build` completes successfully
- [ ] All module dependencies resolve correctly
- [ ] Hilt injection works in sample ViewModel
- [ ] Room database creates with all 5 tables
- [ ] All DAO queries return expected results
- [ ] BaseViewModel pattern is documented and working
- [ ] CI pipeline runs automatically on PR

#### Design System
- [ ] Theme applies to sample screen
- [ ] All colors match specification
- [ ] Typography renders correctly
- [ ] At least 5 components implemented
- [ ] All components have previews
- [ ] Touch targets are minimum 48dp
- [ ] Components pass accessibility scanner

#### Documentation
- [ ] Architecture decision record created
- [ ] Module structure documented
- [ ] Design system usage guide created
- [ ] PR template in use
- [ ] README updated with setup instructions

### Demo Checklist

1. Show clean build from fresh clone
2. Run unit tests, show passing
3. Show CI pipeline execution
4. Display theme on sample screen
5. Demonstrate component previews
6. Show database tables in App Inspection

---

## Handoff to Sprint 3-4

### Completed Artifacts
- Multi-module project structure
- Hilt DI configured
- Room database with all entities
- Repository interfaces defined
- Design system with components
- CI/CD pipeline operational

### Ready for Sprint 3-4
- Feature module structure template exists
- ViewModel pattern established
- Components ready for list screen
- Database ready for list operations

### Known Technical Debt
- (Track any shortcuts taken)
- (Note items deferred to later sprints)

---

*Sprint 1-2 Document v1.0 | Phase 1 - Restock Android*
