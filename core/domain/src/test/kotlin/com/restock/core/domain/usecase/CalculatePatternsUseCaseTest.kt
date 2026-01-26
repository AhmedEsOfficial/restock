package com.restock.core.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.restock.core.domain.model.Pattern
import com.restock.core.domain.model.Purchase
import com.restock.core.domain.port.PatternCalculatorPort
import com.restock.core.domain.repository.PatternRepository
import com.restock.core.domain.repository.PurchaseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlinx.coroutines.test.runTest

/**
 * Unit tests for [CalculatePatternsUseCase].
 */
class CalculatePatternsUseCaseTest {
    private val purchaseRepository: PurchaseRepository = mockk()
    private val patternRepository: PatternRepository = mockk()
    private val patternCalculator: PatternCalculatorPort = mockk()
    private lateinit var useCase: CalculatePatternsUseCase

    @BeforeEach
    fun setup() {
        useCase =
            CalculatePatternsUseCase(
                purchaseRepository = purchaseRepository,
                patternRepository = patternRepository,
                patternCalculator = patternCalculator,
            )
    }

    @Nested
    @DisplayName("Batch pattern calculation")
    inner class BatchCalculationTests {
        @Test
        fun `returns empty result when no items with sufficient purchases`() =
            runTest {
                coEvery { purchaseRepository.getItemsWithMinPurchases(Pattern.MIN_PURCHASES) } returns emptyList()

                val result = useCase()

                assertThat(result.isSuccess).isTrue()
                val calculationResult = result.getOrThrow()
                assertThat(calculationResult.itemsProcessed).isEqualTo(0)
                assertThat(calculationResult.patternsCalculated).isEqualTo(0)
                assertThat(calculationResult.patternsUpdated).isEqualTo(0)
                assertThat(calculationResult.patternsFailed).isEqualTo(0)
                assertThat(calculationResult.hasChanges).isFalse()
            }

        @Test
        fun `calculates patterns for all eligible items`() =
            runTest {
                val itemIds = listOf("item-1", "item-2", "item-3")
                coEvery { purchaseRepository.getItemsWithMinPurchases(Pattern.MIN_PURCHASES) } returns itemIds
                coEvery { purchaseRepository.getPurchasesForItem(any()) } returns createPurchases()
                coEvery { patternRepository.getPatternForItem(any()) } returns null
                every { patternCalculator.calculate(any(), any()) } answers {
                    createPattern(itemId = secondArg())
                }
                coEvery { patternRepository.savePatterns(any()) } returns Result.success(Unit)

                val result = useCase()

                assertThat(result.isSuccess).isTrue()
                val calculationResult = result.getOrThrow()
                assertThat(calculationResult.itemsProcessed).isEqualTo(3)
                assertThat(calculationResult.patternsCalculated).isEqualTo(3)
                coVerify(exactly = 3) { purchaseRepository.getPurchasesForItem(any()) }
            }

        @Test
        fun `counts new patterns correctly`() =
            runTest {
                val itemIds = listOf("new-item-1", "new-item-2")
                coEvery { purchaseRepository.getItemsWithMinPurchases(Pattern.MIN_PURCHASES) } returns itemIds
                coEvery { purchaseRepository.getPurchasesForItem(any()) } returns createPurchases()
                coEvery { patternRepository.getPatternForItem(any()) } returns null // No existing patterns
                every { patternCalculator.calculate(any(), any()) } answers {
                    createPattern(itemId = secondArg())
                }
                coEvery { patternRepository.savePatterns(any()) } returns Result.success(Unit)

                val result = useCase()

                assertThat(result.getOrThrow().patternsCalculated).isEqualTo(2)
                assertThat(result.getOrThrow().patternsUpdated).isEqualTo(0)
            }

        @Test
        fun `counts updated patterns correctly`() =
            runTest {
                val itemIds = listOf("existing-item-1", "existing-item-2")
                coEvery { purchaseRepository.getItemsWithMinPurchases(Pattern.MIN_PURCHASES) } returns itemIds
                coEvery { purchaseRepository.getPurchasesForItem(any()) } returns createPurchases()
                coEvery { patternRepository.getPatternForItem(any()) } returns createPattern() // Existing patterns
                every { patternCalculator.calculate(any(), any()) } answers {
                    createPattern(itemId = secondArg())
                }
                coEvery { patternRepository.savePatterns(any()) } returns Result.success(Unit)

                val result = useCase()

                assertThat(result.getOrThrow().patternsCalculated).isEqualTo(0)
                assertThat(result.getOrThrow().patternsUpdated).isEqualTo(2)
            }

        @Test
        fun `counts mixed new and updated patterns correctly`() =
            runTest {
                val itemIds = listOf("new-item", "existing-item")
                coEvery { purchaseRepository.getItemsWithMinPurchases(Pattern.MIN_PURCHASES) } returns itemIds
                coEvery { purchaseRepository.getPurchasesForItem(any()) } returns createPurchases()
                coEvery { patternRepository.getPatternForItem("new-item") } returns null
                coEvery { patternRepository.getPatternForItem("existing-item") } returns createPattern()
                every { patternCalculator.calculate(any(), any()) } answers {
                    createPattern(itemId = secondArg())
                }
                coEvery { patternRepository.savePatterns(any()) } returns Result.success(Unit)

                val result = useCase()

                assertThat(result.getOrThrow().patternsCalculated).isEqualTo(1)
                assertThat(result.getOrThrow().patternsUpdated).isEqualTo(1)
            }

        @Test
        fun `counts failed calculations correctly`() =
            runTest {
                val itemIds = listOf("success-item", "fail-item")
                coEvery {
                    purchaseRepository.getItemsWithMinPurchases(Pattern.MIN_PURCHASES)
                } returns itemIds
                coEvery { purchaseRepository.getPurchasesForItem(any()) } returns createPurchases()
                coEvery { patternRepository.getPatternForItem(any()) } returns null
                val successPattern = createPattern(itemId = "success-item")
                every { patternCalculator.calculate(any(), "success-item") } returns successPattern
                // Calculation fails for fail-item
                every { patternCalculator.calculate(any(), "fail-item") } returns null
                coEvery { patternRepository.savePatterns(any()) } returns Result.success(Unit)

                val result = useCase()

                assertThat(result.getOrThrow().patternsCalculated).isEqualTo(1)
                assertThat(result.getOrThrow().patternsFailed).isEqualTo(1)
            }

        @Test
        fun `batch saves all calculated patterns`() =
            runTest {
                val itemIds = listOf("item-1", "item-2", "item-3")
                coEvery { purchaseRepository.getItemsWithMinPurchases(Pattern.MIN_PURCHASES) } returns itemIds
                coEvery { purchaseRepository.getPurchasesForItem(any()) } returns createPurchases()
                coEvery { patternRepository.getPatternForItem(any()) } returns null
                every { patternCalculator.calculate(any(), any()) } answers {
                    createPattern(itemId = secondArg())
                }
                val savedPatterns = slot<List<Pattern>>()
                coEvery { patternRepository.savePatterns(capture(savedPatterns)) } returns Result.success(Unit)

                useCase()

                assertThat(savedPatterns.captured).hasSize(3)
                assertThat(savedPatterns.captured.map { it.itemId }).containsExactly("item-1", "item-2", "item-3")
            }

        @Test
        fun `does not save when all calculations fail`() =
            runTest {
                val itemIds = listOf("fail-item-1", "fail-item-2")
                coEvery { purchaseRepository.getItemsWithMinPurchases(Pattern.MIN_PURCHASES) } returns itemIds
                coEvery { purchaseRepository.getPurchasesForItem(any()) } returns createPurchases()
                coEvery { patternRepository.getPatternForItem(any()) } returns null
                every { patternCalculator.calculate(any(), any()) } returns null

                useCase()

                coVerify(exactly = 0) { patternRepository.savePatterns(any()) }
            }
    }

