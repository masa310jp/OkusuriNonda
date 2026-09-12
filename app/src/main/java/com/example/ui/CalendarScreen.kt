package com.example.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DoseRecord
import com.example.data.FrequencyType
import com.example.data.Medicine
import com.example.ui.theme.CoralWarning
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.GreenSuccessLight
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealLight
import com.example.ui.theme.TealPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarScreen(
    viewModel: MedicationViewModel,
    medicines: List<Medicine> = emptyList(),
    selectedDate: String,
    selectedMonth: String,
    monthRecords: List<DoseRecord>,
    selectedDateRecords: List<DoseRecord>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Group month records by date
    val recordsByDate = remember(monthRecords) {
        monthRecords.groupBy { it.date }
    }

    val cal = Calendar.getInstance()
    val parts = selectedMonth.split("-")
    val year = parts.getOrNull(0)?.toIntOrNull() ?: cal.get(Calendar.YEAR)
    val month = (parts.getOrNull(1)?.toIntOrNull() ?: (cal.get(Calendar.MONTH) + 1)) - 1

    cal.set(Calendar.YEAR, year)
    cal.set(Calendar.MONTH, month)
    cal.set(Calendar.DAY_OF_MONTH, 1)

    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    val monthHeader = "${year}年 ${month + 1}月"
    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).format(Date())

    // 各薬の残薬でカバー可能な日付セットを計算（残薬のない日は予定ドットをつけない）
    val coveredDates = remember(medicines, todayStr) {
        val dates = mutableSetOf<String>()
        val p = todayStr.split("-")
        val ty = p.getOrNull(0)?.toIntOrNull() ?: cal.get(Calendar.YEAR)
        val tm = (p.getOrNull(1)?.toIntOrNull() ?: (cal.get(Calendar.MONTH) + 1)) - 1
        val td = p.getOrNull(2)?.toIntOrNull() ?: cal.get(Calendar.DAY_OF_MONTH)

        for (med in medicines) {
            if (!med.isActive || med.remainingCount <= 0 || med.frequencyType == FrequencyType.AS_NEEDED) continue
            var stock = med.remainingCount
            val doseAmount = med.dosePerTime.toInt().coerceAtLeast(1)

            val tempCal = Calendar.getInstance()
            tempCal.set(ty, tm, td)

            var days = 0
            while (stock >= doseAmount && days < 365) {
                val curDate = String.format(
                    Locale.JAPAN,
                    "%04d-%02d-%02d",
                    tempCal.get(Calendar.YEAR),
                    tempCal.get(Calendar.MONTH) + 1,
                    tempCal.get(Calendar.DAY_OF_MONTH)
                )

                if (med.isScheduledForDate(curDate)) {
                    val timesCount = med.scheduledTimes.size.coerceAtLeast(1)
                    val neededForDay = doseAmount * timesCount
                    dates.add(curDate)
                    stock -= neededForDay
                }
                tempCal.add(Calendar.DAY_OF_MONTH, 1)
                days++
            }
        }
        dates
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Month Navigation Card
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
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.changeMonth(-1) },
                        modifier = Modifier.size(48.dp).testTag("prev_month_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "前月へ",
                            tint = TealPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        text = monthHeader,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(
                        onClick = { viewModel.changeMonth(1) },
                        modifier = Modifier.size(48.dp).testTag("next_month_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "翌月へ",
                            tint = TealPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        // Calendar Grid Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Days of week header
                    val daysOfWeek = listOf("日", "月", "火", "水", "木", "金", "土")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        daysOfWeek.forEachIndexed { index, day ->
                            val textColor = when (index) {
                                0 -> Color(0xFFD32F2F) // Sunday Red
                                6 -> Color(0xFF1976D2) // Saturday Blue
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                            Text(
                                text = day,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Calendar Days Grid (Up to 6 weeks x 7 days)
                    val totalCells = ((firstDayOfWeek - 1 + daysInMonth + 6) / 7) * 7
                    for (row in 0 until (totalCells / 7)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            for (col in 0 until 7) {
                                val cellIndex = row * 7 + col
                                val dayNumber = cellIndex - (firstDayOfWeek - 1) + 1

                                if (dayNumber in 1..daysInMonth) {
                                    val dateStr = String.format(Locale.JAPAN, "%04d-%02d-%02d", year, month + 1, dayNumber)
                                    val isSelected = dateStr == selectedDate
                                    val isCurrentToday = dateStr == todayStr

                                    val dayRecords = recordsByDate[dateStr] ?: emptyList()
                                    val total = dayRecords.size
                                    val taken = dayRecords.count { it.isTaken }

                                    val statusDotColor: Color? = when {
                                        total > 0 && taken == total -> GreenSuccess
                                        total > 0 && taken > 0 -> CoralWarning
                                        total > 0 && (dateStr < todayStr || coveredDates.contains(dateStr)) -> Color(0xFFB0BEC5)
                                        else -> null
                                    }

                                    val isDark = LocalIsDarkTheme.current
                                    val cellModifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .then(
                                            if (isSelected) {
                                                // High-contrast clean styling: distinct light teal background with crisp bold border
                                                Modifier
                                                    .background(if (isDark) Color(0xFF003830) else Color(0xFFE0F2F1))
                                                    .border(2.5.dp, TealPrimary, RoundedCornerShape(8.dp))
                                            } else if (isCurrentToday) {
                                                Modifier.background(if (isDark) Color(0xFF3E3B18) else Color(0xFFFFF9C4))
                                                    .border(1.5.dp, Color(0xFFFBC02D), RoundedCornerShape(8.dp))
                                            } else {
                                                Modifier.background(Color.Transparent)
                                            }
                                        )
                                        .clickable { viewModel.selectDate(dateStr) }

                                    Column(
                                        modifier = cellModifier,
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        val dayColor = if (isSelected) {
                                            if (isDark) Color(0xFF80CBC4) else Color(0xFF004D40)
                                        } else if (isCurrentToday) {
                                            if (isDark) Color(0xFFFFEE58) else Color(0xFFE65100)
                                        } else {
                                            when (col) {
                                                0 -> Color(0xFFD32F2F)
                                                6 -> Color(0xFF1976D2)
                                                else -> MaterialTheme.colorScheme.onSurface
                                            }
                                        }

                                        Text(
                                            text = dayNumber.toString(),
                                            fontSize = 17.sp,
                                            fontWeight = if (isSelected || isCurrentToday) FontWeight.Bold else FontWeight.Normal,
                                            color = dayColor
                                        )

                                        // Status dot (指定しても指定しなくても色は常に statusDotColor のまま変化させない)
                                        if (statusDotColor != null) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(statusDotColor)
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                    }
                                } else {
                                    // Empty padding cell
                                    Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(GreenSuccess))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("すべて完了", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.width(12.dp))
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(CoralWarning))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("一部完了", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.width(12.dp))
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFB0BEC5)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("未服用", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Family Share Button (Prominent Requirement)
        item {
            Button(
                onClick = {
                    val shareText = viewModel.generateFamilyShareMessage(selectedDate)
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "ご家族へ服薬状況を共有する"))
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                shape = RoundedCornerShape(14.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("share_with_family_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "共有",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "服薬状況を連絡する",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Selected Date Detail Section
        item {
            val parsed = try {
                SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).parse(selectedDate) ?: Date()
            } catch (_: Exception) {
                Date()
            }
            val dateLabel = SimpleDateFormat("M月d日 (E)", Locale.JAPAN).format(parsed)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$dateLabel の詳細",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                val total = selectedDateRecords.size
                val taken = selectedDateRecords.count { it.isTaken }
                val isDarkTheme = LocalIsDarkTheme.current
                Surface(
                    color = if (total > 0 && taken == total) (if (isDarkTheme) Color(0xFF1E462B) else Color(0xFFF1F8F4)) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = if (total > 0 && taken == total && !isDarkTheme) BorderStroke(1.dp, Color(0xFFC8E6C9)) else null
                ) {
                    Text(
                        text = if (total > 0) "$taken / $total 回 服用済み" else "予定なし",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (total > 0 && taken == total) (if (isDarkTheme) Color(0xFFA5D6A7) else Color(0xFF2E7D32)) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        if (selectedDateRecords.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val isFuture = selectedDate >= todayStr
                    val emptyMsg = if (isFuture) "服薬予定はありません（残薬なし・予定外の日）" else "この日の服薬記録はありません"
                    Text(
                        text = emptyMsg,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(24.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            selectedDateRecords.forEach { record ->
                item(key = record.id) {
                    val isDark = LocalIsDarkTheme.current
                    val cardBg = if (record.isTaken) {
                        if (isDark) Color(0xFF23352B) else Color(0xFFEDF7EE)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                    val medColor = if (record.isTaken) {
                        if (isDark) Color(0xFFB0BEC5) else Color(0xFF1E3A27)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = cardBg
                        ),
                        border = BorderStroke(
                            if (record.isTaken && !isDark) 1.5.dp else 1.dp,
                            if (record.isTaken) (if (isDark) Color(0xFF2E7D32) else Color(0xFF81C784)) else if (isDark) Color(0xFF37474F) else Color(0xFFE0E0E0)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (record.isTaken) 1.dp else 2.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = if (record.isTaken) (if (isDark) Color(0xFF1E462B) else Color(0xFFC8E6C9)) else TealPrimary,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = record.scheduledTime,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (record.isTaken) (if (isDark) Color(0xFFA5D6A7) else Color(0xFF1B5E20)) else Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "[${record.medicineType.displayName}]",
                                        fontSize = 13.sp,
                                        color = if (record.isTaken) (if (isDark) Color(0xFFA5D6A7) else Color(0xFF2E7D32)) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = record.medicineName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = medColor,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                val takenInfo = if (record.isTaken) {
                                    val time = record.takenTimeMillis?.let {
                                        SimpleDateFormat("HH:mm", Locale.JAPAN).format(Date(it))
                                    } ?: ""
                                    "✓ 服用完了済み ($time)"
                                } else {
                                    "未服用"
                                }
                                Text(
                                    text = takenInfo,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (record.isTaken) (if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)) else Color.Gray,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            OutlinedButton(
                                onClick = { viewModel.toggleDose(record) },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (record.isTaken) (if (isDark) Color(0xFF1E462B) else Color(0xFFF4FAF5)) else Color.Transparent,
                                    contentColor = if (record.isTaken) (if (isDark) Color(0xFFA5D6A7) else Color(0xFF388E3C)) else TealPrimary
                                ),
                                border = BorderStroke(1.dp, if (record.isTaken) (if (isDark) Color(0xFF2E7D32) else Color(0xFFD4EAD6)) else TealPrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (record.isTaken) "済 (取消)" else "服用する",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (record.isTaken) (if (isDark) Color(0xFFA5D6A7) else Color(0xFF388E3C)) else TealPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
