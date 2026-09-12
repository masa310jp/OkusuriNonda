package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.data.AppDatabase
import com.example.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MedicationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_MARK_TAKEN = "com.example.ACTION_MARK_TAKEN"
        const val EXTRA_DOSE_RECORD_ID = "extra_dose_record_id"
        const val EXTRA_MEDICINE_ID = "extra_medicine_id"
        const val EXTRA_MEDICINE_NAME = "extra_medicine_name"
        const val EXTRA_DOSE_AMOUNT = "extra_dose_amount"
        const val EXTRA_UNIT = "extra_unit"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_MARK_TAKEN) {
            val recordId = intent.getLongExtra(EXTRA_DOSE_RECORD_ID, -1L)
            val medicineId = intent.getLongExtra(EXTRA_MEDICINE_ID, -1L)
            val medicineName = intent.getStringExtra(EXTRA_MEDICINE_NAME) ?: "お薬"
            val doseAmount = intent.getDoubleExtra(EXTRA_DOSE_AMOUNT, 1.0)
            val unit = intent.getStringExtra(EXTRA_UNIT) ?: "錠"

            if (recordId == -1L || medicineId == -1L) return

            val pendingResult = goAsync()
            val database = AppDatabase.getDatabase(context)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Mark dose as taken
                    database.doseRecordDao().markTaken(recordId, System.currentTimeMillis())

                    // Decrement medicine stock
                    val amountInt = doseAmount.toInt().coerceAtLeast(1)
                    database.medicineDao().decrementStock(medicineId, amountInt)

                    // Check updated medicine stock
                    val medicine = database.medicineDao().getMedicineById(medicineId)
                    if (medicine != null && medicine.isLowStock()) {
                        NotificationHelper.showLowStockNotification(context, medicine)
                    }

                    // Dismiss the notification
                    NotificationHelper.dismissNotification(context, recordId.toInt())

                    // Cancel pending alarm
                    com.example.notification.MedicationAlarmScheduler.cancelAlarmForRecord(context, recordId)

                    // Show Toast on main thread
                    Handler(Looper.getMainLooper()).post {
                        val remainingText = if (medicine != null) {
                            "（残り: ${medicine.remainingCount}${medicine.unit}）"
                        } else ""
                        Toast.makeText(
                            context,
                            "✓「$medicineName」の服用を記録しました$remainingText",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