    @Nested
    @DisplayName("Single item calculation")
    inner class SingleItemCalculationTests {
        @Test
        fun `returns null when insufficient purchases`() =
            runTest {
                coEvery { purchaseRepository.getPurchasesForItem("item-1") } returns
                    listOf(
                        createPurchase(daysAgo = 14),
                        createPurchase(daysAgo = 7),
                    ) // Only 2 purchases

                val result = useCase.calculateForItem("item-1")

                assertThat(result.isSuccess).isTrue()
                assertThat(result.getOrThrow()).isNull()
            }

        @Test
        fun `calculates and saves pattern for single item`() =
            runTest {
                val purchases = createPurchases()
                coEvery { purchaseRepository.getPurchasesForItem("item-1") } returns purchases
                every { patternCalculator.calculate(purchases, "item-1") } returns createPattern(itemId = "item-1")
                coEvery { patternRepository.savePattern(any()) } returns Result.success(Unit)

                val result = useCase.calculateForItem("item-1")

                assertThat(result.isSuccess).isTrue()
                assertThat(result.getOrThrow()).isNotNull()
                assertThat(result.getOrThrow()!!.itemId).isEqualTo("item-1")
                coVerify { patternRepository.savePattern(any()) }
            }

        @Test
        fun `returns null when calculator returns null`() =
            runTest {
                coEvery { purchaseRepository.getPurchasesForItem("item-1") } returns createPurchases()
                every { patternCalculator.calculate(any(), any()) } returns null

                val result = useCase.calculateForItem("item-1")

                assertThat(result.isSuccess).isTrue()
                assertThat(result.getOrThrow()).isNull()
            }

        @Test
        fun `does not save when calculation fails`() =
            runTest {
                coEvery { purchaseRepository.getPurchasesForItem("item-1") } returns createPurchases()
                every { patternCalculator.calculate(any(), any()) } returns null

                useCase.calculateForItem("item-1")

                coVerify(exactly = 0) { patternRepository.savePattern(any()) }
            }
    }

