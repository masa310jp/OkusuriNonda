package com.example.ui

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.FrequencyType
import com.example.data.Medicine
import com.example.data.MedicineType
import com.example.data.WeekDayHelper
import com.example.ui.theme.CoralLight
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
fun MedicineStockScreen(
    viewModel: MedicationViewModel,
    medicines: List<Medicine>,
    lowStockMedicines: List<Medicine>,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingMedicine by remember { mutableStateOf<Medicine?>(null) }
    var deletingMedicine by remember { mutableStateOf<Medicine?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Action: Add New Medicine Button
        item {
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                shape = RoundedCornerShape(14.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("add_medicine_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "新しいお薬・注射を登録する",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Low stock warning banner if any
        if (lowStockMedicines.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CoralLight),
                    border = BorderStroke(2.dp, CoralWarning),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = CoralWarning,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "【お薬アラート】残り日数が少ないお薬があります",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = CoralWarning
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "飲み終わり・使い終わりが近づいています。各カードの「+7日分」ボタンや「+」ボタンを押して処方された分を補充してください。",
                            fontSize = 14.sp,
                            color = Color(0xFF3E2723)
                        )
                    }
                }
            }
        }

        // Medicine List Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "登録中のお薬・残り日数",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${medicines.size} 種類",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TealPrimary
                )
            }
        }

        if (medicines.isEmpty()) {
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
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "登録されているお薬はありません",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "上の「新しいお薬・注射を登録する」ボタンから登録してください。",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(medicines, key = { it.id }) { medicine ->
                MedicineStockCard(
                    medicine = medicine,
                    onQuickAddDays = { days ->
                        val dailyAmount = medicine.dosePerTime * medicine.scheduledTimes.size.coerceAtLeast(1)
                        val addUnits = (dailyAmount * days).toInt().coerceAtLeast(1)
                        viewModel.addStock(medicine.id, addUnits)
                    },
                    onIncrement = {
                        val dailyAmount = medicine.dosePerTime * (if (medicine.frequencyType == FrequencyType.AS_NEEDED) 1 else medicine.scheduledTimes.size.coerceAtLeast(1))
                        val addUnits = dailyAmount.toInt().coerceAtLeast(1)
                        viewModel.addStock(medicine.id, addUnits)
                    },
                    onDecrement = {
                        if (medicine.remainingCount > 0) {
                            val dailyAmount = medicine.dosePerTime * (if (medicine.frequencyType == FrequencyType.AS_NEEDED) 1 else medicine.scheduledTimes.size.coerceAtLeast(1))
                            val subUnits = dailyAmount.toInt().coerceAtLeast(1)
                            viewModel.addStock(medicine.id, -minOf(subUnits, medicine.remainingCount))
                        }
                    },
                    onEdit = { editingMedicine = medicine },
                    onDelete = { deletingMedicine = medicine }
                )
            }
        }
    }

    // Add Medicine Dialog
    if (showAddDialog) {
        MedicineFormDialog(
            title = "新しいお薬・注射の登録",
            initialMedicine = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { newMed ->
                viewModel.addMedicine(newMed)
                showAddDialog = false
            }
        )
    }

    // Edit Medicine Dialog
    editingMedicine?.let { medToEdit ->
        MedicineFormDialog(
            title = "お薬情報の編集",
            initialMedicine = medToEdit,
            onDismiss = { editingMedicine = null },
            onConfirm = { updatedMed ->
                viewModel.updateMedicine(updatedMed)
                editingMedicine = null
            }
        )
    }

    // Delete Confirmation Dialog
    deletingMedicine?.let { medToDelete ->
        val isDark = LocalIsDarkTheme.current
        AlertDialog(
            onDismissRequest = { deletingMedicine = null },
            properties = DialogProperties(decorFitsSystemWindows = false),
            containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White,
            tonalElevation = 0.dp,
            title = {
                Text(
                    text = "お薬の削除確認",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "「${medToDelete.name}」を削除してもよろしいですか？\nこのお薬の服薬予定も削除されます。",
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMedicine(medToDelete)
                        deletingMedicine = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("削除する", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { deletingMedicine = null }) {
                    Text("キャンセル")
                }
            }
        )
    }
}

