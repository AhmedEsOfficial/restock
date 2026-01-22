package com.restock.core.data.database.converter

import androidx.room.TypeConverter
import java.time.Instant

/**
 * Room type converters for complex data types.
 */
class Converters {

    /**
     * Convert Instant to Long for storage.
     */
    @TypeConverter
    fun fromInstant(instant: Instant?): Long? {
        return instant?.toEpochMilli()
    }

    /**
     * Convert Long to Instant for retrieval.
     */
    @TypeConverter
    fun toInstant(epochMilli: Long?): Instant? {
        return epochMilli?.let { Instant.ofEpochMilli(it) }
    }

    /**
     * Convert list of strings to comma-separated string.
     */
    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        return list?.joinToString(",")
    }

    /**
     * Convert comma-separated string to list of strings.
     */
    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.split(",")?.filter { it.isNotEmpty() }
    }
}
