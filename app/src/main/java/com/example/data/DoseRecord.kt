package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dose_records",
    indices = [Index(value = ["date", "medicineId", "scheduledTime"], unique = true)]
)
data class DoseRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicineId: Long,
    val medicineName: String,
    val medicineType: MedicineType,
    val unit: String,
    val doseAmount: Double,
    val instructions: String = "",
    val date: String, // "YYYY-MM-DD"
    val scheduledTime: String, // "HH:mm" e.g. "08:00"
    val isTaken: Boolean = false,
    val takenTimeMillis: Long? = null,
    val note: String = ""
)
