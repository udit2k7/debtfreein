package com.debtfreein.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.launch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.viewmodel.compose.viewModel
import com.debtfreein.app.data.model.CreditCard
import com.debtfreein.app.R
import com.debtfreein.app.data.model.Expense
import com.debtfreein.app.data.model.Investment
import com.debtfreein.app.data.model.SystemLog
import com.debtfreein.app.ui.theme.DebtFreeInTheme
import com.debtfreein.app.ui.viewmodel.FinanceViewModel
import com.debtfreein.app.data.ai.GeminiAdviceReport
import com.debtfreein.app.data.ai.LiquidationAdvice
import com.debtfreein.app.data.ai.PersonalLoanAdvice
import com.debtfreein.app.data.ai.RepaymentPlanItem
import com.google.gson.Gson
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : FragmentActivity() {

    private var isBiometricUnlocked by mutableStateOf(false)
    private lateinit var appLifecycleObserver: AppLifecycleObserver

    // Request permissions for background SMS parsing
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val smsReceived = permissions[Manifest.permission.RECEIVE_SMS] ?: false
        val smsRead = permissions[Manifest.permission.READ_SMS] ?: false
        if (smsReceived && smsRead) {
            Toast.makeText(this, "SMS automatic tracking activated!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Manual tracking only: SMS permissions denied.", Toast.LENGTH_LONG).show()
        }
    }

    private var isHandoffRequested = mutableStateOf(false)

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        checkHandoffIntent(intent)
    }

    private fun checkHandoffIntent(intent: android.content.Intent?) {
        if (intent != null && (intent.action == com.debtfreein.app.data.ai.FailoverObserverService.ACTION_TAKEOVER || intent.getBooleanExtra("trigger_handoff", false))) {
            isHandoffRequested.value = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        checkHandoffIntent(intent)
        
        // Initialize network and execution services with application context
        com.debtfreein.app.data.network.MarketService.initialize(applicationContext)
        com.debtfreein.app.data.network.UpstoxExecutionService.initialize(applicationContext)
        com.debtfreein.app.data.ai.AutonomousPaperTrader.initialize(applicationContext)

        // Request SMS permissions at startup
        checkAndRequestSmsPermissions()

        // Register App Lifecycle Observer
        appLifecycleObserver = AppLifecycleObserver {
            if (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser != null) {
                isBiometricUnlocked = false
                showBiometricPrompt()
            }
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)

        setContent {
            DebtFreeInTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val auth = remember { com.google.firebase.auth.FirebaseAuth.getInstance() }
                    var userAuthenticated by remember { mutableStateOf(auth.currentUser != null) }
                    var isSyncingKeys by remember { mutableStateOf(auth.currentUser != null) }

                    if (isHandoffRequested.value) {
                        AlertDialog(
                            onDismissRequest = { isHandoffRequested.value = false },
                            title = { Text("Confirm Handoff Protocol", fontWeight = FontWeight.Bold) },
                            text = {
                                Text("Alert: Trading Node Offline. Heartbeat missed. Tap 'Confirm Handoff' to boot Phone 2 out of the active slot, claim engine ownership, sync budget & telemetry, and start local execution on Phone 1.")
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        com.debtfreein.app.data.ai.EngineHeartbeatManager.updateActiveDeviceAndTakeover(applicationContext) { success ->
                                            com.debtfreein.app.data.security.SecureStorageManager.fetchSecureKeysFromFirestore {
                                                com.debtfreein.app.data.network.UpstoxExecutionService.setPaperTradingActive(true)
                                                val botIntent = android.content.Intent(applicationContext, com.debtfreein.app.data.ai.TradingBotService::class.java)
                                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                                    applicationContext.startForegroundService(botIntent)
                                                } else {
                                                    applicationContext.startService(botIntent)
                                                }
                                                Toast.makeText(applicationContext, "Handoff Successful: Engine Active on Phone 1", Toast.LENGTH_LONG).show()
                                                isHandoffRequested.value = false
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                                ) {
                                    Text("Confirm Handoff", fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { isHandoffRequested.value = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }

                    LaunchedEffect(userAuthenticated) {
                        if (userAuthenticated) {
                            val uid = auth.currentUser?.uid
                            if (uid != null) {
                                startNotificationsListener(uid)
                                com.google.firebase.messaging.FirebaseMessaging.getInstance().token
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val token = task.result
                                            if (token != null) {
                                                com.debtfreein.app.FCMReceiverService.syncDeviceTokenToFirestore(
                                                    applicationContext,
                                                    uid,
                                                    token
                                                )
                                            }
                                        }
                                    }
                            }
                            com.debtfreein.app.data.security.SecureStorageManager.fetchSecureKeysFromFirestore {
                                isSyncingKeys = false
                            }
                        }
                    }

                    if (!userAuthenticated) {
                        LoginScreen(
                            onLoginSuccess = {
                                userAuthenticated = true
                                isBiometricUnlocked = true
                            }
                        )
                    } else if (isSyncingKeys) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        if (!isBiometricUnlocked) {
                            VaultLockScreen(
                                onUnlockClick = {
                                    showBiometricPrompt()
                                }
                            )
                        } else {
                            AppScreen()
                        }
                    }
                }
            }
        }
    }

    private fun showBiometricPrompt() {
        val biometricManager = BiometricManager.from(this)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        
        if (biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS) {
            val executor = ContextCompat.getMainExecutor(this)
            val biometricPrompt = BiometricPrompt(
                this,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "Authentication required: $errString", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        runOnUiThread {
                            isBiometricUnlocked = true
                        }
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                    }
                }
            )

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Vault Unlock")
                .setSubtitle("Authenticate using your biometric credential or PIN")
                .setAllowedAuthenticators(authenticators)
                .build()

            biometricPrompt.authenticate(promptInfo)
        } else {
            // Fallback if biometrics/device credentials are not configured or supported
            isBiometricUnlocked = true
        }
    }

    override fun onStart() {
        super.onStart()
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            startNotificationsListener(uid)
        }
    }

    override fun onStop() {
        super.onStop()
        stopNotificationsListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopNotificationsListener()
        if (::appLifecycleObserver.isInitialized) {
            ProcessLifecycleOwner.get().lifecycle.removeObserver(appLifecycleObserver)
        }
    }

    private var notificationsListener: com.google.firebase.firestore.ListenerRegistration? = null

    private fun startNotificationsListener(uid: String) {
        if (notificationsListener != null) return
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val listenerStartTimestamp = com.google.firebase.Timestamp.now()
        
        notificationsListener = db.collection("users").document(uid).collection("notifications")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    android.util.Log.e("MainActivity", "Listen failed.", e)
                    return@addSnapshotListener
                }
                
                if (snapshots != null) {
                    for (dc in snapshots.documentChanges) {
                        if (dc.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                            val doc = dc.document
                            val timestamp = doc.getTimestamp("timestamp")
                            if (timestamp == null || timestamp.compareTo(listenerStartTimestamp) >= 0) {
                                val title = doc.getString("title") ?: "Alert"
                                val body = doc.getString("body") ?: ""
                                triggerLocalNotification(title, body)
                            }
                        }
                    }
                }
            }
    }

    private fun stopNotificationsListener() {
        notificationsListener?.remove()
        notificationsListener = null
    }

    private fun triggerLocalNotification(title: String, body: String) {
        val notificationManager = getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        val channelId = "local_alerts_channel"

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Local Alert Notifications",
                android.app.NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = android.content.Intent(this, MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntentFlags = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            android.app.PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = android.app.PendingIntent.getActivity(this, 888, intent, pendingIntentFlags)

        val notification = androidx.core.app.NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun checkAndRequestSmsPermissions() {
        val hasReceive = ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
        val hasRead = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        
        if (!hasReceive || !hasRead) {
            requestPermissionLauncher.launch(
                arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
            )
        }
    }
}

@Composable
fun VaultLockScreen(onUnlockClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Vault Locked",
                tint = Color(0xFF6366F1),
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "Vault Locked",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Authentication is required to access your financial dashboard.",
                color = Color(0xFF94A3B8),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onUnlockClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Unlock Vault", fontWeight = FontWeight.Bold)
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(viewModel: FinanceViewModel = viewModel()) {
    val navController = rememberNavController()
    var selectedTab by remember { mutableIntStateOf(0) }
    var expertSubScreen by remember { mutableStateOf("menu") }
    val tabs = listOf("Dashboard", "Expert Advice", "Investments", "Activity Logs", "Profile")

    Scaffold(
        topBar = {
            Column {
                SmallTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_phone_logo),
                                contentDescription = "Logo",
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "DebtFreeIn",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
                com.debtfreein.app.ui.components.GlobalStatusBar()
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEachIndexed { index, label ->
                    val icon = when (index) {
                        0 -> Icons.Default.Dashboard
                        1 -> Icons.Default.Psychology
                        2 -> Icons.Default.AccountBalanceWallet
                        3 -> Icons.Default.List
                        else -> Icons.Default.Person
                    }
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            navController.navigate("main_tabs") {
                                popUpTo("main_tabs") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "main_tabs",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("main_tabs") {
                Column(modifier = Modifier.fillMaxSize()) {
                    when (selectedTab) {
                        0 -> DashboardTab(
                            viewModel = viewModel,
                            onNavigateToBalanceTransfer = {
                                selectedTab = 1
                                expertSubScreen = "balance_transfer"
                            },
                            onNavigateToHoldings = {
                                navController.navigate("upstox_holdings")
                            }
                        )
                        1 -> GeminiAdviceTab(
                            viewModel = viewModel,
                            subScreen = expertSubScreen,
                            onSubScreenChange = { expertSubScreen = it }
                        )
                        2 -> InvestmentsTab(viewModel)
                        3 -> LogsAndActivityTab(viewModel)
                        4 -> ProfileScreen(
                            viewModel = viewModel,
                            onNavigateToAuth = {
                                selectedTab = 1
                                expertSubScreen = "upstox_auth"
                            }
                        )
                    }
                }
            }
            composable("upstox_holdings") {
                UpstoxHoldingScreen(
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }
    }
}

// ==================== DASHBOARD TAB ====================
@Composable
fun DashboardTab(
    viewModel: FinanceViewModel,
    onNavigateToBalanceTransfer: () -> Unit,
    onNavigateToHoldings: () -> Unit
) {
    val cards by viewModel.cards.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    
    val totalDebt = cards.sumOf { it.currentBalance }
    val totalLimit = cards.sumOf { it.creditLimit }
    val avgApr = if (cards.isNotEmpty()) cards.sumOf { it.apr * (it.currentBalance / if (totalDebt > 0) totalDebt else 1.0) } else 0.0
    val totalMinPayment = cards.sumOf { it.minimumPayment }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Core Debt Metrics Summary
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("TOTAL OUTSTANDING DEBT", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                    Text("₹${String.format("%,.2f", totalDebt)}", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.error)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Weighted APR", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Text("${String.format("%.2f", avgApr)}%", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                        }
                        Column {
                            Text("Credit Utilization", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            val utilization = if (totalLimit > 0) (totalDebt / totalLimit) * 100 else 0.0
                            Text("${String.format("%.1f", utilization)}%", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (utilization > 50) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                        }
                        Column {
                            Text("Min. Monthly Payment", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Text("₹${String.format("%.2f", totalMinPayment)}", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Upstox Holdings Action Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToHoldings() }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = "Holdings",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Upstox Long-term Holdings Portfolio",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Track your long-term equity holdings value and performance in real-time.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Header for Credit Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("CREDIT CARDS", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        // Credit Cards Carousel
        if (cards.isEmpty()) {
            item {
                EmptyStateCard(
                    message = "Zero outstanding liabilities detected. Financial Peace Active."
                )
            }
        } else {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(cards) { card ->
                        CreditCardItem(card = card, onDelete = { viewModel.deleteCard(card) })
                    }
                }
            }
        }

        // Transfer Alert Ticker
        item {
            val highAprCardsBalance = cards.filter { it.currentBalance > 0 && it.apr > 15.0 }.sumOf { it.currentBalance }
            if (highAprCardsBalance > 0.0) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToBalanceTransfer() }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Alert",
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Action Required: ₹${String.format("%,.2f", highAprCardsBalance)} transfer suggested",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E),
                                fontSize = 13.sp
                            )
                            Text(
                                text = "High-interest compounding detected. Tap to optimize balance transfer.",
                                color = Color(0xFFB45309),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Pending Trades Widget
        item {
            val pendingTickets by com.debtfreein.app.data.network.UpstoxExecutionService.pendingTickets.collectAsState()
            val scope = rememberCoroutineScope()
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("TACTICAL INTRADAY TRADES (UPSTOX V3)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            
            if (pendingTickets.isEmpty()) {
                EmptyStateCard(
                    message = "No pending trade tickets. System guardrails active."
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    pendingTickets.forEach { ticket ->
                        com.debtfreein.app.data.network.TradeTicketCard(
                            trade = ticket,
                            onConfirm = {
                                scope.launch {
                                    com.debtfreein.app.data.network.UpstoxExecutionService.executeTrade(ticket)
                                }
                            },
                            onCancel = {
                                com.debtfreein.app.data.network.UpstoxExecutionService.rejectTrade(ticket)
                            }
                        )
                    }
                }
            }
        }

        // Reimbursable Corporate Claims Widget
        item {
            val reimbursableClaims = expenses.filter { it.isReimbursableClaim }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("REIMBURSABLE CORPORATE CLAIMS", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            
            if (reimbursableClaims.isEmpty()) {
                EmptyStateCard(
                    message = "Zero pending corporate claims. Outstanding balances clear."
                )
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        reimbursableClaims.forEach { claim ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(claim.merchant, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text(claim.expenseCategory, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                                Text("₹${claim.amount}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreditCardItem(card: CreditCard, onDelete: () -> Unit) {
    // Beautiful gradient backgrounds based on issuer to look like a premium real card
    val gradient = when (card.issuer.lowercase()) {
        "hdfc", "hdfc bank" -> Brush.horizontalGradient(listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364)))
        "amex", "american express" -> Brush.horizontalGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)))
        "chase", "chase bank" -> Brush.horizontalGradient(listOf(Color(0xFF1F4068), Color(0xFF162447)))
        else -> Brush.horizontalGradient(listOf(Color(0xFF303B76), Color(0xFF252D56)))
    }

    Card(
        modifier = Modifier
            .width(280.dp)
            .height(170.dp)
            .clickable { },
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(card.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        Text(card.issuer, fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White.copy(alpha = 0.6f))
                    }
                }

                // Card last 4 & APR
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("•••• •••• •••• ${card.cardLastFour}", fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f))
                    Text("APR: ${card.apr}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                }

                // Balances and due dates
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text("CURRENT BALANCE", fontSize = 9.sp, color = Color.White.copy(alpha = 0.6f))
                        Text("₹${String.format("%,.2f", card.currentBalance)}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("DUE DAY", fontSize = 9.sp, color = Color.White.copy(alpha = 0.6f))
                        Text("Day ${card.dueDay}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}



// ==================== EXPERT ADVICE TAB ====================
@Composable
fun GeminiAdviceTab(
    viewModel: FinanceViewModel,
    subScreen: String,
    onSubScreenChange: (String) -> Unit
) {
    var showPinLockDialog by remember { mutableStateOf(false) }

    if (showPinLockDialog) {
        com.debtfreein.app.ui.components.PinLockDialog(
            onSuccess = {
                showPinLockDialog = false
                onSubScreenChange("live_trading_hub")
            },
            onDismiss = {
                showPinLockDialog = false
            }
        )
    }

    when (subScreen) {
        "menu" -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A))
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "AI Expert Advice Hub",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSubScreenChange("balance_transfer") }
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Balance Transfer Optimizer", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Consolidate multiple high-APR credit cards into a single lower promo rate account.", fontSize = 12.sp, color = Color(0xFF94A3B8), lineHeight = 16.sp)
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSubScreenChange("intraday_hub") }
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Intraday Trading Hub", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Set allocation limits and monitor Upstox trade execution guardrails in real-time.", fontSize = 12.sp, color = Color(0xFF94A3B8), lineHeight = 16.sp)
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSubScreenChange("paper_trading_dashboard") }
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Autonomous Paper Trading", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Monitor the AI bot's autonomous entries/exits and review its cognitive learnings in real-time.", fontSize = 12.sp, color = Color(0xFF94A3B8), lineHeight = 16.sp)
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF450A0A)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(16.dp))
                        .clickable { showPinLockDialog = true }
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = "Lock", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Access Live Real Trading Engine", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Execute real capital orders directly on Upstox broker API. Requires PIN / Biometric verification.", fontSize = 12.sp, color = Color(0xFFFCA5A5), lineHeight = 16.sp)
                    }
                }
            }
        }
        "balance_transfer" -> {
            BalanceTransferScreen(viewModel = viewModel, onBack = { onSubScreenChange("menu") })
        }
        "intraday_hub" -> {
            IntradayExecutionScreen(
                viewModel = viewModel,
                onBack = { onSubScreenChange("menu") },
                onNavigateToAuth = { onSubScreenChange("upstox_auth") }
            )
        }
        "upstox_auth" -> {
            val authUrl = com.debtfreein.app.data.network.UpstoxExecutionService.getUpstoxAuthUrl(androidx.compose.ui.platform.LocalContext.current)
            UpstoxAuthScreen(
                viewModel = viewModel,
                authUrl = authUrl,
                onAuthComplete = { onSubScreenChange("intraday_hub") },
                onBack = { onSubScreenChange("intraday_hub") }
            )
        }
        "paper_trading_dashboard" -> {
            PaperTradingDashboard(viewModel = viewModel, onBack = { onSubScreenChange("menu") })
        }
        "live_trading_hub" -> {
            LiveTradingHubScreen(viewModel = viewModel, onBack = { onSubScreenChange("menu") })
        }
    }
}

