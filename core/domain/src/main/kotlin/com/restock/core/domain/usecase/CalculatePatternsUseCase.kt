package com.restock.core.domain.usecase

import com.restock.core.domain.model.Pattern
import com.restock.core.domain.port.PatternCalculatorPort
import com.restock.core.domain.repository.PatternRepository
import com.restock.core.domain.repository.PurchaseRepository
import javax.inject.Inject

/**
 * Use case for calculating purchase patterns for all items.
 *
 * This use case coordinates the pattern calculation process:
 * 1. Retrieves all items that have sufficient purchase history
 * 2. Calculates patterns for each item using the pattern calculator
 * 3. Persists calculated patterns to the repository
 *
 * The actual pattern calculation algorithm is delegated to [PatternCalculatorPort]
 * to maintain domain layer purity (no direct ML module dependency).
 *
 * Key behaviors:
 * - Only processes items with at least [Pattern.MIN_PURCHASES] purchases
 * - Patterns are upserted (inserted or updated) to preserve existing data
 * - Returns a [CalculationResult] with statistics about the operation
 * - Non-blocking and safe to call from background workers
 */
class CalculatePatternsUseCase
    @Inject
    constructor(
        private val purchaseRepository: PurchaseRepository,
        private val patternRepository: PatternRepository,
        private val patternCalculator: PatternCalculatorPort,
    ) {
        /**
         * Calculates patterns for all items with sufficient purchase history.
         *
         * @return Result containing [CalculationResult] with statistics on success.
         */
        suspend operator fun invoke(): Result<CalculationResult> =
            runCatching {
                calculatePatternsInternal()
            }

        private suspend fun calculatePatternsInternal(): CalculationResult {
            val eligibleItemIds = purchaseRepository.getItemsWithMinPurchases(Pattern.MIN_PURCHASES)

            if (eligibleItemIds.isEmpty()) {
                return emptyCalculationResult()
            }

            val results = eligibleItemIds.map { itemId -> calculatePatternForItem(itemId) }
            val calculatedPatterns = results.mapNotNull { it.pattern }

            if (calculatedPatterns.isNotEmpty()) {
                patternRepository.savePatterns(calculatedPatterns).getOrThrow()
            }

            return CalculationResult(
                itemsProcessed = eligibleItemIds.size,
                patternsCalculated = results.count { it.isNewPattern },
                patternsUpdated = results.count { it.isUpdatedPattern },
                patternsFailed = results.count { it.isFailed },
            )
        }

        /**
         * Calculates pattern for a single item.
         *
         * Useful for immediate recalculation after a new purchase.
         *
         * @param itemId The ID of the item to calculate pattern for.
         * @return Result containing the calculated [Pattern] or null if insufficient data.
         */
        suspend fun calculateForItem(itemId: String): Result<Pattern?> =
            runCatching {
                calculateForItemInternal(itemId)
            }

        private suspend fun calculateForItemInternal(itemId: String): Pattern? {
            val purchases = purchaseRepository.getPurchasesForItem(itemId)

            if (purchases.size < Pattern.MIN_PURCHASES) {
                return null
            }

            val pattern = patternCalculator.calculate(purchases, itemId)

            pattern?.also { patternRepository.savePattern(it).getOrThrow() }

            return pattern
        }

        private suspend fun calculatePatternForItem(itemId: String): ItemCalculationResult {
            val purchases = purchaseRepository.getPurchasesForItem(itemId)
            val pattern = patternCalculator.calculate(purchases, itemId)

            if (pattern == null) {
                return ItemCalculationResult(pattern = null, isNew = false)
            }

            val existingPattern = patternRepository.getPatternForItem(itemId)
            return ItemCalculationResult(pattern = pattern, isNew = existingPattern == null)
        }

        private fun emptyCalculationResult() =
            CalculationResult(
                itemsProcessed = 0,
                patternsCalculated = 0,
                patternsUpdated = 0,
                patternsFailed = 0,
            )

        /**
         * Internal result for tracking pattern calculation outcome per item.
         */
        private data class ItemCalculationResult(
            val pattern: Pattern?,
            val isNew: Boolean,
        ) {
            val isNewPattern: Boolean get() = pattern != null && isNew
            val isUpdatedPattern: Boolean get() = pattern != null && !isNew
            val isFailed: Boolean get() = pattern == null
        }
    }

/**
 * Result of pattern calculation operation.
 *
 * @property itemsProcessed Total number of items that were evaluated.
 * @property patternsCalculated Number of new patterns created.
 * @property patternsUpdated Number of existing patterns updated.
 * @property patternsFailed Number of items where calculation failed (edge cases).
 */
data class CalculationResult(
    val itemsProcessed: Int,
    val patternsCalculated: Int,
    val patternsUpdated: Int,
    val patternsFailed: Int,
) {
    /**
     * Total number of patterns successfully saved.
     */
    val totalPatternsProcessed: Int
        get() = patternsCalculated + patternsUpdated

    /**
     * Whether any patterns were calculated or updated.
     */
    val hasChanges: Boolean
        get() = totalPatternsProcessed > 0
}
