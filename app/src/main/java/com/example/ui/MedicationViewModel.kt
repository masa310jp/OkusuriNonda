package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.DoseRecord
import com.example.data.MedicationRepository
import com.example.data.Medicine
import com.example.util.PdfReportGenerator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class MedicationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MedicationRepository

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN)
    private val monthFormat = SimpleDateFormat("yyyy-MM", Locale.JAPAN)

    private val _selectedDate = MutableStateFlow(dateFormat.format(Date()))
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _selectedMonth = MutableStateFlow(monthFormat.format(Date()))
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val prefs = application.getSharedPreferences("med_reminder_settings", Context.MODE_PRIVATE)
    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("is_dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        prefs.edit().putBoolean("is_dark_mode", enabled).apply()
    }

    private val _userName = MutableStateFlow(prefs.getString("user_name", "") ?: "")
    val userName: StateFlow<String> = _userName.asStateFlow()

    fun setUserName(name: String) {
        _userName.value = name
        prefs.edit().putString("user_name", name).apply()
    }

    private val _hasPromptedName = MutableStateFlow(prefs.getBoolean("has_prompted_name", false))
    val hasPromptedName: StateFlow<Boolean> = _hasPromptedName.asStateFlow()

    fun markNamePrompted() {
        _hasPromptedName.value = true
        prefs.edit().putBoolean("has_prompted_name", true).apply()
    }

    init {
        val db = AppDatabase.getDatabase(application)
        repository = MedicationRepository(application, db)

        viewModelScope.launch {
            repository.ensureRecordsForDate(_selectedDate.value)
        }
    }

    val activeMedicines: StateFlow<List<Medicine>> = repository.allActiveMedicines
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val lowStockMedicines: StateFlow<List<Medicine>> = activeMedicines
        .map { list -> list.filter { it.isLowStock() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recordsForSelectedDate: StateFlow<List<DoseRecord>> = _selectedDate
        .flatMapLatest { date -> repository.getRecordsForDate(date) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recordsForSelectedMonth: StateFlow<List<DoseRecord>> = _selectedMonth
        .flatMapLatest { month -> repository.getRecordsForMonth(month) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allRecords: StateFlow<List<DoseRecord>> = repository.getAllRecords()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun getTodayDate(): String = dateFormat.format(Date())

    fun isSelectedDateToday(): Boolean = _selectedDate.value == getTodayDate()

    /**
     * アプリ起動時やレジューム時に常に本日を表示するようにリセット
     */
    fun resetToToday() {
        val today = dateFormat.format(Date())
        val thisMonth = monthFormat.format(Date())
        _selectedDate.value = today
        _selectedMonth.value = thisMonth
        _selectedTab.value = 0 // 今日タブに戻す
        viewModelScope.launch {
            repository.ensureRecordsForDate(today)
            repository.ensureRecordsForMonth(thisMonth)
        }
    }

    fun selectDate(date: String) {
        _selectedDate.value = date
        viewModelScope.launch {
            repository.ensureRecordsForDate(date)
        }
    }

    fun setMonth(yearMonth: String) {
        _selectedMonth.value = yearMonth
        viewModelScope.launch {
            repository.ensureRecordsForMonth(yearMonth)
        }
    }

    fun changeMonth(offset: Int) {
        val cal = Calendar.getInstance()
        try {
            val parts = _selectedMonth.value.split("-")
            cal.set(Calendar.YEAR, parts[0].toInt())
            cal.set(Calendar.MONTH, parts[1].toInt() - 1)
            cal.add(Calendar.MONTH, offset)
            val newMonth = monthFormat.format(cal.time)
            _selectedMonth.value = newMonth
            viewModelScope.launch {
                repository.ensureRecordsForMonth(newMonth)
            }
        } catch (_: Exception) {
            _selectedMonth.value = monthFormat.format(Date())
        }
    }

    fun toggleDose(record: DoseRecord) {
        viewModelScope.launch {
            if (record.isTaken) {
                repository.markDoseUntaken(record.id)
            } else {
                repository.markDoseTaken(record.id)
            }
        }
    }

    fun markAllTakenForSelectedDate() {
        viewModelScope.launch {
            repository.markAllDosesTakenForDate(_selectedDate.value)
        }
    }

    fun recordAsNeeded(
        medicine: Medicine,
        note: String = "",
        customTime: String? = null,
        customDate: String? = null
    ) {
        viewModelScope.launch {
            repository.recordAsNeededDose(medicine, note, customTime, customDate ?: _selectedDate.value)
        }
    }

    fun addMedicine(medicine: Medicine) {
        viewModelScope.launch {
            repository.addMedicine(medicine)
        }
    }

    fun updateMedicine(medicine: Medicine) {
        viewModelScope.launch {
            repository.updateMedicine(medicine)
        }
    }

    fun deleteMedicine(medicine: Medicine) {
        viewModelScope.launch {
            repository.deleteMedicine(medicine)
        }
    }

    fun addStock(medicineId: Long, amount: Int) {
        viewModelScope.launch {
            repository.addMedicineStock(medicineId, amount)
        }
    }

    fun updateStock(medicineId: Long, newStock: Int) {
        viewModelScope.launch {
            repository.updateMedicineStock(medicineId, newStock)
        }
    }

    fun testSendNotification(record: DoseRecord) {
        repository.testTriggerNotification(record)
    }

    fun generateFamilyShareMessage(dateStr: String): String {
        val records = recordsForSelectedDate.value
        val total = records.size
        val taken = records.count { it.isTaken }
        val lowStockList = lowStockMedicines.value

        val statusSummary = if (total == 0) {
            "服薬予定はありません"
        } else if (taken == total) {
            "すべて服用完了しました！💮（${taken}/${total}回）"
        } else {
            "一部服用済み（${taken}/${total}回）"
        }

        val details = records.joinToString("\n") { rec ->
            val mark = if (rec.isTaken) "✓【服用済】" else "□【未服用】"
            val doseDisplay = if (rec.doseAmount % 1.0 == 0.0) "${rec.doseAmount.toInt()}${rec.unit}" else "${rec.doseAmount}${rec.unit}"
            "$mark ${rec.scheduledTime} : ${rec.medicineName} ($doseDisplay)"
        }

        val lowStockWarning = if (lowStockList.isNotEmpty()) {
            "\n\n⚠️【残薬注意のお知らせ】\n" + lowStockList.joinToString("\n") {
                "・${it.name}: 残り${it.remainingCount}${it.unit}（約${it.estimatedRemainingDays()}日分）"
            } + "\n※早めの補充・受診をご検討ください。"
        } else ""

        return """
            【服薬連絡】
            日付: $dateStr
            状況: $statusSummary

            ■ 本日の服用一覧:
            $details$lowStockWarning

            （お薬飲んだ？ アプリより）
        """.trimIndent()
    }

    suspend fun createPdfReport(
        context: Context,
        patientName: String,
        periodMonths: Int
    ): File {
        val cal = Calendar.getInstance()
        val endDateStr = dateFormat.format(cal.time)

        cal.add(Calendar.DAY_OF_YEAR, - (periodMonths * 30))
        val startDateStr = dateFormat.format(cal.time)

        val medicines = activeMedicines.value
        val records = repository.getRecordsInRange(startDateStr, endDateStr)
        val recordList = records.first()

        val reportData = PdfReportGenerator.ReportData(
            patientName = patientName,
            startDate = startDateStr,
            endDate = endDateStr,
            medicines = medicines,
            records = recordList
        )

        return PdfReportGenerator.generatePdf(context, reportData)
    }
}
