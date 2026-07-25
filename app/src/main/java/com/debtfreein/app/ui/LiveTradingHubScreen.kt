package com.debtfreein.app.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.debtfreein.app.data.network.CapitalAllocator
import com.debtfreein.app.data.network.UpstoxExecutionService
import com.debtfreein.app.data.security.TokenManager
import com.debtfreein.app.ui.viewmodel.FinanceViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveTradingHubScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sysState by UpstoxExecutionService.systemState.collectAsState()
    val logs by UpstoxExecutionService.logs.collectAsState()
    val netPnL by CapitalAllocator.currentNetPnL.collectAsState()
    val pendingTickets by UpstoxExecutionService.pendingTickets.collectAsState()

    LaunchedEffect(Unit) {
        // Enforce live execution mode on entry
        UpstoxExecutionService.setPaperTradingActive(false)
        com.debtfreein.app.data.system.SystemStatusManager.setLiveTradingActive(true)
    }

    DisposableEffect(Unit) {
        onDispose {
            com.debtfreein.app.data.system.SystemStatusManager.setLiveTradingActive(false)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "LIVE REAL TRADING ENGINE",
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                        Text(
                            "Real Capital Execution • Upstox V3 Broker API",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        containerColor = Color(0xFF0F172A)
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Live Capital Warning Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF450A0A)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFEF4444), RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "LIVE REAL MONEY ENGINE ACTIVE",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                        Text(
                            "Orders confirmed here execute directly on your Upstox broker account.",
                            color = Color(0xFFFCA5A5),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Real-Time Metrics & Guardrails
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Live Session Guardrails", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Live Session PnL:", color = Color(0xFF94A3B8), fontSize = 13.sp)
                        val pnlColor = if (netPnL >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                        Text(
                            text = (if (netPnL >= 0) "+" else "") + "₹${String.format("%.2f", netPnL)}",
                            color = pnlColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Broker Auth Token Status:", color = Color(0xFF94A3B8), fontSize = 13.sp)
                        val isValid = TokenManager.isTokenValid()
                        Text(
                            if (isValid) "JWT Valid" else "Expired / Missing",
                            color = if (isValid) Color(0xFF10B981) else Color(0xFFEF4444),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Execution Guard State:", color = Color(0xFF94A3B8), fontSize = 13.sp)
                        Text(sysState.name, color = Color(0xFF6366F1), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            // Emergency Stop Button
            Button(
                onClick = {
                    viewModel.emergencyStop()
                    Toast.makeText(context, "LIVE EMERGENCY STOP TRIGGERED!", Toast.LENGTH_LONG).show()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
            ) {
                Text("EMERGENCY KILL SWITCH", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 16.sp)
            }

            // Pending Live Trade Tickets
            Text("Pending Live Orders", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            if (pendingTickets.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No live orders queued. Risk controls active.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                }
            } else {
                pendingTickets.forEach { ticket ->
                    com.debtfreein.app.data.network.TradeTicketCard(
                        trade = ticket,
                        onConfirm = {
                            kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                                UpstoxExecutionService.executeTrade(ticket)
                            }
                        },
                        onCancel = {
                            UpstoxExecutionService.rejectTrade(ticket)
                        }
                    )
                }
            }

            // Real Live Engine Logs
            Text("Live Execution Logs", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(180.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (logs.isEmpty()) {
                        Text("No live logs.", color = Color(0xFF64748B), fontSize = 12.sp)
                    } else {
                        logs.forEach { log ->
                            Text(
                                text = log,
                                color = Color(0xFFEF4444),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
