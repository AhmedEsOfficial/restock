package com.restock.core.ml.tflite

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Extension property for model version DataStore.
 */
private val Context.modelVersionDataStore by preferencesDataStore(
    name = "model_versions",
)

/**
 * Manages ML model versioning using DataStore for persistence.
 *
 * This manager tracks:
 * - Current model version installed
 * - When the model was last updated
 * - Model metadata for validation
 *
 * Version management enables:
 * - Future OTA model updates
 * - Version-aware inference
 * - Rollback capability
 * - A/B testing of different model versions
 *
 * For MVP, the bundled model version (1) is the default.
 * Future versions may download updated models from a server.
 *
 * Thread Safety: All operations use DataStore which is thread-safe.
 */
@Singleton
class ModelVersionManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val dataStore = context.modelVersionDataStore

        /**
         * Gets the currently installed model version.
         *
         * @return Current model version, or 0 if no model has been installed.
         */
        suspend fun getCurrentVersion(): Int =
            dataStore.data
                .map { preferences ->
                    preferences[KEY_CURRENT_VERSION] ?: DEFAULT_VERSION
                }
                .first()

        /**
         * Sets the current model version.
         *
         * @param version The new version number to store.
         */
        suspend fun setCurrentVersion(version: Int) {
            dataStore.edit { preferences ->
                preferences[KEY_CURRENT_VERSION] = version
            }
        }

        /**
         * Checks if a model update is available.
         *
         * @param newVersion The version to check against current.
         * @return true if newVersion is higher than current version.
         */
        suspend fun shouldUpdateModel(newVersion: Int): Boolean {
            val current = getCurrentVersion()
            return newVersion > current
        }

        /**
         * Gets the timestamp of when the model was last updated.
         *
         * @return Timestamp in milliseconds, or null if never updated.
         */
        suspend fun getLastUpdateTimestamp(): Long? =
            dataStore.data
                .map { preferences ->
                    preferences[KEY_LAST_UPDATE_TIMESTAMP]
                }
                .first()

        /**
         * Records a model update with the current timestamp.
         *
         * @param version The new model version.
         * @param modelName The name of the model file (for tracking).
         */
        suspend fun recordModelUpdate(
            version: Int,
            modelName: String = TFLiteModelManager.DEFAULT_MODEL_NAME,
        ) {
            dataStore.edit { preferences ->
                preferences[KEY_CURRENT_VERSION] = version
                preferences[KEY_LAST_UPDATE_TIMESTAMP] = System.currentTimeMillis()
                preferences[KEY_MODEL_NAME] = modelName
            }
        }

        /**
         * Gets the name of the currently installed model.
         *
         * @return Model file name, or default if not set.
         */
        suspend fun getCurrentModelName(): String =
            dataStore.data
                .map { preferences ->
                    preferences[KEY_MODEL_NAME] ?: TFLiteModelManager.DEFAULT_MODEL_NAME
                }
                .first()

        /**
         * Observes the current model version as a Flow.
         *
         * @return Flow emitting version updates.
         */
        fun observeCurrentVersion(): Flow<Int> =
            dataStore.data.map { preferences ->
                preferences[KEY_CURRENT_VERSION] ?: DEFAULT_VERSION
            }

        /**
         * Clears all version data (for testing or reset).
         */
        suspend fun clearVersionData() {
            dataStore.edit { preferences ->
                preferences.remove(KEY_CURRENT_VERSION)
                preferences.remove(KEY_LAST_UPDATE_TIMESTAMP)
                preferences.remove(KEY_MODEL_NAME)
            }
        }

        /**
         * Gets comprehensive version information.
         *
         * @return VersionInfo containing all stored version data.
         */
        suspend fun getVersionInfo(): VersionInfo {
            val preferences = dataStore.data.first()
            return VersionInfo(
                version = preferences[KEY_CURRENT_VERSION] ?: DEFAULT_VERSION,
                lastUpdateTimestamp = preferences[KEY_LAST_UPDATE_TIMESTAMP],
                modelName = preferences[KEY_MODEL_NAME] ?: TFLiteModelManager.DEFAULT_MODEL_NAME,
            )
        }

        companion object {
            private val KEY_CURRENT_VERSION = intPreferencesKey("current_model_version")
            private val KEY_LAST_UPDATE_TIMESTAMP = longPreferencesKey("last_update_timestamp")
            private val KEY_MODEL_NAME = stringPreferencesKey("model_name")

            /**
             * Default/initial version (no model installed or bundled model).
             */
            const val DEFAULT_VERSION = 0

            /**
             * Version of the initial bundled model in MVP.
             */
            const val INITIAL_BUNDLED_VERSION = 1
        }
    }

/**
 * Comprehensive version information.
 *
 * @property version Current model version number.
 * @property lastUpdateTimestamp Timestamp of last update in milliseconds, null if never updated.
 * @property modelName Name of the model file.
 */
data class VersionInfo(
    val version: Int,
    val lastUpdateTimestamp: Long?,
    val modelName: String,
) {
    /**
     * Whether any model has been installed/registered.
     */
    val hasModel: Boolean
        get() = version > 0

    /**
     * Whether this is the initial bundled model.
     */
    val isBundledModel: Boolean
        get() = version == ModelVersionManager.INITIAL_BUNDLED_VERSION
}
