package com.restock.core.ml.tflite

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for TFLite model versioning components.
 *
 * Tests cover:
 * - VersionInfo data class behavior
 * - ModelVersionManager constants
 *
 * Note: Full integration tests for ModelVersionManager with DataStore
 * should be run as instrumented tests on Android device/emulator.
 */
class ModelVersionManagerTest {
    @Nested
    @DisplayName("VersionInfo data class")
    inner class VersionInfoTests {
        @Test
        fun `hasModel returns false for version 0`() {
            val info = VersionInfo(version = 0, lastUpdateTimestamp = null, modelName = "test.tflite")

            assertThat(info.hasModel).isFalse()
        }

        @Test
        fun `hasModel returns true for version greater than 0`() {
            val info = VersionInfo(version = 1, lastUpdateTimestamp = null, modelName = "test.tflite")

            assertThat(info.hasModel).isTrue()
        }

        @Test
        fun `hasModel returns true for any positive version`() {
            val info = VersionInfo(version = 5, lastUpdateTimestamp = null, modelName = "test.tflite")

            assertThat(info.hasModel).isTrue()
        }

        @Test
        fun `isBundledModel returns true for version 1`() {
            val info =
                VersionInfo(
                    version = ModelVersionManager.INITIAL_BUNDLED_VERSION,
                    lastUpdateTimestamp = null,
                    modelName = "test.tflite",
                )

            assertThat(info.isBundledModel).isTrue()
        }

        @Test
        fun `isBundledModel returns false for version 0`() {
            val info = VersionInfo(version = 0, lastUpdateTimestamp = null, modelName = "test.tflite")

            assertThat(info.isBundledModel).isFalse()
        }

        @Test
        fun `isBundledModel returns false for version 2`() {
            val info = VersionInfo(version = 2, lastUpdateTimestamp = null, modelName = "test.tflite")

            assertThat(info.isBundledModel).isFalse()
        }

        @Test
        fun `isBundledModel returns false for higher versions`() {
            val info = VersionInfo(version = 10, lastUpdateTimestamp = null, modelName = "test.tflite")

            assertThat(info.isBundledModel).isFalse()
        }

        @Test
        fun `lastUpdateTimestamp can be null`() {
            val info = VersionInfo(version = 1, lastUpdateTimestamp = null, modelName = "test.tflite")

            assertThat(info.lastUpdateTimestamp).isNull()
        }

        @Test
        fun `lastUpdateTimestamp can be set`() {
            val timestamp = System.currentTimeMillis()
            val info = VersionInfo(version = 1, lastUpdateTimestamp = timestamp, modelName = "test.tflite")

            assertThat(info.lastUpdateTimestamp).isEqualTo(timestamp)
        }

        @Test
        fun `modelName is stored correctly`() {
            val info = VersionInfo(version = 1, lastUpdateTimestamp = null, modelName = "custom_model.tflite")

            assertThat(info.modelName).isEqualTo("custom_model.tflite")
        }

        @Test
        fun `equals works correctly for identical info`() {
            val timestamp = 12345L
            val info1 = VersionInfo(version = 1, lastUpdateTimestamp = timestamp, modelName = "test.tflite")
            val info2 = VersionInfo(version = 1, lastUpdateTimestamp = timestamp, modelName = "test.tflite")

            assertThat(info1).isEqualTo(info2)
        }

        @Test
        fun `equals works correctly for different versions`() {
            val info1 = VersionInfo(version = 1, lastUpdateTimestamp = null, modelName = "test.tflite")
            val info2 = VersionInfo(version = 2, lastUpdateTimestamp = null, modelName = "test.tflite")

            assertThat(info1).isNotEqualTo(info2)
        }

        @Test
        fun `equals works correctly for different timestamps`() {
            val info1 = VersionInfo(version = 1, lastUpdateTimestamp = 100L, modelName = "test.tflite")
            val info2 = VersionInfo(version = 1, lastUpdateTimestamp = 200L, modelName = "test.tflite")

            assertThat(info1).isNotEqualTo(info2)
        }

        @Test
        fun `equals works correctly for different model names`() {
            val info1 = VersionInfo(version = 1, lastUpdateTimestamp = null, modelName = "model_a.tflite")
            val info2 = VersionInfo(version = 1, lastUpdateTimestamp = null, modelName = "model_b.tflite")

            assertThat(info1).isNotEqualTo(info2)
        }

        @Test
        fun `hashCode is consistent for equal objects`() {
            val info1 = VersionInfo(version = 1, lastUpdateTimestamp = 100L, modelName = "test.tflite")
            val info2 = VersionInfo(version = 1, lastUpdateTimestamp = 100L, modelName = "test.tflite")

            assertThat(info1.hashCode()).isEqualTo(info2.hashCode())
        }
    }

    @Nested
    @DisplayName("ModelVersionManager constants")
    inner class ConstantsTests {
        @Test
        fun `DEFAULT_VERSION is 0`() {
            assertThat(ModelVersionManager.DEFAULT_VERSION).isEqualTo(0)
        }

        @Test
        fun `INITIAL_BUNDLED_VERSION is 1`() {
            assertThat(ModelVersionManager.INITIAL_BUNDLED_VERSION).isEqualTo(1)
        }

        @Test
        fun `INITIAL_BUNDLED_VERSION is greater than DEFAULT_VERSION`() {
            assertThat(ModelVersionManager.INITIAL_BUNDLED_VERSION)
                .isGreaterThan(ModelVersionManager.DEFAULT_VERSION)
        }
    }
}
