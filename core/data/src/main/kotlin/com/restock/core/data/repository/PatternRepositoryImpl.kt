package com.restock.core.data.repository

import com.restock.core.data.database.dao.PatternDao
import com.restock.core.data.di.IoDispatcher
import com.restock.core.data.mapper.toDomain
import com.restock.core.data.mapper.toEntity
import com.restock.core.domain.model.Pattern
import com.restock.core.domain.model.PatternWithItem
import com.restock.core.domain.repository.PatternRepository
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Implementation of [PatternRepository] using Room database.
 *
 * Manages learned purchase patterns that power the smart suggestions feature.
 * Patterns are calculated from purchase history and stored for quick retrieval.
 */
@Singleton
class PatternRepositoryImpl
    @Inject
    constructor(
        private val patternDao: PatternDao,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : PatternRepository {
        override fun observePatternsWithItems(): Flow<List<PatternWithItem>> =
            patternDao.observePatternsWithItems()
                .map { patterns -> patterns.toDomain() }
                .flowOn(ioDispatcher)

        override suspend fun getSuggestionReadyPatterns(): List<PatternWithItem> =
            withContext(ioDispatcher) {
                patternDao.getHighConfidencePatterns(
                    minConfidence = Pattern.SUGGESTION_CONFIDENCE_THRESHOLD,
                    minSamples = Pattern.MIN_PURCHASES,
                ).toDomain()
            }

        override suspend fun getPatternForItem(itemId: String): Pattern? =
            withContext(ioDispatcher) {
                patternDao.getPatternForItem(itemId)?.toDomain()
            }

        override suspend fun getAllPatterns(): List<Pattern> =
            withContext(ioDispatcher) {
                patternDao.getAllPatterns().toDomain()
            }

        override suspend fun savePattern(pattern: Pattern): Result<Unit> =
            withContext(ioDispatcher) {
                runCatching {
                    patternDao.upsert(pattern.toEntity())
                }
            }

        override suspend fun savePatterns(patterns: List<Pattern>): Result<Unit> =
            withContext(ioDispatcher) {
                runCatching {
                    patternDao.upsertAll(patterns.map { it.toEntity() })
                }
            }

        override suspend fun updateLastSuggested(itemId: String): Result<Unit> =
            withContext(ioDispatcher) {
                runCatching {
                    patternDao.updateLastSuggested(itemId, Instant.now().toEpochMilli())
                }
            }

        override suspend fun deletePattern(itemId: String): Result<Unit> =
            withContext(ioDispatcher) {
                runCatching {
                    patternDao.delete(itemId)
                }
            }
    }
