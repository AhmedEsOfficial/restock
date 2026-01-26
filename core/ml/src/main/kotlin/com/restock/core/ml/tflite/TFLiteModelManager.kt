package com.restock.core.ml.tflite

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages TensorFlow Lite model lifecycle for on-device ML inference.
 *
 * This manager handles:
 * - Loading TFLite models from assets
 * - Running inference on loaded models
 * - Model lifecycle management (loading, closing)
 * - Thread-safe model state
 *
 * For MVP, the primary prediction method remains statistical calculation.
 * TFLite infrastructure is set up for future ML model enhancements.
 *
 * Thread Safety: All operations are synchronized on the model lock.
 * The manager can be safely used from multiple threads.
 *
 * Usage:
 * ```
 * val manager = TFLiteModelManager(context)
 * if (manager.loadModel()) {
 *     val result = manager.runInference(inputData)
 *     // Use result
 * }
 * manager.close()
 * ```
 */
@Singleton
class TFLiteModelManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val modelLock = Any()
        private var interpreter: Interpreter? = null

        @Volatile
        private var isModelLoaded = false

        /**
         * Loads the TFLite model from assets.
         *
         * This method is thread-safe and idempotent. If the model is already loaded,
         * it returns true without reloading.
         *
         * @param modelFileName Name of the model file in assets folder.
         * @return true if model loaded successfully or was already loaded, false on error.
         */
        fun loadModel(modelFileName: String = DEFAULT_MODEL_NAME): Boolean {
            synchronized(modelLock) {
                if (isModelLoaded && interpreter != null) {
                    if (DEBUG_LOGGING_ENABLED) {
                        Log.d(TAG, "Model already loaded: $modelFileName")
                    }
                    return true
                }

                return try {
                    val modelFile = loadModelFile(modelFileName)
                    val options =
                        Interpreter.Options().apply {
                            setNumThreads(NUM_THREADS)
                            // GPU delegate can be added for better performance:
                            // addDelegate(GpuDelegate())
                        }
                    interpreter = Interpreter(modelFile, options)
                    isModelLoaded = true
                    if (DEBUG_LOGGING_ENABLED) {
                        Log.d(TAG, "TFLite model loaded successfully: $modelFileName")
                    }
                    true
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to load TFLite model: $modelFileName", e)
                    isModelLoaded = false
                    interpreter = null
                    false
                }
            }
        }

        /**
         * Loads the model file from assets as a memory-mapped buffer.
         *
         * Memory mapping provides efficient access to the model file without
         * loading the entire file into memory at once.
         *
         * @param fileName Name of the model file in assets folder.
         * @return MappedByteBuffer for the model file.
         * @throws Exception if file cannot be found or read.
         */
        @Suppress("TooGenericExceptionCaught")
        private fun loadModelFile(fileName: String): MappedByteBuffer {
            val assetFileDescriptor = context.assets.openFd(fileName)
            val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
            val fileChannel = fileInputStream.channel

            return try {
                fileChannel.map(
                    FileChannel.MapMode.READ_ONLY,
                    assetFileDescriptor.startOffset,
                    assetFileDescriptor.declaredLength,
                )
            } finally {
                // Note: We don't close the channel here because the MappedByteBuffer
                // is backed by it. The interpreter will use this buffer directly.
                try {
                    assetFileDescriptor.close()
                } catch (e: Exception) {
                    Log.w(TAG, "Error closing asset file descriptor", e)
                }
            }
        }

        /**
         * Runs inference on the loaded model.
         *
         * @param input Input data as float array. Size must match model's expected input size.
         * @return Output array from the model, or null if inference fails.
         */
        fun runInference(input: FloatArray): FloatArray? {
            val currentInterpreter: Interpreter
            synchronized(modelLock) {
                if (!isModelLoaded || interpreter == null) {
                    Log.w(TAG, "Model not loaded, cannot run inference")
                    return null
                }
                currentInterpreter = interpreter!!
            }

            return try {
                val inputShape = currentInterpreter.getInputTensor(0).shape()
                val outputShape = currentInterpreter.getOutputTensor(0).shape()

                // Validate input size
                val expectedInputSize = inputShape.reduce { acc, i -> acc * i }
                if (input.size != expectedInputSize) {
                    Log.e(
                        TAG,
                        "Input size mismatch: expected $expectedInputSize, got ${input.size}",
                    )
                    return null
                }

                // Prepare output buffer
                val outputSize = if (outputShape.size > 1) outputShape[1] else outputShape[0]
                val output = Array(1) { FloatArray(outputSize) }

                // Run inference
                currentInterpreter.run(arrayOf(input), output)
                output[0]
            } catch (e: Exception) {
                Log.e(TAG, "Inference failed", e)
                null
            }
        }

        /**
         * Returns model metadata if available.
         *
         * @return ModelInfo describing input/output specifications, or null if model not loaded.
         */
        fun getModelInfo(): ModelInfo? {
            val currentInterpreter: Interpreter
            synchronized(modelLock) {
                if (!isModelLoaded || interpreter == null) {
                    return null
                }
                currentInterpreter = interpreter!!
            }

            return try {
                val inputTensor = currentInterpreter.getInputTensor(0)
                val outputTensor = currentInterpreter.getOutputTensor(0)

                ModelInfo(
                    inputShape = inputTensor.shape().toList(),
                    outputShape = outputTensor.shape().toList(),
                    inputDataType = inputTensor.dataType().name,
                    outputDataType = outputTensor.dataType().name,
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get model info", e)
                null
            }
        }

        /**
         * Returns whether a model is currently loaded.
         */
        fun isLoaded(): Boolean = isModelLoaded

        /**
         * Closes the interpreter and releases resources.
         *
         * This method should be called when the model is no longer needed
         * to free native resources.
         */
        fun close() {
            synchronized(modelLock) {
                try {
                    interpreter?.close()
                } catch (e: Exception) {
                    Log.w(TAG, "Error closing interpreter", e)
                }
                interpreter = null
                isModelLoaded = false
                if (DEBUG_LOGGING_ENABLED) {
                    Log.d(TAG, "TFLite model closed")
                }
            }
        }

        companion object {
            private const val TAG = "TFLiteModelManager"
            const val DEFAULT_MODEL_NAME = "pattern_model.tflite"

            /**
             * Number of threads for model inference.
             * 2 threads is a good balance for mobile devices.
             */
            private const val NUM_THREADS = 2

            /**
             * Enable debug logging for development.
             * Should be disabled in release builds.
             */
            internal const val DEBUG_LOGGING_ENABLED = true
        }
    }
