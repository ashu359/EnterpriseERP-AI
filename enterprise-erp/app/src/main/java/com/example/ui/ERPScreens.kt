package com.example.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.content.Intent
import android.widget.Toast
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlinx.coroutines.launch

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

// --- Localization Dictionary ---
object Dictionary {
    fun get(key: String, lang: String): String {
        return when (lang) {
            "ES" -> when (key) {
                "dashboard" -> "Tablero"
                "inventory" -> "Inventario"
                "finance" -> "Finanzas"
                "hr" -> "Personal"
                "approvals" -> "Aprobaciones"
                "ai_assistant" -> "Asistente"
                "audit_logs" -> "Auditoría"
                "stock_value" -> "Valor de Inventario"
                "cash_reserve" -> "Saldo de Caja"
                "active_staff" -> "Personal Activo"
                "pending_tasks" -> "Requisitos Pendientes"
                "sync_banking" -> "Sincronizar Banco"
                "role_selection" -> "Autenticación de Rol"
                "current_user" -> "Usuario Autenticado"
                "add_item" -> "Agregar Artículo"
                "add_employee" -> "Agregar Empleado"
                "add_transaction" -> "Registrar Transmisión"
                "audit_subtitle" -> "Registro cronológico de acciones administrativas"
                else -> key
            }
            "DE" -> when (key) {
                "dashboard" -> "Dashboard"
                "inventory" -> "Inventar"
                "finance" -> "Finanzen"
                "hr" -> "Personalwesen"
                "approvals" -> "Freigaben"
                "ai_assistant" -> "Assistent"
                "audit_logs" -> "Prüfprotokoll"
                "stock_value" -> "Lagerwert"
                "cash_reserve" -> "Finanzsaldo"
                "active_staff" -> "Mitarbeiter"
                "pending_tasks" -> "Stehende Anträge"
                "sync_banking" -> "Bank-Synchronisierung"
                "role_selection" -> "Rollenberechtigung"
                "current_user" -> "Aktiver Benutzer"
                "add_item" -> "Artikel Hinzufügen"
                "add_employee" -> "Personal Erfassen"
                "add_transaction" -> "Buchen"
                "audit_subtitle" -> "Sicherheits-Audit-Logs für administrative Aktionen"
                else -> key
            }
            else -> when (key) { // Default EN
                "dashboard" -> "Dashboard"
                "inventory" -> "Inventory"
                "finance" -> "Finance"
                "hr" -> "HR"
                "approvals" -> "Approvals"
                "ai_assistant" -> "AI Assistant"
                "audit_logs" -> "Audit Logs"
                "stock_value" -> "Stock Valuation"
                "cash_reserve" -> "Cash Balance"
                "active_staff" -> "Active Staff"
                "pending_tasks" -> "Pending Requisitions"
                "sync_banking" -> "Sync Banking API"
                "role_selection" -> "Security Role Authentication"
                "current_user" -> "Authenticated User"
                "add_item" -> "Create Item"
                "add_employee" -> "Onboard Employee"
                "add_transaction" -> "Post Transaction"
                "audit_subtitle" -> "System-wide chronological ledger of secure transactions"
                else -> key
            }
        }
    }
}

enum class ERPModule {
    DASHBOARD,
    INVENTORY,
    FINANCE,
    HR,
    MANUFACTURING,
    DMS,
    ACCOUNTING,
    APPROVALS,
    CHATBOT,
    AUDIT_LOGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ERPSystemMainView(viewModel: ERPViewModel) {
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val currentUserName by viewModel.currentUserName.collectAsStateWithLifecycle()
    val activeNotification by viewModel.activeNotification.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val currentAuthenticatedUser by viewModel.currentAuthenticatedUser.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val twoFactorStatus by viewModel.twoFactorStatus.collectAsStateWithLifecycle()

    var activeModule by remember { mutableStateOf(ERPModule.DASHBOARD) }
    var showSqlConsole by remember { mutableStateOf(false) }
    var showGuidelinesDialog by remember { mutableStateOf(false) }
    var showNotificationCenter by remember { mutableStateOf(false) }
    var mobileViewActive by remember { mutableStateOf(false) }

    var startAnimation by remember { mutableStateOf(false) }

    val splashAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "splashAlpha"
    )

