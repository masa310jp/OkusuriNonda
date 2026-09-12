package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicines")
data class Medicine(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: MedicineType = MedicineType.TABLET,
    val unit: String = "錠",
    val dosePerTime: Double = 1.0,
    val instructions: String = "",
    val scheduledTimes: List<String> = listOf("08:00"), // e.g. ["08:00", "19:00"]
    val remainingCount: Int = 14,
    val alertThreshold: Int = 4, // alerts when remainingCount <= alertThreshold
    val hospitalClinic: String = "",
    val memo: String = "",
    val isActive: Boolean = true,
    val frequencyType: FrequencyType = FrequencyType.EVERY_DAY,
    val intervalDays: Int = 1, // 2 = 隔日(2日ごと), 3 = 3日ごと, etc.
    val daysOfWeek: List<Int> = emptyList(), // Calendar.MONDAY(2), etc.
    val startDate: String = "", // "yyyy-MM-dd", 基準日（間隔計算用）
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Checks if medicine stock is low or critical.
     */
    fun isLowStock(): Boolean {
        return isActive && remainingCount <= alertThreshold
    }

    /**
     * 指定された日付 (yyyy-MM-dd) が服薬予定日であるかどうかを判定
     */
    fun isScheduledForDate(dateStr: String): Boolean {
        if (!isActive) return false
        // 開始日より前の日付（開始日以前の過去日）には反映しない
        if (startDate.isNotBlank() && dateStr < startDate) {
            return false
        }
        return when (frequencyType) {
            FrequencyType.EVERY_DAY -> true
            FrequencyType.EVERY_X_DAYS -> {
                val baseDate = if (startDate.isNotBlank()) {
                    startDate
                } else {
                    java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.JAPAN)
                        .format(java.util.Date(createdAt))
                }
                val diffDays = WeekDayHelper.daysBetween(baseDate, dateStr)
                // 基準日以前は対象外（または基準日当日以降で日数間隔が割り切れる日）
                diffDays != null && diffDays >= 0 && (intervalDays <= 1 || diffDays % intervalDays == 0L)
            }
            FrequencyType.DAYS_OF_WEEK -> {
                if (daysOfWeek.isEmpty()) return true
                val day = WeekDayHelper.getDayOfWeek(dateStr)
                day != null && daysOfWeek.contains(day)
            }
            FrequencyType.AS_NEEDED -> false // 頓服薬は定期スケジュール対象外
        }
    }

    /**
     * 服用間隔を日本語で分かりやすく表示
     */
    fun getFrequencyDescription(): String {
        return when (frequencyType) {
            FrequencyType.EVERY_DAY -> "毎日"
            FrequencyType.EVERY_X_DAYS -> {
                when (intervalDays) {
                    1 -> "毎日"
                    else -> "${intervalDays}日おき"
                }
            }
            FrequencyType.DAYS_OF_WEEK -> WeekDayHelper.formatDaysOfWeek(daysOfWeek)
            FrequencyType.AS_NEEDED -> "頓服（必要時のみ）"
        }
    }

    /**
     * 次の服用予定日 (yyyy-MM-dd) を計算（本日以降直近）
     */
    fun getNextScheduledDate(fromDateStr: String): String? {
        if (frequencyType == FrequencyType.AS_NEEDED) return null
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.JAPAN)
        val cal = java.util.Calendar.getInstance()
        try {
            val date = sdf.parse(fromDateStr) ?: return null
            cal.time = date
            // 最大90日先まで探索
            for (i in 0..90) {
                val checkDateStr = sdf.format(cal.time)
                if (isScheduledForDate(checkDateStr)) {
                    return checkDateStr
                }
                cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
        } catch (_: Exception) {
            return null
        }
        return null
    }

    /**
     * 服用間隔を考慮した推定残日数
     */
    fun estimatedRemainingDays(): Int {
        val dailyDoseCount = scheduledTimes.size.coerceAtLeast(1)
        val amountPerDoseDay = dosePerTime * dailyDoseCount
        if (amountPerDoseDay <= 0) return remainingCount

        val effectiveDoseDays = (remainingCount / amountPerDoseDay).toInt()

        return when (frequencyType) {
            FrequencyType.EVERY_DAY -> effectiveDoseDays
            FrequencyType.EVERY_X_DAYS -> {
                val interval = intervalDays.coerceAtLeast(1)
                effectiveDoseDays * interval
            }
            FrequencyType.DAYS_OF_WEEK -> {
                val daysCount = daysOfWeek.size.coerceAtLeast(1)
                (effectiveDoseDays * 7.0 / daysCount).toInt()
            }
            FrequencyType.AS_NEEDED -> remainingCount // 頓服は服用回数分
        }
    }
}
