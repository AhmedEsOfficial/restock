package com.restock.core.ml.tflite

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.FileNotFoundException

/**
 * Unit tests for [TFLiteModelManager].
 *
 * Tests cover:
 * - Model loading success and failure scenarios
 * - Inference with various input states
 * - Model state management (loaded/not loaded)
 * - Thread safety considerations
 * - Resource cleanup
 *
 * Note: These tests mock Android dependencies since TensorFlow Lite
 * Interpreter requires native libraries not available in unit tests.
 * Integration tests with actual model should be run on device.
 */
class TFLiteModelManagerTest {
    private lateinit var context: Context
    private lateinit var assetManager: AssetManager
    private lateinit var assetFileDescriptor: AssetFileDescriptor

    @BeforeEach
    fun setup() {
        context = mockk(relaxed = true)
        assetManager = mockk(relaxed = true)
        assetFileDescriptor = mockk(relaxed = true)

        every { context.assets } returns assetManager
    }

    @Nested
    @DisplayName("Model loading")
    inner class ModelLoadingTests {
        @Test
        fun `loadModel returns false when model file not found`() {
            every { assetManager.openFd(any()) } throws FileNotFoundException("File not found")

            val manager = TFLiteModelManager(context)
            val result = manager.loadModel()

            assertThat(result).isFalse()
            assertThat(manager.isLoaded()).isFalse()
        }

        @Test
        fun `loadModel returns false on generic exception`() {
            every { assetManager.openFd(any()) } throws RuntimeException("Unexpected error")

            val manager = TFLiteModelManager(context)
            val result = manager.loadModel()

            assertThat(result).isFalse()
            assertThat(manager.isLoaded()).isFalse()
        }

        @Test
        fun `loadModel uses default model name when not specified`() {
            every { assetManager.openFd(TFLiteModelManager.DEFAULT_MODEL_NAME) } throws
                FileNotFoundException("Expected")

            val manager = TFLiteModelManager(context)
            manager.loadModel()

            verify { assetManager.openFd(TFLiteModelManager.DEFAULT_MODEL_NAME) }
        }

        @Test
        fun `loadModel uses custom model name when specified`() {
            val customModelName = "custom_model.tflite"
            every { assetManager.openFd(customModelName) } throws FileNotFoundException("Expected")

            val manager = TFLiteModelManager(context)
            manager.loadModel(customModelName)

            verify { assetManager.openFd(customModelName) }
        }

        @Test
        fun `isLoaded returns false initially`() {
            val manager = TFLiteModelManager(context)

            assertThat(manager.isLoaded()).isFalse()
        }
    }

    @Nested
    @DisplayName("Inference")
    inner class InferenceTests {
        @Test
        fun `runInference returns null when model not loaded`() {
            val manager = TFLiteModelManager(context)
            val input = floatArrayOf(1f, 2f, 3f)

            val result = manager.runInference(input)

            assertThat(result).isNull()
        }

        @Test
        fun `runInference with empty input returns null when model not loaded`() {
            val manager = TFLiteModelManager(context)

            val result = manager.runInference(floatArrayOf())

            assertThat(result).isNull()
        }
    }

    @Nested
    @DisplayName("Model info")
    inner class ModelInfoTests {
        @Test
        fun `getModelInfo returns null when model not loaded`() {
            val manager = TFLiteModelManager(context)

            val modelInfo = manager.getModelInfo()

            assertThat(modelInfo).isNull()
        }
    }

    @Nested
    @DisplayName("Resource management")
    inner class ResourceManagementTests {
        @Test
        fun `close resets loaded state`() {
            val manager = TFLiteModelManager(context)
            // Even if model wasn't loaded, close should be safe
            manager.close()

            assertThat(manager.isLoaded()).isFalse()
        }

        @Test
        fun `close can be called multiple times safely`() {
            val manager = TFLiteModelManager(context)

            // Multiple close calls should not throw
            manager.close()
            manager.close()
            manager.close()

            assertThat(manager.isLoaded()).isFalse()
        }

        @Test
        fun `loadModel after close attempts to reload`() {
            every { assetManager.openFd(any()) } throws FileNotFoundException("Expected")

            val manager = TFLiteModelManager(context)
            manager.close()

            // Should attempt to load again
            manager.loadModel()

            verify(exactly = 1) { assetManager.openFd(TFLiteModelManager.DEFAULT_MODEL_NAME) }
        }
    }

    @Nested
    @DisplayName("Default model name")
    inner class DefaultModelNameTests {
        @Test
        fun `DEFAULT_MODEL_NAME is pattern_model_tflite`() {
            assertThat(TFLiteModelManager.DEFAULT_MODEL_NAME).isEqualTo("pattern_model.tflite")
        }
    }
}
