# Sprint 12: Recipe Integration - UI & Storage
## Phase 2, Month 9, Weeks 3-4

**Sprint Duration:** 2 weeks
**Sprint Number:** 12 of 12 (Phase 2)
**Team:** Tech Lead, UI/UX Engineer, Backend Developers (2), DevOps Engineer

---

## 1. Sprint Overview

This sprint completes the recipe integration feature by building the recipe import UI, implementing recipe storage, and enabling one-tap recipe shopping. This is the final sprint of Phase 2, concluding with the Recipe Import milestone and Phase 2 completion review.

### Sprint Goal
> *"Deliver a complete recipe import experience with intuitive UI, saved recipes library, and seamless shopping list integration."*

### Key Outcomes
- Recipe import UI complete and polished
- Recipe storage system operational
- One-tap "Add missing items to list"
- Saved recipes library (Premium)
- Phase 2 completion and review
- Ready for Phase 3 planning

### Prerequisites from Sprint 11
- Recipe URL parsing service operational
- Ingredient normalization working
- Already-have detection implemented
- API endpoint functional

---

## 2. Sprint Backlog

### 2.1 User Stories

#### US-12.1: Recipe Import UI
**As a** user with a recipe URL
**I want** to paste/share it into Restock
**So that** I can quickly add all needed ingredients

