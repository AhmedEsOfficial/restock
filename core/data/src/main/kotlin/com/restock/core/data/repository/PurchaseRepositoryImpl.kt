package com.restock.core.data.repository

import com.restock.core.data.database.dao.PurchaseDao
import com.restock.core.data.di.IoDispatcher
import com.restock.core.data.mapper.toDomain
import com.restock.core.data.mapper.toEntity
import com.restock.core.domain.model.Purchase
import com.restock.core.domain.model.PurchaseEvent
import com.restock.core.domain.repository.PurchaseRepository
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Implementation of [PurchaseRepository] using Room database.
 *
 * Manages purchase history for pattern learning. Every time an item
 * is checked off the list, a purchase is recorded to enable the
 * pattern learning engine to calculate restock intervals.
 *
 * Includes debounce logic to prevent duplicate purchases within a
 * configurable time window (default 4 hours). When a purchase for the
 * same item exists within the window, the quantity is aggregated instead
 * of creating a new record.
 */
@Singleton
class PurchaseRepositoryImpl
    @Inject
    constructor(
        private val purchaseDao: PurchaseDao,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : PurchaseRepository {
        override fun observeAllPurchases(): Flow<List<Purchase>> =
            purchaseDao.observeAllPurchases()
                .map { entities -> entities.toDomain() }
                .flowOn(ioDispatcher)

        override fun observePurchasesForItem(itemId: String): Flow<List<Purchase>> =
            purchaseDao.observePurchasesForItem(itemId)
                .map { entities -> entities.toDomain() }
                .flowOn(ioDispatcher)

        override fun observeLatestPurchases(): Flow<List<Purchase>> =
            purchaseDao.observeLatestPurchases()
                .map { entities -> entities.toDomain() }
                .flowOn(ioDispatcher)

        override suspend fun getPurchasesForItem(itemId: String): List<Purchase> =
            withContext(ioDispatcher) {
                purchaseDao.getPurchasesForItem(itemId).toDomain()
            }

        override suspend fun getLastPurchaseForItem(itemId: String): Purchase? =
            withContext(ioDispatcher) {
                purchaseDao.getLastPurchaseForItem(itemId)?.toDomain()
            }

        override suspend fun getRecentPurchase(
            itemId: String,
            withinHours: Int,
        ): Purchase? =
            withContext(ioDispatcher) {
                val cutoffTime =
                    Instant.now()
                        .minus(Duration.ofHours(withinHours.toLong()))
                        .toEpochMilli()
                purchaseDao.getRecentPurchase(itemId, cutoffTime)?.toDomain()
            }

        override suspend fun getPurchaseCountForItem(itemId: String): Int =
            withContext(ioDispatcher) {
                purchaseDao.getPurchaseCountForItem(itemId)
            }

        override suspend fun getItemIdsWithPurchases(): List<String> =
            withContext(ioDispatcher) {
                purchaseDao.getItemIdsWithPurchases()
            }

        override suspend fun getItemsWithMinPurchases(minPurchases: Int): List<String> =
            withContext(ioDispatcher) {
                purchaseDao.getItemsWithMinPurchases(minPurchases)
            }

        override suspend fun getPurchasesInRange(
            start: Instant,
            end: Instant,
        ): List<Purchase> =
            withContext(ioDispatcher) {
                purchaseDao.getPurchasesInRange(
                    startTime = start.toEpochMilli(),
                    endTime = end.toEpochMilli(),
                ).toDomain()
            }

        override suspend fun recordPurchase(purchase: Purchase): Result<Unit> =
            withContext(ioDispatcher) {
                runCatching {
                    purchaseDao.insert(purchase.toEntity())
                }
            }

        override suspend fun recordPurchaseEvent(event: PurchaseEvent): Result<Purchase> =
            withContext(ioDispatcher) {
                runCatching {
                    // Check for recent purchase within debounce window
                    val cutoffTime =
                        event.purchasedAt
                            .minus(Duration.ofHours(PurchaseEvent.DEFAULT_DEBOUNCE_HOURS.toLong()))
                            .toEpochMilli()

                    val recentPurchase =
                        purchaseDao.getRecentPurchase(
                            itemId = event.itemId,
                            afterTimestamp = cutoffTime,
                        )

                    if (recentPurchase != null) {
                        // Update quantity instead of creating new record
                        val updatedEntity =
                            recentPurchase.copy(
                                quantity = recentPurchase.quantity + event.quantity,
                            )
                        purchaseDao.update(updatedEntity)
                        updatedEntity.toDomain()
                    } else {
                        // Create new purchase record
                        val entity = event.toPurchase().toEntity()
                        purchaseDao.insert(entity)
                        entity.toDomain()
                    }
                }
            }

        override suspend fun recordPurchases(purchases: List<Purchase>): Result<Unit> =
            withContext(ioDispatcher) {
                runCatching {
                    purchaseDao.insertAll(purchases.map { it.toEntity() })
                }
            }

        override suspend fun updatePurchase(purchase: Purchase): Result<Unit> =
            withContext(ioDispatcher) {
                runCatching {
                    purchaseDao.update(purchase.toEntity())
                }
            }

        override suspend fun deletePurchase(id: String): Result<Unit> =
            withContext(ioDispatcher) {
                runCatching {
                    purchaseDao.delete(id)
                }
            }

        override suspend fun deletePurchasesForItem(itemId: String): Result<Unit> =
            withContext(ioDispatcher) {
                runCatching {
                    purchaseDao.deleteForItem(itemId)
                }
            }
    }
