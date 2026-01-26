package com.restock.core.ml.tflite

/**
 * Result of a purchase prediction for an item.
 *
 * Contains the predicted days until the item will be needed again,
 * along with metadata about prediction confidence and source.
 *
 * @property itemId The unique identifier of the item.
 * @property predictedDaysUntilNeeded Estimated days until restock needed, null if insufficient data.
 * @property confidence Confidence score between 0.0 and 1.0. Higher values indicate more reliable predictions.
 * @property source The method/source used to generate this prediction.
 */
data class PredictionResult(
    val itemId: String,
    val predictedDaysUntilNeeded: Double?,
    val confidence: Double,
    val source: PredictionSource,
) {
    /**
     * Whether this prediction is reliable enough for suggestions.
     * Requires confidence above the threshold and a valid prediction.
     */
    val isSuggestionReady: Boolean
        get() = predictedDaysUntilNeeded != null && confidence >= SUGGESTION_CONFIDENCE_THRESHOLD

    /**
     * Human-readable description of the prediction interval.
     */
    val intervalDescription: String?
        get() {
            val days = predictedDaysUntilNeeded?.toInt() ?: return null
            return when {
                days == 1 -> "Daily"
                days == 7 -> "Weekly"
                days == 14 -> "Every 2 weeks"
                days in 28..31 -> "Monthly"
                days < 7 -> "Every $days days"
                days < 14 -> "About weekly"
                days < 28 -> "Every ${days / 7} weeks"
                else -> "Every ${days / 30} months"
            }
        }

    companion object {
        /**
         * Minimum confidence threshold for suggestions.
         */
        const val SUGGESTION_CONFIDENCE_THRESHOLD = 0.6

        /**
         * Creates an insufficient data result for the given item.
         */
        fun insufficientData(itemId: String): PredictionResult =
            PredictionResult(
                itemId = itemId,
                predictedDaysUntilNeeded = null,
                confidence = 0.0,
                source = PredictionSource.INSUFFICIENT_DATA,
            )
    }
}
