package com.restock.core.ml.tflite

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for [PredictionResult] and [PredictionSource].
 *
 * Tests cover:
 * - PredictionResult properties and computed fields
 * - Interval description generation
 * - Suggestion readiness determination
 * - Factory methods
 */
class PredictionResultTest {
    private val testItemId = "test-item-1"

    @Nested
    @DisplayName("isSuggestionReady")
    inner class IsSuggestionReadyTests {
        @Test
        fun `returns true when prediction is valid and confidence above threshold`() {
            val result =
                PredictionResult(
                    itemId = testItemId,
                    predictedDaysUntilNeeded = 7.0,
                    confidence = 0.7,
                    source = PredictionSource.STATISTICAL,
                )

            assertThat(result.isSuggestionReady).isTrue()
        }

        @Test
        fun `returns false when prediction is null`() {
            val result =
                PredictionResult(
                    itemId = testItemId,
                    predictedDaysUntilNeeded = null,
                    confidence = 0.8,
                    source = PredictionSource.INSUFFICIENT_DATA,
                )

            assertThat(result.isSuggestionReady).isFalse()
        }

        @Test
        fun `returns false when confidence below threshold`() {
            val result =
                PredictionResult(
                    itemId = testItemId,
                    predictedDaysUntilNeeded = 7.0,
                    confidence = 0.5,
                    source = PredictionSource.STATISTICAL,
                )

            assertThat(result.isSuggestionReady).isFalse()
        }

        @Test
        fun `returns true when confidence exactly at threshold`() {
            val result =
                PredictionResult(
                    itemId = testItemId,
                    predictedDaysUntilNeeded = 7.0,
                    confidence = PredictionResult.SUGGESTION_CONFIDENCE_THRESHOLD,
                    source = PredictionSource.STATISTICAL,
                )

            assertThat(result.isSuggestionReady).isTrue()
        }
    }

    @Nested
    @DisplayName("intervalDescription")
    inner class IntervalDescriptionTests {
        @Test
        fun `returns null when prediction is null`() {
            val result = PredictionResult.insufficientData(testItemId)

            assertThat(result.intervalDescription).isNull()
        }

        @Test
        fun `returns Daily for 1 day interval`() {
            val result = createResultWithDays(1.0)

            assertThat(result.intervalDescription).isEqualTo("Daily")
        }

        @Test
        fun `returns Weekly for 7 day interval`() {
            val result = createResultWithDays(7.0)

            assertThat(result.intervalDescription).isEqualTo("Weekly")
        }

        @Test
        fun `returns Every 2 weeks for 14 day interval`() {
            val result = createResultWithDays(14.0)

            assertThat(result.intervalDescription).isEqualTo("Every 2 weeks")
        }

        @Test
        fun `returns Monthly for 28-31 day intervals`() {
            assertThat(createResultWithDays(28.0).intervalDescription).isEqualTo("Monthly")
            assertThat(createResultWithDays(30.0).intervalDescription).isEqualTo("Monthly")
            assertThat(createResultWithDays(31.0).intervalDescription).isEqualTo("Monthly")
        }

        @Test
        fun `returns Every X days for 2-6 day intervals`() {
            assertThat(createResultWithDays(3.0).intervalDescription).isEqualTo("Every 3 days")
            assertThat(createResultWithDays(5.0).intervalDescription).isEqualTo("Every 5 days")
        }

        @Test
        fun `returns About weekly for 8-13 day intervals`() {
            assertThat(createResultWithDays(8.0).intervalDescription).isEqualTo("About weekly")
            assertThat(createResultWithDays(10.0).intervalDescription).isEqualTo("About weekly")
        }

        @Test
        fun `returns Every X weeks for 15-27 day intervals`() {
            assertThat(createResultWithDays(21.0).intervalDescription).isEqualTo("Every 3 weeks")
        }

        @Test
        fun `returns Every X months for intervals over 31 days`() {
            assertThat(createResultWithDays(60.0).intervalDescription).isEqualTo("Every 2 months")
            assertThat(createResultWithDays(90.0).intervalDescription).isEqualTo("Every 3 months")
        }
    }

    @Nested
    @DisplayName("Factory methods")
    inner class FactoryMethodTests {
        @Test
        fun `insufficientData creates correct result`() {
            val result = PredictionResult.insufficientData(testItemId)

            assertThat(result.itemId).isEqualTo(testItemId)
            assertThat(result.predictedDaysUntilNeeded).isNull()
            assertThat(result.confidence).isEqualTo(0.0)
            assertThat(result.source).isEqualTo(PredictionSource.INSUFFICIENT_DATA)
        }
    }

    @Nested
    @DisplayName("Constants")
    inner class ConstantsTests {
        @Test
        fun `SUGGESTION_CONFIDENCE_THRESHOLD is 0_6`() {
            assertThat(PredictionResult.SUGGESTION_CONFIDENCE_THRESHOLD).isEqualTo(0.6)
        }
    }

    @Nested
    @DisplayName("PredictionSource enum")
    inner class PredictionSourceTests {
        @Test
        fun `all sources are present`() {
            assertThat(PredictionSource.values().toList())
                .containsExactly(
                    PredictionSource.ML_MODEL,
                    PredictionSource.STATISTICAL,
                    PredictionSource.INSUFFICIENT_DATA,
                )
        }
    }

    private fun createResultWithDays(days: Double) =
        PredictionResult(
            itemId = testItemId,
            predictedDaysUntilNeeded = days,
            confidence = 0.8,
            source = PredictionSource.STATISTICAL,
        )
}