    @Nested
    @DisplayName("Error handling")
    inner class ErrorHandlingTests {
        @Test
        fun `returns failure when purchase repository throws`() =
            runTest {
                coEvery { purchaseRepository.getItemsWithMinPurchases(any()) } throws RuntimeException("DB error")

                val result = useCase()

                assertThat(result.isFailure).isTrue()
            }

        @Test
        fun `returns failure when pattern repository save throws`() =
            runTest {
                coEvery { purchaseRepository.getItemsWithMinPurchases(Pattern.MIN_PURCHASES) } returns listOf("item-1")
                coEvery { purchaseRepository.getPurchasesForItem(any()) } returns createPurchases()
                coEvery { patternRepository.getPatternForItem(any()) } returns null
                every { patternCalculator.calculate(any(), any()) } returns createPattern()
                coEvery { patternRepository.savePatterns(any()) } returns Result.failure(RuntimeException("DB error"))

                val result = useCase()

                assertThat(result.isFailure).isTrue()
            }

        @Test
        fun `calculateForItem returns failure when save throws`() =
            runTest {
                coEvery { purchaseRepository.getPurchasesForItem("item-1") } returns createPurchases()
                every { patternCalculator.calculate(any(), any()) } returns createPattern()
                coEvery { patternRepository.savePattern(any()) } returns Result.failure(RuntimeException("DB error"))

                val result = useCase.calculateForItem("item-1")

                assertThat(result.isFailure).isTrue()
            }
    }

    @Nested
    @DisplayName("CalculationResult properties")
    inner class CalculationResultTests {
        @Test
        fun `totalPatternsProcessed returns sum of calculated and updated`() {
            val result =
                CalculationResult(
                    itemsProcessed = 10,
                    patternsCalculated = 3,
                    patternsUpdated = 5,
                    patternsFailed = 2,
                )

            assertThat(result.totalPatternsProcessed).isEqualTo(8)
        }

        @Test
        fun `hasChanges returns true when patterns calculated`() {
            val result =
                CalculationResult(
                    itemsProcessed = 5,
                    patternsCalculated = 3,
                    patternsUpdated = 0,
                    patternsFailed = 2,
                )

            assertThat(result.hasChanges).isTrue()
        }

        @Test
        fun `hasChanges returns true when patterns updated`() {
            val result =
                CalculationResult(
                    itemsProcessed = 5,
                    patternsCalculated = 0,
                    patternsUpdated = 3,
                    patternsFailed = 2,
                )

            assertThat(result.hasChanges).isTrue()
        }

        @Test
        fun `hasChanges returns false when no patterns processed`() {
            val result =
                CalculationResult(
                    itemsProcessed = 5,
                    patternsCalculated = 0,
                    patternsUpdated = 0,
                    patternsFailed = 5,
                )

            assertThat(result.hasChanges).isFalse()
        }
    }

    // Helper functions
    private fun createPurchases(): List<Purchase> =
        listOf(
            createPurchase(daysAgo = 21),
            createPurchase(daysAgo = 14),
            createPurchase(daysAgo = 7),
            createPurchase(daysAgo = 0),
        )

    private fun createPurchase(
        daysAgo: Int,
        id: String = "purchase-${System.nanoTime()}",
        itemId: String = "test-item",
    ): Purchase {
        val now = Instant.now()
        val purchasedAt = now.minusSeconds(daysAgo * 24L * 60 * 60)
        return Purchase(
            id = id,
            itemId = itemId,
            purchasedAt = purchasedAt,
            quantity = 1,
        )
    }

    private fun createPattern(
        itemId: String = "test-item",
        avgIntervalDays: Double = 7.0,
        confidenceScore: Double = 0.85,
        sampleCount: Int = 5,
    ) = Pattern(
        itemId = itemId,
        avgIntervalDays = avgIntervalDays,
        confidenceScore = confidenceScore,
        sampleCount = sampleCount,
        lastCalculated = Instant.now(),
    )
}
