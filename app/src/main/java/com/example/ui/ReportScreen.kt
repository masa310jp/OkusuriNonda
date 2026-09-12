package com.example.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DoseRecord
import com.example.data.Medicine
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.GreenSuccessLight
import com.example.ui.theme.TealLight
import com.example.ui.theme.TealPrimary
import com.example.util.PdfReportGenerator
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportScreen(
    viewModel: MedicationViewModel,
    medicines: List<Medicine>,
    allRecords: List<DoseRecord>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val savedUserName by viewModel.userName.collectAsStateWithLifecycle()
    var patientName by remember { mutableStateOf(savedUserName) }

    LaunchedEffect(savedUserName) {
        patientName = savedUserName
    }

    var selectedPeriodMonths by remember { mutableIntStateOf(1) } // 1, 2, or 3 months
    var isGenerating by remember { mutableStateOf(false) }
    var lastGeneratedPdf by remember { mutableStateOf<File?>(null) }

    val totalDoses = allRecords.size
    val takenDoses = allRecords.count { it.isTaken }
    val adherencePercent = if (totalDoses > 0) (takenDoses * 100 / totalDoses) else 100

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Overview Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "服薬実績レポート（PDF出力）",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "医師の診察時や薬局でのお薬手帳確認、離れて暮らすご家族への連絡用に、服用実績を綺麗なPDF形式で出力してメール送信できます。",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        // Summary Stats Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TealLight),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("服薬達成率", fontSize = 13.sp, color = TealPrimary)
                        Text(
                            "$adherencePercent%",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("服用回数", fontSize = 13.sp, color = TealPrimary)
                        Text(
                            "$takenDoses / $totalDoses 回",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("登録薬剤", fontSize = 13.sp, color = TealPrimary)
                        Text(
                            "${medicines.size} 種類",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary
                        )
                    }
                }
            }
        }

        // Patient Name Input
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. レポートに記載するお名前（任意）",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = patientName,
                        onValueChange = {
                            patientName = it
                            viewModel.setUserName(it)
                        },
                        label = { Text("患者様氏名 / 服薬者") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("report_patient_name_input")
                    )
                }
            }
        }

        // Period Selector
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. 出力する対象期間",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(1 to "1ヶ月分", 2 to "2ヶ月分", 3 to "3ヶ月分").forEach { (months, label) ->
                            val isSelected = selectedPeriodMonths == months
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedPeriodMonths = months },
                                label = {
                                    Text(
                                        text = label,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TealPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Export & Send Action Button
        item {
            Button(
                onClick = {
                    scope.launch {
                        isGenerating = true
                        try {
                            val pdfFile = viewModel.createPdfReport(
                                context = context,
                                patientName = patientName,
                                periodMonths = selectedPeriodMonths
                            )
                            lastGeneratedPdf = pdfFile

                            // Launch Email / Share Chooser
                            val periodLabel = "${selectedPeriodMonths}ヶ月分"
                            val emailIntent = PdfReportGenerator.createEmailIntent(
                                context = context,
                                pdfFile = pdfFile,
                                patientName = patientName,
                                period = periodLabel
                            )
                            context.startActivity(
                                Intent.createChooser(emailIntent, "医療機関・ご家族へPDFレポートを送信")
                            )
                        } catch (e: Exception) {
                            Toast.makeText(context, "PDF作成に失敗しました: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isGenerating = false
                        }
                    }
                },
                enabled = !isGenerating,
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                shape = RoundedCornerShape(14.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .testTag("generate_and_email_pdf_button")
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("PDFを作成中...", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "📄 PDFをメールで送信",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // View PDF Button
        if (lastGeneratedPdf != null) {
            item {
                OutlinedButton(
                    onClick = {
                        try {
                            val viewIntent = PdfReportGenerator.createViewIntent(context, lastGeneratedPdf!!)
                            context.startActivity(viewIntent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "PDFビューワーが見つかりませんでした", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, TealPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "作成したPDFを端末でプレビュー表示",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary
                    )
                }
            }
        }

        // Report Guide Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalHospital,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PDFレポートに含まれる内容",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "・患者様のお名前、集計対象期間\n・服薬達成率と総回数サマリー\n・登録中の全薬剤（錠剤・注射・吸入等）の用法・用量・現在庫数\n・日別の服用日時実績（○月○日 ○時○分 服用済）\n・医師・薬剤師・ご家族の確認サイン欄",
                        fontSize = 13.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}
