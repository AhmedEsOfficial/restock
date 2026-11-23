# Task: Implement Purchase Recording Infrastructure

**Sprint:** 5-6 (Pattern Learning Engine)
**Owner:** Tech Lead / Senior Developer
**Estimate:** 19 hours (T-5.1 through T-5.8 combined)
**Priority:** P0
**Prerequisites:** Sprint 3-4 completed (list feature functional)

---

## Sprint Context

This sprint implements the "invisible intelligence" that makes Restock unique. The pattern learning engine observes user behavior silently, recording purchase events and calculating buying patterns. No user-facing features change significantly, but the foundation for smart suggestions is built.

### Sprint Theme
> *"The intelligence that learns without asking."*

### Key Principles
1. **Passive Learning:** User takes no action to train the system
2. **On-Device:** All calculations happen locally
3. **Non-Blocking:** Never slow down the main UI
4. **Minimum Data:** Only 3 purchases needed to establish pattern

---

## Task Description

Implement the purchase recording infrastructure that captures every item check-off as a purchase event for pattern learning.

## Implementation

### PurchaseEvent.kt (domain model)

```kotlin
package com.restock.core.domain.model

import java.util.UUID
import kotlinx.datetime.Instant

/**
 * Records a purchase event when an item is checked off.
 *
 * A purchase is only recorded when:
 * 1. Item transitions from unchecked to checked
 * 2. No purchase exists within last 4 hours for same item (debounce)
 */
data class PurchaseEvent(
    val id: String = UUID.randomUUID().toString(),
    val itemId: String,
    val quantity: Int,
    val purchasedAt: Instant,
    val priceCents: Int? = null,
    val storeId: String? = null,
)
```

### Purchase.kt (domain model)

```kotlin
package com.restock.core.domain.model

import kotlinx.datetime.Instant

data class Purchase(
    val id: String,
    val itemId: String,
    val purchasedAt: Instant,
    val quantity: Int,
    val priceCents: Int? = null,
    val storeId: String? = null,
)
```

### PurchaseRepository.kt (interface)

```kotlin
package com.restock.core.domain.repository

import com.restock.core.domain.model.Purchase
import com.restock.core.domain.model.PurchaseEvent
import kotlinx.coroutines.flow.Flow

interface PurchaseRepository {
    suspend fun recordPurchase(event: PurchaseEvent): Result<Purchase>
    suspend fun getPurchasesForItem(itemId: String): List<Purchase>
    suspend fun getRecentPurchase(itemId: String, withinHours: Int = 4): Purchase?
    suspend fun getAllPurchases(): List<Purchase>
    suspend fun getLastPurchase(itemId: String): Purchase?
    suspend fun getItemIdsWithPurchases(): List<String>
    fun observePurchasesForItem(itemId: String): Flow<List<Purchase>>
    fun observeLatestPurchases(): Flow<List<Purchase>>
}
```

### PurchaseEntity.kt

```kotlin
package com.restock.core.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "purchases",
    foreignKeys = [
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["item_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index(value = ["item_id", "purchased_at"]),
        Index(value = ["purchased_at"]),
    ]
)
data class PurchaseEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "item_id")
    val itemId: String,
    @ColumnInfo(name = "purchased_at")
    val purchasedAt: Long,
    val quantity: Int,
    @ColumnInfo(name = "price_cents")
    val priceCents: Int? = null,
    @ColumnInfo(name = "store_id")
    val storeId: String? = null,
)
```

### PurchaseDao.kt

