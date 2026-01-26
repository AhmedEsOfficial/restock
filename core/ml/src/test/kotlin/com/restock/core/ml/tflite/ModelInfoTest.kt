package com.restock.core.ml.tflite

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for [ModelInfo] data class.
 *
 * Tests cover:
 * - Computed properties (expectedInputSize, outputSize, isBatchModel)
 * - String representation
 * - Data class equality
 */
class ModelInfoTest {
    @Nested
    @DisplayName("expectedInputSize")
    inner class ExpectedInputSizeTests {
        @Test
        fun `calculates correct size for 1D input`() {
            val modelInfo =
                ModelInfo(
                    inputShape = listOf(10),
                    outputShape = listOf(1),
                    inputDataType = "FLOAT32",
                    outputDataType = "FLOAT32",
                )

            assertThat(modelInfo.expectedInputSize).isEqualTo(10)
        }

        @Test
        fun `calculates correct size for 2D input with batch`() {
            val modelInfo =
                ModelInfo(
                    inputShape = listOf(1, 10),
                    outputShape = listOf(1, 1),
                    inputDataType = "FLOAT32",
                    outputDataType = "FLOAT32",
                )

            assertThat(modelInfo.expectedInputSize).isEqualTo(10)
        }

        @Test
        fun `calculates correct size for 3D input`() {
            val modelInfo =
                ModelInfo(
                    inputShape = listOf(1, 10, 3),
                    outputShape = listOf(1, 1),
                    inputDataType = "FLOAT32",
                    outputDataType = "FLOAT32",
                )

            assertThat(modelInfo.expectedInputSize).isEqualTo(30)
        }
    }

    @Nested
    @DisplayName("outputSize")
    inner class OutputSizeTests {
        @Test
        fun `calculates correct size for 1D output`() {
            val modelInfo =
                ModelInfo(
                    inputShape = listOf(1, 10),
                    outputShape = listOf(1),
                    inputDataType = "FLOAT32",
                    outputDataType = "FLOAT32",
                )

            assertThat(modelInfo.outputSize).isEqualTo(1)
        }

        @Test
        fun `calculates correct size for 2D output with batch`() {
            val modelInfo =
                ModelInfo(
                    inputShape = listOf(1, 10),
                    outputShape = listOf(1, 5),
                    inputDataType = "FLOAT32",
                    outputDataType = "FLOAT32",
                )

            assertThat(modelInfo.outputSize).isEqualTo(5)
        }

        @Test
        fun `calculates correct size for multi-class output`() {
            val modelInfo =
                ModelInfo(
                    inputShape = listOf(1, 10),
                    outputShape = listOf(1, 10),
                    inputDataType = "FLOAT32",
                    outputDataType = "FLOAT32",
                )

            assertThat(modelInfo.outputSize).isEqualTo(10)
        }
    }

    @Nested
    @DisplayName("isBatchModel")
    inner class IsBatchModelTests {
        @Test
        fun `returns true when first dimension is 1`() {
            val modelInfo =
                ModelInfo(
                    inputShape = listOf(1, 10),
                    outputShape = listOf(1, 1),
                    inputDataType = "FLOAT32",
                    outputDataType = "FLOAT32",
                )

            assertThat(modelInfo.isBatchModel).isTrue()
        }

        @Test
        fun `returns false when first dimension is not 1`() {
            val modelInfo =
                ModelInfo(
                    inputShape = listOf(10),
                    outputShape = listOf(1),
                    inputDataType = "FLOAT32",
                    outputDataType = "FLOAT32",
                )

            assertThat(modelInfo.isBatchModel).isFalse()
        }

        @Test
        fun `returns false for empty input shape`() {
            val modelInfo =
                ModelInfo(
                    inputShape = emptyList(),
                    outputShape = listOf(1),
                    inputDataType = "FLOAT32",
                    outputDataType = "FLOAT32",
                )

            assertThat(modelInfo.isBatchModel).isFalse()
        }

        @Test
        fun `returns false when first dimension is larger batch`() {
            val modelInfo =
                ModelInfo(
                    inputShape = listOf(32, 10),
                    outputShape = listOf(32, 1),
                    inputDataType = "FLOAT32",
                    outputDataType = "FLOAT32",
                )

            assertThat(modelInfo.isBatchModel).isFalse()
        }
    }

    @Nested
    @DisplayName("toString")
    inner class ToStringTests {
        @Test
        fun `provides readable string representation`() {
            val modelInfo =
                ModelInfo(
                    inputShape = listOf(1, 10),
                    outputShape = listOf(1, 1),
                    inputDataType = "FLOAT32",
                    outputDataType = "FLOAT32",
                )

            val result = modelInfo.toString()

            assertThat(result).contains("input=[1, 10]")
            assertThat(result).contains("FLOAT32")
            assertThat(result).contains("output=[1, 1]")
        }
    }

    @Nested
    @DisplayName("Data class equality")
    inner class EqualityTests {
        @Test
        fun `equal objects have same hash code`() {
            val info1 =
                ModelInfo(
                    inputShape = listOf(1, 10),
                    outputShape = listOf(1, 1),
                    inputDataType = "FLOAT32",
                    outputDataType = "FLOAT32",
                )
            val info2 =
                ModelInfo(
                    inputShape = listOf(1, 10),
                    outputShape = listOf(1, 1),
                    inputDataType = "FLOAT32",
                    outputDataType = "FLOAT32",
                )

            assertThat(info1).isEqualTo(info2)
            assertThat(info1.hashCode()).isEqualTo(info2.hashCode())
        }

        @Test
        fun `different input shapes are not equal`() {
            val info1 =
                ModelInfo(
                    inputShape = listOf(1, 10),
                    outputShape = listOf(1, 1),
                    inputDataType = "FLOAT32",
                    outputDataType = "FLOAT32",
                )
            val info2 =
                ModelInfo(
                    inputShape = listOf(1, 20),
                    outputShape = listOf(1, 1),
                    inputDataType = "FLOAT32",
                    outputDataType = "FLOAT32",
                )

            assertThat(info1).isNotEqualTo(info2)
        }

        @Test
        fun `different data types are not equal`() {
            val info1 =
                ModelInfo(
                    inputShape = listOf(1, 10),
                    outputShape = listOf(1, 1),
                    inputDataType = "FLOAT32",
                    outputDataType = "FLOAT32",
                )
            val info2 =
                ModelInfo(
                    inputShape = listOf(1, 10),
                    outputShape = listOf(1, 1),
                    inputDataType = "INT8",
                    outputDataType = "FLOAT32",
                )

            assertThat(info1).isNotEqualTo(info2)
        }
    }
}
