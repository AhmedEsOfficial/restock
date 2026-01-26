package com.restock.core.ml.tflite

import com.google.common.truth.Truth.assertThat
import com.restock.core.domain.model.Pattern
import com.restock.core.domain.model.Purchase
import com.restock.core.ml.pattern.PatternCalculator
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Unit tests for [PatternMLModel].
 *
 * Tests cover:
 * - Initialization and ML availability
 * - Statistical fallback when ML unavailable
 * - Prediction result generation
 * - ML enhancement path
 * - Shutdown behavior
 */
class PatternMLModelTest {
    private lateinit var modelManager: TFLiteModelManager
    private lateinit var statisticalCalculator: PatternCalculator
    private lateinit var patternMLModel: PatternMLModel
    private val now = Instant.now()
    private val testItemId = "test-item-1"

    @BeforeEach
    fun setup() {
        modelManager = mockk(relaxed = true)
        statisticalCalculator = mockk()
        patternMLModel = PatternMLModel(modelManager, statisticalCalculator)
    }

    @Nested
    @DisplayName("Initialization")
    inner class InitializationTests {
        @Test
        fun `initialize returns true when model loads successfully`() {
            every { modelManager.loadModel(any()) } returns true

            val result = patternMLModel.initialize()

            assertThat(result).isTrue()
            assertThat(patternMLModel.isMLAvailable()).isTrue()
        }

        @Test
        fun `initialize returns false when model fails to load`() {
            every { modelManager.loadModel(any()) } returns false

            val result = patternMLModel.initialize()

            assertThat(result).isFalse()
            assertThat(patternMLModel.isMLAvailable()).isFalse()
        }

        @Test
        fun `isMLAvailable returns false before initialization`() {
            assertThat(patternMLModel.isMLAvailable()).isFalse()
        }
    }

    @Nested
    @DisplayName("Prediction with statistical fallback")
    inner class StatisticalFallbackTests {
        @Test
        fun `returns insufficient data when pattern cannot be calculated`() {
            every { statisticalCalculator.calculate(any(), any()) } returns null

            val result = patternMLModel.predictNextPurchase(testItemId, emptyList())

            assertThat(result.itemId).isEqualTo(testItemId)
            assertThat(result.source).isEqualTo(PredictionSource.INSUFFICIENT_DATA)
            assertThat(result.predictedDaysUntilNeeded).isNull()
            assertThat(result.confidence).isEqualTo(0.0)
        }

        @Test
        fun `returns statistical prediction when ML not initialized`() {
            val pattern = createPattern(avgIntervalDays = 7.0, confidenceScore = 0.85)
            every { statisticalCalculator.calculate(any(), any()) } returns pattern
            every { modelManager.loadModel(any()) } returns false

            val purchases = createPurchases(5)
            val result = patternMLModel.predictNextPurchase(testItemId, purchases)

            assertThat(result.source).isEqualTo(PredictionSource.STATISTICAL)
            assertThat(result.predictedDaysUntilNeeded).isEqualTo(7.0)
            assertThat(result.confidence).isEqualTo(0.85)
        }

        @Test
        fun `uses statistical calculator for pattern calculation`() {
            val pattern = createPattern(avgIntervalDays = 14.0, confidenceScore = 0.9)
            every { statisticalCalculator.calculate(any(), any()) } returns pattern

            val purchases = createPurchases(3)
            patternMLModel.predictNextPurchase(testItemId, purchases)

            verify { statisticalCalculator.calculate(purchases, testItemId) }
        }

        @Test
        fun `returns statistical prediction when not enough samples for ML`() {
            every { modelManager.loadModel(any()) } returns true
            patternMLModel.initialize()

            val pattern = createPattern(avgIntervalDays = 7.0, confidenceScore = 0.8)
            every { statisticalCalculator.calculate(any(), any()) } returns pattern

            // Only 3 purchases, but ML needs 5+ samples
            val purchases = createPurchases(3)
            val result = patternMLModel.predictNextPurchase(testItemId, purchases)

            assertThat(result.source).isEqualTo(PredictionSource.STATISTICAL)
        }
    }