    val splashScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "splashScale"
    )

    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        startAnimation = true
        kotlinx.coroutines.delay(2000)
        startAnimation = false
        kotlinx.coroutines.delay(800)
        showSplash = false
    }

    MyApplicationTheme(darkTheme = isDarkMode) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (showSplash) {
                SplashScreen(alpha = splashAlpha, scale = splashScale)
            } else {
                if (showSqlConsole) {
                SQLConsoleDialog(
                    viewModel = viewModel,
                    onDismiss = { showSqlConsole = false }
                )
            }
            if (showGuidelinesDialog) {
                GuidelinesDialog(onDismiss = { showGuidelinesDialog = false })
            }
            if (showNotificationCenter) {
                NotificationCenterDialog(
                    viewModel = viewModel,
                    onDismiss = { showNotificationCenter = false }
                )
            }

            if (currentAuthenticatedUser == null) {
                if (twoFactorStatus == "PROMPTED") {
                    TwoFactorScreen(viewModel = viewModel)
                } else {
                    AuthScreen(viewModel = viewModel, currentLanguage = currentLanguage)
                }
            } else {
                val scaffoldContent = @Composable { paddingValues: PaddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        val mainScrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .then(
                                    if (activeModule != ERPModule.CHATBOT) {
                                        Modifier.verticalScroll(mainScrollState)
                                    } else {
                                        Modifier
                                    }
                                )
                                .padding(16.dp)
                        ) {
                            // Security Segmented Access controls
                            RoleSelectorCard(
                                currentRole = currentRole,
                                currentUserName = currentUserName,
                                onRoleSelected = { viewModel.switchRole(it) },
                                currentLang = currentLanguage
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action Status alert banner
                            if (activeNotification != null) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("action_status_notification")
                                        .animateContentSize(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = AlertRed)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = activeNotification ?: "",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(onClick = { viewModel.clearActiveNotification() }) {
                                            Icon(Icons.Default.Close, contentDescription = "Close", tint = AlertRed)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                            }

                            // Dynamic module router
                            when (activeModule) {
                                ERPModule.DASHBOARD -> DashboardModule(viewModel, currentLanguage) { activeModule = it }
                                ERPModule.INVENTORY -> InventoryModule(viewModel, currentLanguage)
                                ERPModule.FINANCE -> FinanceModule(viewModel, currentLanguage)
                                ERPModule.HR -> HRModule(viewModel, currentLanguage)
                                ERPModule.APPROVALS -> ApprovalsModule(viewModel, currentLanguage)
                                ERPModule.CHATBOT -> ChatbotModule(viewModel)
                                ERPModule.AUDIT_LOGS -> AuditLogsModule(viewModel, currentLanguage)
                                ERPModule.MANUFACTURING -> ManufacturingModule(viewModel, currentLanguage)
                                ERPModule.DMS -> DmsModule(viewModel, currentLanguage)
                                ERPModule.ACCOUNTING -> AccountingModule(viewModel, currentLanguage)
                            }
                        }
                    }
                }

                // Core Scaffold Layout
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Dns,
                                        contentDescription = "ERP Host",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Enterprise ERP",
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 18.sp
                                    )
                                }
                            },
                            actions = {
                                // Language Selector
                                TextButton(
                                    onClick = {
                                        val nextLang = when (currentLanguage) {
                                            "EN" -> "ES"
                                            "ES" -> "DE"
                                            else -> "EN"
                                        }
                                        viewModel.setLanguage(nextLang)
                                    },
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text(currentLanguage, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                // Mobile View toggle icon
                                IconButton(onClick = { mobileViewActive = !mobileViewActive }) {
                                    Icon(
                                        imageVector = if (mobileViewActive) Icons.Default.DesktopMac else Icons.Default.Smartphone,
                                        contentDescription = "Toggle Companion Layout",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                // Interactive Notification Bell with Badge
                                IconButton(
                                    onClick = { showNotificationCenter = true },
                                    modifier = Modifier.testTag("notif_bell")
                                ) {
                                    BadgedBox(
                                        badge = {
                                            val unread = notifications.filter { !it.isRead }.size
                                            if (unread > 0) {
                                                Badge(containerColor = AlertRed) {
                                                    Text("$unread", color = Color.White, fontSize = 10.sp)
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Notifications, contentDescription = "Alerts")
                                    }
                                }

                                // SQLite Console dialog launch button
                                IconButton(
                                    onClick = { showSqlConsole = true },
                                    modifier = Modifier.testTag("terminal_trigger")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Terminal,
                                        contentDescription = "SQLite Workspace console",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                IconButton(onClick = { showGuidelinesDialog = true }) {
                                    Icon(Icons.Default.HelpOutline, contentDescription = "Guides", tint = MaterialTheme.colorScheme.primary)
                                }

                                IconButton(onClick = { viewModel.toggleDarkMode() }) {
                                    Icon(
                                        imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                        contentDescription = "Toggle Dark Mode",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                IconButton(onClick = { viewModel.logout() }) {
                                    Icon(Icons.Default.Logout, contentDescription = "Log out", tint = AlertRed)
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                titleContentColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    },
                    bottomBar = {
                        // Responsive Bottom Bar supporting standard tabs + Quick "MORE" Menu Sheet
                        var showMoreMenu by remember { mutableStateOf(false) }

                        if (showMoreMenu) {
                            MoreModulesBottomSheet(
                                activeModule = activeModule,
                                onSelect = {
                                    activeModule = it
                                    showMoreMenu = false
                                },
                                onDismissRequest = { showMoreMenu = false }
                            )
                        }

                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            NavigationBarItem(
                                selected = activeModule == ERPModule.DASHBOARD,
                                onClick = { activeModule = ERPModule.DASHBOARD },
                                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                                label = { Text(Dictionary.get("dashboard", currentLanguage), fontSize = 10.sp, maxLines = 1) },
                                modifier = Modifier.testTag("nav_dashboard")
                            )
                            NavigationBarItem(
                                selected = activeModule == ERPModule.INVENTORY,
                                onClick = { activeModule = ERPModule.INVENTORY },
                                icon = { Icon(Icons.Default.Inventory, contentDescription = "Inventory") },
                                label = { Text(Dictionary.get("inventory", currentLanguage), fontSize = 10.sp, maxLines = 1) },
                                modifier = Modifier.testTag("nav_inventory")
                            )
                            NavigationBarItem(
                                selected = activeModule == ERPModule.FINANCE,
                                onClick = { activeModule = ERPModule.FINANCE },
                                icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Finance") },
                                label = { Text(Dictionary.get("finance", currentLanguage), fontSize = 10.sp, maxLines = 1) },
                                modifier = Modifier.testTag("nav_finance")
                            )
                            NavigationBarItem(
                                selected = activeModule == ERPModule.HR,
                                onClick = { activeModule = ERPModule.HR },
                                icon = { Icon(Icons.Default.Groups, contentDescription = "HR") },
                                label = { Text(Dictionary.get("hr", currentLanguage), fontSize = 10.sp, maxLines = 1) },
                                modifier = Modifier.testTag("nav_hr")
                            )
                            NavigationBarItem(
                                selected = false,
                                onClick = { showMoreMenu = true },
                                icon = { Icon(Icons.Default.Menu, contentDescription = "More") },
                                label = { Text("Modules", fontSize = 10.sp, maxLines = 1, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.testTag("nav_more")
                            )
                        }
                    }
                ) { innerPadding ->
                    if (mobileViewActive) {
                        // Companion Mobile Device Frame Wrapper
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.9f))
                                .padding(innerPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                modifier = Modifier
                                    .width(360.dp)
                                    .height(640.dp)
                                    .padding(8.dp)
                                    .border(4.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(28.dp))
                                    .shadow(16.dp, RoundedCornerShape(26.dp)),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                                shape = RoundedCornerShape(26.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    // Simulated System Mobile Header
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .padding(horizontal = 20.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("12:00 PM 🕛", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Icon(Icons.Default.BatteryChargingFull, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Text("4G", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                                        }
                                    }

                                    // Compact mobile scrollable content workspace
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                            .verticalScroll(rememberScrollState())
                                            .padding(12.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)).padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Mobile Companion Hub", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                IconButton(onClick = { mobileViewActive = false }) {
                                                    Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                                                }
                                            }

                                            Text("Quick attendance action clock:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                            Button(
                                                onClick = {
                                                    viewModel.sendNotification("PUSH", "Attendance Logged", "Logged clock event mobile.", "Leave Approved")
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("⏱️ Simulated Mobile Clock-In")
                                            }

                                            HorizontalDivider()

                                            Text("Active notifications list:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            notifications.take(3).forEach { notif ->
                                                Card(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Column(modifier = Modifier.padding(10.dp)) {
                                                        Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                        Text(notif.message, fontSize = 11.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        scaffoldContent(innerPadding)
                    }
                }
            }
            }
        }
    }
}

// --- OPTION MODULE COMPANION MENU SHEET ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreModulesBottomSheet(
    activeModule: ERPModule,
    onSelect: (ERPModule) -> Unit,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 24.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "SYSTEM RESOURCE CLUSTER MODULES",
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            // Grid of all 10 Modules
            val rows = listOf(
                listOf(
                    Triple(ERPModule.MANUFACTURING, "Manufacturing", Icons.Default.Factory),
                    Triple(ERPModule.DMS, "Documents DMS", Icons.Default.Folder),
                    Triple(ERPModule.ACCOUNTING, "Double Entry Acc", Icons.Default.AccountBalance)
                ),
                listOf(
                    Triple(ERPModule.APPROVALS, "Approvals Flow", Icons.Default.Draw),
                    Triple(ERPModule.CHATBOT, "AI Copilot Command", Icons.Default.SupportAgent),
                    Triple(ERPModule.AUDIT_LOGS, "System Audit Trail", Icons.Default.ReceiptLong)
                )
            )

            rows.forEach { col ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    col.forEach { (module, title, icon) ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onSelect(module) }
                                .testTag("more_nav_${module.name.lowercase()}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (activeModule == module) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(icon, contentDescription = title, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 2)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- WIDGET ROLE SELECTOR CARD ---
@Composable
fun RoleSelectorCard(
    currentRole: ERPRole,
    currentUserName: String,
    onRoleSelected: (ERPRole) -> Unit,
    currentLang: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${Dictionary.get("role_selection", currentLang)}: ${currentRole.name}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${Dictionary.get("current_user", currentLang)}: $currentUserName",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                ERPRole.values().forEach { role ->
                    Button(
                        onClick = { onRoleSelected(role) },
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .testTag("role_switch_${role.name.lowercase()}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentRole == role) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = if (currentRole == role) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(role.name, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                    }
                }
            }
        }
    }
}

// --- MODULE: SYSTEM DASHBOARD & ADVANCED ANALYTICS ---
@Composable
fun DashboardModule(
    viewModel: ERPViewModel,
    lang: String,
    onNavigate: (ERPModule) -> Unit
) {
    val items by viewModel.inventoryItems.collectAsStateWithLifecycle()
    val txs by viewModel.transactions.collectAsStateWithLifecycle()
    val staff by viewModel.employees.collectAsStateWithLifecycle()
    val approvals by viewModel.approvalRequests.collectAsStateWithLifecycle()
    val bankingStatus by viewModel.bankingStatus.collectAsStateWithLifecycle()

    var executiveViewEnabled by remember { mutableStateOf(false) }

    // Analytics Metrics
    val totalStockVal = items.sumOf { it.quantity * it.price }
    val netRevenue = txs.filter { it.type == "REVENUE" }.sumOf { it.amount }
    val netExpenses = txs.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val cashReserve = netRevenue - netExpenses
    val pendingRequisitions = approvals.filter { it.status == "PENDING" }.size

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = Dictionary.get("dashboard", lang),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // View Toggler (CEO/General)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .clickable { executiveViewEnabled = !executiveViewEnabled }
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(Icons.Default.Insights, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (executiveViewEnabled) "CEO View (Active)" else "Standard View",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Core Metrics
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MetricCard(
            title = Dictionary.get("stock_value", lang),
            value = String.format("$%,.2f", totalStockVal),
            icon = Icons.Default.Inventory2,
            iconColor = WarningAmber,
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            title = Dictionary.get("cash_reserve", lang),
            value = String.format("$%,.2f", cashReserve),
            icon = Icons.Default.Savings,
            iconColor = CompletedGreen,
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(modifier = Modifier.height(10.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MetricCard(
            title = Dictionary.get("active_staff", lang),
            value = "${staff.size} members",
            icon = Icons.Default.Badge,
            iconColor = ErpPrimaryDark,
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            title = Dictionary.get("pending_tasks", lang),
            value = "$pendingRequisitions Reqs",
            icon = Icons.Default.PendingActions,
            iconColor = AlertRed,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(14.dp))

    if (executiveViewEnabled) {
        // --- ADANCED EXECUTIVE CEO HUB ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "📊 EXECUTIVE TREND ANALYSIS & PREDICTIONS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Custom Canvas Drawn Chart (Revenue Line & Expense Bars)
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    val width = size.width
                    val height = size.height

                    // Draw Background Grid Lines
                    val gridLines = 4
                    for (i in 0..gridLines) {
                        val y = (height / gridLines) * i
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.2f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Simulated Revenue Trend points (Lines)
                    val points = listOf(
                        Offset(00f, height * 0.8f),
                        Offset(width * 0.2f, height * 0.6f),
                        Offset(width * 0.4f, height * 0.9f),
                        Offset(width * 0.6f, height * 0.4f),
                        Offset(width * 0.8f, height * 0.3f),
                        Offset(width, height * 0.2f)
                    )

                    for (i in 0 until points.size - 1) {
                        drawLine(
                            color = CompletedGreen,
                            start = points[i],
                            end = points[i + 1],
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    // Cumulative indicators
                    drawCircle(color = CompletedGreen, radius = 4.dp.toPx(), center = points.last())
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("May 1", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("May 15", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("May 30 (Today)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                // Productivity and Stock Turnover KPIs
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Productivity Index", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("94.2% Outstanding", fontWeight = FontWeight.Black, fontSize = 13.sp, color = CompletedGreen)
                        }
                    }
                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Inventory Turnover", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("4.85x High Ratio", fontWeight = FontWeight.Black, fontSize = 13.sp, color = WarningAmber)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Generative AI Business Insights Panel
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, "AI", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AI EXECUTIVE FORECAST & RECOMMENDATION", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Due to microchip inventory depletion in Nagoya Hub, production order run Circuit Board Assemblies must trigger raw supply reorders. May financial ledger suggests a net savings margin increase of 12% following streamlined staff allocation. Pune logistics has the highest performance score of 98%. Recommend bulk purchasing raw Kraft materials ahead of next-quarter sales surge.",
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    } else {
        // Standard Banking Api & Cash flow charts
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "SECURED BANKING RECONCILIATION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(if (bankingStatus == "SYNCING") WarningAmber else CompletedGreen, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (bankingStatus == "SYNCING") "System Synchronizing..." else "Host Reconciled (99.9% Uptime SLA)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { viewModel.syncBankingAPI() },
                        enabled = bankingStatus != "SYNCING",
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        if (bankingStatus == "SYNCING") {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("Reconcile", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Standard Cashflow graph
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "CASHFLOW LEDGER BREAKDOWN",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    val totalLedger = netRevenue + netExpenses
                    if (totalLedger > 0) {
                        val revPercent = (netRevenue / totalLedger).toFloat()
                        val expPercent = (netExpenses / totalLedger).toFloat()
                        Box(modifier = Modifier.weight(revPercent).fillMaxHeight().background(CompletedGreen))
                        Box(modifier = Modifier.weight(expPercent).fillMaxHeight().background(AlertRed))
                    } else {
                        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).background(CompletedGreen, CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Revenues ($${String.format("%,.0f", netRevenue)})", fontSize = 11.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).background(AlertRed, CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Expenses ($${String.format("%,.0f", netExpenses)})", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { onNavigate(ERPModule.APPROVALS) }, modifier = Modifier.weight(1f)) {
                        Text("Approvals Grid", fontSize = 11.sp)
                    }
                    OutlinedButton(onClick = { onNavigate(ERPModule.CHATBOT) }, modifier = Modifier.weight(1f)) {
                        Text("Chat Assistant", fontSize = 11.sp)
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Operational Goal list
    var g1 by remember { mutableStateOf(false) }
    var g2 by remember { mutableStateOf(false) }
    var g3 by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("OPERATIONAL GOALS CHECKLIST", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(8.dp))

            listOf(
                Triple("Observe Low Stock flags", "Look at Pune database inventory.", g1 to { g1 = !g1 }),
                Triple("Audit Salary Log Adjustments", "Ensure old/new salary delta tracks on employees.", g2 to { g2 = !g2 }),
                Triple("Verify double entry credits", "Check chart of accounts records balance sheets.", g3 to { g3 = !g3 })
            ).forEach { (h, b, actions) ->
                val (checked, onToggle) = actions
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onToggle() }.padding(vertical = 4.dp).fillMaxWidth()) {
                    Checkbox(checked = checked, onCheckedChange = { onToggle() })
                    Spacer(modifier = Modifier.width(4.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(h, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (checked) Color.Gray else MaterialTheme.colorScheme.onSurface)
                        Text(b, fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = title.uppercase(Locale.ROOT), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = iconColor)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

// --- MODULE: INVENTORY & BARCODE SYSTEMS ---
@Composable
fun InventoryModule(viewModel: ERPViewModel, lang: String) {
    val items by viewModel.inventoryItems.collectAsStateWithLifecycle()
    val warehouse by viewModel.selectedWarehouse.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Inventory Manager", fontSize = 21.sp, fontWeight = FontWeight.Bold)
            Text("Warehousing operations & barcodes", fontSize = 11.sp, color = Color.Gray)
        }
        Spacer(modifier = Modifier.width(8.dp))
        // Warehouse filter
        var showMenu by remember { mutableStateOf(false) }
        Box(modifier = Modifier.wrapContentWidth()) {
            Button(
                onClick = { showMenu = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                contentPadding = PaddingValues(horizontal = 10.dp)
            ) {
                Text(warehouse, fontSize = 11.sp, maxLines = 1, softWrap = false)
                Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp))
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                listOf("All Warehouses", "Pune Warehouse", "Nagpur Warehouse", "Mumbai Warehouse").forEach { w ->
                    DropdownMenuItem(text = { Text(w) }, onClick = {
                        viewModel.selectedWarehouse.value = w
                        showMenu = false
                    })
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    Button(
        onClick = { showAddDialog = true },
        modifier = Modifier.fillMaxWidth().testTag("add_inventory_trigger")
    ) {
        Icon(Icons.Default.Add, "Add")
        Spacer(modifier = Modifier.width(6.dp))
        Text("Create New Inventory Item")
    }

    Spacer(modifier = Modifier.height(14.dp))

    // List of cataloged items
    val filtered = if (warehouse == "All Warehouses") items else items.filter { it.warehouse == warehouse }
    if (filtered.isEmpty()) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Text("No inventory items found in $warehouse.", modifier = Modifier.padding(24.dp), textAlign = TextAlign.Center, color = Color.Gray)
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            filtered.forEach { item ->
                val isLow = item.quantity <= item.reorderPoint
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = if (isLow) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("SKU: ${item.sku} | ${item.category}", fontSize = 11.sp, color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = String.format("$%,.2f", item.price),
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                modifier = Modifier.wrapContentWidth()
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Stock: ${item.quantity} units (Reorder point: ${item.reorderPoint})", fontSize = 12.sp)
                                Text("Warehouse: ${item.warehouse}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.wrapContentWidth()
                            ) {
                                Button(
                                    onClick = { viewModel.updateInventoryStock(item, item.quantity + 10) },
                                    contentPadding = PaddingValues(horizontal = 2.dp),
                                    modifier = Modifier.height(30.dp).width(46.dp)
                                ) {
                                    Text("+10", fontSize = 10.sp, maxLines = 1, softWrap = false)
                                }
                                Button(
                                    onClick = { if (item.quantity >= 10) viewModel.updateInventoryStock(item, item.quantity - 10) },
                                    contentPadding = PaddingValues(horizontal = 2.dp),
                                    modifier = Modifier.height(30.dp).width(46.dp)
                                ) {
                                    Text("-10", fontSize = 10.sp, maxLines = 1, softWrap = false)
                                }
                                IconButton(
                                    onClick = { viewModel.deleteInventoryItem(item) },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.Delete, "Delete", tint = AlertRed, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Simulated Barcode visualization
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.QrCode, null, modifier = Modifier.size(20.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.width(10.dp))

                            // Draw barcode lines with adaptive color
                            val barcodeColor = MaterialTheme.colorScheme.onSurface
                            Canvas(modifier = Modifier.width(120.dp).height(18.dp)) {
                                val barWidth = 3f
                                var x = 0f
                                while (x < size.width) {
                                    val isBar = Random.nextBoolean()
                                    if (isBar) {
                                        drawRect(
                                            color = barcodeColor,
                                            topLeft = Offset(x, 0f),
                                            size = Size(barWidth, size.height)
                                        )
                                    }
                                    x += barWidth + 2f
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("*${item.sku}*", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color.Gray)
                            Spacer(modifier = Modifier.weight(1f))
                            TextButton(onClick = { viewModel.sendNotification("PUSH", "Label Printed", "Dispatched stock barcode label to Nagoya terminal.", "PO Approved") }) {
                                Text("Print Tag", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var inputSku by remember { mutableStateOf("") }
        var autogenSku by remember { mutableStateOf(true) }
        var qty by remember { mutableStateOf("100") }
        var reorder by remember { mutableStateOf("40") }
        var price by remember { mutableStateOf("12.50") }
        var cat by remember { mutableStateOf("Raw Materials") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Record Catalog item") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item Name") })
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = autogenSku, onCheckedChange = { autogenSku = it })
                        Text("Autogenerate SKU sequence", fontSize = 12.sp)
                    }
                    if (!autogenSku) {
                        OutlinedTextField(value = inputSku, onValueChange = { inputSku = it }, label = { Text("Manual SKU") })
                    }
                    OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Quantity") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = reorder, onValueChange = { reorder = it }, label = { Text("Reorder Point") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
            },
            confirmButton = {
                Button(onClick = {
                    val finalSku = if (autogenSku) "Autogenerated" else inputSku
                    viewModel.addInventoryItem(name, finalSku, qty.toIntOrNull() ?: 0, reorder.toIntOrNull() ?: 0, price.toDoubleOrNull() ?: 0.0, cat)
                    showAddDialog = false
                }) { Text("Save Item") }
            }
        )
    }
}

// --- MODULE: FINANCE TRANSACTIONS ---
@Composable
fun FinanceModule(viewModel: ERPViewModel, lang: String) {
    val txs by viewModel.transactions.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Finance Transactions", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Accounts cash allocations ledger", fontSize = 11.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { showAddDialog = true }, modifier = Modifier.testTag("add_tx_button").wrapContentWidth()) {
                Text("Post Ledger", maxLines = 1, softWrap = false)
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            txs.forEach { tx ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(tx.category, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(tx.description, fontSize = 11.sp, color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = if (tx.type == "REVENUE") "+$${tx.amount}" else "-$${tx.amount}",
                                fontWeight = FontWeight.Black,
                                color = if (tx.type == "REVENUE") CompletedGreen else AlertRed,
                                maxLines = 1,
                                modifier = Modifier.wrapContentWidth()
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var type by remember { mutableStateOf("EXPENSE") }
        var sum by remember { mutableStateOf("600") }
        var cat by remember { mutableStateOf("Inventory Purchases") }
        var desc by remember { mutableStateOf("Purchase of raw cardboards supplies") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Log Cash transaction") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row {
                        Button(onClick = { type = "REVENUE" }, colors = ButtonDefaults.buttonColors(containerColor = if (type == "REVENUE") CompletedGreen else Color.LightGray)) { Text("Revenue") }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(onClick = { type = "EXPENSE" }, colors = ButtonDefaults.buttonColors(containerColor = if (type == "EXPENSE") AlertRed else Color.LightGray)) { Text("Expense") }
                    }
                    OutlinedTextField(value = sum, onValueChange = { sum = it }, label = { Text("Amount") })
                    OutlinedTextField(value = cat, onValueChange = { cat = it }, label = { Text("Category") })
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Brief details") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.addFinanceTransaction(type, sum.toDoubleOrNull() ?: 0.0, cat, desc)
                    showAddDialog = false
                }) { Text("Post") }
            }
        )
    }
}

// --- MODULE: HRMS ADVANCED RECRUITMENT ---
@Composable
fun HRModule(viewModel: ERPViewModel, lang: String) {
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val candidates by viewModel.jobCandidates.collectAsStateWithLifecycle()
    val hrError by viewModel.hrError.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0 = Directory, 1 = Recruitment Hub, 2 = Performance Scorecards
    var showAddDialog by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Human Resources", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text("Staff directories, performances & interviews", fontSize = 11.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (activeTab == 0) {
                Button(onClick = { showAddDialog = true }, modifier = Modifier.testTag("onboard_employee_button").wrapContentWidth()) {
                    Text("Onboard", maxLines = 1, softWrap = false)
                }
            }
        }

        // Subtabs
        TabRow(selectedTabIndex = activeTab) {
            Tab(selected = activeTab == 0, onClick = { activeTab = 0 }) { Text("Directory", fontSize = 12.sp, modifier = Modifier.padding(10.dp)) }
            Tab(selected = activeTab == 1, onClick = { activeTab = 1 }) { Text("Recruitment Hub", fontSize = 12.sp, modifier = Modifier.padding(10.dp)) }
            Tab(selected = activeTab == 2, onClick = { activeTab = 2 }) { Text("Performance Score", fontSize = 12.sp, modifier = Modifier.padding(10.dp)) }
        }

        if (hrError != null) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(hrError ?: "", color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    IconButton(onClick = { viewModel.clearHrError() }) { Icon(Icons.Default.Close, null, tint = AlertRed) }
                }
            }
        }

        when (activeTab) {
            0 -> {
                // Employees directories list
                employees.forEach { emp ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Vertically centered person/avatar icon
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Employee Avatar",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(emp.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Department: ${emp.department} | Role: ${emp.role}", fontSize = 11.sp, color = Color.Gray)
                                        Text("Email: ${emp.email}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "₹${String.format("%,.0f", emp.salary)}/mo",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.wrapContentWidth()
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            // Salary Adjustments form
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1.2f)
                                ) {
                                    var textSalary by remember { mutableStateOf("") }
                                    OutlinedTextField(
                                        value = textSalary,
                                        onValueChange = { textSalary = it },
                                        placeholder = { Text("New salary", fontSize = 10.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f).height(45.dp),
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp),
                                        singleLine = true
                                    )
                                    Button(
                                        onClick = {
                                            val amt = textSalary.toDoubleOrNull()
                                            if (amt != null) {
                                                viewModel.updateEmployeeSalary(emp.id, amt)
                                                textSalary = ""
                                            }
                                        },
                                        modifier = Modifier.height(34.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp)
                                    ) {
                                        Text("Apply", fontSize = 9.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                TextButton(
                                    onClick = { viewModel.toggleClockStatus(emp) },
                                    modifier = Modifier.weight(0.8f).wrapContentWidth(Alignment.End),
                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = if (emp.isClockedIn) "Clocked In 🟢" else "Clocked Out 🔴",
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // Recruitment candidates pipe hub
                Text("Active Candidate pipeline board:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                var newCandName by remember { mutableStateOf("") }
                var newCandPos by remember { mutableStateOf("") }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(value = newCandName, onValueChange = { newCandName = it }, placeholder = { Text("Candidate Name", fontSize = 11.sp) }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = newCandPos, onValueChange = { newCandPos = it }, placeholder = { Text("Position Title", fontSize = 11.sp) }, modifier = Modifier.weight(1f))
                    Button(onClick = {
                        if (newCandName.isNotEmpty() && newCandPos.isNotEmpty()) {
                            viewModel.addCandidate(newCandName, newCandPos)
                            newCandName = ""
                            newCandPos = ""
                        }
                    }) { Text("Add") }
                }

                Spacer(modifier = Modifier.height(10.dp))

                candidates.forEach { cand ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(cand.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Applied position: ${cand.position}", fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                        .wrapContentWidth()
                                ) {
                                    Text(
                                        text = "Workflow Step: ${cand.stage}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            if (cand.stage != "Hired") {
                                Button(
                                    onClick = { viewModel.advanceCandidate(cand.id) },
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    modifier = Modifier.height(32.dp).wrapContentWidth()
                                ) {
                                    Text("Advance Node", fontSize = 10.sp, maxLines = 1, softWrap = false)
                                }
                            } else {
                                Text(
                                    text = "Hired Profile Loaded ✅",
                                    fontSize = 10.sp,
                                    color = CompletedGreen,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.wrapContentWidth()
                                )
                            }
                        }
                    }
                }
            }
            2 -> {
                // Performance KPIs Scorecards
                employees.forEach { emp ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(emp.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("KPI Analytics targets scorecard review:", fontSize = 11.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))

                            listOf(
                                "Attendance Rate" to emp.attendanceScore / 100f,
                                "Task Speed Completion" to emp.taskCompletion / 100f,
                                "Sales Targets Secured" to emp.salesTarget / 100f
                            ).forEach { (lbl, pct) ->
                                Column {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(lbl, fontSize = 11.sp)
                                        Text("${(pct * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    LinearProgressIndicator(
                                        progress = { pct },
                                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                        color = if (pct > 0.9f) CompletedGreen else WarningAmber
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var dept by remember { mutableStateOf("Finance") }
        var sal by remember { mutableStateOf("75000") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Profile Onboarding") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Corporate Email") })
                    OutlinedTextField(value = dept, onValueChange = { dept = it }, label = { Text("Department") })
                    OutlinedTextField(value = sal, onValueChange = { sal = it }, label = { Text("Monthly Salary (₹)") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.addEmployee(name, email, dept, sal.toDoubleOrNull() ?: 0.0, "EMPLOYEE")
                    showAddDialog = false
                }) { Text("Onboard Staff") }
            }
        )
    }
}

// --- MODULE: MANUFACTURING & BOM TELEMENTRY ---
@Composable
fun ManufacturingModule(viewModel: ERPViewModel, lang: String) {
    val orders by viewModel.manufacturingOrders.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Manufacturing Hub", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text("BOM layouts, telemetry machines & nodes", fontSize = 11.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { showAddDialog = true }, modifier = Modifier.wrapContentWidth()) {
                Text("Queue Production", maxLines = 1, softWrap = false)
            }
        }

        orders.forEach { order ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(order.productName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("BOM: ${order.bomDetails}", fontSize = 11.sp)
                            Text("Work Cell: ${order.workCenter} | Machine: ${order.machineUsed}", fontSize = 11.sp, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .wrapContentWidth()
                        ) {
                            Text(
                                text = order.status,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Machine Live telemetries simulated stats
                    if (order.status == "MANUFACTURING") {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                            Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("⚙️ RPM: 3420", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CompletedGreen)
                                Text("🌡️ Temp: 68°C", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WarningAmber)
                                Text("🟢 Telemetry: Connected", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Units ordered: ${order.quantity}", fontSize = 12.sp)
                            Text("Quality check: ${order.qualityStatus}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (order.qualityStatus == "APPROVED") CompletedGreen else AlertRed)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.wrapContentWidth()
                        ) {
                            if (order.status == "QUALITY_CHECK" && order.qualityStatus == "PENDING") {
                                Button(
                                    onClick = { viewModel.conductQualityCheck(order, true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CompletedGreen),
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("QA Pass", fontSize = 10.sp, maxLines = 1, softWrap = false)
                                }
                                Button(
                                    onClick = { viewModel.conductQualityCheck(order, false) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("QA Fail", fontSize = 10.sp, maxLines = 1, softWrap = false)
                                }
                            } else if (order.status != "FINISHED_GOODS") {
                                Button(
                                    onClick = { viewModel.advanceManufacturing(order) },
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Next Step", fontSize = 10.sp, maxLines = 1, softWrap = false)
                                }
                            } else {
                                Text(
                                    text = "Run Certified ✅",
                                    fontSize = 11.sp,
                                    color = CompletedGreen,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.wrapContentWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var product by remember { mutableStateOf("Circuit Board Assembly") }
        var qty by remember { mutableStateOf("50") }
        var bom by remember { mutableStateOf("50 Microchips, 200 Screws") }
        var ws by remember { mutableStateOf("Acoustic Workstation B") }
        var mach by remember { mutableStateOf("Assembly Mech v3") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Schedule production run") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = product, onValueChange = { product = it }, label = { Text("Product Name") })
                    OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Quantity") })
                    OutlinedTextField(value = bom, onValueChange = { bom = it }, label = { Text("BOM details") })
                    OutlinedTextField(value = ws, onValueChange = { ws = it }, label = { Text("Work center room") })
                    OutlinedTextField(value = mach, onValueChange = { mach = it }, label = { Text("Equipment Name") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.addManufacturingOrder(product, qty.toIntOrNull() ?: 10, bom, ws, mach)
                    showAddDialog = false
                }) { Text("Dispatch Run") }
            }
        )
    }
}

// --- MODULE: DOCUMENT MANAGEMENT SYSTEM (DMS) ---
@Composable
fun DmsModule(viewModel: ERPViewModel, lang: String) {
    val documents by viewModel.documents.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    var previewDocName by remember { mutableStateOf<String?>(null) }

    if (previewDocName != null) {
        AlertDialog(
            onDismissRequest = { previewDocName = null },
            title = { Text("PDF View Document: $previewDocName") },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color.White)
                        .border(1.dp, Color.Gray)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(64.dp), tint = AlertRed)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("[SECURED PDF PREVIEW MOCK]", fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("Government-Archived records successfully parsed. Verification Certificate Active.", fontSize = 11.sp, color = Color.DarkGray, textAlign = TextAlign.Center)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { previewDocName = null }) { Text("Close Screen") }
            }
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("DMS Cloud Vault", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text("Archived corporate certifications & Aadhaar/PAN", fontSize = 11.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.wrapContentWidth()
            ) {
                Text("Upload Card", maxLines = 1, softWrap = false)
            }
        }

        documents.forEach { doc ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = doc.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text("Category: ${doc.category} | Version: v${doc.version}", fontSize = 11.sp)
                                Text("Expiry Date: ${doc.expiryDate}", fontSize = 11.sp, color = if (doc.expiryDate.contains("2026")) AlertRed else Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.wrapContentWidth()
                        ) {
                            TextButton(
                                onClick = { previewDocName = doc.name },
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Text("Preview", maxLines = 1, softWrap = false)
                            }
                            TextButton(
                                onClick = { viewModel.deleteDocument(doc.id) },
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Text("Delete", color = AlertRed, maxLines = 1, softWrap = false)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("PAN_Card_Alice_Corp.pdf") }
        var category by remember { mutableStateOf("PAN") }
        var expiry by remember { mutableStateOf("N/A") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Upload Secure Document") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("File Name") })
                    OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category (Aadhaar, PAN, Offer Letter, etc)") })
                    OutlinedTextField(value = expiry, onValueChange = { expiry = it }, label = { Text("Expiry Expiration") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.uploadDocument(name, category, expiry)
                    showAddDialog = false
                }) { Text("Archived Save") }
            }
        )
    }
}

// --- MODULE: DOUBLE-ENTRY ACCOUNTING ---
@Composable
fun AccountingModule(viewModel: ERPViewModel, lang: String) {
    val coas by viewModel.chartOfAccounts.collectAsStateWithLifecycle()
    val journals by viewModel.journalEntries.collectAsStateWithLifecycle()

    var showEntryDialog by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Accounting Ledger", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text("Double entry accounts balance sheets", fontSize = 11.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { showEntryDialog = true }, modifier = Modifier.wrapContentWidth()) {
                Text("Record Entry", maxLines = 1, softWrap = false)
            }
        }

        var tabIndex by remember { mutableStateOf(0) }
        TabRow(selectedTabIndex = tabIndex) {
            Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }) { Text("Chart of Accounts", fontSize = 11.sp, modifier = Modifier.padding(10.dp)) }
            Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }) { Text("Journal Log", fontSize = 11.sp, modifier = Modifier.padding(10.dp)) }
        }

        if (tabIndex == 0) {
            coas.forEach { account ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("[${account.code}] ${account.name}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Type: ${account.type}", fontSize = 11.sp, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "$${String.format("%,.2f", account.balance)}",
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            modifier = Modifier.wrapContentWidth()
                        )
                    }
                }
            }
        } else {
            journals.forEach { j ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(j.description, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Debit Acct: ${j.debitAccount} | Credit Acct: ${j.creditAccount}",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "$${String.format("%,.2f", j.amount)}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                modifier = Modifier.wrapContentWidth()
                            )
                        }
                    }
                }
            }
        }
    }

    if (showEntryDialog) {
        var desc by remember { mutableStateOf("Manual cash correction post") }
        var debitAcct by remember { mutableStateOf("1010") }
        var creditAcct by remember { mutableStateOf("4010") }
        var amt by remember { mutableStateOf("150.00") }

        AlertDialog(
            onDismissRequest = { showEntryDialog = false },
            title = { Text("Log journal entry debit/credit") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") })
                    OutlinedTextField(value = debitAcct, onValueChange = { debitAcct = it }, label = { Text("Debit Account Code e.g 1010") })
                    OutlinedTextField(value = creditAcct, onValueChange = { creditAcct = it }, label = { Text("Credit Account Code e.g 4010") })
                    OutlinedTextField(value = amt, onValueChange = { amt = it }, label = { Text("Value ($)") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.addJournalEntry(desc, debitAcct, creditAcct, amt.toDoubleOrNull() ?: 0.0)
                    showEntryDialog = false
                }) { Text("Post Journal") }
            }
        )
    }
}

// --- MODULE: WORKFLOW APPROVALS ENGINE ---
@Composable
fun ApprovalsModule(viewModel: ERPViewModel, lang: String) {
    val reqs by viewModel.approvalRequests.collectAsStateWithLifecycle()
    val role by viewModel.currentRole.collectAsStateWithLifecycle()

    var activeApprovalSection by remember { mutableStateOf(0) } // 0 = Pending grid, 1 = History details

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Approvals Engine", fontSize = 21.sp, fontWeight = FontWeight.Bold)
            Text("Multi-level administrative workflows", fontSize = 11.sp, color = Color.Gray)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).padding(4.dp).wrapContentWidth()) {
            TextButton(onClick = { activeApprovalSection = 0 }, colors = ButtonDefaults.textButtonColors(contentColor = if (activeApprovalSection == 0) MaterialTheme.colorScheme.primary else Color.Gray), contentPadding = PaddingValues(horizontal = 8.dp)) { Text("Active", maxLines = 1, softWrap = false) }
            TextButton(onClick = { activeApprovalSection = 1 }, colors = ButtonDefaults.textButtonColors(contentColor = if (activeApprovalSection == 1) MaterialTheme.colorScheme.primary else Color.Gray), contentPadding = PaddingValues(horizontal = 8.dp)) { Text("Resolved", maxLines = 1, softWrap = false) }
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    if (activeApprovalSection == 0) {
        val pendings = reqs.filter { it.status == "PENDING" }
        if (pendings.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text("All administrative workflow requisitions cleared.", modifier = Modifier.padding(24.dp), textAlign = TextAlign.Center, color = Color.Gray)
            }
        } else {
            pendings.forEach { req ->
                // Check if request is escalated (> 1 day/seeding triggers > 2 days)
                val isEscalated = (System.currentTimeMillis() - req.timestamp) > 86400000 * 2

                Card(
                    modifier = Modifier.fillMaxWidth().testTag("req_${req.id}"),
                    colors = CardDefaults.cardColors(containerColor = if (isEscalated) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface),
                    border = if (isEscalated) BorderStroke(1.5.dp, AlertRed) else null
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Box(modifier = Modifier.background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Text(req.requestType, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            }

                            if (isEscalated) {
                                Box(modifier = Modifier.background(AlertRed, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                                    Text("🚨 ESCALATED (PENDING > 2 DAYS)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(req.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(req.details, fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Visual progress stage timeline
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("MANAGER Approval", "FINANCE Approval", "DIRECTOR Approval").forEach { stage ->
                                val isActive = req.currentStage == stage
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(20.dp)
                                        .background(if (isActive) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.4f), RoundedCornerShape(4.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(stage, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isActive) Color.White else Color.Black)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Requester nominee: ${req.requester} | History: ${req.historyLog}", fontSize = 10.sp, color = Color.Gray)

                        if (req.comments != null) {
                            Text("Reject comments: ${req.comments}", fontSize = 11.sp, color = AlertRed, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (role == ERPRole.EMPLOYEE) {
                            Text("🔒 Switch to MANAGER or ADMIN role to approve this workflow node.", fontSize = 11.sp, color = AlertRed, textAlign = TextAlign.Center)
                        } else {
                            var responseComment by remember { mutableStateOf("") }
                            OutlinedTextField(
                                value = responseComment,
                                onValueChange = { responseComment = it },
                                placeholder = { Text("Reason commentary (required on Reject)", fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = { viewModel.processMultiStageRequest(req, true, responseComment) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = CompletedGreen)
                                ) {
                                    Text("Approve Step", fontSize = 11.sp)
                                }
                                Button(
                                    onClick = {
                                        if (responseComment.isNotEmpty()) {
                                            viewModel.processMultiStageRequest(req, false, responseComment)
                                        } else {
                                            viewModel.processRequest(req, "REJECTED")
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
                                ) {
                                    Text("Reject Box", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        val resolved = reqs.filter { it.status != "PENDING" }
        resolved.forEach { h ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(h.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Requester: ${h.requester}", fontSize = 11.sp)
                            Text(h.historyLog, fontSize = 10.sp, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .background(if (h.status == "APPROVED") CompletedGreen.copy(alpha = 0.15f) else AlertRed.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .wrapContentWidth()
                        ) {
                            Text(
                                text = h.status,
                                fontWeight = FontWeight.Bold,
                                color = if (h.status == "APPROVED") CompletedGreen else AlertRed,
                                fontSize = 11.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- MODULE: AI COPILOT VOICE COMMANDS CHAT ---
@Composable
fun ChatbotModule(viewModel: ERPViewModel) {
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val loading by viewModel.isChatLoading.collectAsStateWithLifecycle()

    var textInput by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("AI Copilot Control", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text("Command trigger chips or chat parameters", fontSize = 11.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = { viewModel.clearChat() }, modifier = Modifier.wrapContentWidth()) {
                Text("Clear Chat", maxLines = 1, softWrap = false)
            }
        }

        // Quick command chips
        Text("AI Quick Command Action Chips (Click to dispatch):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(
                "employees absent today",
                "generate salary report",
                "overdue invoices",
                "forecast next month"
            ).forEach { cmd ->
                Card(
                    modifier = Modifier.clickable { viewModel.sendChatMessage(cmd) }.testTag("chip_$cmd"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Text(cmd, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        DisplayChatLogs(messages = messages, isLoading = loading)

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("E.g. generate salary report...") },
                modifier = Modifier.weight(1f).testTag("chat_input")
            )
            Spacer(modifier = Modifier.width(6.dp))
            Button(
                onClick = {
                    if (textInput.isNotEmpty()) {
                        viewModel.sendChatMessage(textInput)
                        textInput = ""
                    }
                },
                modifier = Modifier.testTag("send_chat_button")
            ) {
                Text("Run")
            }
        }
    }
}

@Composable
fun DisplayChatLogs(messages: List<ChatMessage>, isLoading: Boolean) {
    Card(modifier = Modifier.fillMaxWidth().height(260.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(12.dp).fillMaxSize().verticalScroll(rememberScrollState())) {
            messages.forEach { msg ->
                val isUser = msg.role == "user"
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = msg.text,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(10.dp),
                            color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (isLoading) {
                Text("Copilot compiling intelligence forecasts...", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(8.dp))
            }
        }
    }
}

// --- MODULE: SECURE CHRONOLOGY AUDIT TRAIL ---
@Composable
fun AuditLogsModule(viewModel: ERPViewModel, lang: String) {
    val logs by viewModel.auditLogs.collectAsStateWithLifecycle()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(Dictionary.get("audit_logs", lang), fontSize = 21.sp, fontWeight = FontWeight.Bold)
        Text(Dictionary.get("audit_subtitle", lang), fontSize = 11.sp, color = Color.Gray)

        Column(modifier = Modifier.fillMaxWidth()) {
            logs.forEach { log ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("audit_${log.id}")) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                            Text(
                                text = log.action,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                    .wrapContentWidth()
                            ) {
                                Text(
                                    text = log.module,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                        Text("Operator: ${log.performedBy} (${log.role})", fontSize = 10.sp, color = Color.Gray)

                        // Column layout of audit trails details
                        if (log.tableName != "SYSTEM") {
                            Card(modifier = Modifier.fillMaxWidth().padding(top = 6.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Table changes: ${log.tableName}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text("Old value: ${log.oldValue}", fontSize = 10.sp, color = AlertRed)
                                    Text("New value: ${log.newValue}", fontSize = 10.sp, color = CompletedGreen)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("IP: ${log.ipAddress} | Location: ${log.location}", fontSize = 9.sp, color = Color.Gray)
                            val f = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }
                            Text(f.format(Date(log.timestamp)), fontSize = 9.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

// --- ELEGANT ENTERPRISE LOGO ---
@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    animatedScale: Float = 1.0f
) {
    Box(
        modifier = modifier
            .size(size)
            .scale(animatedScale),
        contentAlignment = Alignment.Center
    ) {
        // Soft radial background glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        
        // Outer decorative ring
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), CircleShape)
                .padding(6.dp)
        ) {
            // Inner corporate ring
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1E2130), CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Centered Corporate Shield Icon Layers
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    modifier = Modifier.size(size * 0.45f),
                    tint = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    modifier = Modifier.size(size * 0.22f),
                    tint = Color.White
                )
            }
        }
    }
}

// --- STARTUP ANIMATED SPLASH SCREEN ---
@Composable
fun SplashScreen(alpha: Float, scale: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F111A)) // Deep space dark corporate theme
            .alpha(alpha),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AppLogo(
                animatedScale = scale,
                size = 130.dp
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "ENTERPRISE ONE",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.scale(scale)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Secure Global ERP Suite",
                fontSize = 12.sp,
                color = Color.Gray,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.alpha(alpha)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// --- GATEWAY SAFETY SECURE ACCESS PANEL MOCK ---
@Composable
fun AuthScreen(viewModel: ERPViewModel, currentLanguage: String) {
    val authError by viewModel.authError.collectAsStateWithLifecycle()
    var isSignUp by remember { mutableStateOf(false) }

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var dept by remember { mutableStateOf("HR") }
    var role by remember { mutableStateOf("EMPLOYEE") }

    Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
        Card(modifier = Modifier.fillMaxWidth().widthIn(max = 450.dp)) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AppLogo(size = 80.dp)
                Text("Enterprise Portal System", fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Text(if (isSignUp) "Register Secure Identity Workspace" else "Sign In authorized ERP account", fontSize = 12.sp, color = Color.Gray)

                Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).padding(4.dp)) {
                    TextButton(onClick = { isSignUp = false }, modifier = Modifier.weight(1f)) { Text("Sign In") }
                    TextButton(onClick = { isSignUp = true }, modifier = Modifier.weight(1f)) { Text("Sign Up") }
                }

                if (authError != null) {
                    Text(authError ?: "", color = AlertRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username login") }, modifier = Modifier.fillMaxWidth())

                // Custom password transform
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password credentials") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (isSignUp) {
                    OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Legal Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Corporate Email Address") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = dept, onValueChange = { dept = it }, label = { Text("Primary Department Room") }, modifier = Modifier.fillMaxWidth())

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Select Role:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        
                        // First line: ADMIN & MANAGER
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("ADMIN", "MANAGER").forEach { r ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { role = r }
                                ) {
                                    RadioButton(
                                        selected = role == r,
                                        onClick = { role = r }
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(r, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        
                        // Second line: EMPLOYEE
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val r = "EMPLOYEE"
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { role = r }
                            ) {
                                RadioButton(
                                    selected = role == r,
                                    onClick = { role = r }
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(r, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }


                }

                Button(
                    onClick = {
                        if (isSignUp) {
                            viewModel.signup(username, password, fullName, role, email, dept) { ok ->
                                if (ok) isSignUp = false
                            }
                        } else {
                            viewModel.login(username, password) { }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("auth_submit_button")
                ) {
                    Text(if (isSignUp) "Create Workspace Account" else "Authenticate Securely")
                }

                Text("Hint: login with: admin/admin, manager/manager, or employee/employee for instant role walkthrough.", fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.Center)
            }
        }
    }
}

// --- OTP TWOFACTOR VERIFICATION DIALOG SCREEN ---
@Composable
fun TwoFactorScreen(viewModel: ERPViewModel) {
    var otpCode by remember { mutableStateOf("") }
    val error by viewModel.authError.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
        Card(modifier = Modifier.fillMaxWidth().widthIn(max = 400.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Icon(Icons.Default.VpnKey, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                Text("2FA Passcode Gateway", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("A dynamic 2-Factor check is active. Enter the temporary OTP code to complete authentication.", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)

                if (error != null) {
                    Text(error ?: "", color = AlertRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedTextField(
                    value = otpCode,
                    onValueChange = { otpCode = it },
                    placeholder = { Text("Code: 123456", textAlign = TextAlign.Center) },
                    modifier = Modifier.fillMaxWidth().testTag("two_factor_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Button(
                    onClick = { viewModel.verifyTwoFactor(otpCode) { } },
                    modifier = Modifier.fillMaxWidth().testTag("two_factor_verify")
                ) {
                    Text("Verify Dynamic OTP")
                }
            }
        }
    }
}

// --- OPTION DIALOG NOTIFICATION CENTER ---
@Composable
fun NotificationCenterDialog(viewModel: ERPViewModel, onDismiss: () -> Unit) {
    val list by viewModel.notifications.collectAsStateWithLifecycle()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Corporate Alert Center") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().height(280.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Historical communications log", fontSize = 11.sp, color = Color.Gray)
                    TextButton(onClick = { viewModel.clearAllNotifications() }) { Text("Clear All Alerts") }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    list.forEach { notif ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { viewModel.dismissNotification(notif.id) },
                            colors = CardDefaults.cardColors(containerColor = if (notif.isRead) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(notif.message, fontSize = 11.sp)
                                    Text("Channel: ${notif.type} | Cat: ${notif.category}", fontSize = 9.sp, color = Color.Gray)
                                }
                                if (!notif.isRead) {
                                    Box(modifier = Modifier.size(8.dp).background(AlertRed, CircleShape))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Close Screen") }
        }
    )
}

// --- SECURITY GUIDELINE WORKSPACE INFORMATION DIALOG ---
@Composable
fun GuidelinesDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MenuBook, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Enterprise Guidelines")
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Enterprise System Guidelines Workspace Security details:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                listOf(
                    "🔑 2-Factor Security Authorization" to "Enforced dynamic OTP validations safeguard organizational entries during sign up and logins.",
                    "📦 Barcoding SKU Generations" to "Automatic serial identifiers are created on adding inventory. Code prints can be dispatched to Nagoya channels.",
                    "⚡ Multi-Stage Workflows Rejection" to "Approval Requests advance through sequential manager, finance, and director steps. Reject boxes prompt comments.",
                    "📊 Executive CEO Dashboard Insights" to "Drawn canvas profit line charts and dynamic demand reports assist administrative planning."
                ).forEach { (h, b) ->
                    Column {
                        Text(h, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        Text(b, fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Acknowledged") }
        }
    )
}

// --- INTERACTIVE RAW SQL QUERY EXECUTOR WORKSPACE CONSOLE ---
@Composable
fun SQLConsoleDialog(viewModel: ERPViewModel, onDismiss: () -> Unit) {
    var rawTextQuery by remember { mutableStateOf("SELECT * FROM employees") }
    val result by viewModel.sqlQueryResult.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTabIndex by remember { mutableStateOf(0) }
    
    // Excel/SQL Sync state
    var selectedTable by remember { mutableStateOf("employees") }
    var exportFormat by remember { mutableStateOf("CSV") } // "CSV" or "SQL"
    var exportResultText by remember { mutableStateOf("") }
    
    var importTextIn by remember { mutableStateOf("") }
    var importResultMessage by remember { mutableStateOf("") }
    var isImportError by remember { mutableStateOf(false) }

    val tablesList = listOf(
        Pair("employees", "Staff/Employees"),
        Pair("inventory_items", "Inventory Stock"),
        Pair("finance_transactions", "Finance Register"),
        Pair("manufacturing_orders", "Manufacturing Orders"),
        Pair("approval_requests", "Approvals Workflow"),
        Pair("dms_documents", "Document Security Index"),
        Pair("chart_of_accounts", "Chart of Accounts Ledger"),
        Pair("journal_entries", "Journal Ledger Entries"),
        Pair("audit_logs", "Audit History Logs"),
        Pair("user_accounts", "Registered ERP Logins")
    )

    // Activity launcher for choosing CSV/SQL file
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val contentResolver = context.contentResolver
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val stringBuilder = StringBuilder()
                    var line: String? = reader.readLine()
                    while (line != null) {
                        stringBuilder.append(line).append("\n")
                        line = reader.readLine()
                    }
                    importTextIn = stringBuilder.toString()
                    Toast.makeText(context, "File loaded successfully!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error reading file: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    AlertDialog(
        onDismissRequest = {
            viewModel.clearSQLResult()
            onDismiss()
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Sync, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("SQL & Excel Data Sync Space")
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().height(480.dp)) {
                // TabRow
                TabRow(selectedTabIndex = selectedTabIndex, containerColor = Color.Transparent) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("Direct Terminal", fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.Terminal, null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("Excel / File Sync", fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.ImportExport, null, modifier = Modifier.size(16.dp)) }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                if (selectedTabIndex == 0) {
                    // TAB 0: Direct terminal Console
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Query or execute direct SQLite commands on Room tables:", fontSize = 11.sp, color = Color.Gray)
                        OutlinedTextField(
                            value = rawTextQuery,
                            onValueChange = { rawTextQuery = it },
                            modifier = Modifier.fillMaxWidth().height(80.dp).testTag("sql_console_input"),
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.executeRawSQL(rawTextQuery) },
                                modifier = Modifier.weight(1f).testTag("sql_execute_button")
                            ) {
                                Text("Execute Query")
                            }
                            Button(
                                onClick = { viewModel.clearSQLResult() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                            ) {
                                Text("Reset")
                            }
                        }

                        Text("Terminal output:", fontSize = 11.sp, fontWeight = FontWeight.Bold)

                        Card(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            Box(modifier = Modifier.padding(10.dp).fillMaxSize().verticalScroll(rememberScrollState()).horizontalScroll(rememberScrollState())) {
                                if (result != null) {
                                    val r = result!!
                                    if (r.errorMessage != null) {
                                        Text("SQL compilation issue:\n${r.errorMessage}", color = AlertRed, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                                    } else {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text("Affected rows: ${r.affectedRows}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            Row {
                                                r.headers.forEach { h ->
                                                    Text(h, fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp).padding(4.dp), fontSize = 10.sp, maxLines = 1)
                                                }
                                            }
                                            HorizontalDivider()
                                            r.rows.forEach { row ->
                                                Row {
                                                    row.forEach { valStr ->
                                                        Text(valStr, modifier = Modifier.width(100.dp).padding(4.dp), fontSize = 10.sp, maxLines = 1)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Text("SQL Terminal ready. Enter a query and run.", color = Color.Gray, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                } else {
                    // TAB 1: Bulk Excel/CSV & SQL Sync
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                            Column(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
                                Text("💻 Laptop-to-Mobile Direct Data Sync Engine", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                Text("See data in Excel/SQL, modify values on your laptop, and paste or load them here to instantly update the ERP Room Database.", fontSize = 10.sp, color = Color.Gray)
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Select Table:", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(80.dp))
                            var expandedDropdown by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                Button(
                                    onClick = { expandedDropdown = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                                    modifier = Modifier.fillMaxWidth().height(36.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                ) {
                                    Text(tablesList.firstOrNull { it.first == selectedTable }?.second ?: selectedTable, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp))
                                }
                                DropdownMenu(expanded = expandedDropdown, onDismissRequest = { expandedDropdown = false }) {
                                    tablesList.forEach { p ->
                                        DropdownMenuItem(
                                            text = { Text("${p.second} (${p.first})", fontSize = 12.sp) },
                                            onClick = {
                                                selectedTable = p.first
                                                expandedDropdown = false
                                                exportResultText = ""
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Export section
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)).padding(8.dp).clip(RoundedCornerShape(4.dp))
                        ) {
                            Text("📤 STEP 1: Export Data to Laptop", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { exportFormat = "CSV" }) {
                                    RadioButton(selected = exportFormat == "CSV", onClick = { exportFormat = "CSV" })
                                    Text("Excel CSV", fontSize = 11.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { exportFormat = "SQL" }) {
                                    RadioButton(selected = exportFormat == "SQL", onClick = { exportFormat = "SQL" })
                                    Text("SQL Dumps", fontSize = 11.sp)
                                }
                                
                                Spacer(modifier = Modifier.weight(1f))
                                
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            if (exportFormat == "CSV") {
                                                exportResultText = viewModel.exportTableToCsv(selectedTable)
                                            } else {
                                                exportResultText = viewModel.exportTableToSql(selectedTable)
                                            }
                                        }
                                    },
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                ) {
                                    Text("Generate Export", fontSize = 11.sp)
                                }
                            }

                            if (exportResultText.isNotEmpty()) {
                                OutlinedTextField(
                                    value = exportResultText,
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier.fillMaxWidth().height(100.dp),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp),
                                    placeholder = { Text("Generated output data is shown here...", fontSize = 10.sp) }
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    Button(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(exportResultText))
                                            Toast.makeText(context, "Copied in Excel compatible format to Clipboard!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Copy Data", fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = {
                                            try {
                                                val intent = Intent().apply {
                                                    action = Intent.ACTION_SEND
                                                    putExtra(Intent.EXTRA_TEXT, exportResultText)
                                                    type = "text/plain"
                                                }
                                                val chooser = Intent.createChooser(intent, "Share $selectedTable Export")
                                                context.startActivity(chooser)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Error sharing: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Icon(Icons.Default.Share, null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Share To Laptop", fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        // Import section
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)).padding(8.dp).clip(RoundedCornerShape(4.dp))
                        ) {
                            Text("📥 STEP 2: Import Modifications Back from Laptop", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("Either load an edited .csv/.sql file from storage or paste spreadsheet values below directly", fontSize = 9.sp, color = Color.Gray)

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = { filePickerLauncher.launch("*/*") },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                                    modifier = Modifier.weight(1f).height(32.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.Upload, null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Choose File", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = { importTextIn = "" },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp)
                                ) {
                                    Text("Clear", fontSize = 11.sp)
                                }
                            }

                            OutlinedTextField(
                                value = importTextIn,
                                onValueChange = { importTextIn = it },
                                modifier = Modifier.fillMaxWidth().height(100.dp).testTag("sql_excel_import_input"),
                                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp),
                                label = { Text("Paste Data content / Review loaded file here", fontSize = 10.sp) },
                                placeholder = { Text("ID,Name,Email...\n1,Sanjay K,sanjay@gmail.com...", fontSize = 9.sp) }
                            )

                            Button(
                                onClick = {
                                    val textToImport = importTextIn.trim()
                                    if (textToImport.isEmpty()) {
                                        importResultMessage = "Please load a file or paste some CSV/SQL data."
                                        isImportError = true
                                        return@Button
                                    }

                                    coroutineScope.launch {
                                        // Auto-detect if it is SQL or CSV
                                        val isSqlText = textToImport.startsWith("INSERT", ignoreCase = true) || textToImport.contains("INSERT INTO", ignoreCase = true)
                                        if (isSqlText) {
                                            val (count, err) = viewModel.importSqlStatements(textToImport)
                                            if (err != null) {
                                                importResultMessage = "SQL Import failed: $err"
                                                isImportError = true
                                            } else {
                                                importResultMessage = "Successfully executed $count SQL commands. Database updated!"
                                                isImportError = false
                                                Toast.makeText(context, "Direct SQL Sync completed!", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            val (count, err) = viewModel.importCsvToTable(selectedTable, textToImport)
                                            if (err != null) {
                                                importResultMessage = "CSV Sync failing: $err"
                                                isImportError = true
                                            } else {
                                                importResultMessage = "Successfully imported & replacement-merged $count records into local table '$selectedTable'. UI updated live!"
                                                isImportError = false
                                                Toast.makeText(context, "Spreadsheet Excel CSV sync completed!", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(36.dp).testTag("bulk_import_process_button")
                            ) {
                                Text("Sync Back Into Database", fontSize = 11.sp)
                            }

                            if (importResultMessage.isNotEmpty()) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isImportError) AlertRed.copy(alpha = 0.15f) else CompletedGreen.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = importResultMessage,
                                        color = if (isImportError) AlertRed else CompletedGreen,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(8.dp),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                viewModel.clearSQLResult()
                onDismiss()
            }) { Text("Done") }
        }
    )
}
