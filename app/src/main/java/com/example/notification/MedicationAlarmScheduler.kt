package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.AppDatabase
import com.example.data.DoseRecord
import com.example.receiver.MedicationAlarmReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object MedicationAlarmScheduler {

    private const val TAG = "MedicationAlarmScheduler"

    /**
     * 指定された服薬レコードの服薬時刻にアラームを設定します。
     * 服薬時刻が未来であり、かつ未服用の場合のみ設定します。
     */
    fun scheduleAlarmForRecord(context: Context, record: DoseRecord) {
        if (record.isTaken) {
            cancelAlarmForRecord(context, record.id)
            return
        }

        val dateTimeStr = "${record.date} ${record.scheduledTime}"
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.JAPAN)
        val triggerDate = try {
            sdf.parse(dateTimeStr)
        } catch (e: Exception) {
            null
        } ?: return

        val triggerTimeMillis = triggerDate.time
        val nowMillis = System.currentTimeMillis()

        // 過去の時刻には設定しない（ただし直近1分以内であれば猶予）
        if (triggerTimeMillis <= nowMillis) {
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            action = MedicationAlarmReceiver.ACTION_MEDICATION_DOSE_ALARM
            putExtra(MedicationAlarmReceiver.EXTRA_DOSE_RECORD_ID, record.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            record.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled alarm for record ${record.id} (${record.medicineName}) at $dateTimeStr")
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException on exact alarm, falling back to setAndAllowWhileIdle", e)
            try {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to schedule alarm", ex)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule alarm for record ${record.id}", e)
        }
    }

    /**
     * 指定された服薬レコードのアラームを解除します。
     */
    fun cancelAlarmForRecord(context: Context, recordId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            action = MedicationAlarmReceiver.ACTION_MEDICATION_DOSE_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            recordId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Cancelled alarm for record $recordId")
        }
    }

    /**
     * 今日から今後7日間の未服用レコードすべてのアラームを再設定します。
     * アプリ起動時、お薬更新時、端末起動時（Boot完了時）に実行されます。
     */
    fun rescheduleAllUpcomingAlarms(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN)
                val todayCal = Calendar.getInstance()
                val todayStr = sdf.format(todayCal.time)

                // 7日後までの日付
                todayCal.add(Calendar.DAY_OF_MONTH, 7)
                val endStr = sdf.format(todayCal.time)

                val upcomingRecords = db.doseRecordDao().getUpcomingUntakenRecords(todayStr, endStr)
                for (record in upcomingRecords) {
                    scheduleAlarmForRecord(context, record)
                }
                Log.d(TAG, "Rescheduled ${upcomingRecords.size} upcoming alarms")
            } catch (e: Exception) {
                Log.e(TAG, "Error rescheduling alarms", e)
            }
        }
    }
}
