package com.restock.core.data.repository

import com.restock.core.data.database.dao.PurchaseDao
import com.restock.core.data.di.IoDispatcher
import com.restock.core.data.mapper.toDomain
import com.restock.core.data.mapper.toEntity
import com.restock.core.domain.model.Purchase
import com.restock.core.domain.repository.PurchaseRepository
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

        override suspend fun getPurchasesForItem(itemId: String): List<Purchase> =
            withContext(ioDispatcher) {
                purchaseDao.getPurchasesForItem(itemId).toDomain()
            }

        override suspend fun getLastPurchaseForItem(itemId: String): Purchase? =
            withContext(ioDispatcher) {
                purchaseDao.getLastPurchaseForItem(itemId)?.toDomain()
            }

        override suspend fun getPurchaseCountForItem(itemId: String): Int =
            withContext(ioDispatcher) {
                purchaseDao.getPurchaseCountForItem(itemId)
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

        override suspend fun recordPurchases(purchases: List<Purchase>): Result<Unit> =
            withContext(ioDispatcher) {
                runCatching {
                    purchaseDao.insertAll(purchases.map { it.toEntity() })
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
