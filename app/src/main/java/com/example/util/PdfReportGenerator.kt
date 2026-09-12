package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.DoseRecord
import com.example.data.Medicine
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    data class ReportData(
        val patientName: String,
        val startDate: String,
        val endDate: String,
        val medicines: List<Medicine>,
        val records: List<DoseRecord>
    )

    fun generatePdf(context: Context, data: ReportData): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size in points
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
        }

        val primaryColor = Color.rgb(0, 105, 92) // Teal
        val secondaryColor = Color.rgb(224, 242, 241) // Light teal
        val grayBorder = Color.rgb(200, 200, 200)
        val grayText = Color.rgb(100, 100, 100)
        val greenSuccess = Color.rgb(46, 125, 50)
        val orangeWarning = Color.rgb(230, 81, 0)

        var y = 40f
        val leftMargin = 36f
        val rightMargin = 559f
        val contentWidth = rightMargin - leftMargin

        // Header Background Banner
        val headerRect = RectF(leftMargin, y, rightMargin, y + 55f)
        paint.color = primaryColor
        canvas.drawRoundRect(headerRect, 8f, 8f, paint)

        // Header Title
        paint.color = Color.WHITE
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("【服薬管理・お薬手帳 実績レポート】", leftMargin + 16f, y + 34f, paint)

        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT
        val todayStr = SimpleDateFormat("yyyy年MM月dd日", Locale.JAPAN).format(Date())
        val dateText = "作成日: $todayStr"
        val dateWidth = paint.measureText(dateText)
        canvas.drawText(dateText, rightMargin - dateWidth - 16f, y + 34f, paint)

        y += 75f

        // Patient & Period Info Card
        val infoRect = RectF(leftMargin, y, rightMargin, y + 55f)
        paint.color = secondaryColor
        canvas.drawRoundRect(infoRect, 6f, 6f, paint)

        paint.color = primaryColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(infoRect, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.rgb(30, 30, 30)
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val displayName = if (data.patientName.isNotBlank()) "${data.patientName} 様" else "ご本人 様"
        canvas.drawText("患者氏名: $displayName", leftMargin + 14f, y + 25f, paint)

        paint.textSize = 11f
        paint.typeface = Typeface.DEFAULT
        paint.color = grayText
        canvas.drawText("対象期間: ${data.startDate} 〜 ${data.endDate}", leftMargin + 14f, y + 43f, paint)

        // Adherence Rate Summary
        val totalScheduled = data.records.size
        val totalTaken = data.records.count { it.isTaken }
        val adherenceRate = if (totalScheduled > 0) (totalTaken * 100.0 / totalScheduled) else 100.0
        val adherenceStr = String.format(Locale.JAPAN, "%.1f%%", adherenceRate)

        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = if (adherenceRate >= 85) greenSuccess else orangeWarning
        val adhText = "服薬達成率: $adherenceStr ($totalTaken / $totalScheduled 回)"
        val adhWidth = paint.measureText(adhText)
        canvas.drawText(adhText, rightMargin - adhWidth - 14f, y + 34f, paint)

        y += 75f

        // Section: Registered Medicines
        paint.color = primaryColor
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("■ 登録薬剤・服用指示一覧（残薬情報含む）", leftMargin, y, paint)
        y += 12f

        // Medicines Table Header
        paint.color = Color.rgb(240, 240, 240)
        val medHeaderRect = RectF(leftMargin, y, rightMargin, y + 20f)
        canvas.drawRect(medHeaderRect, paint)

        paint.color = Color.BLACK
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("薬剤名", leftMargin + 6f, y + 14f, paint)
        canvas.drawText("区分", leftMargin + 180f, y + 14f, paint)
        canvas.drawText("用法・用量", leftMargin + 250f, y + 14f, paint)
        canvas.drawText("服用時間", leftMargin + 370f, y + 14f, paint)
        canvas.drawText("現在庫", leftMargin + 450f, y + 14f, paint)
        y += 20f

        paint.typeface = Typeface.DEFAULT
        paint.textSize = 9f

        for (med in data.medicines.take(6)) {
            paint.color = Color.WHITE
            val rowRect = RectF(leftMargin, y, rightMargin, y + 18f)
            canvas.drawRect(rowRect, paint)

            paint.color = grayBorder
            paint.strokeWidth = 0.5f
            canvas.drawLine(leftMargin, y + 18f, rightMargin, y + 18f, paint)

            paint.color = Color.rgb(20, 20, 20)
            val truncatedName = if (med.name.length > 18) med.name.substring(0, 17) + "…" else med.name
            canvas.drawText(truncatedName, leftMargin + 6f, y + 13f, paint)
            canvas.drawText(med.type.displayName, leftMargin + 180f, y + 13f, paint)

            val freqDesc = med.getFrequencyDescription()
            val doseDisplay = if (med.dosePerTime % 1.0 == 0.0) "${med.dosePerTime.toInt()}${med.unit}" else "${med.dosePerTime}${med.unit}"
            val instructionWithFreq = if (freqDesc == "毎日") "1回 $doseDisplay" else "1回 $doseDisplay [$freqDesc]"
            val truncatedInstruction = if (instructionWithFreq.length > 20) instructionWithFreq.substring(0, 19) + "…" else instructionWithFreq
            canvas.drawText(truncatedInstruction, leftMargin + 250f, y + 13f, paint)
            val timeText = if (med.scheduledTimes.isNotEmpty()) med.scheduledTimes.joinToString("・") else "頓服 (適時)"
            canvas.drawText(timeText, leftMargin + 370f, y + 13f, paint)

            paint.color = if (med.isLowStock()) orangeWarning else Color.BLACK
            val stockText = "${med.remainingCount}${med.unit}" + if (med.isLowStock()) " (要補充)" else ""
            canvas.drawText(stockText, leftMargin + 450f, y + 13f, paint)

            y += 18f
        }

        y += 20f

        // Section: Detailed Intake Records
        paint.color = primaryColor
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("■ 服薬実績記録（直近履歴）", leftMargin, y, paint)
        y += 12f

        // Records Table Header
        paint.color = Color.rgb(240, 240, 240)
        val recHeaderRect = RectF(leftMargin, y, rightMargin, y + 20f)
        canvas.drawRect(recHeaderRect, paint)

        paint.color = Color.BLACK
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("日付", leftMargin + 6f, y + 14f, paint)
        canvas.drawText("時刻", leftMargin + 85f, y + 14f, paint)
        canvas.drawText("薬剤名", leftMargin + 135f, y + 14f, paint)
        canvas.drawText("区分", leftMargin + 300f, y + 14f, paint)
        canvas.drawText("服用状態", leftMargin + 370f, y + 14f, paint)
        canvas.drawText("記録時間", leftMargin + 450f, y + 14f, paint)
        y += 20f

        paint.typeface = Typeface.DEFAULT
        paint.textSize = 9f

        val timeFormat = SimpleDateFormat("HH:mm", Locale.JAPAN)
        // Show up to 16 recent records to fit neatly on page
        val recordsToShow = data.records.take(16)
        for (rec in recordsToShow) {
            paint.color = Color.WHITE
            val rowRect = RectF(leftMargin, y, rightMargin, y + 18f)
            canvas.drawRect(rowRect, paint)

            paint.color = grayBorder
            paint.strokeWidth = 0.5f
            canvas.drawLine(leftMargin, y + 18f, rightMargin, y + 18f, paint)

            paint.color = Color.rgb(30, 30, 30)
            canvas.drawText(rec.date, leftMargin + 6f, y + 13f, paint)
            canvas.drawText(rec.scheduledTime, leftMargin + 85f, y + 13f, paint)

            val truncatedMed = if (rec.medicineName.length > 16) rec.medicineName.substring(0, 15) + "…" else rec.medicineName
            canvas.drawText(truncatedMed, leftMargin + 135f, y + 13f, paint)
            canvas.drawText(rec.medicineType.displayName, leftMargin + 300f, y + 13f, paint)

            if (rec.isTaken) {
                paint.color = greenSuccess
                canvas.drawText("✓ 服用済", leftMargin + 370f, y + 13f, paint)
                paint.color = grayText
                val takenAtStr = rec.takenTimeMillis?.let { timeFormat.format(Date(it)) } ?: "--:--"
                canvas.drawText(takenAtStr, leftMargin + 450f, y + 13f, paint)
            } else {
                paint.color = Color.RED
                canvas.drawText("× 未服用", leftMargin + 370f, y + 13f, paint)
                paint.color = grayText
                canvas.drawText("-", leftMargin + 450f, y + 13f, paint)
            }

            y += 18f
        }

        y += 25f

        // Doctor / Family Memo & Signature Box
        val memoRect = RectF(leftMargin, y, rightMargin, y + 70f)
        paint.color = Color.rgb(250, 250, 250)
        canvas.drawRoundRect(memoRect, 6f, 6f, paint)

        paint.color = grayBorder
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(memoRect, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        paint.color = primaryColor
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("【医療機関・薬局・ご家族 確認欄】", leftMargin + 10f, y + 18f, paint)

        paint.color = grayText
        paint.textSize = 9f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("確認日: 202    年    月    日      確認者印 / 署名: ____________________", leftMargin + 10f, y + 36f, paint)
        canvas.drawText("特記事項・処方メモ: ", leftMargin + 10f, y + 54f, paint)

        // Footer
        paint.color = grayText
        paint.textSize = 8.5f
        canvas.drawText("※ 本レポートは服薬管理アプリ「お薬飲んだ？」により出力された実績データです。", leftMargin, 815f, paint)

        document.finishPage(page)

        // Save to file
        val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.JAPAN).format(Date())
        val pdfFile = File(reportsDir, "medication_report_${timeStamp}.pdf")

        FileOutputStream(pdfFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        return pdfFile
    }

    fun getFileUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun createEmailIntent(
        context: Context,
        pdfFile: File,
        patientName: String,
        period: String
    ): Intent {
        val uri = getFileUri(context, pdfFile)
        val nameDisplay = if (patientName.isNotBlank()) "${patientName}様" else "ご本人"

        val subject = "【服薬実績レポート】$nameDisplay の服薬記録 ($period)"
        val body = """
            医療機関・ご家族の皆様へ

            服薬管理アプリ「お薬飲んだ？」より出力された服薬実績レポート（PDF）をお送りいたします。

            ■ 患者氏名: $nameDisplay
            ■ 対象期間: $period

            添付のPDFファイルをご確認いただきますようお願い申し上げます。
            残薬の確認や次回の処方・診察の参考にご活用ください。
        """.trimIndent()

        return Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun createViewIntent(context: Context, pdfFile: File): Intent {
        val uri = getFileUri(context, pdfFile)
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