@Composable
fun SimpleMarkdownRenderer(markdown: String) {
    val lines = markdown.split("\n")
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        lines.forEach { line ->
            val cleanLine = line.trim()
            when {
                // Header level 1 & 2
                cleanLine.startsWith("##") -> {
                    Text(
                        text = cleanLine.substring(2).trim(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                    )
                }
                cleanLine.startsWith("#") -> {
                    Text(
                        text = cleanLine.substring(1).trim(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                    )
                }
                // Bullet point
                cleanLine.startsWith("-") || cleanLine.startsWith("*") -> {
                    Row(modifier = Modifier.fillMaxWidth().padding(start = 8.dp)) {
                        Text("•  ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(
                            text = cleanLine.substring(1).trim(),
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                // General text
                cleanLine.isNotEmpty() -> {
                    Text(
                        text = cleanLine,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    )
                }
                else -> {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StructuredAdviceRenderer(report: GeminiAdviceReport) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // 1. Executive Summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = "Summary", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Executive Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(report.summary, fontSize = 13.sp, lineHeight = 18.sp)
            }
        }

        // 2. Liquidations
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Sell, contentDescription = "Sell", tint = MaterialTheme.colorScheme.tertiary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Asset Liquidations", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.tertiary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Sell underperforming assets whose yields are lower than card debt APRs:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(12.dp))

                if (report.liquidations.isEmpty()) {
                    Text("No asset liquidations recommended at this time.", fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp))
                } else {
                    report.liquidations.forEach { liq ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Sell: ${liq.quantityToSell} ${liq.assetSymbol}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Realized Value: ₹${String.format("%,.2f", liq.estimatedValue)}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ArrowForward, contentDescription = "Pay off", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Target card payoff: ${liq.targetCardName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(liq.reason, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // 3. Personal Loan Advice
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountBalance, contentDescription = "Loan", tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Personal Loan Consolidation", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.secondary)
                }
                Spacer(modifier = Modifier.height(12.dp))

                val loan = report.personalLoan
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (loan.makesSense) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "CONSOLIDATION VIABILITY",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (loan.makesSense) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Badge(
                        containerColor = if (loan.makesSense) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (loan.makesSense) Color.Black else MaterialTheme.colorScheme.onSurface
                    ) {
                        Text(
                            text = if (loan.makesSense) "RECOMMENDED" else "NOT RECOMMENDED",
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                if (loan.makesSense) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Suggested Loan Principal:", fontSize = 13.sp)
                        Text("₹${String.format("%,.2f", loan.recommendedAmount)}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Max Viable Loan APR:", fontSize = 13.sp)
                        Text("${loan.maxViableApr}%", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.tertiary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Est. Net Interest Saved/yr:", fontSize = 13.sp)
                        Text("₹${String.format("%,.2f", loan.interestSavings)}", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Consolidate card debts: ${loan.targetCardNames.joinToString(", ")}", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(loan.explanation, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
            }
        }

        // 4. Repayment Priority
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TrendingDown, contentDescription = "Repayment", tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Payoff Priority (Avalanche Plan)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Prioritized list of debt contributions after meeting minimums:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(12.dp))

                report.repaymentPlan.sortedBy { it.priority }.forEach { plan ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Badge(
                            containerColor = if (plan.priority == 1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (plan.priority == 1) Color.White else MaterialTheme.colorScheme.onSurface
                        ) {
                            Text("#${plan.priority}", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(plan.cardName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Strategy: ${plan.strategy}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Extra pay: ₹${String.format("%,.2f", plan.recommendedMonthlyPayment)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                            Text("/ month", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                }
            }
        }

        // 5. Reimbursable Claims Tracker
        if (!report.reimbursableReceivables.isNullOrEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = "Reimbursable", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reimbursable Receivables (Employer Claims)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Office/corporate expenses to claim back from employer, plus 30-day card interest:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(12.dp))

                    report.reimbursableReceivables.forEach { item ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${item.merchant} (via ${item.cardName})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Principal: ₹${String.format("%,.2f", item.amount)}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("30-Day Accrued Interest:", fontSize = 12.sp, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Medium)
                                Text("₹${String.format("%,.2f", item.interestAccrued30Days)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.tertiary)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(item.employerClaimRecommendation, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // 6. SIP & Wealth Portfolio Analysis
        if (report.sipAnalysis != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = "SIP Analysis", tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SIP & Portfolio Rebalancing", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Current Sector/Asset Exposure:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(report.sipAnalysis.currentExposure, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Rebalancing & Diversification Recommendations:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.tertiary)
                    Text(report.sipAnalysis.rebalancingRecommendation, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Long-Term Wealth Generation Advice:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Text(report.sipAnalysis.wealthGenerationAdvice, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                }
            }
        }
    }
}



// ==================== INVESTMENTS TAB ====================
@Composable
fun InvestmentsTab(viewModel: FinanceViewModel) {
    val investments by viewModel.investments.collectAsState()
    val isRefreshing by viewModel.isRefreshingMarket.collectAsState()
    val marketApiKey by viewModel.marketApiKey.collectAsState()

    var showAddAssetDialog by remember { mutableStateOf(false) }
    var localMarketKeyInput by remember { mutableStateOf(marketApiKey) }

    val totalValue = investments.sumOf { it.currentPrice * it.quantity }
    val totalCost = investments.sumOf { it.purchasePrice * it.quantity }
    val netPnL = totalValue - totalCost

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Portfolio valuation summary
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("TOTAL PORTFOLIO VALUATION", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                    Text("₹${String.format("%,.2f", totalValue)}", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Invested: ₹${String.format("%,.2f", totalCost)}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        Text(
                            text = (if (netPnL >= 0) "+" else "") + "₹${String.format("%,.2f", netPnL)} (${String.format("%.2f", if (totalCost > 0) (netPnL / totalCost) * 100.0 else 0.0)}%)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (netPnL >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }



        // Section header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("INVESTMENT HOLDINGS (Equity / F&O)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row {
                    Button(
                        onClick = { viewModel.syncMarketPrices() },
                        enabled = !isRefreshing && investments.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Sync", modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sync API", fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { showAddAssetDialog = true }) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Add Asset", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                    }
                }
            }
        }

        // Table items
        if (investments.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No assets logged. Seed demo data or add holdings.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        } else {
            items(investments) { asset ->
                InvestmentAssetRow(asset = asset, viewModel = viewModel)
            }
        }
    }

    if (showAddAssetDialog) {
        AddAssetDialog(
            onDismiss = { showAddAssetDialog = false },
            onAdd = { symbol, name, qty, buyPrice, currPrice, type, returnApr, broker, sipAmt ->
                viewModel.addInvestment(symbol, name, qty, buyPrice, currPrice, type, returnApr, broker, sipAmt)
                showAddAssetDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentAssetRow(asset: Investment, viewModel: FinanceViewModel) {
    val totalCost = asset.purchasePrice * asset.quantity
    val currentValue = asset.currentPrice * asset.quantity
    val pnl = currentValue - totalCost
    var editingPrice by remember { mutableStateOf(false) }
    var priceInput by remember { mutableStateOf(asset.currentPrice.toString()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            asset.symbol,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Badge(
                            containerColor = if (asset.assetType == "EQUITY") MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                            contentColor = if (asset.assetType == "EQUITY") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary
                        ) {
                            Text(asset.assetType, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp))
                        }
                    }
                    Text(asset.name, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                
                IconButton(onClick = { viewModel.deleteInvestment(asset.id) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Qty: ${asset.quantity}", fontSize = 12.sp)
                    Text("Buy Price: ₹${asset.purchasePrice}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                
                // Allow manual overrides
                Column(horizontalAlignment = Alignment.End) {
                    if (editingPrice) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = priceInput,
                                onValueChange = { priceInput = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(80.dp).height(46.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(onClick = {
                                priceInput.toDoubleOrNull()?.let {
                                    viewModel.updateInvestmentPrice(asset, it)
                                }
                                editingPrice = false
                            }) {
                                Icon(Icons.Default.Check, contentDescription = "Save Price", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { editingPrice = true }
                        ) {
                            Text("Price: ₹${asset.currentPrice}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Edit, contentDescription = "Edit Price", modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                    Text("Est Yield: ${asset.expectedReturnApr}%", fontSize = 11.sp, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Medium)
                }
            }

            if (asset.brokerName.isNotBlank() || asset.monthlySipAmount > 0.0) {
                Spacer(modifier = Modifier.height(6.dp))
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f))
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (asset.brokerName.isNotBlank()) {
                        Text("Broker: ${asset.brokerName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Medium)
                    }
                    if (asset.monthlySipAmount > 0.0) {
                        Text("Monthly SIP: ₹${String.format("%,.2f", asset.monthlySipAmount)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Holdings Value: ₹${String.format("%,.2f", currentValue)}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text(
                    text = (if (pnl >= 0) "+" else "") + "₹${String.format("%,.2f", pnl)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (pnl >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// ==================== LOGS & SMS TAB ====================
@Composable
fun LogsAndActivityTab(viewModel: FinanceViewModel) {
    val expenses by viewModel.expenses.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val cards by viewModel.cards.collectAsState()

    var mockSmsText by remember { mutableStateOf("Alert: Your HDFC Bank Credit Card ending in 4321 was spent for Rs. 750.00 at STARBUCKS on 2026-07-14.") }
    var showExpenseDialog by remember { mutableStateOf(false) }

    var logTabSelected by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Tab header: Transactions vs Diagnostics
        TabRow(
            selectedTabIndex = logTabSelected,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.clip(RoundedCornerShape(8.dp))
        ) {
            Tab(selected = logTabSelected == 0, onClick = { logTabSelected = 0 }, text = { Text("Expenses Log") })
            Tab(selected = logTabSelected == 1, onClick = { logTabSelected = 1 }, text = { Text("System logs") })
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (logTabSelected == 0) {
            // Expenses list
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("TRANSACTION LOG", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Button(onClick = { showExpenseDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Expense", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Expense")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (expenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No expenses logged yet.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(expenses) { expense ->
                        val card = cards.find { it.id == expense.cardId }
                        ExpenseRow(expense = expense, card = card, viewModel = viewModel, onDelete = { viewModel.deleteExpense(expense) })
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mock SMS trigger widget
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Message, contentDescription = "SMS", tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SIMULATE INCOMING BANK SMS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Text("Test the background parsing engine immediately without sending a real SMS.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.padding(bottom = 8.dp))
                    
                    OutlinedTextField(
                        value = mockSmsText,
                        onValueChange = { mockSmsText = it },
                        modifier = Modifier.fillMaxWidth().height(70.dp),
                        textStyle = TextStyle(fontSize = 11.sp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(onClick = {
                            viewModel.injectMockSms(mockSmsText)
                        }) {
                            Text("Inject SMS", fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            // Diagnostics System Logs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("SYSTEM DIAGNOSTICS", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                TextButton(onClick = { viewModel.injectMockSms("This is a non-transaction test SMS. OTP 12345.") }) {
                    Text("Trigger Warn Log")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No logs recorded.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                        .background(Color.Black)
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    items(logs) { log ->
                        val logColor = when (log.level) {
                            "ERROR" -> MaterialTheme.colorScheme.error
                            "WARN" -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.primary
                        }
                        val timeStr = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(log.timestamp))
                        Text(
                            text = "[$timeStr] ${log.level}: ${log.message}",
                            color = logColor,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }

    if (showExpenseDialog) {
        AddExpenseDialog(
            cards = cards,
            onDismiss = { showExpenseDialog = false },
            onAdd = { amount, merchant, category, cardId, isReimbursable, expCategory ->
                viewModel.addExpense(amount, merchant, category, cardId, isReimbursable, expCategory)
                showExpenseDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseRow(expense: Expense, card: CreditCard?, viewModel: FinanceViewModel, onDelete: () -> Unit) {
    val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(expense.timestamp))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(expense.merchant, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Badge(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), contentColor = MaterialTheme.colorScheme.primary) {
                        Text(expense.category, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp))
                    }
                    if (expense.expenseCategory.isNotBlank() && expense.expenseCategory != "Other" && expense.expenseCategory != "Manual") {
                        Spacer(modifier = Modifier.width(6.dp))
                        Badge(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), contentColor = MaterialTheme.colorScheme.secondary) {
                            Text(expense.expenseCategory, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp))
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(dateStr, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    if (card != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("• Paid via ${card.name} (${card.cardLastFour})", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                }

                if (!expense.rawSmsText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "SMS: \"${expense.rawSmsText}\"",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        lineHeight = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = expense.isReimbursableClaim,
                        onCheckedChange = { viewModel.toggleExpenseReimbursable(expense) },
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reimbursable Claim", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                }
            }

            Text(
                "₹${String.format("%.2f", expense.amount)}",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
            }
        }
    }
}

// ==================== DIALOG COMPONENTS ====================



@Composable
fun AddAssetDialog(onDismiss: () -> Unit, onAdd: (String, String, Double, Double, Double, String, Double, String, Double) -> Unit) {
    var symbol by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("") }
    var buyPrice by remember { mutableStateOf("") }
    var currentPrice by remember { mutableStateOf("") }
    var assetType by remember { mutableStateOf("EQUITY") }
    var returnApr by remember { mutableStateOf("") }
    var brokerName by remember { mutableStateOf("") }
    var monthlySipAmount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Investment Asset") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = symbol, onValueChange = { symbol = it }, label = { Text("Symbol (e.g. RELIANCE, Axis SIP)") })
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Asset Name (e.g. Axis Small Cap)") })
                OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Quantity") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = buyPrice, onValueChange = { buyPrice = it }, label = { Text("Average Buy Price (INR)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = currentPrice, onValueChange = { currentPrice = it }, label = { Text("Current Price (INR)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = brokerName, onValueChange = { brokerName = it }, label = { Text("Broker Name (e.g. Upstox, Zerodha)") })
                
                if (assetType == "MUTUAL_FUND_SIP") {
                    OutlinedTextField(value = monthlySipAmount, onValueChange = { monthlySipAmount = it }, label = { Text("Monthly SIP Amount (INR)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }

                Text("Asset Type:", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = assetType == "EQUITY", onClick = { assetType = "EQUITY" })
                            Text("Equity", fontSize = 13.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = assetType == "OPTION", onClick = { assetType = "OPTION" })
                            Text("Option", fontSize = 13.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = assetType == "FUTURE", onClick = { assetType = "FUTURE" })
                            Text("Future", fontSize = 13.sp)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = assetType == "MUTUAL_FUND_SIP", onClick = { assetType = "MUTUAL_FUND_SIP" })
                            Text("Mutual Fund SIP", fontSize = 13.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = assetType == "MCX_COMMODITY", onClick = { assetType = "MCX_COMMODITY" })
                            Text("MCX Commodity", fontSize = 13.sp)
                        }
                    }
                }

                OutlinedTextField(value = returnApr, onValueChange = { returnApr = it }, label = { Text("Expected Return Yield (%)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        },
        confirmButton = {
            Button(onClick = {
                val qtyVal = qty.toDoubleOrNull() ?: 0.0
                val buyVal = buyPrice.toDoubleOrNull() ?: 0.0
                val curVal = currentPrice.toDoubleOrNull() ?: buyVal
                val returnVal = returnApr.toDoubleOrNull() ?: 12.0
                val sipVal = monthlySipAmount.toDoubleOrNull() ?: 0.0
                if (symbol.isNotBlank() && name.isNotBlank() && qtyVal > 0) {
                    onAdd(symbol.uppercase(), name, qtyVal, buyVal, curVal, assetType, returnVal, brokerName, sipVal)
                }
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddExpenseDialog(cards: List<CreditCard>, onDismiss: () -> Unit, onAdd: (Double, String, String, Long?, Boolean, String) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var merchant by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Manual") }
    var selectedCardId by remember { mutableStateOf<Long?>(null) }
    var isExpanded by remember { mutableStateOf(false) }
    var isReimbursable by remember { mutableStateOf(false) }
    var expenseCategory by remember { mutableStateOf("Office Expense") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log New Expense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount (INR)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = merchant, onValueChange = { merchant = it }, label = { Text("Merchant (e.g. Starbucks)") })
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category (e.g. Food)") })
                OutlinedTextField(value = expenseCategory, onValueChange = { expenseCategory = it }, label = { Text("Detailed Category (e.g. School Fees, Office Expense)") })
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isReimbursable, onCheckedChange = { isReimbursable = it })
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reimbursable Claim", fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("Source Credit Card:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                
                // Simplified Spinner using Box and DropdownMenu
                Box {
                    val selectedCardName = if (selectedCardId == null) "Cash / Bank (Direct)" 
                                            else cards.find { it.id == selectedCardId }?.let { "${it.name} (${it.cardLastFour})" } ?: "Cash / Bank"
                    
                    Button(onClick = { isExpanded = true }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)) {
                        Text(selectedCardName)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Expand")
                    }
                    DropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Cash / Bank (Direct)") },
                            onClick = { selectedCardId = null; isExpanded = false }
                        )
                        cards.forEach { card ->
                            DropdownMenuItem(
                                text = { Text("${card.name} (${card.cardLastFour})") },
                                onClick = { selectedCardId = card.id; isExpanded = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val amtVal = amount.toDoubleOrNull() ?: 0.0
                if (amtVal > 0 && merchant.isNotBlank()) {
                    onAdd(amtVal, merchant, category, selectedCardId, isReimbursable, expenseCategory)
                }
            }) {
                Text("Log")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun EmptyStateCard(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = com.debtfreein.app.R.drawable.img_empty_financial_peace),
                contentDescription = "Financial Peace",
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Financial Peace Active",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = message,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}
