package com.debtfreein.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.debtfreein.app.data.network.UpstoxHolding
import com.debtfreein.app.ui.viewmodel.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpstoxHoldingScreen(
    navController: NavController,
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val state by viewModel.holdingsState.collectAsState()

    // Fetch holdings on screen launch
    LaunchedEffect(Unit) {
        viewModel.fetchUpstoxHoldings(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Upstox Long-term Holdings",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
        containerColor = Color(0xFF0F172A),
        modifier = modifier
    ) { paddingValues ->
        when (val uiState = state) {
            is com.debtfreein.app.ui.viewmodel.FinanceViewModel.HoldingsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(Color(0xFF0F172A)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFF6366F1))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Fetching holdings from Upstox...",
                            color = Color(0xFF94A3B8),
                            fontSize = 14.sp
                        )
                    }
                }
            }
            is com.debtfreein.app.ui.viewmodel.FinanceViewModel.HoldingsUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(Color(0xFF0F172A)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "Error Loading Portfolio",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.message,
                            color = Color(0xFFEF4444),
                            fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.fetchUpstoxHoldings(context) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            is com.debtfreein.app.ui.viewmodel.FinanceViewModel.HoldingsUiState.Success -> {
                val holdings = uiState.holdings
                val totalCost = holdings.sumOf { it.quantity * it.average_price }
                val totalValue = holdings.sumOf { it.quantity * it.last_price }
                val totalPnL = totalValue - totalCost
                val pnlPercent = if (totalCost > 0.0) (totalPnL / totalCost) * 100 else 0.0
                val pnlColor = if (totalPnL >= 0.0) Color(0xFF10B981) else Color(0xFFEF4444)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Portfolio Summary Header Card
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "PORTFOLIO VALUATION",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF94A3B8)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "₹${String.format("%,.2f", totalValue)}",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Total Investment",
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                        Text(
                                            text = "₹${String.format("%,.2f", totalCost)}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Total P&L",
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                        Text(
                                            text = (if (totalPnL >= 0.0) "+" else "") + "₹${String.format("%,.2f", totalPnL)} (${String.format("%.2f", pnlPercent)}%)",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = pnlColor
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Section Title
                    item {
                        Text(
                            text = "HOLDINGS LIST",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    // Holdings List Items
                    items(holdings) { holding ->
                        HoldingItemCard(holding = holding)
                    }
                }
            }
        }
    }
}

@Composable
fun HoldingItemCard(holding: UpstoxHolding) {
    val cost = holding.quantity * holding.average_price
    val currentValue = holding.quantity * holding.last_price
    val pnl = currentValue - cost
    val pnlPercent = if (cost > 0.0) (pnl / cost) * 100 else 0.0
    val pnlColor = if (pnl >= 0.0) Color(0xFF10B981) else Color(0xFFEF4444)

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Company Name & ISIN
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = holding.company_name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = holding.isin,
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Light
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = pnlColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = (if (pnl >= 0.0) "+" else "") + "${String.format("%.2f", pnlPercent)}%",
                        color = pnlColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body: Values & Statistics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "QUANTITY", fontSize = 9.sp, color = Color(0xFF64748B))
                    Text(text = "${holding.quantity}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column {
                    Text(text = "AVG PRICE", fontSize = 9.sp, color = Color(0xFF64748B))
                    Text(text = "₹${String.format("%.2f", holding.average_price)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "LAST PRICE", fontSize = 9.sp, color = Color(0xFF64748B))
                    Text(text = "₹${String.format("%.2f", holding.last_price)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFF334155))
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Footer: Current Value & P&L
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "CURRENT VALUE", fontSize = 9.sp, color = Color(0xFF94A3B8))
                    Text(
                        text = "₹${String.format("%,.2f", currentValue)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "UNREALIZED P&L", fontSize = 9.sp, color = Color(0xFF94A3B8))
                    Text(
                        text = (if (pnl >= 0.0) "+" else "") + "₹${String.format("%,.2f", pnl)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = pnlColor
                    )
                }
            }
        }
    }
}
