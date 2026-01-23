package com.restock.core.data.mapper

import com.restock.core.data.database.entity.PatternEntity
import com.restock.core.domain.model.Pattern
import com.restock.core.domain.model.PatternWithItem
import java.time.Instant
import com.restock.core.data.database.dao.PatternWithItem as PatternWithItemDao

/**
 * Maps PatternEntity to Pattern domain model.
 */
fun PatternEntity.toDomain(): Pattern =
    Pattern(
        itemId = itemId,
        avgIntervalDays = avgIntervalDays,
        confidenceScore = confidenceScore,
        sampleCount = sampleCount,
        lastCalculated = Instant.ofEpochMilli(lastCalculated),
        lastSuggested = lastSuggested?.let { Instant.ofEpochMilli(it) },
    )

/**
 * Maps Pattern domain model to PatternEntity.
 */
fun Pattern.toEntity(): PatternEntity =
    PatternEntity(
        itemId = itemId,
        avgIntervalDays = avgIntervalDays,
        confidenceScore = confidenceScore,
        sampleCount = sampleCount,
        lastCalculated = lastCalculated.toEpochMilli(),
        lastSuggested = lastSuggested?.toEpochMilli(),
    )

/**
 * Maps PatternWithItem (from DAO JOIN) to PatternWithItem domain model.
 */
fun PatternWithItemDao.toDomain(): PatternWithItem =
    PatternWithItem(
        itemId = itemId,
        itemName = itemName,
        avgIntervalDays = avgIntervalDays,
        confidenceScore = confidenceScore,
        sampleCount = sampleCount,
        lastCalculated = Instant.ofEpochMilli(lastCalculated),
        lastSuggested = lastSuggested?.let { Instant.ofEpochMilli(it) },
        lastPurchasedAt = lastPurchasedAt?.let { Instant.ofEpochMilli(it) },
    )

/**
 * Maps a list of PatternEntities to domain models.
 */
fun List<PatternEntity>.toDomain(): List<Pattern> = map { it.toDomain() }

/**
 * Maps a list of PatternWithItem from DAO to domain models.
 */
@JvmName("toDomainPatternWithItem")
fun List<PatternWithItemDao>.toDomain(): List<PatternWithItem> = map { it.toDomain() }
