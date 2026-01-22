# Task: Implement Meal Tags Data Layer

**Sprint:** 9-10 (Meal Tags & Polish)
**Owner:** Tech Lead / Senior Developer
**Estimate:** 15 hours (T-9.1 through T-9.7 combined)
**Priority:** P0
**Prerequisites:** Sprint 7-8 completed (suggestions feature operational)

---

## Sprint Context

This sprint adds the meal tagging feature and focuses heavily on polish, performance optimization, and accessibility. Meal tags provide lightweight categorization without requiring rigid meal planning.

### Sprint Theme
> *"Refine every detail until it delights."*

### Key Principles
1. **Optional feature:** Meal tags enhance, don't require
2. **Lightweight:** One-tap add/remove
3. **Learning:** Track frequency for future tag suggestions

---

## Task Description

Implement the meal tags data layer including entities, DAOs, repository, and use cases.

## Implementation

### MealType.kt (domain model)

```kotlin
package com.restock.core.domain.model

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
```

### MealTag.kt (domain model)

```kotlin
package com.restock.core.domain.model

data class MealTag(
    val id: String,
    val itemId: String,
    val mealType: MealType,
    val frequencyCount: Int,
)

data class ItemWithMealTags(
    val item: CurrentListItem,
    val mealTags: List<MealType>,
)
```

### MealTagEntity.kt

```kotlin
package com.restock.core.data.database.entity

import androidx.room.*

@Entity(
    tableName = "meal_tags",
    foreignKeys = [
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["item_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index(value = ["item_id", "meal_type"], unique = true),
        Index(value = ["meal_type"]),
    ]
)
data class MealTagEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "item_id")
    val itemId: String,
    @ColumnInfo(name = "meal_type")
    val mealType: String,
    @ColumnInfo(name = "frequency_count")
    val frequencyCount: Int = 1,
)
```

### MealTagDao.kt

```kotlin
package com.restock.core.data.database.dao

import androidx.room.*
import com.restock.core.data.database.entity.MealTagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealTagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: MealTagEntity)

    @Query("SELECT * FROM meal_tags WHERE item_id = :itemId AND meal_type = :mealType")
    suspend fun getTag(itemId: String, mealType: String): MealTagEntity?

    @Query("SELECT * FROM meal_tags WHERE item_id = :itemId")
    suspend fun getTagsForItem(itemId: String): List<MealTagEntity>

    @Query("SELECT * FROM meal_tags WHERE item_id = :itemId")
    fun observeTagsForItem(itemId: String): Flow<List<MealTagEntity>>

    @Query("SELECT DISTINCT item_id FROM meal_tags WHERE meal_type = :mealType")
    suspend fun getItemIdsForMeal(mealType: String): List<String>

    @Query("""
        UPDATE meal_tags
        SET frequency_count = frequency_count + 1
        WHERE item_id = :itemId AND meal_type = :mealType
    """)
    suspend fun incrementFrequency(itemId: String, mealType: String)

    @Query("DELETE FROM meal_tags WHERE item_id = :itemId AND meal_type = :mealType")
    suspend fun deleteTag(itemId: String, mealType: String)

    @Query("SELECT * FROM meal_tags ORDER BY frequency_count DESC")
    fun observeAllTags(): Flow<List<MealTagEntity>>

    @Query("""
        SELECT * FROM meal_tags
        WHERE item_id IN (
            SELECT id FROM items WHERE normalized_name LIKE '%' || :normalizedName || '%'
        )
        GROUP BY meal_type
        ORDER BY frequency_count DESC
        LIMIT 1
    """)
    suspend fun getMostFrequentTagForSimilarItem(normalizedName: String): MealTagEntity?
}
```

### MealTagRepository.kt (interface)

```kotlin
package com.restock.core.domain.repository

import com.restock.core.domain.model.*
import kotlinx.coroutines.flow.Flow

interface MealTagRepository {
    suspend fun addTag(itemId: String, mealType: MealType): Result<MealTag>
    suspend fun removeTag(itemId: String, mealType: MealType): Result<Unit>
    suspend fun getTagsForItem(itemId: String): List<MealType>
    suspend fun getItemsByMeal(mealType: MealType): List<GroceryItem>
    fun observeItemsWithTags(): Flow<List<ItemWithMealTags>>
    suspend fun suggestTag(itemName: String): MealType?
}
```

### MealTagRepositoryImpl.kt