```kotlin
package com.restock.core.data.database.dao

import androidx.room.*
import com.restock.core.data.database.entity.PurchaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(purchase: PurchaseEntity)

    @Update
    suspend fun update(purchase: PurchaseEntity)

    @Query("SELECT * FROM purchases WHERE item_id = :itemId ORDER BY purchased_at DESC")
    suspend fun getPurchasesForItem(itemId: String): List<PurchaseEntity>

    @Query("SELECT * FROM purchases WHERE item_id = :itemId ORDER BY purchased_at DESC")
    fun observePurchasesForItem(itemId: String): Flow<List<PurchaseEntity>>

    @Query("""
        SELECT * FROM purchases
        WHERE item_id = :itemId AND purchased_at > :afterTimestamp
        ORDER BY purchased_at DESC
        LIMIT 1
    """)
    suspend fun getRecentPurchase(itemId: String, afterTimestamp: Long): PurchaseEntity?

    @Query("SELECT * FROM purchases WHERE item_id = :itemId ORDER BY purchased_at DESC LIMIT 1")
    suspend fun getLastPurchase(itemId: String): PurchaseEntity?

    @Query("SELECT * FROM purchases ORDER BY purchased_at DESC")
    suspend fun getAllPurchases(): List<PurchaseEntity>

    @Query("SELECT DISTINCT item_id FROM purchases")
    suspend fun getItemIdsWithPurchases(): List<String>

    @Query("""
        SELECT p.* FROM purchases p
        INNER JOIN (
            SELECT item_id, MAX(purchased_at) as max_time
            FROM purchases
            GROUP BY item_id
        ) latest ON p.item_id = latest.item_id AND p.purchased_at = latest.max_time
    """)
    fun observeLatestPurchases(): Flow<List<PurchaseEntity>>
}
```

### PurchaseRepositoryImpl.kt

```kotlin
package com.restock.core.data.repository

import com.restock.core.data.database.dao.PurchaseDao
import com.restock.core.data.database.entity.PurchaseEntity
import com.restock.core.domain.model.Purchase
import com.restock.core.domain.model.PurchaseEvent
import com.restock.core.domain.repository.PurchaseRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours

class PurchaseRepositoryImpl @Inject constructor(
    private val purchaseDao: PurchaseDao,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : PurchaseRepository {

    override suspend fun recordPurchase(event: PurchaseEvent): Result<Purchase> =
        withContext(dispatcher) {
            runCatching {
                // Check for recent purchase (debounce)
                val cutoffTime = Clock.System.now()
                    .minus(4.hours)
                    .toEpochMilliseconds()

                val recent = purchaseDao.getRecentPurchase(
                    itemId = event.itemId,
                    afterTimestamp = cutoffTime
                )

                if (recent != null) {
                    // Update quantity instead of new record
                    val updated = recent.copy(
                        quantity = recent.quantity + event.quantity
                    )
                    purchaseDao.update(updated)
                    return@runCatching updated.toDomain()
                }

                // Create new purchase record
                val entity = event.toEntity()
                purchaseDao.insert(entity)
                entity.toDomain()
            }
        }

    override suspend fun getPurchasesForItem(itemId: String): List<Purchase> =
        withContext(dispatcher) {
            purchaseDao.getPurchasesForItem(itemId).map { it.toDomain() }
        }

    override suspend fun getRecentPurchase(itemId: String, withinHours: Int): Purchase? =
        withContext(dispatcher) {
            val cutoff = Clock.System.now()
                .minus(withinHours.hours)
                .toEpochMilliseconds()
            purchaseDao.getRecentPurchase(itemId, cutoff)?.toDomain()
        }

    override suspend fun getLastPurchase(itemId: String): Purchase? =
        withContext(dispatcher) {
            purchaseDao.getLastPurchase(itemId)?.toDomain()
        }

    override suspend fun getAllPurchases(): List<Purchase> =
        withContext(dispatcher) {
            purchaseDao.getAllPurchases().map { it.toDomain() }
        }

    override suspend fun getItemIdsWithPurchases(): List<String> =
        withContext(dispatcher) {
            purchaseDao.getItemIdsWithPurchases()
        }

    override fun observePurchasesForItem(itemId: String): Flow<List<Purchase>> =
        purchaseDao.observePurchasesForItem(itemId)
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(dispatcher)

    override fun observeLatestPurchases(): Flow<List<Purchase>> =
        purchaseDao.observeLatestPurchases()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(dispatcher)

    private fun PurchaseEvent.toEntity() = PurchaseEntity(
        id = id,
        itemId = itemId,
        purchasedAt = purchasedAt.toEpochMilliseconds(),
        quantity = quantity,
        priceCents = priceCents,
        storeId = storeId,
    )

    private fun PurchaseEntity.toDomain() = Purchase(
        id = id,
        itemId = itemId,
        purchasedAt = kotlinx.datetime.Instant.fromEpochMilliseconds(purchasedAt),
        quantity = quantity,
        priceCents = priceCents,
        storeId = storeId,
    )
}
```

### RecordPurchaseUseCase.kt

