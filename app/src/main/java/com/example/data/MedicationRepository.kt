package com.example.data

import android.content.Context
import com.example.notification.MedicationAlarmScheduler
import com.example.notification.NotificationHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MedicationRepository(
    private val context: Context,
    database: AppDatabase,
) {
    private val medicineDao = database.medicineDao()
    private val doseRecordDao = database.doseRecordDao()

    val allActiveMedicines: Flow<List<Medicine>> = medicineDao.getAllActiveMedicines()
    val lowStockCount: Flow<Int> = medicineDao.getLowStockCount()

    fun getRecordsForDate(date: String): Flow<List<DoseRecord>> {
        return doseRecordDao.getRecordsForDate(date)
    }

    fun getRecordsForMonth(monthPrefix: String): Flow<List<DoseRecord>> {
        return doseRecordDao.getRecordsForMonth(monthPrefix)
    }

    fun getRecordsInRange(startDate: String, endDate: String): Flow<List<DoseRecord>> {
        return doseRecordDao.getRecordsInRange(startDate, endDate)
    }

    fun getAllRecords(): Flow<List<DoseRecord>> {
        return doseRecordDao.getAllRecords()
    }

    suspend fun ensureRecordsForDate(date: String) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).format(Date())
        if (date >= todayStr) {
            syncRecordsWithCurrentStock()
        } else {
            val activeMedicines = allActiveMedicines.first()
            for (med in activeMedicines) {
                if (med.isScheduledForDate(date)) {
                    for (time in med.scheduledTimes) {
                        val existing = doseRecordDao.getRecord(date, med.id, time)
                        if (existing == null) {
                            val record = DoseRecord(
                                medicineId = med.id,
                                medicineName = med.name,
                                medicineType = med.type,
                                unit = med.unit,
                                doseAmount = med.dosePerTime,
                                instructions = med.instructions,
                                date = date,
                                scheduledTime = time,
                                isTaken = false
                            )
                            doseRecordDao.insertRecord(record)
                        }
                    }
                } else {
                    for (time in med.scheduledTimes) {
                        val existing = doseRecordDao.getRecord(date, med.id, time)
                        if (existing != null && !existing.isTaken) {
                            doseRecordDao.deleteRecord(existing)
                        }
                    }
                }
            }
        }
    }

    suspend fun ensureRecordsForMonth(yearMonth: String) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).format(Date())
        val currentMonth = todayStr.substring(0, 7) // "yyyy-MM"

        // 常に最新の残薬に合わせて今日以降の予定を同期
        syncRecordsWithCurrentStock()

        // 過去月の場合のみ過去日チェック
        if (yearMonth < currentMonth) {
            val parts = yearMonth.split("-")
            val year = parts.getOrNull(0)?.toIntOrNull() ?: return
            val month = parts.getOrNull(1)?.toIntOrNull() ?: return
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month - 1)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

            for (day in 1..daysInMonth) {
                val dateStr = String.format(Locale.JAPAN, "%04d-%02d-%02d", year, month, day)
                if (dateStr < todayStr) {
                    ensureRecordsForDate(dateStr)
                }
            }
        }
    }

    /**
     * 各薬の残薬（remainingCount）に基づいて、今日以降の服薬予定レコードを同期します。
     * 残薬のない日（在庫切れ以降）には未服用レコードを作成せず、既存の未服用レコードがあれば削除します。
     */
    suspend fun syncRecordsWithCurrentStock() {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).format(Date())
        val activeMedicines = allActiveMedicines.first()

        for (med in activeMedicines) {
            // 残薬が0以下、または定期服用ではない（頓服）場合
            if (med.remainingCount <= 0 || med.frequencyType == FrequencyType.AS_NEEDED) {
                doseRecordDao.deleteUntakenRecordsFromDate(med.id, todayStr)
                continue
            }

            var stockLeft = med.remainingCount
            val doseAmountInt = med.dosePerTime.toInt().coerceAtLeast(1)

            val cal = Calendar.getInstance()
            val parts = todayStr.split("-")
            val y = parts[0].toInt()
            val m = parts[1].toInt() - 1
            val d = parts[2].toInt()
            cal.set(y, m, d)

            val allowedSlots = mutableSetOf<Pair<String, String>>()
            var daysChecked = 0

            // 最大365日先まで確認（残薬が尽きたら終了）
            while (stockLeft >= doseAmountInt && daysChecked < 365) {
                val curDateStr = String.format(
                    Locale.JAPAN,
                    "%04d-%02d-%02d",
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.DAY_OF_MONTH)
                )

                if (med.isScheduledForDate(curDateStr)) {
                    for (time in med.scheduledTimes) {
                        if (curDateStr == todayStr) {
                            val existing = doseRecordDao.getRecord(curDateStr, med.id, time)
                            if (existing != null && existing.isTaken) {
                                allowedSlots.add(curDateStr to time)
                                continue
                            }
                        }

                        if (stockLeft >= doseAmountInt) {
                            allowedSlots.add(curDateStr to time)
                            stockLeft -= doseAmountInt

                            val existing = doseRecordDao.getRecord(curDateStr, med.id, time)
                            if (existing == null) {
                                doseRecordDao.insertRecord(
                                    DoseRecord(
                                        medicineId = med.id,
                                        medicineName = med.name,
                                        medicineType = med.type,
                                        unit = med.unit,
                                        doseAmount = med.dosePerTime,
                                        instructions = med.instructions,
                                        date = curDateStr,
                                        scheduledTime = time,
                                        isTaken = false
                                    )
                                )
                            }
                        } else {
                            break
                        }
                    }
                }

                cal.add(Calendar.DAY_OF_MONTH, 1)
                daysChecked++
            }

            // 今日以降で、残薬の範囲外（allowedSlots に含まれない）未服用レコードを削除
            val untakenRecords = doseRecordDao.getUntakenRecordsFromDate(med.id, todayStr)
            for (rec in untakenRecords) {
                if (!allowedSlots.contains(rec.date to rec.scheduledTime)) {
                    doseRecordDao.deleteRecord(rec)
                }
            }
        }

        // 予定が同期されたため、未来の服薬アラームを再スケジュール
        MedicationAlarmScheduler.rescheduleAllUpcomingAlarms(context)
    }

    suspend fun markDoseTaken(recordId: Long, takenTime: Long = System.currentTimeMillis()) {
        val record = doseRecordDao.getRecordById(recordId) ?: return
        if (!record.isTaken) {
            doseRecordDao.markTaken(recordId, takenTime)
            MedicationAlarmScheduler.cancelAlarmForRecord(context, recordId)
            val doseAmountInt = record.doseAmount.toInt().coerceAtLeast(1)
            medicineDao.decrementStock(record.medicineId, doseAmountInt)

            // 残薬が変化したため、残薬数に合わせて未来の予定を同期
            syncRecordsWithCurrentStock()

            // Check if stock became low
            val med = medicineDao.getMedicineById(record.medicineId)
            if (med != null && med.isLowStock()) {
                NotificationHelper.showLowStockNotification(context, med)
            }
        }
    }

    suspend fun markDoseUntaken(recordId: Long) {
        val record = doseRecordDao.getRecordById(recordId) ?: return
        if (record.isTaken) {
            doseRecordDao.markUntaken(recordId)
            val doseAmountInt = record.doseAmount.toInt().coerceAtLeast(1)
            medicineDao.addStock(record.medicineId, doseAmountInt)

            // 残薬が戻ったため、残薬数に合わせて未来の予定を同期
            syncRecordsWithCurrentStock()
        }
    }

    suspend fun markAllDosesTakenForDate(date: String) {
        val records = doseRecordDao.getRecordsForDate(date).first()
        val now = System.currentTimeMillis()
        for (rec in records) {
            if (!rec.isTaken) {
                markDoseTaken(rec.id, now)
            }
        }
    }

    suspend fun addMedicine(medicine: Medicine): Long {
        val id = medicineDao.insertMedicine(medicine)
        val savedMed = medicine.copy(id = id)
        if (savedMed.startDate.isNotBlank()) {
            doseRecordDao.deleteUntakenRecordsBeforeDate(savedMed.id, savedMed.startDate)
        }
        // 残薬（在庫数）に合わせて未来の服薬予定を同期
        syncRecordsWithCurrentStock()
        return id
    }

    suspend fun updateMedicine(medicine: Medicine) {
        medicineDao.updateMedicine(medicine)
        if (medicine.startDate.isNotBlank()) {
            doseRecordDao.deleteUntakenRecordsBeforeDate(medicine.id, medicine.startDate)
        }
        syncRecordsWithCurrentStock()
    }

    /**
     * 頓服薬（必要時のみ服用）を服用したとして記録
     */
    suspend fun recordAsNeededDose(
        medicine: Medicine,
        note: String = "",
        customTimeStr: String? = null,
        customDateStr: String? = null
    ) {
        val now = System.currentTimeMillis()
        val todayStr = customDateStr ?: SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).format(Date(now))
        val timeStr = customTimeStr ?: SimpleDateFormat("HH:mm", Locale.JAPAN).format(Date(now))

        val takenTimeMillis = try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.JAPAN)
            sdf.parse("$todayStr $timeStr")?.time ?: now
        } catch (_: Exception) {
            now
        }

        val record = DoseRecord(
            medicineId = medicine.id,
            medicineName = medicine.name,
            medicineType = medicine.type,
            unit = medicine.unit,
            doseAmount = medicine.dosePerTime,
            instructions = medicine.instructions,
            date = todayStr,
            scheduledTime = timeStr,
            isTaken = true,
            takenTimeMillis = takenTimeMillis,
            note = note
        )
        doseRecordDao.insertRecord(record)

        val doseAmountInt = medicine.dosePerTime.toInt().coerceAtLeast(1)
        medicineDao.decrementStock(medicine.id, doseAmountInt)
        syncRecordsWithCurrentStock()

        val updatedMed = medicineDao.getMedicineById(medicine.id)
        if (updatedMed != null && updatedMed.isLowStock()) {
            NotificationHelper.showLowStockNotification(context, updatedMed)
        }
    }

    suspend fun deleteMedicine(medicine: Medicine) {
        medicineDao.deleteMedicine(medicine)
        doseRecordDao.deleteRecordsForMedicine(medicine.id)
        syncRecordsWithCurrentStock()
    }

    suspend fun addMedicineStock(medicineId: Long, amount: Int) {
        medicineDao.addStock(medicineId, amount)
        syncRecordsWithCurrentStock()
    }

    suspend fun updateMedicineStock(medicineId: Long, newStock: Int) {
        medicineDao.updateStock(medicineId, newStock)
        syncRecordsWithCurrentStock()
    }

    fun testTriggerNotification(record: DoseRecord) {
        NotificationHelper.showMedicationNotification(context, record)
    }

    fun testTriggerLowStockNotification(medicine: Medicine) {
        NotificationHelper.showLowStockNotification(context, medicine)
    }
}
