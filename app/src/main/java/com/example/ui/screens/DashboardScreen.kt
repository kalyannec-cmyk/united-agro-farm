package com.example.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.BalanceSheetSummary
import com.example.model.DailyTrendPoint
import com.example.model.FarmBalanceSheetLedger
import com.example.model.FarmTransaction
import com.example.model.MonthlyDashboardSummary
import com.example.ui.components.PdfExportDialog
import com.example.ui.components.PdfReportTypeSelection
import com.example.ui.theme.AgroGoldTertiary
import com.example.ui.theme.AgroGreenPrimary
import com.example.ui.viewmodel.FarmScreen
import java.text.NumberFormat
import java.util.Locale

enum class DashboardViewMode {
    RECHARTS_WEB,
    COMPOSE_NATIVE
}

@Composable
fun DashboardScreen(
    summary: MonthlyDashboardSummary,
    selectedMonth: String,
    balanceSheet: BalanceSheetSummary? = null,
    balanceSheetLedger: FarmBalanceSheetLedger? = null,
    transactions: List<FarmTransaction> = emptyList(),
    isEnglish: Boolean = false,
    onMonthChange: (String) -> Unit,
    onNavigate: (FarmScreen) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var viewMode by remember { mutableStateOf(DashboardViewMode.RECHARTS_WEB) }
    var selectedPointForDetail by remember { mutableStateOf<DailyTrendPoint?>(null) }
    var showPdfDialog by remember { mutableStateOf(false) }

    val formattedIncome = formatNpr(summary.totalIncome)
    val formattedExpense = formatNpr(summary.totalExpense)
    val formattedNet = formatNpr(summary.netCashFlow)
    val formattedInv = formatNpr(summary.currentInventoryValue)

    val marginPercent = if (summary.totalIncome > 0) {
        (summary.netCashFlow / summary.totalIncome) * 100.0
    } else 0.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        // Top Header with Month Navigator & Engine Toggle
        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onMonthChange(shiftMonth(selectedMonth, -1)) },
                            modifier = Modifier.testTag("btn_prev_dashboard_month")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous Month",
                                tint = AgroGreenPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = AgroGreenPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isEnglish) summary.monthLabelEn else summary.monthLabelNp,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AgroGreenPrimary
                                )
                            }
                            Text(
                                text = if (isEnglish) "United Agro Farm Dashboard" else "ललितपुर युनाइटेड एग्रो फार्म - वित्त तथा मौज्दात",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { onMonthChange(shiftMonth(selectedMonth, 1)) },
                            modifier = Modifier.testTag("btn_next_dashboard_month")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next Month",
                                tint = AgroGreenPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Engine Mode Switcher
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = viewMode == DashboardViewMode.RECHARTS_WEB,
                            onClick = { viewMode = DashboardViewMode.RECHARTS_WEB },
                            label = {
                                Text(
                                    text = if (isEnglish) "Recharts Engine (Web)" else "Recharts ड्यासबोर्ड (Interactive)",
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Web,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AgroGreenPrimary,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            ),
                            modifier = Modifier.testTag("chip_recharts_mode")
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        FilterChip(
                            selected = viewMode == DashboardViewMode.COMPOSE_NATIVE,
                            onClick = { viewMode = DashboardViewMode.COMPOSE_NATIVE },
                            label = {
                                Text(
                                    text = if (isEnglish) "Native Compose" else "नेटिभ ग्राफिक्स",
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AgroGreenPrimary,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            ),
                            modifier = Modifier.testTag("chip_compose_mode")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { showPdfDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD54F),
                            contentColor = Color(0xFF1B3B22)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("btn_dashboard_export_pdf")
                    ) {
                        Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEnglish) "Export Monthly Report (PDF)" else "मासिक वासलात तथा वित्तीय समीक्षा (PDF Export)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Summary KPI Strip (Cards)
        item {
            Column(modifier = Modifier.padding(horizontal = 14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiStatCard(
                        title = if (isEnglish) "Total Income" else "कुल आम्दानी",
                        value = formattedIncome,
                        subtitle = if (isEnglish) "Top: ${summary.topIncomeCategory}" else "मुख्य: ${summary.topIncomeCategory}",
                        color = Color(0xFF2E7D32),
                        icon = Icons.Default.TrendingUp,
                        modifier = Modifier.weight(1f)
                    )

                    KpiStatCard(
                        title = if (isEnglish) "Total Expenses" else "कुल खर्च",
                        value = formattedExpense,
                        subtitle = if (isEnglish) "Top: ${summary.topExpenseCategory}" else "मुख्य: ${summary.topExpenseCategory}",
                        color = Color(0xFFD32F2F),
                        icon = Icons.Default.ArrowDownward,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiStatCard(
                        title = if (isEnglish) "Net Cash Flow" else "खुद नाफा / बचत",
                        value = formattedNet,
                        subtitle = String.format(Locale.US, "%.1f%% %s", marginPercent, if (isEnglish) "Margin" else "नाफा दर"),
                        color = if (summary.netCashFlow >= 0) Color(0xFFE65100) else Color(0xFFC2185B),
                        icon = Icons.Default.MonetizationOn,
                        modifier = Modifier.weight(1f)
                    )

                    KpiStatCard(
                        title = if (isEnglish) "Stock Valuation" else "मौज्दात मूल्यांकन",
                        value = formattedInv,
                        subtitle = "${summary.totalStockUnits.toInt()} ${if (isEnglish) "items in stock" else "एकाइ स्टक"}",
                        color = Color(0xFF00796B),
                        icon = Icons.Default.Inventory2,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Active View Section: Either Recharts Webview or Native Compose Charts
        item {
            Spacer(modifier = Modifier.height(14.dp))
            if (viewMode == DashboardViewMode.RECHARTS_WEB) {
                // Recharts Web Engine inside AndroidView
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .testTag("card_recharts_container"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(Color(0xFF2E7D32), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isEnglish) "Recharts Dual Dashboard" else "Recharts अन्तरक्रियात्मक चार्ट",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "React + Recharts v3",
                                style = MaterialTheme.typography.labelSmall,
                                color = AgroGreenPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // WebView Container
                        RechartsWebViewContainer(
                            summary = summary,
                            isEnglish = isEnglish,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(640.dp)
                        )
                    }
                }
            } else {
                // Native Compose Charts (Canvas Bar & Area charts)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                ) {
                    // Chart 1: Daily Income vs Expenses Bar Chart
                    NativeDailyIncomeExpenseChart(
                        trends = summary.dailyTrends,
                        isEnglish = isEnglish,
                        selectedPoint = selectedPointForDetail,
                        onSelectPoint = { pt -> selectedPointForDetail = pt }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Chart 2: Inventory Value Trends Area Chart
                    NativeInventoryTrendChart(
                        trends = summary.dailyTrends,
                        isEnglish = isEnglish,
                        selectedPoint = selectedPointForDetail,
                        onSelectPoint = { pt -> selectedPointForDetail = pt }
                    )
                }
            }
        }

        // Selected Day Detail Card (if user tapped a day in native chart)
        item {
            selectedPointForDetail?.let { pt ->
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .testTag("card_day_detail"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                    border = BorderStroke(1.dp, Color(0xFFA5D6A7))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${pt.dateStr} (${pt.dayLabel})",
                                fontWeight = FontWeight.Bold,
                                color = AgroGreenPrimary,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${if (isEnglish) "Income" else "आम्दानी"}: ${formatNpr(pt.income)}  •  ${if (isEnglish) "Expense" else "खर्च"}: ${formatNpr(pt.expense)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${if (isEnglish) "Inventory Value" else "मौज्दात मूल्य"}: ${formatNpr(pt.inventoryValue)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF00796B)
                            )
                        }
                        IconButton(onClick = { selectedPointForDetail = null }) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Close", tint = AgroGreenPrimary)
                        }
                    }
                }
            }
        }

        // Daily Ledger Breakdown Table
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.padding(horizontal = 14.dp)) {
                Text(
                    text = if (isEnglish) "Day-by-Day Financial & Stock Log" else "दैनिक आय-व्यय तथा मौज्दात विवरण",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isEnglish) "Detailed record for ${summary.monthYear}" else "${summary.monthYear} महिनाको दैनिक विवरण",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (summary.dailyTrends.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isEnglish) "No transactions recorded for this month yet." else "यस महिनाको कुनै कारोबार दर्ता भएको छैन।",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(summary.dailyTrends.reversed()) { pt ->
                DailyTrendRowCard(
                    point = pt,
                    isEnglish = isEnglish,
                    onClick = { selectedPointForDetail = pt }
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
        }

        // Quick Module Action Buttons
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { onNavigate(FarmScreen.FINANCE) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_goto_finance"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AgroGreenPrimary)
                ) {
                    Icon(imageVector = Icons.Default.MonetizationOn, contentDescription = null, tint = AgroGreenPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = if (isEnglish) "Record Cash Book" else "नयाँ आय-व्यय", color = AgroGreenPrimary, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onNavigate(FarmScreen.INVENTORY) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_goto_inventory"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary)
                ) {
                    Icon(imageVector = Icons.Default.Inventory2, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = if (isEnglish) "Manage Stock" else "स्टक अपडेट", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showPdfDialog) {
        PdfExportDialog(
            initialType = PdfReportTypeSelection.FINANCIAL_SUMMARY,
            balanceSheet = balanceSheet ?: BalanceSheetSummary(
                asOfDate = selectedMonth,
                cashInHand = 85000.0,
                bankBalanceMuktinath = 425000.0,
                esewaBalance = 35000.0,
                accountsReceivable = 48000.0,
                inventoryStockValue = summary.currentInventoryValue,
                fixedAssetsEquipment = 650000.0,
                fixedAssetsLandShed = 1200000.0,
                livestockValuation = 850000.0,
                accountsPayable = 65000.0,
                accruedSalaries = 45000.0,
                shortTermLoans = 250000.0,
                ownerCapital = 2500000.0,
                netFarmProfit = summary.netCashFlow
            ),
            balanceSheetLedger = balanceSheetLedger ?: FarmBalanceSheetLedger(),
            dashboardSummary = summary,
            transactions = transactions,
            isEnglish = isEnglish,
            onDismiss = { showPdfDialog = false }
        )
    }
}

@Composable
private fun KpiStatCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(color.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun DailyTrendRowCard(
    point: DailyTrendPoint,
    isEnglish: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .clickable { onClick() }
            .testTag("trend_row_${point.day}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(AgroGreenPrimary.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${point.day}",
                        fontWeight = FontWeight.Bold,
                        color = AgroGreenPrimary,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = point.dayLabel,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${if (isEnglish) "Stock Val" else "मौज्दात"}: ${formatNpr(point.inventoryValue)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF00796B),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "+${formatNpr(point.income)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "-${formatNpr(point.expense)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F)
                    )
                }
                val netColor = if (point.net >= 0) Color(0xFFE65100) else Color(0xFFC2185B)
                val sign = if (point.net >= 0) "+" else ""
                Text(
                    text = "${if (isEnglish) "Net" else "खुद"}: $sign${formatNpr(point.net)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = netColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// --------------------------------------------------------------------------
// RECHARTS WEBVIEW INTEGRATION COMPONENT
// --------------------------------------------------------------------------

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun RechartsWebViewContainer(
    summary: MonthlyDashboardSummary,
    isEnglish: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    val jsonPayload = remember(summary, isEnglish) {
        buildDashboardJson(summary, isEnglish)
    }

    LaunchedEffect(jsonPayload, webViewRef) {
        webViewRef?.let { webView ->
            val escaped = jsonPayload.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ")
            webView.evaluateJavascript("window.updateDashboardData(\"$escaped\");", null)
        }
    }

    AndroidView(
        modifier = modifier.testTag("recharts_webview"),
        factory = { ctx ->
            WebView(ctx).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    allowFileAccess = true
                }
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        val escaped = jsonPayload.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ")
                        view?.evaluateJavascript("window.updateDashboardData(\"$escaped\");", null)
                    }
                }
                loadUrl("file:///android_asset/recharts_dashboard.html")
                webViewRef = this
            }
        },
        update = { webView ->
            val escaped = jsonPayload.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ")
            webView.evaluateJavascript("window.updateDashboardData(\"$escaped\");", null)
        }
    )
}

// --------------------------------------------------------------------------
// NATIVE COMPOSE CANVAS CHARTS
// --------------------------------------------------------------------------

@Composable
private fun NativeDailyIncomeExpenseChart(
    trends: List<DailyTrendPoint>,
    isEnglish: Boolean,
    selectedPoint: DailyTrendPoint?,
    onSelectPoint: (DailyTrendPoint) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("native_income_expense_chart"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEnglish) "Daily Income vs Expenses" else "दैनिक आम्दानी तथा खर्च तुलना",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isEnglish) "Tap bar for details" else "विवरणका लागि छुनुहोस्",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val maxAmount = remember(trends) {
                val m = trends.maxOfOrNull { maxOf(it.income, it.expense) } ?: 10000.0
                if (m <= 0) 10000.0 else m * 1.15
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(trends) {
                            detectTapGestures { offset ->
                                val slotW = size.width / trends.size.coerceAtLeast(1)
                                val tappedIdx = (offset.x / slotW).toInt().coerceIn(0, (trends.size - 1).coerceAtLeast(0))
                                if (tappedIdx in trends.indices) {
                                    onSelectPoint(trends[tappedIdx])
                                }
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    val bottomMargin = 24.dp.toPx()
                    val chartH = h - bottomMargin
                    val n = trends.size
                    if (n == 0) return@Canvas

                    val slotW = w / n
                    val barW = (slotW * 0.32f).coerceIn(4f, 20f)

                    // Draw 3 subtle horizontal dashed guide lines
                    val steps = 3
                    for (i in 1..steps) {
                        val y = chartH * (i.toFloat() / steps)
                        drawLine(
                            color = Color(0xFFECEFF1),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Draw bottom axis
                    drawLine(
                        color = Color(0xFFCFD8DC),
                        start = Offset(0f, chartH),
                        end = Offset(w, chartH),
                        strokeWidth = 1.5.dp.toPx()
                    )

                    // Draw dual bars for each trend point
                    for ((idx, pt) in trends.withIndex()) {
                        val cx = idx * slotW + slotW / 2f
                        val incH = ((pt.income / maxAmount) * chartH).toFloat().coerceAtLeast(3f)
                        val expH = ((pt.expense / maxAmount) * chartH).toFloat().coerceAtLeast(3f)

                        val incX = cx - barW - 1.5f
                        val incY = chartH - incH

                        val expX = cx + 1.5f
                        val expY = chartH - expH

                        val isSel = selectedPoint?.day == pt.day

                        // Income Bar (Green)
                        drawRoundRect(
                            color = if (isSel) Color(0xFF1B5E20) else Color(0xFF2E7D32),
                            topLeft = Offset(incX, incY),
                            size = Size(barW, incH),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )

                        // Expense Bar (Red)
                        drawRoundRect(
                            color = if (isSel) Color(0xFFB71C1C) else Color(0xFFD32F2F),
                            topLeft = Offset(expX, expY),
                            size = Size(barW, expH),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )

                        // Highlight halo if tapped
                        if (isSel) {
                            drawCircle(
                                color = AgroGoldTertiary,
                                radius = 4.dp.toPx(),
                                center = Offset(cx, chartH + 14.dp.toPx())
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(10.dp).background(Color(0xFF2E7D32), RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (isEnglish) "Daily Income" else "दैनिक आम्दानी", style = MaterialTheme.typography.labelSmall)

                Spacer(modifier = Modifier.width(16.dp))

                Box(modifier = Modifier.size(10.dp).background(Color(0xFFD32F2F), RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (isEnglish) "Daily Expenses" else "दैनिक खर्च", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun NativeInventoryTrendChart(
    trends: List<DailyTrendPoint>,
    isEnglish: Boolean,
    selectedPoint: DailyTrendPoint?,
    onSelectPoint: (DailyTrendPoint) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("native_inventory_trend_chart"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEnglish) "Total Inventory Value Trends" else "कुल मौज्दात मूल्य ट्रेन्ड (महिना)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Area Valuation",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF00796B),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val minVal = remember(trends) {
                val m = trends.minOfOrNull { it.inventoryValue } ?: 200000.0
                (m * 0.95).coerceAtLeast(0.0)
            }
            val maxVal = remember(trends) {
                val m = trends.maxOfOrNull { it.inventoryValue } ?: 300000.0
                m * 1.05
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(trends) {
                            detectTapGestures { offset ->
                                val slotW = size.width / (trends.size - 1).coerceAtLeast(1)
                                val tappedIdx = ((offset.x + slotW / 2) / slotW).toInt().coerceIn(0, (trends.size - 1).coerceAtLeast(0))
                                if (tappedIdx in trends.indices) {
                                    onSelectPoint(trends[tappedIdx])
                                }
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    val bottomMargin = 24.dp.toPx()
                    val chartH = h - bottomMargin
                    val n = trends.size
                    if (n < 2) return@Canvas

                    val range = (maxVal - minVal).coerceAtLeast(1.0)
                    val points = trends.mapIndexed { idx, pt ->
                        val x = idx * (w / (n - 1))
                        val norm = ((pt.inventoryValue - minVal) / range).toFloat().coerceIn(0f, 1f)
                        val y = chartH - (norm * (chartH - 20.dp.toPx())) - 10.dp.toPx()
                        Offset(x, y)
                    }

                    // Background grid
                    for (i in 1..3) {
                        val y = chartH * (i.toFloat() / 3)
                        drawLine(
                            color = Color(0xFFECEFF1),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Area Path
                    val areaPath = Path().apply {
                        moveTo(points[0].x, chartH)
                        for (pt in points) {
                            lineTo(pt.x, pt.y)
                        }
                        lineTo(points.last().x, chartH)
                        close()
                    }

                    drawPath(
                        path = areaPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF00796B).copy(alpha = 0.45f),
                                Color(0xFF00796B).copy(alpha = 0.05f)
                            ),
                            startY = 0f,
                            endY = chartH
                        )
                    )

                    // Line Path
                    val linePath = Path().apply {
                        moveTo(points[0].x, points[0].y)
                        for (i in 1 until points.size) {
                            val prev = points[i - 1]
                            val curr = points[i]
                            val midX = (prev.x + curr.x) / 2f
                            cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                        }
                    }

                    drawPath(
                        path = linePath,
                        color = Color(0xFF00796B),
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // Points
                    for ((idx, pt) in points.withIndex()) {
                        val isSel = selectedPoint?.day == trends[idx].day
                        drawCircle(
                            color = if (isSel) AgroGoldTertiary else Color.White,
                            radius = if (isSel) 6.dp.toPx() else 4.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = Color(0xFF00796B),
                            radius = if (isSel) 6.dp.toPx() else 4.dp.toPx(),
                            center = pt,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(10.dp).background(Color(0xFF00796B), CircleShape))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isEnglish) "Farm Stock Valuation Curve (NPR)" else "कुल फार्म मौज्दात मूल्य रेखा (रू)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// --------------------------------------------------------------------------
// HELPERS
// --------------------------------------------------------------------------

private fun buildDashboardJson(
    summary: MonthlyDashboardSummary,
    isEnglish: Boolean
): String {
    val dailyList = summary.dailyTrends.joinToString(",") { p ->
        """{"day":${p.day},"dayLabel":"${p.dayLabel}","dateStr":"${p.dateStr}","income":${p.income},"expense":${p.expense},"net":${p.net},"inventoryValue":${p.inventoryValue}}"""
    }
    val label = if (isEnglish) summary.monthLabelEn else summary.monthLabelNp
    return """
    {
      "monthLabel": "$label",
      "isEnglish": $isEnglish,
      "kpis": {
        "totalIncome": ${summary.totalIncome},
        "totalExpense": ${summary.totalExpense},
        "netCashFlow": ${summary.netCashFlow},
        "inventoryValuation": ${summary.currentInventoryValue},
        "totalStockUnits": ${summary.totalStockUnits.toInt()},
        "lowStockCount": ${summary.lowStockCount}
      },
      "dailyData": [$dailyList]
    }
    """.trimIndent()
}

private fun formatNpr(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))
    return "रू " + formatter.format(amount.toInt())
}