```kotlin
package com.restock.core.domain.usecase

import com.restock.core.domain.model.PurchaseEvent
import com.restock.core.domain.repository.PurchaseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import timber.log.Timber
import javax.inject.Inject

class RecordPurchaseUseCase @Inject constructor(
    private val purchaseRepository: PurchaseRepository,
    private val patternWorkScheduler: PatternWorkScheduler,
    @ApplicationScope private val scope: CoroutineScope,
) {
    /**
     * Records a purchase event asynchronously.
     * Does not block the calling coroutine.
     */
    operator fun invoke(itemId: String, quantity: Int) {
        scope.launch {
            val event = PurchaseEvent(
                itemId = itemId,
                quantity = quantity,
                purchasedAt = Clock.System.now(),
            )

            purchaseRepository.recordPurchase(event)
                .onSuccess {
                    // Trigger pattern recalculation
                    patternWorkScheduler.triggerImmediateCalculation()
                }
                .onFailure { e ->
                    Timber.e(e, "Failed to record purchase")
                }
        }
    }
}
```

### Updated CheckItemUseCase.kt

```kotlin
package com.restock.core.domain.usecase

import com.restock.core.domain.repository.CurrentListRepository
import javax.inject.Inject

class CheckItemUseCase @Inject constructor(
    private val currentListRepository: CurrentListRepository,
    private val recordPurchaseUseCase: RecordPurchaseUseCase,
) {
    suspend operator fun invoke(id: String, isChecked: Boolean): Result<Unit> {
        return if (isChecked) {
            // Get item details before checking
            val listItem = currentListRepository.getById(id)
                ?: return Result.failure(IllegalArgumentException("Item not found"))

            // Check the item
            currentListRepository.checkItem(id)
                .onSuccess {
                    // Record purchase asynchronously (don't wait)
                    recordPurchaseUseCase(
                        itemId = listItem.itemId,
                        quantity = listItem.quantity,
                    )
                }
        } else {
            currentListRepository.uncheckItem(id)
        }
    }
}
```

## Acceptance Criteria

- [ ] PurchaseRepository interface created
- [ ] PurchaseRepositoryImpl handles recording and queries
- [ ] RecordPurchaseUseCase integrates with CheckItemUseCase
- [ ] Debounce logic prevents duplicate purchases within 4 hours
- [ ] Recording happens asynchronously (no UI delay)
- [ ] Works offline
- [ ] Unit tests for repository (90%+ coverage)
- [ ] Integration with CheckItemUseCase verified

## Testing

```kotlin
class PurchaseRepositoryTest {

    @Test
    fun `recordPurchase creates new record when no recent purchase`() = runTest {
        val repository = createRepository()

        val event = PurchaseEvent(
            itemId = "milk",
            quantity = 1,
            purchasedAt = Clock.System.now(),
        )

        val result = repository.recordPurchase(event)

        assertThat(result.isSuccess).isTrue()
        assertThat(repository.getPurchasesForItem("milk")).hasSize(1)
    }

    @Test
    fun `recordPurchase updates quantity when recent purchase exists`() = runTest {
        val repository = createRepository()

        // First purchase
        val event1 = PurchaseEvent(
            itemId = "milk",
            quantity = 1,
            purchasedAt = Clock.System.now(),
        )
        repository.recordPurchase(event1)

        // Second purchase within 4 hours
        val event2 = PurchaseEvent(
            itemId = "milk",
            quantity = 2,
            purchasedAt = Clock.System.now(),
        )
        repository.recordPurchase(event2)

        // Should still be 1 record but with quantity 3
        val purchases = repository.getPurchasesForItem("milk")
        assertThat(purchases).hasSize(1)
        assertThat(purchases.first().quantity).isEqualTo(3)
    }

    @Test
    fun `recordPurchase creates new record after 4 hours`() = runTest {
        // Test with mocked clock
    }
}
```

## Definition of Done

- [ ] All code compiles without errors
- [ ] Repository handles all CRUD operations
- [ ] Debounce prevents duplicates correctly
- [ ] Async recording doesn't block UI
- [ ] Unit tests pass (90%+ coverage)
- [ ] Integration with CheckItemUseCase works

## Dependent Tasks

This task blocks:
- 002_pattern_calculation.md
- 003_background_processing.md

## Notes

- Recording is fire-and-forget (async)
- Use ApplicationScope for coroutine that outlives ViewModel
- 4-hour debounce handles same shopping trip multiple check-offs
- Quantity aggregation helps with accurate pattern learning
