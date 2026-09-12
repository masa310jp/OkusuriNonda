package com.example.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 服用間隔・頻度の区分
 */
enum class FrequencyType(val displayName: String, val description: String) {
    EVERY_DAY("毎日", "毎日決まった時間に服用"),
    EVERY_X_DAYS("何日おき", "2日おきや3日おきなど"),
    DAYS_OF_WEEK("曜日指定", "毎週決まった曜日に服用"),
    AS_NEEDED("頓服（必要時）", "痛みや症状がある時のみ服用");

    companion object {
        fun fromString(value: String?): FrequencyType {
            return try {
                if (value != null) valueOf(value) else EVERY_DAY
            } catch (_: Exception) {
                EVERY_DAY
            }
        }
    }
}

/**
 * 曜日の日本語表示用ヘルパー
 */
object WeekDayHelper {
    val ALL_WEEK_DAYS = listOf(
        Calendar.MONDAY to "月",
        Calendar.TUESDAY to "火",
        Calendar.WEDNESDAY to "水",
        Calendar.THURSDAY to "木",
        Calendar.FRIDAY to "金",
        Calendar.SATURDAY to "土",
        Calendar.SUNDAY to "日"
    )

    fun formatDaysOfWeek(days: List<Int>): String {
        if (days.isEmpty()) return "指定なし"
        if (days.size == 7) return "毎日"
        val map = mapOf(
            Calendar.MONDAY to "月",
            Calendar.TUESDAY to "火",
            Calendar.WEDNESDAY to "水",
            Calendar.THURSDAY to "木",
            Calendar.FRIDAY to "金",
            Calendar.SATURDAY to "土",
            Calendar.SUNDAY to "日"
        )
        val sortedDays = days.sortedBy {
            when (it) {
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY -> 4
                Calendar.FRIDAY -> 5
                Calendar.SATURDAY -> 6
                Calendar.SUNDAY -> 7
                else -> 8
            }
        }
        return "毎週 " + sortedDays.mapNotNull { map[it] }.joinToString("・") + "曜日"
    }

    fun getDayOfWeek(dateStr: String): Int? {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN)
            val date = sdf.parse(dateStr) ?: return null
            val cal = Calendar.getInstance()
            cal.time = date
            cal.get(Calendar.DAY_OF_WEEK)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * 2つの日付 (yyyy-MM-dd) の日数差 (date2 - date1) を算出
     */
    fun daysBetween(dateStr1: String, dateStr2: String): Long? {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN)
            val d1 = sdf.parse(dateStr1) ?: return null
            val d2 = sdf.parse(dateStr2) ?: return null

            val c1 = Calendar.getInstance().apply {
                time = d1
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val c2 = Calendar.getInstance().apply {
                time = d2
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val diffMillis = c2.timeInMillis - c1.timeInMillis
            diffMillis / (24 * 60 * 60 * 1000)
        } catch (_: Exception) {
            null
        }
    }
}