@Composable
fun MedicineStockCard(
    medicine: Medicine,
    onQuickAddDays: (Int) -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLow = medicine.isLowStock()
    val estDays = medicine.estimatedRemainingDays()

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isLow) Color(0xFFFFF8F5) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.5.dp,
            if (isLow) CoralWarning else Color(0xFFE0E0E0)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("medicine_card_${medicine.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Icon + Name + Type Badge + Edit/Delete Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isLow) CoralLight else TealLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = medicine.type.getIcon(),
                            contentDescription = null,
                            tint = if (isLow) CoralWarning else TealPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = medicine.name,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        // 薬の種類を一行で表示
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 3.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = medicine.type.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        // その下に服用間隔を一行で表示（薬の種類・用法と同じ表示スタイル）
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = medicine.getFrequencyDescription(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "編集",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "削除",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dose details & Hospital
            val doseAmountDisplay = if (medicine.dosePerTime % 1.0 == 0.0) "${medicine.dosePerTime.toInt()}${medicine.unit}" else "${medicine.dosePerTime}${medicine.unit}"
            val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).format(Date()) }
            val isScheduledToday = medicine.isScheduledForDate(todayStr)
            val nextDate = if (!isScheduledToday) medicine.getNextScheduledDate(todayStr) else null
            val nextDateDisplay = if (nextDate != null) {
                try {
                    val d = SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).parse(nextDate)
                    if (d != null) SimpleDateFormat("M月d日(E)", Locale.JAPAN).format(d) else nextDate
                } catch (_: Exception) { nextDate }
            } else null

            Column {
                Text(
                    text = "服用時刻: ${medicine.scheduledTimes.joinToString("・")} (1回 $doseAmountDisplay)",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (medicine.frequencyType != FrequencyType.EVERY_DAY) {
                    if (isScheduledToday) {
                        Text(
                            text = "✓ 本日は服薬予定日です",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    } else if (nextDateDisplay != null) {
                        Text(
                            text = "📅 次回の服薬予定日: $nextDateDisplay",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF455A64),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            if (medicine.hospitalClinic.isNotBlank()) {
                Text(
                    text = "処方元: ${medicine.hospitalClinic}",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Prominent Days Counter Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isLow) CoralLight else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "あと ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLow) CoralWarning else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$estDays",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isLow) CoralWarning else TealPrimary
                            )
                            Text(
                                text = if (medicine.frequencyType == FrequencyType.AS_NEEDED) " 回分" else " 日分",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLow) CoralWarning else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }

                        Text(
                            text = if (isLow) "⚠️ 残りわずか (残量: ${medicine.remainingCount}${medicine.unit})" else "内訳: ${medicine.remainingCount}${medicine.unit}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isLow) CoralWarning else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // Stepper (- / +) buttons: adjusts by 1 day
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onDecrement,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "1日分減らす",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "1日分",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        IconButton(
                            onClick = onIncrement,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "1日分増やす",
                                tint = TealPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Refill Buttons for Seniors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onQuickAddDays(7) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Text("+7日分", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { onQuickAddDays(14) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Text("+14日分", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { onQuickAddDays(30) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Text("+30日分", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MedicineFormDialog(
    title: String,
    initialMedicine: Medicine?,
    onDismiss: () -> Unit,
    onConfirm: (Medicine) -> Unit
) {
    var name by remember { mutableStateOf(initialMedicine?.name ?: "") }
    var selectedType by remember { mutableStateOf(initialMedicine?.type ?: MedicineType.TABLET) }
    var unit by remember { mutableStateOf(initialMedicine?.unit ?: selectedType.defaultUnit) }
    var dosePerTime by remember { mutableDoubleStateOf(initialMedicine?.dosePerTime ?: 1.0) }
    val initialDoseInt = initialMedicine?.dosePerTime?.toInt() ?: 1
    val isInitialCustom = initialMedicine != null && (initialMedicine.dosePerTime % 1.0 != 0.0 || initialMedicine.dosePerTime < 1.0 || initialMedicine.dosePerTime > 10.0)
    var doseSelectionMode by remember { mutableStateOf(if (isInitialCustom) "その他" else "$initialDoseInt") }
    var customDoseText by remember { mutableStateOf(if (isInitialCustom) "${initialMedicine?.dosePerTime ?: 1.0}" else "") }
    var doseDropdownExpanded by remember { mutableStateOf(false) }
    var unitDropdownExpanded by remember { mutableStateOf(false) }
    var instructions by remember { mutableStateOf(initialMedicine?.instructions ?: "") }
    var hospitalClinic by remember { mutableStateOf(initialMedicine?.hospitalClinic ?: "") }

    // Frequency settings (User requested custom intervals like every X days)
    var frequencyType by remember { mutableStateOf(initialMedicine?.frequencyType ?: FrequencyType.EVERY_DAY) }
    var intervalDays by remember { mutableIntStateOf(initialMedicine?.intervalDays ?: 2) }
    var intervalDaysDropdownExpanded by remember { mutableStateOf(false) }
    val daysOfWeek = remember {
        mutableStateListOf<Int>().apply {
            addAll(initialMedicine?.daysOfWeek ?: listOf(Calendar.MONDAY, Calendar.WEDNESDAY, Calendar.FRIDAY))
        }
    }
    val context = LocalContext.current
    var startDate by remember {
        mutableStateOf(
            initialMedicine?.startDate?.takeIf { it.isNotBlank() }
                ?: SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).format(Date())
        )
    }

    // Predefined timings
    val scheduledTimes = remember {
        mutableStateListOf<String>().apply {
            addAll(initialMedicine?.scheduledTimes ?: listOf("08:00"))
        }
    }

    // Days remaining and alert days
    val initialDailyAmount = (initialMedicine?.dosePerTime ?: 1.0) * (if (initialMedicine?.frequencyType == FrequencyType.AS_NEEDED) 1 else (initialMedicine?.scheduledTimes?.size?.coerceAtLeast(1) ?: 1))
    val initDays = initialMedicine?.estimatedRemainingDays() ?: if (initialDailyAmount > 0) ((initialMedicine?.remainingCount ?: 14) / initialDailyAmount).toInt() else 14
    val initAlertDays = if (initialDailyAmount > 0) ((initialMedicine?.alertThreshold ?: 4) / initialDailyAmount).toInt().coerceAtLeast(1) else 3

    var remainingDays by remember { mutableIntStateOf(initDays.coerceIn(1, 100)) }
    var alertDays by remember { mutableIntStateOf(initAlertDays.coerceIn(1, 14)) }
    var remainingDaysDropdownExpanded by remember { mutableStateOf(false) }
    var alertDaysDropdownExpanded by remember { mutableStateOf(false) }

    var showCustomTimePicker by remember { mutableStateOf(false) }
    var customHour by remember { mutableIntStateOf(8) }
    var customMinute by remember { mutableIntStateOf(0) }
    var editingTimeVal by remember { mutableStateOf<String?>(null) }

    val isDark = LocalIsDarkTheme.current
    val formFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White,
        unfocusedContainerColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White,
        focusedBorderColor = if (isDark) Color(0xFFB0BEC5) else Color(0xFF212121),
        unfocusedBorderColor = if (isDark) Color(0xFF78909C) else Color(0xFF333333),
        focusedLabelColor = if (isDark) Color(0xFFE2E4E2) else Color(0xFF212121),
        unfocusedLabelColor = if (isDark) Color(0xFFB0BEC5) else Color(0xFF616161),
        focusedTextColor = if (isDark) Color.White else Color(0xFF212121),
        unfocusedTextColor = if (isDark) Color.White else Color(0xFF212121)
    )

    // Dialog for adding/editing time with Material 3 TimePicker
    if (showCustomTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = customHour,
            initialMinute = customMinute,
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = {
                showCustomTimePicker = false
                editingTimeVal = null
            },
            properties = DialogProperties(decorFitsSystemWindows = false),
            containerColor = if (LocalIsDarkTheme.current) MaterialTheme.colorScheme.surface else Color.White,
            tonalElevation = 0.dp,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (editingTimeVal != null) "服用時刻を変更する" else "服用時刻を追加する",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "時計の文字盤をタップまたは針を動かして指定してください",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val isDarkTimePicker = LocalIsDarkTheme.current
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = if (isDarkTimePicker) Color(0xFF162E2B) else TealLight.copy(alpha = 0.5f),
                            clockDialUnselectedContentColor = if (isDarkTimePicker) Color(0xFF80CBC4) else Color(0xFF004D40),
                            clockDialSelectedContentColor = Color.White,
                            selectorColor = TealPrimary,
                            containerColor = MaterialTheme.colorScheme.surface,
                            periodSelectorBorderColor = TealPrimary,
                            periodSelectorSelectedContainerColor = if (isDarkTimePicker) Color(0xFF004D40) else TealLight,
                            timeSelectorSelectedContainerColor = if (isDarkTimePicker) Color(0xFF004D40) else TealLight,
                            timeSelectorSelectedContentColor = if (isDarkTimePicker) Color(0xFFE0F2F1) else TealDark,
                            timeSelectorUnselectedContainerColor = if (isDarkTimePicker) Color(0xFF162E2B) else MaterialTheme.colorScheme.surfaceVariant,
                            timeSelectorUnselectedContentColor = if (isDarkTimePicker) Color(0xFF80CBC4) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val formatted = String.format(Locale.JAPAN, "%02d:%02d", timePickerState.hour, timePickerState.minute)
                        if (editingTimeVal != null) {
                            scheduledTimes.remove(editingTimeVal)
                        }
                        if (!scheduledTimes.contains(formatted)) {
                            scheduledTimes.add(formatted)
                            scheduledTimes.sort()
                        }
                        showCustomTimePicker = false
                        editingTimeVal = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text(if (editingTimeVal != null) "変更する" else "この時間を追加", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCustomTimePicker = false
                    editingTimeVal = null
                }) {
                    Text("キャンセル")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(decorFitsSystemWindows = false),
        modifier = Modifier.imePadding(),
        containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White,
        tonalElevation = 0.dp,
        title = {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color(0xFF212121)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Medicine Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("お薬の名前 (目薬等でもOK)") },
                    singleLine = true,
                    colors = formFieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_medicine_name")
                )

                // Medicine Type Selector
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF7FAF8)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF3F4846) else Color(0xFFE0E0E0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "お薬の種類を選んでください",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            MedicineType.values().forEach { type ->
                                val isSelected = type == selectedType
                                Surface(
                                    color = if (isSelected) TealPrimary else MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, if (isSelected) TealPrimary else Color.LightGray),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedType = type
                                            unit = type.defaultUnit
                                        }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = type.getIcon(),
                                            contentDescription = null,
                                            tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = type.displayName,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Dosage & Unit (1回の量はプルダウンリスト: 1〜10、その他)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "1回の服用量・単位",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // プルダウン式 1回の量 (1〜10, その他)
                        Box(modifier = Modifier.weight(1.2f)) {
                            OutlinedTextField(
                                value = if (doseSelectionMode == "その他") "その他" else "$doseSelectionMode",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("1回の量") },
                                trailingIcon = {
                                    IconButton(onClick = { doseDropdownExpanded = true }) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = "選択肢を開く",
                                            tint = if (isDark) Color.White else Color(0xFF212121)
                                        )
                                    }
                                },
                                colors = formFieldColors,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { doseDropdownExpanded = true }
                                    .testTag("dose_dropdown_field")
                            )

                            DropdownMenu(
                                expanded = doseDropdownExpanded,
                                onDismissRequest = { doseDropdownExpanded = false },
                                modifier = Modifier.heightIn(max = 280.dp)
                            ) {
                                (1..10).forEach { num ->
                                    DropdownMenuItem(
                                        text = { Text("$num", fontSize = 16.sp, fontWeight = FontWeight.Medium) },
                                        onClick = {
                                            doseSelectionMode = "$num"
                                            dosePerTime = num.toDouble()
                                            doseDropdownExpanded = false
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("その他 (自由入力)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TealPrimary) },
                                    onClick = {
                                        doseSelectionMode = "その他"
                                        doseDropdownExpanded = false
                                    }
                                )
                            }
                        }

                        // 単位 (プルダウンリスト: 薬の種類により自動変更され、手動選択も可能)
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = unit,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("単位") },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "単位を選択",
                                        tint = if (isDark) Color.White else Color(0xFF212121)
                                    )
                                },
                                singleLine = true,
                                colors = formFieldColors,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("unit_dropdown_field")
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { unitDropdownExpanded = true }
                            )

                            DropdownMenu(
                                expanded = unitDropdownExpanded,
                                onDismissRequest = { unitDropdownExpanded = false },
                                modifier = Modifier.heightIn(max = 280.dp)
                            ) {
                                val standardUnits = listOf(
                                    "錠", "カプセル", "包", "ml", "滴", "枚", "吸入", "回", "本", "g", "mg", "噴霧", "袋", "個"
                                )
                                val unitOptions = if (unit.isNotEmpty() && !standardUnits.contains(unit)) {
                                    listOf(unit) + standardUnits
                                } else {
                                    standardUnits
                                }
                                unitOptions.forEach { u ->
                                    val isSelected = unit == u
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = u,
                                                fontSize = 16.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) TealPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        onClick = {
                                            unit = u
                                            unitDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 「その他」選択時の自由入力欄
                    if (doseSelectionMode == "その他") {
                        OutlinedTextField(
                            value = customDoseText,
                            onValueChange = {
                                customDoseText = it
                                val parsed = it.toDoubleOrNull()
                                if (parsed != null && parsed > 0) {
                                    dosePerTime = parsed
                                }
                            },
                            label = { Text("具体的な1回の量 (例: 0.5, 2.5, 15)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            colors = formFieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // 服用開始日の設定（「本日」と「カレンダーから選ぶ」のみ）
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF7FAF8)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF3F4846) else Color(0xFFE0E0E0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = TealPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "服用開始日",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TealDark
                            )
                        }

                        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).format(Date())
                        val isToday = startDate == todayStr

                        // 選択中の開始日表示
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, TealPrimary.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val formattedDate = try {
                                    val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).parse(startDate)
                                    if (parsed != null) SimpleDateFormat("yyyy年M月d日 (E)", Locale.JAPAN).format(parsed) else startDate
                                } catch (_: Exception) {
                                    startDate
                                }
                                Text(
                                    text = if (isToday) "$formattedDate（本日）" else formattedDate,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TealPrimary
                                )
                            }
                        }

                        // 選択肢: 「本日」と「カレンダーから選ぶ」
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // 「本日」ボタン
                            Button(
                                onClick = { startDate = todayStr },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isToday) TealPrimary else MaterialTheme.colorScheme.surface,
                                    contentColor = if (isToday) Color.White else MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(8.dp),
                                border = if (!isToday) BorderStroke(1.dp, Color.LightGray) else null,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "本日",
                                    fontSize = 14.sp,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium
                                )
                            }

                            // 「カレンダーから選ぶ」ボタン
                            Button(
                                onClick = {
                                    val cal = Calendar.getInstance()
                                    try {
                                        val parts = startDate.split("-")
                                        if (parts.size == 3) {
                                            cal.set(Calendar.YEAR, parts[0].toInt())
                                            cal.set(Calendar.MONTH, parts[1].toInt() - 1)
                                            cal.set(Calendar.DAY_OF_MONTH, parts[2].toInt())
                                        }
                                    } catch (_: Exception) {}
                                    DatePickerDialog(
                                        context,
                                        { _, y, m, d ->
                                            startDate = String.format(Locale.JAPAN, "%04d-%02d-%02d", y, m + 1, d)
                                        },
                                        cal.get(Calendar.YEAR),
                                        cal.get(Calendar.MONTH),
                                        cal.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!isToday) TealPrimary else MaterialTheme.colorScheme.surface,
                                    contentColor = if (!isToday) Color.White else MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(8.dp),
                                border = if (isToday) BorderStroke(1.dp, Color.LightGray) else null,
                                modifier = Modifier.weight(1.3f)
                            ) {
                                Text(
                                    text = "カレンダーから選ぶ",
                                    fontSize = 14.sp,
                                    fontWeight = if (!isToday) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Frequency Settings Section (毎日 / 何日おき / 曜日指定 / 頓服)
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF7FAF8)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF3F4846) else Color(0xFFE0E0E0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "服用間隔・頻度",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // 4 Frequency Type Options
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FrequencyType.values().forEach { type ->
                                val isSelected = frequencyType == type
                                Surface(
                                    color = if (isSelected) TealPrimary else MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, if (isSelected) TealPrimary else Color.LightGray),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { frequencyType = type }
                                ) {
                                    Text(
                                        text = type.displayName,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        // Specific options based on selected frequency
                        when (frequencyType) {
                            FrequencyType.EVERY_DAY -> {
                                Text(
                                    text = "毎日、指定した時刻に服用します。",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            FrequencyType.EVERY_X_DAYS -> {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        text = "服用する間隔を選んでください：",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "日数を指定:",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )

                                        Box(modifier = Modifier.width(160.dp)) {
                                            OutlinedTextField(
                                                value = "${intervalDays}日おき",
                                                onValueChange = {},
                                                readOnly = true,
                                                trailingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Default.KeyboardArrowDown,
                                                        contentDescription = "日数を選択",
                                                        tint = if (isDark) Color.White else Color(0xFF212121)
                                                    )
                                                },
                                                singleLine = true,
                                                colors = formFieldColors,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("dropdown_interval_days")
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .matchParentSize()
                                                    .clickable { intervalDaysDropdownExpanded = true }
                                            )

                                            DropdownMenu(
                                                expanded = intervalDaysDropdownExpanded,
                                                onDismissRequest = { intervalDaysDropdownExpanded = false },
                                                modifier = Modifier.heightIn(max = 280.dp)
                                            ) {
                                                (1..28).forEach { days ->
                                                    val isSelected = intervalDays == days
                                                    DropdownMenuItem(
                                                        text = {
                                                            Text(
                                                                text = "${days}日おき",
                                                                fontSize = 15.sp,
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                                color = if (isSelected) TealPrimary else MaterialTheme.colorScheme.onSurface
                                                            )
                                                        },
                                                        onClick = {
                                                            intervalDays = days
                                                            intervalDaysDropdownExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Text(
                                        text = "※数え始めの開始日: $startDate から ${intervalDays}日おきに予定が入ります。",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            FrequencyType.DAYS_OF_WEEK -> {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "服用する曜日をタップして選んでください：",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    // Days of week selector buttons
                                    val weekDays = listOf(
                                        Calendar.SUNDAY to ("日" to Color(0xFFD32F2F)),
                                        Calendar.MONDAY to ("月" to MaterialTheme.colorScheme.onSurface),
                                        Calendar.TUESDAY to ("火" to MaterialTheme.colorScheme.onSurface),
                                        Calendar.WEDNESDAY to ("水" to MaterialTheme.colorScheme.onSurface),
                                        Calendar.THURSDAY to ("木" to MaterialTheme.colorScheme.onSurface),
                                        Calendar.FRIDAY to ("金" to MaterialTheme.colorScheme.onSurface),
                                        Calendar.SATURDAY to ("土" to Color(0xFF1976D2))
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        weekDays.forEach { (dayInt, pair) ->
                                            val (label, defaultTextColor) = pair
                                            val isChecked = daysOfWeek.contains(dayInt)
                                            Surface(
                                                color = if (isChecked) TealPrimary else MaterialTheme.colorScheme.surface,
                                                shape = CircleShape,
                                                border = BorderStroke(1.dp, if (isChecked) TealPrimary else Color.LightGray),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(1f)
                                                    .clip(CircleShape)
                                                    .clickable {
                                                        if (isChecked) {
                                                            if (daysOfWeek.size > 1) daysOfWeek.remove(dayInt)
                                                        } else {
                                                            daysOfWeek.add(dayInt)
                                                            daysOfWeek.sort()
                                                        }
                                                    }
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = label,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isChecked) Color.White else defaultTextColor
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Quick shortcuts for weekday selection
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(
                                            onClick = {
                                                daysOfWeek.clear()
                                                daysOfWeek.addAll(listOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY))
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("平日のみ", fontSize = 12.sp)
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                daysOfWeek.clear()
                                                daysOfWeek.addAll(listOf(Calendar.SATURDAY, Calendar.SUNDAY))
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("週末のみ", fontSize = 12.sp)
                                        }
                                    }

                                    Text(
                                        text = "選択中: ${WeekDayHelper.formatDaysOfWeek(daysOfWeek)}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TealPrimary
                                    )
                                }
                            }
                            FrequencyType.AS_NEEDED -> {
                                Text(
                                    text = "【頓服薬】痛む時や発熱時など、症状がある時だけ服用します。毎日の定期通知は行わず、飲んだ時に「今日のお薬」画面から記録できます。",
                                    fontSize = 13.sp,
                                    color = Color(0xFFE65100)
                                )
                            }
                        }
                    }
                }

                // Scheduled Times Chips (only relevant if not PRN / AS_NEEDED)
                if (frequencyType != FrequencyType.AS_NEEDED) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "服用する時間帯",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "設定中: ${scheduledTimes.size}回 / 日",
                                fontSize = 12.sp,
                                color = TealPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Button to open custom time picker
                        Button(
                            onClick = {
                                customHour = 8
                                customMinute = 0
                                editingTimeVal = null
                                showCustomTimePicker = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("時間を追加する", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // List of currently configured scheduled times with deletion chip
                    if (scheduledTimes.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            scheduledTimes.forEach { timeVal ->
                                Surface(
                                    color = TealPrimary,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.clickable {
                                        val parts = timeVal.split(":")
                                        customHour = parts.getOrNull(0)?.toIntOrNull() ?: 8
                                        customMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0
                                        editingTimeVal = timeVal
                                        showCustomTimePicker = true
                                    }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccessTime,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = timeVal,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(
                                            onClick = {
                                                if (scheduledTimes.size > 1) {
                                                    scheduledTimes.remove(timeVal)
                                                }
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "$timeVal を削除",
                                                tint = Color.White.copy(alpha = 0.85f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "👆時間をタップすると変更できます",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "時間を設定してください（右上の「時間を追加する」をタップ）",
                            fontSize = 12.sp,
                            color = Color(0xFFD32F2F)
                        )
                    }
                }

                // Initial Days & Alert Threshold Days (あと何日分: 1〜100, 警告する日数: 1〜14 のプルダウンリスト)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "お薬の残量・警告設定",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // あと何日分 (プルダウンリスト 1〜100)
                        val daysSuffix = if (frequencyType == FrequencyType.AS_NEEDED) "回分" else "日分"
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = "$remainingDays$daysSuffix",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("あと何日分") },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "あと何日分を選択",
                                        tint = if (isDark) Color.White else Color(0xFF212121)
                                    )
                                },
                                colors = formFieldColors,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("dropdown_remaining_days")
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { remainingDaysDropdownExpanded = true }
                            )

                            DropdownMenu(
                                expanded = remainingDaysDropdownExpanded,
                                onDismissRequest = { remainingDaysDropdownExpanded = false },
                                modifier = Modifier.heightIn(max = 280.dp)
                            ) {
                                (1..100).forEach { days ->
                                    val isSelected = remainingDays == days
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "$days$daysSuffix",
                                                fontSize = 15.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) TealPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        onClick = {
                                            remainingDays = days
                                            remainingDaysDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // 警告する日数 (プルダウンリスト 1〜14)
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = "${alertDays}日前",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("警告する日数") },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "警告する日数を選択",
                                        tint = if (isDark) Color.White else Color(0xFF212121)
                                    )
                                },
                                colors = formFieldColors,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("dropdown_alert_days")
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { alertDaysDropdownExpanded = true }
                            )

                            DropdownMenu(
                                expanded = alertDaysDropdownExpanded,
                                onDismissRequest = { alertDaysDropdownExpanded = false },
                                modifier = Modifier.heightIn(max = 280.dp)
                            ) {
                                (1..14).forEach { days ->
                                    val isSelected = alertDays == days
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "${days}日前",
                                                fontSize = 15.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) TealPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        onClick = {
                                            alertDays = days
                                            alertDaysDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    val dailyAmount = dosePerTime * (if (frequencyType == FrequencyType.AS_NEEDED) 1 else scheduledTimes.size.coerceAtLeast(1))
                    val totalUnits = (remainingDays * dailyAmount).toInt()
                    val doseDisplay = if (dosePerTime % 1.0 == 0.0) dosePerTime.toInt().toString() else dosePerTime.toString()
                    Text(
                        text = "※内訳: 合計約 $totalUnits $unit (1日 ${if (frequencyType == FrequencyType.AS_NEEDED) 1 else scheduledTimes.size}回 × ${doseDisplay}${unit})",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Clinic / Hospital
                OutlinedTextField(
                    value = hospitalClinic,
                    onValueChange = { hospitalClinic = it },
                    label = { Text("処方医院・クリニック名 (任意)") },
                    singleLine = true,
                    colors = formFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val finalDailyAmount = dosePerTime * (if (frequencyType == FrequencyType.AS_NEEDED) 1 else scheduledTimes.size.coerceAtLeast(1))
                        val calculatedRemainingCount = (remainingDays * finalDailyAmount).toInt().coerceAtLeast(0)
                        val calculatedAlertThreshold = (alertDays * finalDailyAmount).toInt().coerceAtLeast(1)

                        val med = Medicine(
                            id = initialMedicine?.id ?: 0L,
                            name = name.trim(),
                            type = selectedType,
                            unit = unit.trim().ifEmpty { selectedType.defaultUnit },
                            dosePerTime = dosePerTime,
                            instructions = instructions.trim(),
                            scheduledTimes = if (frequencyType == FrequencyType.AS_NEEDED) emptyList() else scheduledTimes.toList().ifEmpty { listOf("08:00") },
                            remainingCount = calculatedRemainingCount,
                            alertThreshold = calculatedAlertThreshold,
                            hospitalClinic = hospitalClinic.trim(),
                            frequencyType = frequencyType,
                            intervalDays = intervalDays,
                            daysOfWeek = daysOfWeek.toList(),
                            startDate = startDate
                        )
                        onConfirm(med)
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Text(
                    text = if (initialMedicine == null) "登録する" else "保存する",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル", fontSize = 15.sp)
            }
        }
    )
}
