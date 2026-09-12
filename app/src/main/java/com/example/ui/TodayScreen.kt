package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.DoseRecord
import com.example.data.FrequencyType
import com.example.data.Medicine
import com.example.ui.theme.CoralLight
import com.example.ui.theme.CoralWarning
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.GreenSuccessLight
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.TealLight
import com.example.ui.theme.TealPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    viewModel: MedicationViewModel,
    records: List<DoseRecord>,
    lowStockMedicines: List<Medicine>,
    medicines: List<Medicine> = emptyList(),
    selectedDate: String,
    onNavigateToStock: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAsNeededDialog by remember { mutableStateOf(false) }
    var asNeededSelectedTime by remember {
        mutableStateOf(SimpleDateFormat("HH:mm", Locale.JAPAN).format(Date()))
    }
    var showTimePickerForAsNeeded by remember { mutableStateOf(false) }
    val totalDoses = records.size
    val takenDoses = records.count { it.isTaken }
    val isAllTaken = totalDoses > 0 && takenDoses == totalDoses

    val parsedDate = try {
        SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).parse(selectedDate) ?: Date()
    } catch (_: Exception) {
        Date()
    }

    val displayDateHeader = SimpleDateFormat("M月d日 (E)", Locale.JAPAN).format(parsedDate)
    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).format(Date())
    val isToday = selectedDate == todayStr

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Date Navigation Bar
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val cal = Calendar.getInstance().apply {
                                time = parsedDate
                                add(Calendar.DAY_OF_YEAR, -1)
                            }
                            viewModel.selectDate(SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).format(cal.time))
                        },
                        modifier = Modifier.size(48.dp).testTag("prev_day_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "前日へ",
                            tint = TealPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = displayDateHeader,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isToday) {
                            Surface(
                                color = TealPrimary.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = "本日のお薬",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TealPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                                )
                            }
                        } else {
                            Text(
                                text = "「今日」に戻る",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .clickable { viewModel.selectDate(todayStr) }
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            val cal = Calendar.getInstance().apply {
                                time = parsedDate
                                add(Calendar.DAY_OF_YEAR, 1)
                            }
                            viewModel.selectDate(SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).format(cal.time))
                        },
                        modifier = Modifier.size(48.dp).testTag("next_day_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "翌日へ",
                            tint = TealPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        // Return to Today prominent button when viewing another day
        if (!isToday) {
            item {
                Button(
                    onClick = { viewModel.resetToToday() },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("back_to_today_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Today,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "本日（今日）のお薬一覧に戻る",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // PRN / As-needed medicine quick record action if any exists
        val asNeededMeds = medicines.filter { it.frequencyType == FrequencyType.AS_NEEDED }
        if (asNeededMeds.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFB74D)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "💊 頓服薬（解熱剤など）",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100)
                            )
                            Text(
                                text = "頓服薬を飲んだら押して下さい👉",
                                fontSize = 12.sp,
                                color = Color(0xFF5D4037)
                            )
                        }
                        Button(
                            onClick = {
                                asNeededSelectedTime = SimpleDateFormat("HH:mm", Locale.JAPAN).format(Date())
                                showAsNeededDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("as_needed_quick_record_button")
                        ) {
                            Text("飲んだ", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Low Stock Alert Banner (Prominent Alert for Seniors)
        if (lowStockMedicines.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CoralLight),
                    border = BorderStroke(2.dp, CoralWarning),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("low_stock_banner")
                        .clickable { onNavigateToStock() }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(CoralWarning),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "残薬アラート",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "⚠️【残薬注意】お薬が残りわずかです",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFBF360C)
                            )
                            val names = lowStockMedicines.joinToString("、") { "${it.name} (残${it.remainingCount}${it.unit})" }
                            Text(
                                text = names,
                                fontSize = 14.sp,
                                color = Color(0xFF3E2723),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Text(
                                text = "タップして残薬確認・補充する >",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = CoralWarning,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Daily Progress Summary Card
        item {
            val isDark = LocalIsDarkTheme.current
            val cardBg = if (isAllTaken) {
                if (isDark) Color(0xFF1E462B) else Color(0xFFEDF7EE)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
            val cardBorder = if (isAllTaken) {
                BorderStroke(if (!isDark) 1.5.dp else 1.dp, if (isDark) Color(0xFF2E7D32) else Color(0xFF81C784))
            } else null

            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = cardBorder,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isAllTaken) Icons.Default.CheckCircle else Icons.Default.Today,
                            contentDescription = null,
                            tint = if (isAllTaken) (if (isDark) Color(0xFF81C784) else Color(0xFF43A047)) else TealPrimary,
                            modifier = Modifier.size(34.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isAllTaken) "✓ 本日はすべて服用完了済みです！💮" else "本日の服薬進捗",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAllTaken) (if (isDark) Color.White else Color(0xFF2E7D32)) else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "全 $totalDoses 回中 $takenDoses 回 服用完了",
                                fontSize = 15.sp,
                                fontWeight = if (isAllTaken) FontWeight.Medium else FontWeight.Normal,
                                color = if (isAllTaken) (if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF43A047)) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (!isAllTaken && totalDoses > 0) {
                        Button(
                            onClick = { viewModel.markAllTakenForSelectedDate() },
                            colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(46.dp)
                                .testTag("take_all_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "一括完了",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Section Title: Dose List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "服用予定一覧",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "時間順",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (records.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "この日の服用予定はありません",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "「お薬・残薬」タブからお薬を登録すると、ここに毎日の予定が表示されます。",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // 未服用の薬を時間順で上に、服用完了済みの薬を最下部に配置
            val sortedRecords = records.sortedWith(
                compareBy<DoseRecord> { it.isTaken }
                    .thenBy { it.scheduledTime }
            )
            items(sortedRecords, key = { it.id }) { record ->
                val med = medicines.find { it.id == record.medicineId }
                val freqDesc = med?.getFrequencyDescription()
                DoseRecordCard(
                    record = record,
                    frequencyDesc = freqDesc,
                    onToggleTaken = { viewModel.toggleDose(record) }
                )
            }
        }

        // Off-schedule medicines info (e.g. Every X days or Days of week not scheduled today)
        val scheduledMedIds = records.map { it.medicineId }.toSet()
        val offScheduleMedicines = medicines.filter {
            it.frequencyType != FrequencyType.EVERY_DAY &&
            it.frequencyType != FrequencyType.AS_NEEDED &&
            !scheduledMedIds.contains(it.id)
        }
        if (offScheduleMedicines.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "📅 本日はお休みの定期薬（次回予定日）",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        offScheduleMedicines.forEach { offMed ->
                            val nextDate = offMed.getNextScheduledDate(selectedDate)
                            val nextDisplay = if (nextDate != null) {
                                try {
                                    val d = SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).parse(nextDate)
                                    if (d != null) SimpleDateFormat("M月d日(E)", Locale.JAPAN).format(d) else nextDate
                                } catch (_: Exception) { nextDate }
                            } else "未定"

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = offMed.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "服用間隔: ${offMed.getFrequencyDescription()}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    color = TealPrimary.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "次回: $nextDisplay",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TealPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // PRN / As-needed medicine intake dialog
    if (showAsNeededDialog) {
        val asNeededMeds = medicines.filter { it.frequencyType == FrequencyType.AS_NEEDED }
        val isDark = LocalIsDarkTheme.current
        AlertDialog(
            onDismissRequest = { showAsNeededDialog = false },
            properties = DialogProperties(decorFitsSystemWindows = false),
            containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White,
            tonalElevation = 0.dp,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = Color(0xFFE65100),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "頓服薬の服用記録",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF212121)
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "飲んだお薬と時間を指定して「記録する」を押してください。",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // 服用時刻の指定カード
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(0xFF2D2319) else Color(0xFFFFF3E0)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFFFB74D)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // 1列目: 服用時刻だけ
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "服用時刻：",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else Color(0xFF5D4037)
                                )
                                Surface(
                                    color = Color(0xFFE65100),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = asNeededSelectedTime,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // 2列目: その下に「いま」と「時間を指定」を横並びで配置
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val nowFormatted = SimpleDateFormat("HH:mm", Locale.JAPAN).format(Date())
                                val isNow = asNeededSelectedTime == nowFormatted
                                Button(
                                    onClick = {
                                        asNeededSelectedTime = SimpleDateFormat("HH:mm", Locale.JAPAN).format(Date())
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isNow) Color(0xFFE65100) else (if (isDark) Color(0xFF3E3228) else Color(0xFFFFCC80)),
                                        contentColor = if (isNow) Color.White else (if (isDark) Color(0xFFFFB74D) else Color(0xFF5D4037))
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .testTag("as_needed_time_now_button")
                                ) {
                                    Text("いま", fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                }

                                OutlinedButton(
                                    onClick = { showTimePickerForAsNeeded = true },
                                    border = BorderStroke(1.dp, Color(0xFFE65100)),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color(0xFFE65100)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    modifier = Modifier
                                        .weight(1.8f)
                                        .height(42.dp)
                                        .testTag("as_needed_change_time_button")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            tint = Color(0xFFE65100),
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("時間を指定", fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }

                    if (asNeededMeds.isEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "現在、頓服薬として登録されているお薬はありません。\n「お薬・残薬」タブから頓服薬を追加してください。",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    } else {
                        asNeededMeds.forEach { med ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFFBFBFB)
                                ),
                                border = BorderStroke(1.dp, if (isDark) Color(0xFF424242) else Color(0xFFE0E0E0)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = med.name,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "[${med.type.displayName}]",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = "1回 ${if (med.dosePerTime % 1.0 == 0.0) med.dosePerTime.toInt() else med.dosePerTime}${med.unit}",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "残薬: ${med.remainingCount}${med.unit}",
                                            fontSize = 12.sp,
                                            color = if (med.isLowStock()) CoralWarning else TealPrimary,
                                            fontWeight = if (med.isLowStock()) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            viewModel.recordAsNeeded(med, customTime = asNeededSelectedTime)
                                            showAsNeededDialog = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("as_needed_record_button_${med.id}")
                                    ) {
                                        Text("記録する", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAsNeededDialog = false }) {
                    Text("閉じる", fontSize = 15.sp)
                }
            }
        )
    }

    // 頓服薬の服用時刻ピッカーダイアログ
    if (showTimePickerForAsNeeded) {
        val (h, m) = try {
            val parts = asNeededSelectedTime.split(":")
            parts[0].toInt() to parts[1].toInt()
        } catch (_: Exception) {
            val cal = Calendar.getInstance()
            cal.get(Calendar.HOUR_OF_DAY) to cal.get(Calendar.MINUTE)
        }
        val timePickerState = rememberTimePickerState(
            initialHour = h,
            initialMinute = m,
            is24Hour = true
        )
        val isDark = LocalIsDarkTheme.current
        AlertDialog(
            onDismissRequest = { showTimePickerForAsNeeded = false },
            properties = DialogProperties(decorFitsSystemWindows = false),
            containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White,
            tonalElevation = 0.dp,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = Color(0xFFE65100),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("飲んだ時刻を指定", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "時計の文字盤をタップして飲んだ時刻を指定してください",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = if (isDark) Color(0xFF2C2219) else Color(0xFFFFE0B2),
                            clockDialUnselectedContentColor = if (isDark) Color(0xFFFFB74D) else Color(0xFF3E2723),
                            clockDialSelectedContentColor = Color.White,
                            selectorColor = Color(0xFFE65100),
                            containerColor = MaterialTheme.colorScheme.surface,
                            periodSelectorBorderColor = Color(0xFFE65100),
                            periodSelectorSelectedContainerColor = if (isDark) Color(0xFF5D4037) else Color(0xFFFFE0B2),
                            timeSelectorSelectedContainerColor = if (isDark) Color(0xFF5D4037) else Color(0xFFFFE0B2),
                            timeSelectorSelectedContentColor = if (isDark) Color(0xFFFFCC80) else Color(0xFFBF360C),
                            timeSelectorUnselectedContainerColor = if (isDark) Color(0xFF2C2219) else MaterialTheme.colorScheme.surfaceVariant,
                            timeSelectorUnselectedContentColor = if (isDark) Color(0xFFFFB74D) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        asNeededSelectedTime = String.format(Locale.JAPAN, "%02d:%02d", timePickerState.hour, timePickerState.minute)
                        showTimePickerForAsNeeded = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                ) {
                    Text("この時間にする", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePickerForAsNeeded = false }) {
                    Text("キャンセル")
                }
            }
        )
    }
}

@Composable
fun DoseRecordCard(
    record: DoseRecord,
    frequencyDesc: String? = null,
    onToggleTaken: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val cardBorderColor by animateColorAsState(
        targetValue = if (record.isTaken) (if (isDark) Color(0xFF2E7D32) else Color(0xFF81C784)) else TealPrimary.copy(alpha = 0.3f),
        label = "borderColor"
    )
    val cardContainerColor = if (record.isTaken) {
        if (isDark) Color(0xFF23352B) else Color(0xFFEDF7EE)
    } else {
        MaterialTheme.colorScheme.surface
    }
    val medNameColor = if (record.isTaken) {
        if (isDark) Color(0xFFB0BEC5) else Color(0xFF1E3A27)
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val doseTextColor = if (record.isTaken) {
        if (isDark) Color(0xFF78909C) else Color(0xFF2E7D32)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = cardContainerColor
        ),
        border = BorderStroke(if (record.isTaken && !isDark) 1.5.dp else 1.dp, cardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (record.isTaken) 1.dp else 3.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("dose_card_${record.id}")
    ) {
        if (record.isTaken) {
            // 簡素化された服用完了表示（最下部に表示）
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "服用完了",
                        tint = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = if (isDark) Color(0xFF1E462B) else Color(0xFFC8E6C9),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = record.scheduledTime,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0xFFA5D6A7) else Color(0xFF1B5E20),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = record.medicineName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = medNameColor
                            )
                        }
                        val doseText = if (record.doseAmount % 1.0 == 0.0) {
                            "${record.doseAmount.toInt()}${record.unit}"
                        } else {
                            "${record.doseAmount}${record.unit}"
                        }
                        val timeStr = record.takenTimeMillis?.let {
                            SimpleDateFormat("HH:mm 服用", Locale.JAPAN).format(Date(it))
                        } ?: "服用済み"
                        Text(
                            text = "$doseText ・ $timeStr",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = doseTextColor,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                OutlinedButton(
                    onClick = onToggleTaken,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    border = BorderStroke(1.dp, if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) else Color(0xFF81C784)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF2E7D32)
                    )
                ) {
                    Text("取消", fontSize = 12.sp)
                }
            }
        } else {
            // 未服用のカード（通常表示・ベルマークなし）
            Column(modifier = Modifier.padding(16.dp)) {
                // Header Row: Time and Type Badge (ベルマークなし)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Time Badge
                        Surface(
                            color = TealPrimary,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = record.scheduledTime,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Type Badge
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = record.medicineType.getIcon(),
                                    contentDescription = null,
                                    tint = TealPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = record.medicineType.displayName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (!frequencyDesc.isNullOrBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = TealPrimary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = frequencyDesc,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TealPrimary,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Medicine Name
                Text(
                    text = record.medicineName,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = medNameColor
                )

                // Dose Amount
                val doseText = if (record.doseAmount % 1.0 == 0.0) {
                    "${record.doseAmount.toInt()}${record.unit}"
                } else {
                    "${record.doseAmount}${record.unit}"
                }

                Text(
                    text = "1回の量: $doseText",
                    fontSize = 16.sp,
                    color = doseTextColor,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Big prominent touch target for taking medicine
                Button(
                    onClick = onToggleTaken,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess),
                    shape = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("take_dose_button_${record.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "服用を記録する",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