**Acceptance Criteria:**
- [ ] URL input field with paste detection
- [ ] Share intent receiver for recipe URLs
- [ ] Loading state while parsing
- [ ] Recipe preview with image, name, servings
- [ ] Ingredient list with already-have badges
- [ ] Selectable ingredients (unselect items don't want)
- [ ] "Add X items to list" button
- [ ] Error states for failed parsing

**Story Points:** 8
**Assignee:** UI/UX Engineer

**Recipe Import UI:**
```kotlin
@Composable
fun RecipeImportScreen(
    viewModel: RecipeImportViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar
        TopAppBar(
            title = { Text("Import Recipe") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.Close, "Close")
                }
            }
        )

        when (val state = uiState) {
            is RecipeImportState.Input -> RecipeUrlInput(
                url = state.url,
                onUrlChange = viewModel::updateUrl,
                onImport = viewModel::importRecipe,
                isLoading = state.isLoading,
                error = state.error
            )

            is RecipeImportState.Preview -> RecipePreview(
                recipe = state.recipe,
                selectedIngredients = state.selectedIngredients,
                onToggleIngredient = viewModel::toggleIngredient,
                onSelectAll = viewModel::selectAllMissing,
                onAddToList = viewModel::addToList,
                onSaveRecipe = viewModel::saveRecipe
            )

            is RecipeImportState.Success -> RecipeSuccess(
                itemsAdded = state.itemsAdded,
                recipeSaved = state.recipeSaved,
                onDone = onNavigateBack,
                onViewList = { /* Navigate to list */ }
            )
        }
    }
}

@Composable
fun RecipeUrlInput(
    url: String,
    onUrlChange: (String) -> Unit,
    onImport: () -> Unit,
    isLoading: Boolean,
    error: String?,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.MenuBook,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Import Recipe",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Paste a recipe URL to extract ingredients",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = url,
            onValueChange = onUrlChange,
            label = { Text("Recipe URL") },
            placeholder = { Text("https://allrecipes.com/recipe/...") },
            singleLine = true,
            trailingIcon = {
                if (url.isNotEmpty()) {
                    IconButton(onClick = { onUrlChange("") }) {
                        Icon(Icons.Default.Clear, "Clear")
                    }
                }
            },
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onImport,
            enabled = url.isNotBlank() && !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Import Recipe")
            }
        }

        // Supported sites hint
        Spacer(Modifier.height(24.dp))

        Text(
            text = "Works with AllRecipes, Food Network, NYT Cooking, and more",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun RecipePreview(
    recipe: ParsedRecipe,
    selectedIngredients: Set<Int>,
    onToggleIngredient: (Int) -> Unit,
    onSelectAll: () -> Unit,
    onAddToList: () -> Unit,
    onSaveRecipe: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Recipe header
        RecipeHeader(recipe = recipe)

        // Ingredients section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ingredients (${recipe.ingredients.size})",
                style = MaterialTheme.typography.titleMedium
            )

            TextButton(onClick = onSelectAll) {
                Text("Select Missing")
            }
        }

        // Ingredient list
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            itemsIndexed(recipe.ingredients) { index, ingredient ->
                RecipeIngredientRow(
                    ingredient = ingredient,
                    isSelected = index in selectedIngredients,
                    onToggle = { onToggleIngredient(index) }
                )
            }
        }

        // Action buttons
        Surface(
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                val missingCount = selectedIngredients.size

                Button(
                    onClick = onAddToList,
                    enabled = missingCount > 0,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add $missingCount Items to List")
                }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onSaveRecipe,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Bookmark, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Save Recipe")
                }
            }
        }
    }
}

@Composable
fun RecipeIngredientRow(
    ingredient: RecipeIngredient,
    isSelected: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 12.dp)
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() },
            enabled = !ingredient.alreadyHave
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = ingredient.normalizedName,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (ingredient.alreadyHave)
                    TextDecoration.LineThrough else TextDecoration.None
            )

            Text(
                text = ingredient.rawText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (ingredient.alreadyHave) {
            Badge {
                Text("Have")
            }
        }
    }
}
```

---

#### US-12.2: Share Intent Handling
**As a** user browsing recipes
**I want** to share a recipe URL to Restock
**So that** I can import it quickly

**Acceptance Criteria:**
- [ ] App receives share intents with URLs
- [ ] Filters for recipe site URLs
- [ ] Opens directly to import screen
- [ ] Pre-fills URL in input
- [ ] Handles invalid URLs gracefully

**Story Points:** 3
**Assignee:** Tech Lead

**Share Intent Handler:**
```kotlin
// In AndroidManifest.xml
<activity
    android:name=".feature.recipe.RecipeImportActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.SEND" />
        <category android:name="android.intent.category.DEFAULT" />
        <data android:mimeType="text/plain" />
    </intent-filter>
</activity>

// RecipeImportActivity.kt
class RecipeImportActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedUrl = when (intent?.action) {
            Intent.ACTION_SEND -> {
                intent.getStringExtra(Intent.EXTRA_TEXT)?.let { text ->
                    extractUrl(text)
                }
            }
            else -> null
        }

        setContent {
            RestockTheme {
                RecipeImportScreen(
                    initialUrl = sharedUrl,
                    onNavigateBack = { finish() }
                )
            }
        }
    }

    private fun extractUrl(text: String): String? {
        val urlPattern = Regex("https?://[^\\s]+")
        return urlPattern.find(text)?.value
    }
}
```

---

#### US-12.3: Recipe Storage
**As a** premium user
**I want** to save recipes for later
**So that** I can quickly re-shop for favorite meals

**Acceptance Criteria:**
- [ ] Save recipe with name, URL, ingredients
- [ ] Recipe stored locally (and synced)
- [ ] Premium feature (gated)
- [ ] View saved recipes list
- [ ] Delete saved recipes
- [ ] Quick "Shop this recipe" action

**Story Points:** 8
**Assignee:** Backend Developer 1

**Recipe Storage Model:**
```kotlin
// Domain entity
data class SavedRecipe(
    val id: String,
    val name: String,
    val sourceUrl: String,
    val imageUrl: String?,
    val servings: Int?,
    val ingredients: List<RecipeIngredient>,
    val savedAt: Instant,
    val lastUsedAt: Instant?
)

// Room entity
@Entity(tableName = "saved_recipes")
data class SavedRecipeEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "source_url") val sourceUrl: String,
    @ColumnInfo(name = "image_url") val imageUrl: String?,
    @ColumnInfo(name = "servings") val servings: Int?,
    @ColumnInfo(name = "saved_at") val savedAt: Long,
    @ColumnInfo(name = "last_used_at") val lastUsedAt: Long?
)

@Entity(
    tableName = "recipe_ingredients",
    foreignKeys = [
        ForeignKey(
            entity = SavedRecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipe_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RecipeIngredientEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "recipe_id") val recipeId: String,
    @ColumnInfo(name = "raw_text") val rawText: String,
    @ColumnInfo(name = "normalized_name") val normalizedName: String,
    @ColumnInfo(name = "quantity") val quantity: Double?,
    @ColumnInfo(name = "unit") val unit: String?,
    @ColumnInfo(name = "position") val position: Int
)

// DAO
@Dao
interface RecipeDao {
    @Query("SELECT * FROM saved_recipes ORDER BY saved_at DESC")
    fun observeSavedRecipes(): Flow<List<SavedRecipeWithIngredients>>

    @Transaction
    @Query("SELECT * FROM saved_recipes WHERE id = :id")
    suspend fun getRecipeWithIngredients(id: String): SavedRecipeWithIngredients?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: SavedRecipeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(ingredients: List<RecipeIngredientEntity>)

    @Transaction
    suspend fun saveRecipe(recipe: SavedRecipeEntity, ingredients: List<RecipeIngredientEntity>) {
        insertRecipe(recipe)
        insertIngredients(ingredients)
    }

    @Query("DELETE FROM saved_recipes WHERE id = :id")
    suspend fun deleteRecipe(id: String)

    @Query("UPDATE saved_recipes SET last_used_at = :timestamp WHERE id = :id")
    suspend fun updateLastUsed(id: String, timestamp: Long)
}

data class SavedRecipeWithIngredients(
    @Embedded val recipe: SavedRecipeEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "recipe_id"
    )
    val ingredients: List<RecipeIngredientEntity>
)
```

---

#### US-12.4: Saved Recipes Library
**As a** premium user
**I want** to browse my saved recipes
**So that** I can quickly find and shop them

**Acceptance Criteria:**
- [ ] List of saved recipes with images
- [ ] Search/filter recipes
- [ ] Tap to view recipe details
- [ ] Quick "Add to list" action
- [ ] Delete with confirmation
- [ ] Empty state for new users

**Story Points:** 8
**Assignee:** UI/UX Engineer

**Saved Recipes UI:**
```kotlin
@Composable
fun SavedRecipesScreen(
    viewModel: SavedRecipesViewModel = hiltViewModel(),
    onRecipeClick: (String) -> Unit,
    onImportNew: () -> Unit,
) {
    val recipes by viewModel.recipes.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar
        SearchBar(
            query = searchQuery,
            onQueryChange = viewModel::updateSearch,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        when {
            recipes.isEmpty() && searchQuery.isEmpty() -> {
                SavedRecipesEmptyState(onImportNew = onImportNew)
            }
            recipes.isEmpty() -> {
                NoResultsState(query = searchQuery)
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recipes, key = { it.id }) { recipe ->
                        SavedRecipeCard(
                            recipe = recipe,
                            onClick = { onRecipeClick(recipe.id) },
                            onQuickAdd = { viewModel.quickAddToList(recipe.id) },
                            onDelete = { viewModel.deleteRecipe(recipe.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SavedRecipeCard(
    recipe: SavedRecipe,
    onClick: () -> Unit,
    onQuickAdd: () -> Unit,
    onDelete: () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            // Recipe image
            AsyncImage(
                model = recipe.imageUrl,
                contentDescription = recipe.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.recipe_placeholder)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${recipe.ingredients.size} ingredients",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                recipe.lastUsedAt?.let {
                    Text(
                        text = "Last made ${it.toRelativeTime()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column {
                IconButton(onClick = onQuickAdd) {
                    Icon(Icons.Default.AddShoppingCart, "Add to list")
                }

                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, "Delete")
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Recipe?") },
            text = { Text("\"${recipe.name}\" will be removed from your saved recipes.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
```

---

#### US-12.5: One-Tap Recipe Shopping
**As a** user viewing a saved recipe
**I want** to add all missing ingredients with one tap
**So that** I can quickly prepare my shopping list

**Acceptance Criteria:**
- [ ] "Shop this recipe" button prominent
- [ ] Shows count of missing items
- [ ] Detects already-have at time of shopping
- [ ] Updates "last used" timestamp
- [ ] Success feedback
- [ ] Navigation to list after adding

**Story Points:** 5
**Assignee:** Backend Developer 1

**Quick Shop Use Case:**
```kotlin
class ShopRecipeUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val itemRepository: ItemRepository,
    private val alreadyHaveDetector: AlreadyHaveDetector,
) {
    suspend operator fun invoke(recipeId: String): Result<ShopRecipeResult> {
        // Get recipe
        val recipe = recipeRepository.getRecipe(recipeId)
            ?: return Result.failure(RecipeNotFoundException())

        // Check what user already has (current detection)
        val ingredientsWithStatus = alreadyHaveDetector.detectAlreadyHave(
            recipe.ingredients.map { NormalizedIngredient(it.normalizedName, it.quantity, it.unit, it.rawText) }
        )

        val missingIngredients = ingredientsWithStatus.filter { !it.alreadyHave }

        if (missingIngredients.isEmpty()) {
            return Result.success(ShopRecipeResult(
                addedCount = 0,
                alreadyHadAll = true
            ))
        }

        // Add missing ingredients to list
        missingIngredients.forEach { item ->
            itemRepository.addToCurrentList(
                name = item.ingredient.name,
                quantity = item.ingredient.quantity?.toInt() ?: 1,
                unit = item.ingredient.unit,
                source = ItemSource.RECIPE
            )
        }

        // Update last used
        recipeRepository.updateLastUsed(recipeId)

        return Result.success(ShopRecipeResult(
            addedCount = missingIngredients.size,
            alreadyHadAll = false
        ))
    }
}

data class ShopRecipeResult(
    val addedCount: Int,
    val alreadyHadAll: Boolean
)
```

---

#### US-12.6: Phase 2 Completion Review
**As a** team
**I want** to review Phase 2 completion
**So that** we can assess readiness for Phase 3

**Acceptance Criteria:**
- [ ] All Phase 2 milestones reviewed
- [ ] Success metrics evaluated
- [ ] Technical debt documented
- [ ] User feedback summarized
- [ ] Phase 3 prerequisites assessed
- [ ] Go/no-go decision documented

**Story Points:** 5
**Assignee:** Tech Lead

**Phase 2 Review Checklist:**
```markdown
## Phase 2 Completion Review

### Milestone Achievement
- [ ] M1: Cloud Sync - Multi-device sync operational
- [ ] M2: Premium Launch - Subscriptions active
- [ ] M3: Voice Input - Voice adds items successfully
- [ ] M4: NL Complete - "I'm making tacos" works
- [ ] M5: Recipe Import - URL paste imports ingredients
- [ ] M6: 100K Users - Total installs (check analytics)

### Success Metrics
- [ ] Total users target (100,000)
- [ ] Premium conversion rate (3-5%)
- [ ] Monthly Active Users (40,000+)
- [ ] Day 30 retention (20%+)
- [ ] NL input accuracy (>90%)
- [ ] Recipe parse success (>85%)
- [ ] Cloud sync reliability (99.9%)

### Technical Debt
- [ ] List all known issues
- [ ] Prioritize for Phase 3 or backlog
- [ ] Document workarounds

### User Feedback Summary
- [ ] App store reviews analysis
- [ ] Support tickets themes
- [ ] Feature requests

### Phase 3 Prerequisites
- [ ] 100,000 total users achieved
- [ ] Premium conversion rate > 3%
- [ ] Cloud infrastructure stable
- [ ] Backend team operational
- [ ] ML Engineer hire initiated
```

---

### 2.2 Technical Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies |
|---------|------|----------|------------|--------------|
| T-12.1 | Build RecipeImportScreen | UI/UX | 10 | - |
| T-12.2 | Build RecipeUrlInput composable | UI/UX | 4 | - |
| T-12.3 | Build RecipePreview composable | UI/UX | 8 | - |
| T-12.4 | Build RecipeIngredientRow | UI/UX | 3 | - |
| T-12.5 | Implement share intent handling | Tech Lead | 4 | - |
| T-12.6 | Create saved_recipes table migration | Backend Dev 1 | 3 | - |
| T-12.7 | Create RecipeDao | Backend Dev 1 | 4 | T-12.6 |
| T-12.8 | Create RecipeRepository | Backend Dev 1 | 6 | T-12.7 |
| T-12.9 | Create RecipeImportViewModel | Backend Dev 1 | 6 | T-12.8 |
| T-12.10 | Build SavedRecipesScreen | UI/UX | 8 | - |
| T-12.11 | Build SavedRecipeCard | UI/UX | 4 | - |
| T-12.12 | Create SavedRecipesViewModel | Backend Dev 2 | 6 | T-12.8 |
| T-12.13 | Implement ShopRecipeUseCase | Backend Dev 1 | 6 | T-12.8 |
| T-12.14 | Add recipe to navigation | Tech Lead | 2 | T-12.1 |
| T-12.15 | Premium gate recipe features | Tech Lead | 2 | - |
| T-12.16 | Add recipe to sync | Backend Dev 2 | 6 | T-12.8 |
| T-12.17 | Write recipe UI tests | UI/UX | 4 | T-12.1 |
| T-12.18 | Write repository tests | Backend Dev 1 | 4 | T-12.8 |
| T-12.19 | End-to-end recipe testing | Backend Dev 2 | 6 | All |
| T-12.20 | Phase 2 completion review | Tech Lead | 8 | All |
| T-12.21 | Document Phase 2 learnings | Tech Lead | 4 | T-12.20 |

---

## 3. Team Assignments

### Tech Lead
**Focus:** Integration and Phase 2 review
- Implement share intent handling
- Add recipe to navigation
- Premium gate recipe features
- Conduct Phase 2 completion review
- Document learnings

### UI/UX Engineer
**Focus:** Recipe UI implementation
- Build RecipeImportScreen
- Build RecipePreview
- Build SavedRecipesScreen
- Build SavedRecipeCard
- Write UI tests

### Backend Developer 1
**Focus:** Recipe storage and ViewModel
- Create database migration
- Create RecipeDao and Repository
- Create RecipeImportViewModel
- Implement ShopRecipeUseCase
- Write repository tests

### Backend Developer 2
**Focus:** Integration and testing
- Create SavedRecipesViewModel
- Add recipe to sync service
- End-to-end testing
- Bug fixes

### DevOps Engineer
**Focus:** Monitoring and metrics
- Recipe feature monitoring
- Phase 2 metrics collection
- Prepare for Phase 3 infrastructure
- Cost analysis

---

## 4. Definition of Done

### Sprint Level
- [ ] Recipe import UI complete and polished
- [ ] Share intent handling working
- [ ] Recipe storage operational
- [ ] Saved recipes library functional
- [ ] One-tap shopping working
- [ ] Recipe sync working
- [ ] All Phase 2 milestones reviewed
- [ ] Phase 2 documentation complete
- [ ] Ready for Phase 3

### Story Level
- [ ] Acceptance criteria met
- [ ] UI matches designs
- [ ] Premium gating working
- [ ] Code reviewed
- [ ] Tests passing

---

## 5. Success Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| Recipe import completion rate | >80% | Analytics |
| Recipe save rate | >40% of imports | Analytics |
| Re-shop rate | >20% of saved recipes | Analytics |
| Recipe feature satisfaction | >4.0 | User feedback |

---

## 6. Milestone Achievement

### Recipe Import Milestone Complete
At the end of this sprint, the **M5: Recipe Import** milestone from Phase 2 should be achieved:

- [ ] URL paste imports ingredients
- [ ] Ingredient normalization working
- [ ] Already-have detection functional
- [ ] Recipe storage operational
- [ ] One-tap recipe shopping working
- [ ] Premium feature properly gated

### Phase 2 Complete
All Phase 2 milestones should be achieved:

- [ ] M1: Cloud Sync ✓
- [ ] M2: Premium Launch ✓
- [ ] M3: Voice Input ✓
- [ ] M4: NL Complete ✓
- [ ] M5: Recipe Import ✓
- [ ] M6: 100K Users (verify)

---

## 7. Transition to Phase 3

### Phase 3 Prerequisites Check
- [ ] 100,000 total users achieved
- [ ] Premium conversion rate > 3%
- [ ] Cloud infrastructure stable and scalable
- [ ] Backend team fully operational
- [ ] ML Engineer hire initiated

### Phase 3 Preparation (Start During Sprint)
- [ ] Begin ML Engineer recruitment
- [ ] Evaluate TFLite model requirements for fridge scanning
- [ ] Design household sharing architecture
- [ ] Plan CameraX integration

### Handoff to Phase 3
- [ ] Phase 2 retrospective completed
- [ ] Phase 3 kickoff scheduled
- [ ] Technical debt prioritized
- [ ] Team capacity confirmed

---

## 8. Deliverables Checklist

- [ ] RecipeImportScreen implementation
- [ ] Share intent handling
- [ ] Recipe database schema
- [ ] RecipeRepository implementation
- [ ] SavedRecipesScreen implementation
- [ ] ShopRecipeUseCase implementation
- [ ] Recipe sync integration
- [ ] Phase 2 completion review document
- [ ] Phase 2 learnings document
- [ ] Phase 3 readiness assessment

---

*Sprint 12 Document v1.0 | Phase 2: Growth & Intelligence | Restock Android*
