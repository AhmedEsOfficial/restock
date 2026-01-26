package com.restock.core.ml.tflite

import android.util.Log
import com.restock.core.domain.model.Purchase
import com.restock.core.ml.pattern.PatternCalculator
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ML-enhanced pattern model for predicting purchase intervals.
 *
 * This model provides a unified interface for purchase predictions that:
 * 1. Attempts to use TensorFlow Lite ML model for enhanced predictions
 * 2. Falls back to statistical calculations if ML is unavailable
 * 3. Applies confidence boosting when ML predictions are available
 *
 * Architecture:
 * - Primary method (MVP): Statistical calculation via [PatternCalculator]
 * - Enhancement path: TFLite model inference when loaded
 * - Graceful degradation: Always has a fallback
 *
 * For MVP, the statistical model is the primary method. The TFLite infrastructure
 * is set up to enable future ML model enhancements without code changes.
 *
 * Thread Safety: All operations are thread-safe.
 */
@Singleton
class PatternMLModel
    @Inject
    constructor(
        private val modelManager: TFLiteModelManager,
        private val statisticalCalculator: PatternCalculator,
    ) {
        @Volatile
        private var isInitialized = false

        /**
         * Initializes the ML model.
         *
         * Attempts to load the TFLite model. If loading fails, the system
         * gracefully falls back to statistical calculations.
         *
         * @return true if ML model loaded successfully, false otherwise.
         *         Note: false does not indicate failure - statistical fallback is available.
         */
        fun initialize(): Boolean {
            isInitialized = modelManager.loadModel()
            if (!isInitialized) {
                Log.w(TAG, "ML model not available, using statistical fallback")
            }
            return isInitialized
        }

        /**
         * Predicts when an item will next be needed.
         *
         * Uses ML model if available and sufficient data exists,
         * otherwise uses statistical calculation.
         *
         * @param itemId The unique identifier of the item.
         * @param purchaseHistory List of historical purchases for the item.
         * @return PredictionResult containing prediction details and source.
         */
        fun predictNextPurchase(
            itemId: String,
            purchaseHistory: List<Purchase>,
        ): PredictionResult {
            // First, calculate statistical pattern (always available)
            val pattern = statisticalCalculator.calculate(purchaseHistory, itemId)

            // If no pattern can be calculated, return insufficient data
            if (pattern == null) {
                if (DEBUG_LOGGING_ENABLED) {
                    Log.d(TAG, "Insufficient data for prediction: $itemId")
                }
                return PredictionResult.insufficientData(itemId)
            }

            // Try ML enhancement if model is loaded and we have enough samples
            val mlPrediction =
                if (isInitialized && purchaseHistory.size >= ML_MIN_SAMPLES) {
                    tryMLPrediction(purchaseHistory)
                } else {
                    null
                }

            // Return ML-enhanced result if available, otherwise statistical
            return if (mlPrediction != null) {
                PredictionResult(
                    itemId = itemId,
                    predictedDaysUntilNeeded = mlPrediction,
                    confidence = (pattern.confidenceScore * ML_CONFIDENCE_BOOST).coerceAtMost(1.0),
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
        }

        /**
         * Attempts to get a prediction from the ML model.
         *
         * Prepares input data by:
         * 1. Sorting purchases by date
         * 2. Calculating intervals between purchases
         * 3. Taking the last INPUT_WINDOW_SIZE intervals
         * 4. Padding with mean if fewer intervals available
         *
         * @param purchaseHistory List of historical purchases.
         * @return Predicted days until next purchase, or null if inference fails.
         */
        private fun tryMLPrediction(purchaseHistory: List<Purchase>): Double? {
            // Prepare input: calculate intervals between purchases
            val intervals =
                purchaseHistory
                    .sortedBy { it.purchasedAt }
                    .zipWithNext { a, b ->
                        val durationMs =
                            Duration.between(a.purchasedAt, b.purchasedAt).toMillis()
                        (durationMs / MILLIS_PER_DAY).toFloat()
                    }
                    .takeLast(INPUT_WINDOW_SIZE)
                    .toFloatArray()

            if (intervals.isEmpty()) {
                return null
            }

            // Pad with mean if fewer than INPUT_WINDOW_SIZE intervals
            val inputArray =
                if (intervals.size < INPUT_WINDOW_SIZE) {
                    val mean = intervals.average().toFloat()
                    FloatArray(INPUT_WINDOW_SIZE) { i ->
                        intervals.getOrElse(i) { mean }
                    }
                } else {
                    intervals
                }

            return runInference(inputArray)
        }

        /**
         * Runs inference on the ML model.
         *
         * @param input Prepared input data.
         * @return Predicted value, or null if inference fails.
         */
        private fun runInference(input: FloatArray): Double? {
            val output = modelManager.runInference(input)
            if (output == null) {
                if (DEBUG_LOGGING_ENABLED) {
                    Log.d(TAG, "ML inference returned null")
                }
                return null
            }

            val prediction = output.firstOrNull()?.toDouble()
            if (DEBUG_LOGGING_ENABLED && prediction != null) {
                Log.d(TAG, "ML prediction: $prediction days")
            }
            return prediction
        }

        /**
         * Checks if the ML model is initialized.
         *
         * @return true if ML model is loaded and ready for inference.
         */
        fun isMLAvailable(): Boolean = isInitialized

        /**
         * Gets model information if available.
         *
         * @return ModelInfo or null if model not loaded.
         */
        fun getModelInfo(): ModelInfo? = modelManager.getModelInfo()

        /**
         * Shuts down the ML model and releases resources.
         */
        fun shutdown() {
            modelManager.close()
            isInitialized = false
            if (DEBUG_LOGGING_ENABLED) {
                Log.d(TAG, "PatternMLModel shutdown complete")
            }
        }

        companion object {
            private const val TAG = "PatternMLModel"

            /**
             * Number of purchase intervals used as model input.
             */
            const val INPUT_WINDOW_SIZE = 10

            /**
             * Minimum purchases required before attempting ML prediction.
             * ML needs more data than statistical to be useful.
             */
            const val ML_MIN_SAMPLES = 5

            /**
             * Confidence boost factor applied to ML predictions.
             * ML predictions are assumed to be 10% more confident when available.
             */
            const val ML_CONFIDENCE_BOOST = 1.1

            /**
             * Milliseconds per day for interval calculation.
             */
            private const val MILLIS_PER_DAY = 24.0 * 60.0 * 60.0 * 1000.0

            /**
             * Enable debug logging for development.
             */
            internal const val DEBUG_LOGGING_ENABLED = true
        }
    }
