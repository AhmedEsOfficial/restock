package com.restock.core.ml.tflite

/**
 * Indicates the source/method used for a prediction.
 *
 * The prediction system uses a fallback hierarchy:
 * 1. ML_MODEL - TensorFlow Lite model inference (most accurate when trained)
 * 2. STATISTICAL - Statistical pattern calculation using coefficient of variation
 * 3. INSUFFICIENT_DATA - Not enough purchase history to make a prediction
 */
enum class PredictionSource {
    /**
     * Prediction made using the TensorFlow Lite ML model.
     * This source is used when:
     * - The ML model is loaded successfully
     * - Sufficient purchase history exists for ML inference
     * - The model produces a valid prediction
     */
    ML_MODEL,

    /**
     * Prediction made using statistical analysis.
     * Uses coefficient of variation (CV) based confidence scoring.
     * This is the primary method for MVP and serves as fallback when ML fails.
     */
    STATISTICAL,

    /**
     * No prediction possible due to insufficient data.
     * Returned when there are fewer than MIN_PURCHASES (3) purchases
     * for the item.
     */
    INSUFFICIENT_DATA,
}
