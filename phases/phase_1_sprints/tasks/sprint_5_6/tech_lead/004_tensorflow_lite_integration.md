# Task: Implement TensorFlow Lite Integration

**Sprint:** 5-6 (Pattern Learning Engine)
**Owner:** Tech Lead / Senior Developer
**Estimate:** 17 hours (T-8.1 through T-8.8 combined)
**Priority:** P0
**Prerequisites:** 002_pattern_calculation.md

---

## Sprint Context

TensorFlow Lite enables on-device ML inference for advanced pattern recognition in future versions. This sprint sets up the infrastructure, with the statistical model as the primary method and TFLite as a future enhancement path.

### Sprint Theme
> *"The intelligence that learns without asking."*

### Key Principles
- Infrastructure over accuracy (for MVP)
- Graceful fallback to statistical model
- On-device only (privacy-preserving)
- Model versioning for future updates

---

## Task Description

Set up TensorFlow Lite infrastructure for on-device ML inference with proper fallback to statistical pattern calculation.

## Implementation

### Gradle Dependencies

```kotlin
// build.gradle.kts (core:ml module)
dependencies {
    // TensorFlow Lite
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")

    // Optional: GPU delegate for faster inference
    // implementation("org.tensorflow:tensorflow-lite-gpu:2.14.0")
}
```

### TFLiteModelManager.kt

```kotlin
package com.restock.core.ml.tflite

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.Interpreter
import timber.log.Timber
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TFLiteModelManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var interpreter: Interpreter? = null
    private var isModelLoaded = false

    /**
     * Loads the TFLite model from assets.
     * @return true if model loaded successfully, false otherwise
     */
    fun loadModel(modelFileName: String = DEFAULT_MODEL_NAME): Boolean {
        return try {
            val modelFile = loadModelFile(modelFileName)
            val options = Interpreter.Options().apply {
                setNumThreads(2)
                // GPU delegate can be added for better performance:
                // addDelegate(GpuDelegate())
            }
            interpreter = Interpreter(modelFile, options)
            isModelLoaded = true
            Timber.d("TFLite model loaded successfully: $modelFileName")
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to load TFLite model: $modelFileName")
            isModelLoaded = false
            false
        }
    }

    private fun loadModelFile(fileName: String): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(fileName)
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            assetFileDescriptor.startOffset,
            assetFileDescriptor.declaredLength
        )
    }

    /**
     * Runs inference on the loaded model.
     * @param input Input data as float array
     * @return Output array, or null if inference fails
     */
    fun runInference(input: FloatArray): FloatArray? {
        val currentInterpreter = interpreter
        if (currentInterpreter == null || !isModelLoaded) {
            Timber.w("Model not loaded, cannot run inference")
            return null
        }

        return try {
            val inputShape = currentInterpreter.getInputTensor(0).shape()
            val outputShape = currentInterpreter.getOutputTensor(0).shape()

            // Validate input size
            val expectedInputSize = inputShape.reduce { acc, i -> acc * i }
            if (input.size != expectedInputSize) {
                Timber.e("Input size mismatch: expected $expectedInputSize, got ${input.size}")
                return null
            }

            val output = Array(1) { FloatArray(outputShape[1]) }
            currentInterpreter.run(arrayOf(input), output)
            output[0]
        } catch (e: Exception) {
            Timber.e(e, "Inference failed")
            null
        }
    }

    /**
     * Returns model metadata if available.
     */
    fun getModelInfo(): ModelInfo? {
        val currentInterpreter = interpreter ?: return null

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
            Timber.e(e, "Failed to get model info")
            null
        }
    }

    fun isLoaded(): Boolean = isModelLoaded

    fun close() {
        interpreter?.close()
        interpreter = null
        isModelLoaded = false
        Timber.d("TFLite model closed")
    }

    companion object {
        const val DEFAULT_MODEL_NAME = "pattern_model.tflite"
    }
}

data class ModelInfo(
    val inputShape: List<Int>,
    val outputShape: List<Int>,
    val inputDataType: String,
    val outputDataType: String,
)
```