```kotlin
package com.restock.core.data.repository

import com.restock.core.data.database.dao.MealTagDao
import com.restock.core.data.database.dao.ItemDao
import com.restock.core.data.database.dao.CurrentListDao
import com.restock.core.data.database.entity.MealTagEntity
import com.restock.core.domain.model.*
import com.restock.core.domain.repository.MealTagRepository
import com.restock.core.domain.util.normalizeItemName
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class MealTagRepositoryImpl @Inject constructor(
    private val mealTagDao: MealTagDao,
    private val itemDao: ItemDao,
    private val currentListDao: CurrentListDao,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : MealTagRepository {

    override suspend fun addTag(itemId: String, mealType: MealType): Result<MealTag> =
        withContext(dispatcher) {
            runCatching {
                val existing = mealTagDao.getTag(itemId, mealType.name)

                if (existing != null) {
                    // Increment frequency
                    mealTagDao.incrementFrequency(itemId, mealType.name)
                    MealTag(
                        id = existing.id,
                        itemId = existing.itemId,
                        mealType = mealType,
                        frequencyCount = existing.frequencyCount + 1,
                    )
                } else {
                    // Create new tag
                    val entity = MealTagEntity(
                        id = UUID.randomUUID().toString(),
                        itemId = itemId,
                        mealType = mealType.name,
                        frequencyCount = 1,
                    )
                    mealTagDao.insert(entity)
                    MealTag(
                        id = entity.id,
                        itemId = entity.itemId,
                        mealType = mealType,
                        frequencyCount = 1,
                    )
                }
            }
        }

    override suspend fun removeTag(itemId: String, mealType: MealType): Result<Unit> =
        withContext(dispatcher) {
            runCatching {
                mealTagDao.deleteTag(itemId, mealType.name)
            }
        }

    override suspend fun getTagsForItem(itemId: String): List<MealType> =
        withContext(dispatcher) {
            mealTagDao.getTagsForItem(itemId)
                .mapNotNull { MealType.fromString(it.mealType) }
        }

    override suspend fun getItemsByMeal(mealType: MealType): List<GroceryItem> =
        withContext(dispatcher) {
            val itemIds = mealTagDao.getItemIdsForMeal(mealType.name)
            itemIds.mapNotNull { itemDao.getById(it)?.toDomain() }
        }

    override fun observeItemsWithTags(): Flow<List<ItemWithMealTags>> =
        combine(
            currentListDao.observeCurrentList(),
            mealTagDao.observeAllTags(),
        ) { currentList, allTags ->
            val tagsByItem = allTags.groupBy { it.itemId }

            currentList.map { listItem ->
                val tags = tagsByItem[listItem.itemId]
                    ?.mapNotNull { MealType.fromString(it.mealType) }
                    ?: emptyList()

                ItemWithMealTags(
                    item = listItem.toDomain(),
                    mealTags = tags,
                )
            }
        }.flowOn(dispatcher)

    override suspend fun suggestTag(itemName: String): MealType? =
        withContext(dispatcher) {
            val normalized = itemName.normalizeItemName()

            // Check common breakfast items
            if (BREAKFAST_KEYWORDS.any { normalized.contains(it) }) {
                return@withContext MealType.BREAKFAST
            }

            // Check common lunch items
            if (LUNCH_KEYWORDS.any { normalized.contains(it) }) {
                return@withContext MealType.LUNCH
            }

            // Check common dinner items
            if (DINNER_KEYWORDS.any { normalized.contains(it) }) {
                return@withContext MealType.DINNER
            }

            // Check common snack items
            if (SNACK_KEYWORDS.any { normalized.contains(it) }) {
                return@withContext MealType.SNACK
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
            "toast", "yogurt", "juice", "coffee", "milk", "bagel",
            "muffin", "croissant", "granola"
        )

        private val LUNCH_KEYWORDS = listOf(
            "sandwich", "salad", "soup", "wrap", "deli", "bread",
            "cheese", "turkey", "ham", "hummus", "pita"
        )

        private val DINNER_KEYWORDS = listOf(
            "chicken", "beef", "steak", "fish", "salmon", "pasta",
            "rice", "potato", "vegetable", "broccoli", "carrot"
        )

        private val SNACK_KEYWORDS = listOf(
            "chip", "cracker", "cookie", "candy", "nut", "pretzel",
            "popcorn", "fruit", "apple", "banana", "grape"
        )
    }
}
```

### Use Cases

```kotlin
// AddMealTagUseCase.kt
package com.restock.feature.meals.domain

import com.restock.core.domain.model.MealTag
import com.restock.core.domain.model.MealType
import com.restock.core.domain.repository.MealTagRepository
import javax.inject.Inject

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

// SuggestMealTagUseCase.kt
class SuggestMealTagUseCase @Inject constructor(
    private val mealTagRepository: MealTagRepository,
) {
    suspend operator fun invoke(itemName: String): MealType? =
        mealTagRepository.suggestTag(itemName)
}
```

## Acceptance Criteria

- [ ] MealTagRepository interface created
- [ ] MealTagRepositoryImpl handles all operations
- [ ] Tag frequency tracking works
- [ ] Tag suggestion based on keywords works
- [ ] Tag suggestion based on history works
- [ ] Unit tests for repository (90%+ coverage)

## Testing

```kotlin
class MealTagRepositoryTest {

    @Test
    fun `addTag creates new tag when not exists`() = runTest {
        val repository = createRepository()

        val result = repository.addTag("milk-id", MealType.BREAKFAST)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()?.mealType).isEqualTo(MealType.BREAKFAST)
    }

    @Test
    fun `addTag increments frequency when tag exists`() = runTest {
        val repository = createRepository()

        repository.addTag("milk-id", MealType.BREAKFAST)
        val result = repository.addTag("milk-id", MealType.BREAKFAST)

        assertThat(result.getOrNull()?.frequencyCount).isEqualTo(2)
    }

    @Test
    fun `suggestTag returns BREAKFAST for eggs`() = runTest {
        val repository = createRepository()

        val suggestion = repository.suggestTag("eggs")

        assertThat(suggestion).isEqualTo(MealType.BREAKFAST)
    }
}
```

## Definition of Done

- [ ] All code compiles without errors
- [ ] Repository handles CRUD operations
- [ ] Tag suggestions work correctly
- [ ] Unit tests pass (90%+ coverage)
- [ ] Integration with UI ready

## Notes

- Keep keyword lists configurable for future expansion
- Frequency count enables learning over time
- Historical tag suggestions require 3+ occurrences
