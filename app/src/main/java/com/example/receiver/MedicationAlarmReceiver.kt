package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.AppDatabase
import com.example.notification.MedicationAlarmScheduler
import com.example.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MedicationAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_MEDICATION_DOSE_ALARM = "com.example.ACTION_MEDICATION_DOSE_ALARM"
        const val EXTRA_DOSE_RECORD_ID = "extra_dose_record_id"
        private const val TAG = "MedicationAlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "Received action: $action")

        when (action) {
            ACTION_MEDICATION_DOSE_ALARM -> {
                val recordId = intent.getLongExtra(EXTRA_DOSE_RECORD_ID, -1L)
                if (recordId == -1L) return

                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = AppDatabase.getDatabase(context)
                        val record = db.doseRecordDao().getRecordById(recordId)
                        if (record != null && !record.isTaken) {
                            Log.d(TAG, "Triggering notification for record: ${record.id} (${record.medicineName})")
                            NotificationHelper.showMedicationNotification(context, record)
                        } else {
                            Log.d(TAG, "Record $recordId is either taken or not found")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error handling medication alarm", e)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }

            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        Log.d(TAG, "System event $action: rescheduling all upcoming alarms")
                        MedicationAlarmScheduler.rescheduleAllUpcomingAlarms(context)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }
}
