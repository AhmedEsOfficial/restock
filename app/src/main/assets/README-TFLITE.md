# TensorFlow Lite Model Placement

## Overview

This directory is for TensorFlow Lite model files used by the Restock pattern learning engine.

## Model Requirements

For the pattern prediction model (`pattern_model.tflite`):

- **Input Shape**: `[1, 10]` - Batch of 1, 10 purchase interval values (in days)
- **Output Shape**: `[1, 1]` - Single predicted days until next purchase
- **Data Type**: `FLOAT32` for both input and output

## MVP Notes

For MVP (Sprint 5-6), the TFLite infrastructure is set up but the **statistical model is the primary prediction method**. The system gracefully falls back to statistical calculation when:

1. No model file is present
2. Model fails to load
3. Model inference fails
4. Insufficient purchase history for ML (< 5 samples)

## Future Model Placement

When a trained model is ready:

1. Export the model as `pattern_model.tflite`
2. Place the file in this directory (`app/src/main/assets/`)
3. The `TFLiteModelManager` will automatically load it on initialization
4. Update the model version via `ModelVersionManager` if using versioning

## Model Size Guidelines

- Keep model size < 5MB for optimal app size
- Consider model quantization for smaller footprint
- GPU delegate support is available but optional (CPU is sufficient for this use case)

## Testing

The system can be tested without a model file - the statistical fallback ensures all functionality works correctly.
