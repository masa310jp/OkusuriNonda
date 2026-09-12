package com.example.data

import androidx.room.TypeConverter

class AppTypeConverters {

    @TypeConverter
    fun fromMedicineType(type: MedicineType?): String {
        return type?.name ?: MedicineType.TABLET.name
    }

    @TypeConverter
    fun toMedicineType(value: String?): MedicineType {
        return try {
            if (value != null) MedicineType.valueOf(value) else MedicineType.TABLET
        } catch (_: Exception) {
            MedicineType.TABLET
        }
    }

    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        return list?.joinToString(separator = ",") ?: ""
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    @TypeConverter
    fun fromFrequencyType(type: FrequencyType?): String {
        return type?.name ?: FrequencyType.EVERY_DAY.name
    }

    @TypeConverter
    fun toFrequencyType(value: String?): FrequencyType {
        return FrequencyType.fromString(value)
    }

    @TypeConverter
    fun fromIntList(list: List<Int>?): String {
        return list?.joinToString(separator = ",") ?: ""
    }

    @TypeConverter
    fun toIntList(value: String?): List<Int> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(",").mapNotNull { it.trim().toIntOrNull() }
    }
}
