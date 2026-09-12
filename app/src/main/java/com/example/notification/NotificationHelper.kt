package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.DoseRecord
import com.example.data.Medicine
import com.example.receiver.MedicationActionReceiver

object NotificationHelper {

    const val CHANNEL_ID_REMINDER = "medication_reminder_channel"
    const val CHANNEL_ID_LOW_STOCK = "medication_low_stock_channel"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Medication Reminder Channel (High importance with sound and vibration)
            val reminderChannel = NotificationChannel(
                CHANNEL_ID_REMINDER,
                "服薬リマインダー",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "服薬時刻をお知らせする通知です"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400)
            }

            // Low Stock Alert Channel
            val lowStockChannel = NotificationChannel(
                CHANNEL_ID_LOW_STOCK,
                "残薬アラート",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "薬の残りが少なくなった際のお知らせ通知です"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(lowStockChannel)
        }
    }

    fun showMedicationNotification(
        context: Context,
        record: DoseRecord,
    ) {
        val notificationManager = NotificationManagerCompat.from(context)

        // Open app intent
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            record.id.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // "Taken" (服用完了) Action Intent
        val takenIntent = Intent(context, MedicationActionReceiver::class.java).apply {
            action = MedicationActionReceiver.ACTION_MARK_TAKEN
            putExtra(MedicationActionReceiver.EXTRA_DOSE_RECORD_ID, record.id)
            putExtra(MedicationActionReceiver.EXTRA_MEDICINE_ID, record.medicineId)
            putExtra(MedicationActionReceiver.EXTRA_MEDICINE_NAME, record.medicineName)
            putExtra(MedicationActionReceiver.EXTRA_DOSE_AMOUNT, record.doseAmount)
            putExtra(MedicationActionReceiver.EXTRA_UNIT, record.unit)
        }
        val takenPendingIntent = PendingIntent.getBroadcast(
            context,
            record.id.toInt(),
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val amountDisplay = if ((record.doseAmount % 1.0) == 0.0) {
            "${record.doseAmount.toInt()}${record.unit}"
        } else {
            "${record.doseAmount}${record.unit}"
        }

        val contentText = "${record.medicineName} ($amountDisplay)"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_REMINDER)
            .setSmallIcon(R.drawable.ic_notification_medication)
            .setContentTitle("【服薬のお時間です】${record.scheduledTime}")
            .setContentText(contentText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${record.medicineName} を服用する時刻です。\n服用量: $amountDisplay\n\n服用後に下の「服用完了」ボタンを押してください。"),
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(soundUri)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(
                android.R.drawable.checkbox_on_background,
                "✓ 服用完了にする",
                takenPendingIntent,
            )

        try {
            notificationManager.notify(record.id.toInt(), builder.build())
        } catch (_: SecurityException) {
            // Notifications permission not granted
        }
    }

    fun showLowStockNotification(
        context: Context,
        medicine: Medicine,
    ) {
        val notificationManager = NotificationManagerCompat.from(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            (medicine.id + 10000).toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val daysLeft = medicine.estimatedRemainingDays()
        val builder = NotificationCompat.Builder(context, CHANNEL_ID_LOW_STOCK)
            .setSmallIcon(R.drawable.ic_notification_medication)
            .setContentTitle("⚠️【残薬注意】${medicine.name}")
            .setContentText("残りあと${medicine.remainingCount}${medicine.unit}（約${daysLeft}日分）です。")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("「${medicine.name}」の残りが少なくなっています。\n現在庫: ${medicine.remainingCount}${medicine.unit}（約${daysLeft}日分）\n早めにかかりつけ医への受診や薬局での処方・補充を行ってください。"),
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            notificationManager.notify((medicine.id + 10000).toInt(), builder.build())
        } catch (_: SecurityException) {
            // Permission not granted
        }
    }

    fun dismissNotification(context: Context, id: Int) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(id)
    }
}