    @Nested
    @DisplayName("ML-enhanced prediction")
    inner class MLEnhancedPredictionTests {
        @Test
        fun `attempts ML prediction when model loaded and enough samples`() {
            every { modelManager.loadModel(any()) } returns true
            every { modelManager.runInference(any()) } returns floatArrayOf(8.0f)
            patternMLModel.initialize()

            val pattern = createPattern(avgIntervalDays = 7.0, confidenceScore = 0.8)
            every { statisticalCalculator.calculate(any(), any()) } returns pattern

            // 6 purchases for ML (>= ML_MIN_SAMPLES)
            val purchases = createPurchases(6)
            val result = patternMLModel.predictNextPurchase(testItemId, purchases)

            assertThat(result.source).isEqualTo(PredictionSource.ML_MODEL)
            assertThat(result.predictedDaysUntilNeeded).isEqualTo(8.0)
        }

        @Test
        fun `ML prediction boosts confidence score`() {
            every { modelManager.loadModel(any()) } returns true
            every { modelManager.runInference(any()) } returns floatArrayOf(7.0f)
            patternMLModel.initialize()

            val pattern = createPattern(avgIntervalDays = 7.0, confidenceScore = 0.8)
            every { statisticalCalculator.calculate(any(), any()) } returns pattern

            val purchases = createPurchases(6)
            val result = patternMLModel.predictNextPurchase(testItemId, purchases)

            // Confidence boosted by ML_CONFIDENCE_BOOST (1.1)
            // 0.8 * 1.1 = 0.88
            assertThat(result.confidence).isWithin(0.01).of(0.88)
        }

        @Test
        fun `ML confidence boost is capped at 1_0`() {
            every { modelManager.loadModel(any()) } returns true
            every { modelManager.runInference(any()) } returns floatArrayOf(7.0f)
            patternMLModel.initialize()

            val pattern = createPattern(avgIntervalDays = 7.0, confidenceScore = 0.95)
            every { statisticalCalculator.calculate(any(), any()) } returns pattern

            val purchases = createPurchases(6)
            val result = patternMLModel.predictNextPurchase(testItemId, purchases)

            // 0.95 * 1.1 = 1.045, but should be capped at 1.0
            assertThat(result.confidence).isAtMost(1.0)
        }

        @Test
        fun `falls back to statistical when ML inference returns null`() {
            every { modelManager.loadModel(any()) } returns true
            every { modelManager.runInference(any()) } returns null
            patternMLModel.initialize()

            val pattern = createPattern(avgIntervalDays = 7.0, confidenceScore = 0.8)
            every { statisticalCalculator.calculate(any(), any()) } returns pattern

            val purchases = createPurchases(6)
            val result = patternMLModel.predictNextPurchase(testItemId, purchases)

            assertThat(result.source).isEqualTo(PredictionSource.STATISTICAL)
            assertThat(result.predictedDaysUntilNeeded).isEqualTo(7.0)
        }

        @Test
        fun `falls back to statistical when ML inference returns empty array`() {
            every { modelManager.loadModel(any()) } returns true
            every { modelManager.runInference(any()) } returns floatArrayOf()
            patternMLModel.initialize()

            val pattern = createPattern(avgIntervalDays = 7.0, confidenceScore = 0.8)
            every { statisticalCalculator.calculate(any(), any()) } returns pattern

            val purchases = createPurchases(6)
            val result = patternMLModel.predictNextPurchase(testItemId, purchases)

            assertThat(result.source).isEqualTo(PredictionSource.STATISTICAL)
        }
    }

    @Nested
    @DisplayName("Model info")
    inner class ModelInfoTests {
        @Test
        fun `getModelInfo delegates to model manager`() {
            val modelInfo =
                ModelInfo(
                    inputShape = listOf(1, 10),
                    outputShape = listOf(1, 1),
                    inputDataType = "FLOAT32",
                    outputDataType = "FLOAT32",
                )
            every { modelManager.getModelInfo() } returns modelInfo

            val result = patternMLModel.getModelInfo()

            assertThat(result).isEqualTo(modelInfo)
        }

        @Test
        fun `getModelInfo returns null when no model loaded`() {
            every { modelManager.getModelInfo() } returns null

            val result = patternMLModel.getModelInfo()

            assertThat(result).isNull()
        }
    }

    @Nested
    @DisplayName("Shutdown")
    inner class ShutdownTests {
        @Test
        fun `shutdown closes model manager`() {
            patternMLModel.shutdown()

            verify { modelManager.close() }
        }

        @Test
        fun `isMLAvailable returns false after shutdown`() {
            every { modelManager.loadModel(any()) } returns true
            patternMLModel.initialize()
            assertThat(patternMLModel.isMLAvailable()).isTrue()

            patternMLModel.shutdown()

            assertThat(patternMLModel.isMLAvailable()).isFalse()
        }
    }

    @Nested
    @DisplayName("Constants")
    inner class ConstantsTests {
        @Test
        fun `INPUT_WINDOW_SIZE is 10`() {
            assertThat(PatternMLModel.INPUT_WINDOW_SIZE).isEqualTo(10)
        }

        @Test
        fun `ML_MIN_SAMPLES is 5`() {
            assertThat(PatternMLModel.ML_MIN_SAMPLES).isEqualTo(5)
        }

        @Test
        fun `ML_CONFIDENCE_BOOST is 1_1`() {
            assertThat(PatternMLModel.ML_CONFIDENCE_BOOST).isWithin(0.01).of(1.1)
        }
    }

    // Helper functions
    private fun createPattern(
        avgIntervalDays: Double,
        confidenceScore: Double,
        sampleCount: Int = 5,
    ) = Pattern(
        itemId = testItemId,
        avgIntervalDays = avgIntervalDays,
        confidenceScore = confidenceScore,
        sampleCount = sampleCount,
        lastCalculated = now,
    )

    private fun createPurchases(count: Int): List<Purchase> =
        (0 until count).map { index ->
            Purchase(
                id = "purchase-$index",
                itemId = testItemId,
                purchasedAt = now.minus((count - index - 1).toLong() * 7, ChronoUnit.DAYS),
                quantity = 1,
            )
        }
}
