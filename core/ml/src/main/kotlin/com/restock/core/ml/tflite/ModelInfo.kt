package com.restock.core.ml.tflite

/**
 * Metadata about a loaded TensorFlow Lite model.
 *
 * This information is extracted from the model's tensor specifications
 * and can be used to validate input data and understand output format.
 *
 * @property inputShape The shape of the model's input tensor (e.g., [1, 10] for batch of 10 features).
 * @property outputShape The shape of the model's output tensor (e.g., [1, 1] for single prediction).
 * @property inputDataType The data type of input tensor (e.g., "FLOAT32").
 * @property outputDataType The data type of output tensor (e.g., "FLOAT32").
 */
data class ModelInfo(
    val inputShape: List<Int>,
    val outputShape: List<Int>,
    val inputDataType: String,
    val outputDataType: String,
) {
    /**
     * Expected number of input features (total elements in input tensor).
     */
    val expectedInputSize: Int
        get() = inputShape.reduce { acc, dim -> acc * dim }

    /**
     * Number of output values produced by the model.
     */
    val outputSize: Int
        get() = outputShape.reduce { acc, dim -> acc * dim }

    /**
     * Whether the model expects batch input (first dimension represents batch size).
     */
    val isBatchModel: Boolean
        get() = inputShape.isNotEmpty() && inputShape.first() == 1

    /**
     * String representation for logging/debugging.
     */
    override fun toString(): String =
        "ModelInfo(input=$inputShape[$inputDataType], output=$outputShape[$outputDataType])"
}
