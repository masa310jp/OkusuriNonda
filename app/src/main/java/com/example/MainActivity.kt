package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notification.MedicationAlarmScheduler
import com.example.notification.NotificationHelper
import com.example.ui.CalendarScreen
import com.example.ui.MedicineStockScreen
import com.example.ui.MedicationViewModel
import com.example.ui.ReportScreen
import com.example.ui.SettingsScreen
import com.example.ui.TodayScreen
import com.example.ui.theme.CoralLight
import com.example.ui.theme.CoralWarning
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealLight
import com.example.ui.theme.TealPrimary

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        // Create notification channels
        NotificationHelper.createNotificationChannels(this)
        MedicationAlarmScheduler.rescheduleAllUpcomingAlarms(this)

        setContent {
            val viewModel: MedicationViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
            MyApplicationTheme(darkTheme = isDarkMode) {
                MedicationReminderApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationReminderApp(
    viewModel: MedicationViewModel = viewModel()
) {
    val context = LocalContext.current
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val activeMedicines by viewModel.activeMedicines.collectAsStateWithLifecycle()
    val lowStockMedicines by viewModel.lowStockMedicines.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val todayRecords by viewModel.recordsForSelectedDate.collectAsStateWithLifecycle()
    val monthRecords by viewModel.recordsForSelectedMonth.collectAsStateWithLifecycle()
    val allRecords by viewModel.allRecords.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val hasPromptedName by viewModel.hasPromptedName.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    var showInitialNameDialog by remember { mutableStateOf(false) }
    var initialNameInput by remember { mutableStateOf("") }
    var showSettingsScreen by remember { mutableStateOf(false) }

    // Check if initial name popup should be shown on app launch
    LaunchedEffect(hasPromptedName, userName) {
        if (!hasPromptedName && userName.isBlank()) {
            showInitialNameDialog = true
        }
    }

    // Notification Permission check for Android 13+
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            MedicationAlarmScheduler.rescheduleAllUpcomingAlarms(context)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // アプリ起動時およびフォアグラウンド復帰時は常に「本日」を表示
                viewModel.resetToToday()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        // 起動時にも確実に本日を表示
        viewModel.resetToToday()
        if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    if (showSettingsScreen) {
        BackHandler {
            showSettingsScreen = false
        }
        SettingsScreen(
            viewModel = viewModel,
            onNavigateBack = { showSettingsScreen = false }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Medication,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "お薬飲んだ？",
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    },
                    actions = {
                        if (lowStockMedicines.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.selectTab(2) },
                                modifier = Modifier.testTag("top_bar_low_stock_alert")
                            ) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = CoralWarning,
                                            contentColor = Color.White
                                        ) {
                                            Text("${lowStockMedicines.size}")
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "残薬アラート",
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                        }
                        IconButton(
                            onClick = { showSettingsScreen = true },
                            modifier = Modifier.testTag("top_bar_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "設定",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = if (isDarkMode) MaterialTheme.colorScheme.surface else TealPrimary,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    // Tab 0: 今日のお薬 (タップで常に今日の日付・今日の画面にリセット)
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { viewModel.resetToToday() },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Today,
                                contentDescription = "今日のお薬",
                                modifier = Modifier.size(26.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "今日のお薬",
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TealPrimary,
                            selectedTextColor = TealPrimary,
                            indicatorColor = TealLight
                        ),
                        modifier = Modifier.testTag("nav_tab_today")
                    )

                    // Tab 1: カレンダー
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { viewModel.selectTab(1) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "カレンダー",
                                modifier = Modifier.size(26.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "カレンダー",
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TealPrimary,
                            selectedTextColor = TealPrimary,
                            indicatorColor = TealLight
                        ),
                        modifier = Modifier.testTag("nav_tab_calendar")
                    )

                    // Tab 2: お薬・残薬
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { viewModel.selectTab(2) },
                        icon = {
                            if (lowStockMedicines.isNotEmpty()) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = CoralWarning,
                                            contentColor = Color.White
                                        ) {
                                            Text("${lowStockMedicines.size}")
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Inventory2,
                                        contentDescription = "お薬・残薬",
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Inventory2,
                                    contentDescription = "お薬・残薬",
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        },
                        label = {
                            Text(
                                text = "お薬・残薬",
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TealPrimary,
                            selectedTextColor = TealPrimary,
                            indicatorColor = TealLight
                        ),
                        modifier = Modifier.testTag("nav_tab_stock")
                    )

                    // Tab 3: レポート出力
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { viewModel.selectTab(3) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = "レポート",
                                modifier = Modifier.size(26.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "レポート",
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TealPrimary,
                            selectedTextColor = TealPrimary,
                            indicatorColor = TealLight
                        ),
                        modifier = Modifier.testTag("nav_tab_report")
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Notification permission banner if not granted
                if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = CoralWarning,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "服薬通知を受け取るには許可が必要です",
                                    fontSize = 13.sp,
                                    color = Color(0xFFE65100)
                                )
                            }

                            Button(
                                onClick = {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CoralWarning),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("許可する", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Screen Tab Content
                when (selectedTab) {
                    0 -> TodayScreen(
                        viewModel = viewModel,
                        records = todayRecords,
                        lowStockMedicines = lowStockMedicines,
                        medicines = activeMedicines,
                        selectedDate = selectedDate,
                        onNavigateToStock = { viewModel.selectTab(2) }
                    )
                    1 -> CalendarScreen(
                        viewModel = viewModel,
                        medicines = activeMedicines,
                        selectedDate = selectedDate,
                        selectedMonth = selectedMonth,
                        monthRecords = monthRecords,
                        selectedDateRecords = todayRecords
                    )
                    2 -> MedicineStockScreen(
                        viewModel = viewModel,
                        medicines = activeMedicines,
                        lowStockMedicines = lowStockMedicines
                    )
                    3 -> ReportScreen(
                        viewModel = viewModel,
                        medicines = activeMedicines,
                        allRecords = allRecords
                    )
                }
            }
        }
    }

    // 初回起動時の名前入力ポップアップ
    if (showInitialNameDialog) {
        AlertDialog(
            onDismissRequest = {
                viewModel.markNamePrompted()
                showInitialNameDialog = false
            },
            properties = DialogProperties(decorFitsSystemWindows = false),
            modifier = Modifier.imePadding(),
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "お名前を入力してください",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "服薬管理やレポート出力で使用するお名前を登録してください（後から「設定」画面でも変更できます）。",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = initialNameInput,
                        onValueChange = { initialNameInput = it },
                        label = { Text("お名前") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("initial_name_popup_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = initialNameInput.trim()
                        if (trimmed.isNotEmpty()) {
                            viewModel.setUserName(trimmed)
                        }
                        viewModel.markNamePrompted()
                        showInitialNameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("initial_name_popup_save_button")
                ) {
                    Text("登録する", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.markNamePrompted()
                        showInitialNameDialog = false
                    }
                ) {
                    Text("あとで")
                }
            }
        )
    }
}