### PatternMLModel.kt

```kotlin
package com.restock.core.ml.pattern

import com.restock.core.domain.model.Purchase
import com.restock.core.ml.tflite.TFLiteModelManager
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatternMLModel @Inject constructor(
    private val modelManager: TFLiteModelManager,
    private val statisticalCalculator: PatternCalculator,
) {
    private var isInitialized = false

    /**
     * Initializes the ML model.
     * Falls back to statistical if ML fails.
     */
    fun initialize(): Boolean {
        isInitialized = modelManager.loadModel()
        if (!isInitialized) {
            Timber.w("ML model not available, using statistical fallback")
        }
        return isInitialized
    }

    /**
     * Predicts when an item will next be needed.
     * Uses ML model if available, otherwise statistical calculation.
     */
    fun predictNextPurchase(
        itemId: String,
        purchaseHistory: List<Purchase>,
    ): PredictionResult {
        // For MVP, always use statistical model
        // ML model can enhance predictions in future versions
        val pattern = statisticalCalculator.calculatePattern(purchaseHistory)

        return if (pattern != null) {
            // Try ML enhancement if model loaded
            val mlEnhanced = if (isInitialized && purchaseHistory.size >= ML_MIN_SAMPLES) {
                tryMLPrediction(purchaseHistory)
            } else {
                null
            }

            if (mlEnhanced != null) {
                PredictionResult(
                    itemId = itemId,
                    predictedDaysUntilNeeded = mlEnhanced,
                    confidence = pattern.confidenceScore * ML_CONFIDENCE_BOOST,
                    source = PredictionSource.ML_MODEL,
                )
            } else {
                PredictionResult(
                    itemId = itemId,
                    predictedDaysUntilNeeded = pattern.avgIntervalDays,
                    confidence = pattern.confidenceScore,
                    source = PredictionSource.STATISTICAL,
                )
            }
        } else {
            PredictionResult(
                itemId = itemId,
                predictedDaysUntilNeeded = null,
                confidence = 0.0,
                source = PredictionSource.INSUFFICIENT_DATA,
            )
        }
    }

    private fun tryMLPrediction(purchaseHistory: List<Purchase>): Double? {
        // Prepare input: normalize intervals
        val intervals = purchaseHistory
            .sortedBy { it.purchasedAt }
            .zipWithNext { a, b ->
                val durationMs = b.purchasedAt.toEpochMilliseconds() -
                    a.purchasedAt.toEpochMilliseconds()
                (durationMs / (24 * 60 * 60 * 1000.0)).toFloat() // Days
            }
            .takeLast(INPUT_WINDOW_SIZE)
            .toFloatArray()

        if (intervals.size < INPUT_WINDOW_SIZE) {
            // Pad with mean
            val mean = intervals.average().toFloat()
            val padded = FloatArray(INPUT_WINDOW_SIZE) { i ->
                intervals.getOrElse(i) { mean }
            }
            return runInference(padded)
        }

        return runInference(intervals)
    }

    private fun runInference(input: FloatArray): Double? {
        val output = modelManager.runInference(input) ?: return null
        return output.firstOrNull()?.toDouble()
    }

    fun shutdown() {
        modelManager.close()
        isInitialized = false
    }

    companion object {
        private const val INPUT_WINDOW_SIZE = 10
        private const val ML_MIN_SAMPLES = 5
        private const val ML_CONFIDENCE_BOOST = 1.1 // 10% confidence boost for ML
    }
}

enum class PredictionSource {
    STATISTICAL,
    ML_MODEL,
    INSUFFICIENT_DATA,
}

data class PredictionResult(
    val itemId: String,
    val predictedDaysUntilNeeded: Double?,
    val confidence: Double,
    val source: PredictionSource,
)
```

### Placeholder Model

For MVP, create a placeholder TFLite model or skip ML entirely:

```kotlin
// If no actual model, the statistical fallback handles everything
// Place an empty or simple model in app/src/main/assets/pattern_model.tflite
```

### Model Versioning System

```kotlin
package com.restock.core.ml.tflite

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.modelDataStore by preferencesDataStore(name = "model_versions")

@Singleton
class ModelVersionManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val currentVersionKey = intPreferencesKey("current_model_version")

    suspend fun getCurrentVersion(): Int =
        context.modelDataStore.data
            .map { it[currentVersionKey] ?: 0 }
            .first()

    suspend fun setCurrentVersion(version: Int) {
        context.modelDataStore.edit { prefs ->
            prefs[currentVersionKey] = version
        }
    }

    suspend fun shouldUpdateModel(newVersion: Int): Boolean {
        val current = getCurrentVersion()
        return newVersion > current
    }

    companion object {
        const val INITIAL_VERSION = 1
    }
}
```

## Acceptance Criteria

- [ ] TFLite dependencies added
- [ ] TFLiteModelManager handles model loading
- [ ] Inference wrapper works correctly
- [ ] Fallback to statistical model if TFLite fails
- [ ] Model versioning system in place
- [ ] Unit tests for ML components
- [ ] Integration tests verify fallback

## Testing

```kotlin
class TFLiteModelManagerTest {

    @Test
    fun `loadModel returns false when model file missing`() {
        val context = mockk<Context>(relaxed = true)
        every { context.assets.openFd(any()) } throws Exception("File not found")

        val manager = TFLiteModelManager(context)
        val result = manager.loadModel()

        assertThat(result).isFalse()
        assertThat(manager.isLoaded()).isFalse()
    }

    @Test
    fun `runInference returns null when model not loaded`() {
        val context = mockk<Context>(relaxed = true)
        val manager = TFLiteModelManager(context)

        val result = manager.runInference(floatArrayOf(1f, 2f, 3f))

        assertThat(result).isNull()
    }
}

class PatternMLModelTest {

    @Test
    fun `predictNextPurchase uses statistical when ML unavailable`() {
        val modelManager = mockk<TFLiteModelManager>()
        every { modelManager.loadModel() } returns false

        val calculator = PatternCalculator()
        val mlModel = PatternMLModel(modelManager, calculator)

        mlModel.initialize()

        val purchases = listOf(
            purchase(daysAgo = 21),
            purchase(daysAgo = 14),
            purchase(daysAgo = 7),
            purchase(daysAgo = 0),
        )

        val result = mlModel.predictNextPurchase("milk", purchases)

        assertThat(result.source).isEqualTo(PredictionSource.STATISTICAL)
        assertThat(result.predictedDaysUntilNeeded).isCloseTo(7.0, within(0.5))
    }

    @Test
    fun `predictNextPurchase returns insufficient data for few purchases`() {
        val modelManager = mockk<TFLiteModelManager>()
        val calculator = PatternCalculator()
        val mlModel = PatternMLModel(modelManager, calculator)

        val purchases = listOf(
            purchase(daysAgo = 7),
            purchase(daysAgo = 0),
        )

        val result = mlModel.predictNextPurchase("milk", purchases)

        assertThat(result.source).isEqualTo(PredictionSource.INSUFFICIENT_DATA)
        assertThat(result.predictedDaysUntilNeeded).isNull()
    }
}
```

## Definition of Done

- [ ] TFLite runtime integrated
- [ ] Model loading works (or graceful failure)
- [ ] Inference produces results
- [ ] Statistical fallback always available
- [ ] Model versioning system ready
- [ ] Unit tests pass
- [ ] Performance profiled

## Notes

- For MVP, statistical model is primary
- TFLite infrastructure enables future ML enhancements
- GPU delegate is optional (CPU fine for this use case)
- Model file can be placeholder for initial release
- Consider model download from server in Phase 2
- Keep model small (< 5MB) for app size
