package com.example.ui.screens

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.local.Order
import com.example.ui.theme.AgroGoldTertiary
import com.example.ui.theme.AgroGreenPrimary
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Data classes for Monthly Revenue calculations
data class MonthRevenueBucket(
    val monthKey: String,          // e.g. "2026-10"
    val monthLabelNp: String,      // e.g. "कार्तिक २०८३"
    val monthLabelEn: String,      // e.g. "Oct 2026"
    val totalRevenue: Double,
    val verifiedRevenue: Double,
    val pendingRevenue: Double,
    val orderCount: Int,
    val averageOrderValue: Double,
    val orders: List<Order>
)

data class RevenueAnalytics(
    val totalRevenue: Double,
    val verifiedRevenue: Double,
    val pendingRevenue: Double,
    val totalOrders: Int,
    val averageOrderValue: Double,
    val peakMonthLabel: String,
    val peakMonthRevenue: Double,
    val monthlyBuckets: List<MonthRevenueBucket>,
    val verifiedOrdersCount: Int,
    val pendingOrdersCount: Int
)

enum class RevenueVisualizationEngine {
    D3_RECHARTS_WEB,
    NATIVE_COMPOSE_CANVAS
}

/**
 * Computes monthly revenue metrics from Room database orders.
 */
fun aggregateOrdersByMonth(orders: List<Order>): RevenueAnalytics {
    val nonCancelled = orders.filter { it.status != "CANCELLED" }
    val totalRev = nonCancelled.sumOf { it.totalAmount }
    val verifiedRev = nonCancelled.filter { it.status == "VERIFIED" || it.status == "COMPLETED" }.sumOf { it.totalAmount }
    val pendingRev = nonCancelled.filter { it.status == "PENDING_VERIFICATION" }.sumOf { it.totalAmount }
    val totalCount = orders.size
    val aov = if (nonCancelled.isNotEmpty()) totalRev / nonCancelled.size else 0.0

    val sdfYearMonth = SimpleDateFormat("yyyy-MM", Locale.US)
    val sdfFull = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    // Group orders by month key
    val grouped = orders.groupBy { order ->
        val dateStr = order.orderDate.trim()
        when {
            dateStr.length >= 7 && dateStr[4] == '-' -> dateStr.substring(0, 7)
            order.createdAt > 0 -> sdfYearMonth.format(Date(order.createdAt))
            else -> "2026-10"
        }
    }

    // Map month key to Nepali and English labels
    fun getMonthLabels(key: String): Pair<String, String> {
        val parts = key.split("-")
        val monthNum = parts.getOrNull(1)?.toIntOrNull() ?: 10
        val yearNum = parts.getOrNull(0) ?: "2026"
        return when (monthNum) {
            1 -> "माघ २०८२" to "Jan $yearNum"
            2 -> "फागुन २०८२" to "Feb $yearNum"
            3 -> "चैत २०८२" to "Mar $yearNum"
            4 -> "वैशाख २०८३" to "Apr $yearNum"
            5 -> "जेठ २०८३" to "May $yearNum"
            6 -> "असार २०८३" to "Jun $yearNum"
            7 -> "साउन २०८३" to "Jul $yearNum"
            8 -> "भदौ २०८३" to "Aug $yearNum"
            9 -> "असोज २०८३" to "Sep $yearNum"
            10 -> "कार्तिक २०८३" to "Oct $yearNum"
            11 -> "मंसिर २०८३" to "Nov $yearNum"
            12 -> "पुस २०८३" to "Dec $yearNum"
            else -> "महिना $key" to key
        }
    }

    val sortedKeys = grouped.keys.sorted()
    val buckets = sortedKeys.map { key ->
        val list = grouped[key] ?: emptyList()
        val mTotal = list.filter { it.status != "CANCELLED" }.sumOf { it.totalAmount }
        val mVerified = list.filter { it.status == "VERIFIED" || it.status == "COMPLETED" }.sumOf { it.totalAmount }
        val mPending = list.filter { it.status == "PENDING_VERIFICATION" }.sumOf { it.totalAmount }
        val count = list.size
        val mAov = if (count > 0) mTotal / count else 0.0
        val (npLabel, enLabel) = getMonthLabels(key)

        MonthRevenueBucket(
            monthKey = key,
            monthLabelNp = npLabel,
            monthLabelEn = enLabel,
            totalRevenue = mTotal,
            verifiedRevenue = mVerified,
            pendingRevenue = mPending,
            orderCount = count,
            averageOrderValue = mAov,
            orders = list.sortedByDescending { it.createdAt }
        )
    }

    val peakBucket = buckets.maxByOrNull { it.totalRevenue }
    val peakLabel = peakBucket?.monthLabelEn ?: "N/A"
    val peakRev = peakBucket?.totalRevenue ?: 0.0
    val verifiedCount = orders.count { it.status == "VERIFIED" || it.status == "COMPLETED" }
    val pendingCount = orders.count { it.status == "PENDING_VERIFICATION" }

    return RevenueAnalytics(
        totalRevenue = totalRev,
        verifiedRevenue = verifiedRev,
        pendingRevenue = pendingRev,
        totalOrders = totalCount,
        averageOrderValue = aov,
        peakMonthLabel = peakLabel,
        peakMonthRevenue = peakRev,
        monthlyBuckets = buckets,
        verifiedOrdersCount = verifiedCount,
        pendingOrdersCount = pendingCount
    )
}

/**
 * Builds JSON payload for WebView D3 & Recharts asset.
 */
fun buildRevenueJsonPayload(analytics: RevenueAnalytics, isEnglish: Boolean, orders: List<Order>): String {
    val monthsJson = analytics.monthlyBuckets.joinToString(",") { b ->
        """{"monthKey":"${b.monthKey}","monthLabelNp":"${b.monthLabelNp}","monthLabelEn":"${b.monthLabelEn}","revenue":${b.totalRevenue},"verifiedRevenue":${b.verifiedRevenue},"pendingRevenue":${b.pendingRevenue},"orderCount":${b.orderCount},"aov":${b.averageOrderValue.toInt()}}"""
    }

    val statusVerified = orders.filter { it.status == "VERIFIED" }
    val statusCompleted = orders.filter { it.status == "COMPLETED" }
    val statusPending = orders.filter { it.status == "PENDING_VERIFICATION" }

    val statusJson = """[
        {"status":"VERIFIED","labelNp":"प्रमाणित","labelEn":"Verified","count":${statusVerified.size},"amount":${statusVerified.sumOf { it.totalAmount }},"color":"#2E7D32"},
        {"status":"COMPLETED","labelNp":"सम्पन्न","labelEn":"Completed","count":${statusCompleted.size},"amount":${statusCompleted.sumOf { it.totalAmount }},"color":"#00796B"},
        {"status":"PENDING","labelNp":"प्रतिक्षारत","labelEn":"Pending Verification","count":${statusPending.size},"amount":${statusPending.sumOf { it.totalAmount }},"color":"#F57F17"}
    ]"""

    val modeBank = orders.filter { it.paymentMode == "BANK_TRANSFER" }
    val modeCod = orders.filter { it.paymentMode == "CASH_ON_DELIVERY" }
    val modeEsewa = orders.filter { it.paymentMode.contains("ESEWA", ignoreCase = true) }

    val paymentJson = """[
        {"mode":"BANK_TRANSFER","labelNp":"मुक्तिनाथ बैंक ट्रान्सफर","labelEn":"Muktinath Bank Transfer","count":${modeBank.size},"amount":${modeBank.sumOf { it.totalAmount }},"color":"#1B5E20"},
        {"mode":"CASH_ON_DELIVERY","labelNp":"नगद भुक्तानी (COD)","labelEn":"Cash on Delivery","count":${modeCod.size},"amount":${modeCod.sumOf { it.totalAmount }},"color":"#FFB300"},
        {"mode":"ESEWA","labelNp":"ईसेवा वालेट","labelEn":"eSewa Wallet","count":${modeEsewa.size},"amount":${modeEsewa.sumOf { it.totalAmount }},"color":"#43A047"}
    ]"""

    return """{
        "isEnglish":$isEnglish,
        "totalRevenue":${analytics.totalRevenue},
        "verifiedRevenue":${analytics.verifiedRevenue},
        "pendingRevenue":${analytics.pendingRevenue},
        "totalOrders":${analytics.totalOrders},
        "averageOrderValue":${analytics.averageOrderValue.toInt()},
        "peakMonth":"${analytics.peakMonthLabel}",
        "peakMonthRevenue":${analytics.peakMonthRevenue},
        "months":[$monthsJson],
        "statusBreakdown":$statusJson,
        "paymentModeBreakdown":$paymentJson
    }""".replace("\n", " ").trim()
}

/**
 * MonthlyRevenueScreen Composable
 * Visualizes monthly revenue based on orders stored in the Room database
 * using D3.js and Recharts, with interactive tooltips, breakdown tables,
 * and an optional native Jetpack Compose Canvas visualization.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyRevenueScreen(
    orders: List<Order>,
    isEnglish: Boolean = false,
    onBack: (() -> Unit)? = null,
    onNavigateToPastOrders: (() -> Unit)? = null,
    onNavigateToCreateOrder: (() -> Unit)? = null,
    onSeedSampleOrders: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = onBack != null) {
        onBack?.invoke()
    }

    var selectedEngine by remember { mutableStateOf(RevenueVisualizationEngine.D3_RECHARTS_WEB) }
    var expandedMonthKey by remember { mutableStateOf<String?>(null) }
    var verifiedOnlyFilter by remember { mutableStateOf(false) }

    val filteredOrders = remember(orders, verifiedOnlyFilter) {
        if (verifiedOnlyFilter) {
            orders.filter { it.status == "VERIFIED" || it.status == "COMPLETED" }
        } else {
            orders
        }
    }

    val analytics = remember(filteredOrders) {
        aggregateOrdersByMonth(filteredOrders)
    }

    val jsonPayload = remember(analytics, isEnglish, filteredOrders) {
        buildRevenueJsonPayload(analytics, isEnglish, filteredOrders)
    }

    val currencyFormat = remember {
        NumberFormat.getNumberInstance(Locale("en", "IN"))
    }

    fun formatNpr(value: Double): String = "रू " + currencyFormat.format(value.toLong())

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("monthly_revenue_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isEnglish) "Monthly Revenue (D3 & Recharts)" else "मासिक आम्दानी (D3/Recharts)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (isEnglish) "Visualized from ${orders.size} Room Database Orders" else "Room Database का ${orders.size} अर्डरहरूमा आधारित",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("revenue_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }
                },
                actions = {
                    if (onNavigateToPastOrders != null) {
                        IconButton(
                            onClick = onNavigateToPastOrders,
                            modifier = Modifier.testTag("revenue_goto_orders_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = if (isEnglish) "Past Orders" else "विगतका अर्डरहरू",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AgroGreenPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. KPI Summary Metrics Cards
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KpiSummaryCard(
                            title = if (isEnglish) "TOTAL REVENUE" else "कुल आम्दानी (REVENUE)",
                            value = formatNpr(analytics.totalRevenue),
                            subtitle = if (isEnglish) "${analytics.totalOrders} total orders" else "कुल ${analytics.totalOrders} वटा अर्डरहरू",
                            accentColor = AgroGreenPrimary,
                            icon = Icons.Default.MonetizationOn,
                            modifier = Modifier.weight(1f)
                        )
                        KpiSummaryCard(
                            title = if (isEnglish) "VERIFIED REVENUE" else "प्रमाणित आम्दानी",
                            value = formatNpr(analytics.verifiedRevenue),
                            subtitle = if (isEnglish) "${analytics.verifiedOrdersCount} verified" else "${analytics.verifiedOrdersCount} वटा प्रमाणित",
                            accentColor = Color(0xFF00796B),
                            icon = Icons.Default.CheckCircle,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KpiSummaryCard(
                            title = if (isEnglish) "AVG ORDER (AOV)" else "औसत अर्डर मूल्य",
                            value = formatNpr(analytics.averageOrderValue),
                            subtitle = if (isEnglish) "Per order average" else "प्रति अर्डर औसत रकम",
                            accentColor = Color(0xFF3949AB),
                            icon = Icons.Default.TrendingUp,
                            modifier = Modifier.weight(1f)
                        )
                        KpiSummaryCard(
                            title = if (isEnglish) "PEAK MONTH" else "उच्चतम आम्दानी महिना",
                            value = if (analytics.peakMonthRevenue > 0) formatNpr(analytics.peakMonthRevenue) else "रू 0",
                            subtitle = analytics.peakMonthLabel,
                            accentColor = Color(0xFFF57F17),
                            icon = Icons.Default.CalendarMonth,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 2. Control Bar: Engine Toggle & Filter Chips
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (isEnglish) "Visualization Options" else "भिजुअलाइजेशन विकल्पहरू",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedEngine == RevenueVisualizationEngine.D3_RECHARTS_WEB,
                                onClick = { selectedEngine = RevenueVisualizationEngine.D3_RECHARTS_WEB },
                                label = { Text(if (isEnglish) "D3 & Recharts (Web)" else "D3 / Recharts (Web)") },
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
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("engine_toggle_d3_web")
                            )

                            FilterChip(
                                selected = selectedEngine == RevenueVisualizationEngine.NATIVE_COMPOSE_CANVAS,
                                onClick = { selectedEngine = RevenueVisualizationEngine.NATIVE_COMPOSE_CANVAS },
                                label = { Text(if (isEnglish) "Native Canvas" else "नेभिट क्यानभास") },
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
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("engine_toggle_native_canvas")
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            FilterChip(
                                selected = verifiedOnlyFilter,
                                onClick = { verifiedOnlyFilter = !verifiedOnlyFilter },
                                label = {
                                    Text(
                                        if (isEnglish) "Verified Only" else "प्रमाणित अर्डर मात्र",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                modifier = Modifier.testTag("filter_verified_toggle")
                            )

                            if (orders.size < 6 && onSeedSampleOrders != null) {
                                OutlinedButton(
                                    onClick = onSeedSampleOrders,
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("seed_multi_month_orders_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isEnglish) "Seed 6-Mo Trends" else "६ महिनाको डाटा थप्नुहोस्",
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Main Chart Display
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("revenue_chart_container_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isEnglish) "Monthly Revenue Visualization" else "मासिक आम्दानी तथ्यांक ग्राफ",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (selectedEngine == RevenueVisualizationEngine.D3_RECHARTS_WEB)
                                        "Powered by D3.js SVG & Recharts"
                                    else
                                        "Powered by Jetpack Compose Canvas",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AgroGreenPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = AgroGreenPrimary.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, AgroGreenPrimary.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = if (selectedEngine == RevenueVisualizationEngine.D3_RECHARTS_WEB) "D3 & Recharts" else "Compose Canvas",
                                    color = AgroGreenPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        if (selectedEngine == RevenueVisualizationEngine.D3_RECHARTS_WEB) {
                            // D3 & Recharts WebView Container
                            D3RechartsWebView(
                                jsonPayload = jsonPayload,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(420.dp)
                            )
                        } else {
                            // Native Jetpack Compose Canvas Bar Chart
                            NativeMonthlyRevenueCanvas(
                                buckets = analytics.monthlyBuckets,
                                isEnglish = isEnglish,
                                peakRevenue = analytics.peakMonthRevenue,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(280.dp)
                            )
                        }
                    }
                }
            }

            // 4. Monthly Revenue Breakdown List with expandable order details
            item {
                Text(
                    text = if (isEnglish) "Monthly Breakdown & Audit (${analytics.monthlyBuckets.size} Months)" else "महिना अनुसार अर्डर तथा आम्दानी विवरण (${analytics.monthlyBuckets.size} महिना)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (analytics.monthlyBuckets.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = if (isEnglish) "No monthly orders recorded in Room database yet" else "डाटाबेसमा कुनै मासिक अर्डर तथ्यांक फेला परेन",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            if (onNavigateToCreateOrder != null) {
                                Button(
                                    onClick = onNavigateToCreateOrder,
                                    colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isEnglish) "Create First Order" else "पहिलो अर्डर सिर्जना गर्नुहोस्")
                                }
                            }
                        }
                    }
                }
            } else {
                items(analytics.monthlyBuckets, key = { it.monthKey }) { bucket ->
                    val isExpanded = expandedMonthKey == bucket.monthKey
                    val label = if (isEnglish) bucket.monthLabelEn else bucket.monthLabelNp
                    val progress = if (analytics.peakMonthRevenue > 0) {
                        (bucket.totalRevenue / analytics.peakMonthRevenue).toFloat().coerceIn(0f, 1f)
                    } else 0f

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                            .testTag("month_card_${bucket.monthKey}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expandedMonthKey = if (isExpanded) null else bucket.monthKey
                                }
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .background(AgroGreenPrimary.copy(alpha = 0.12f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = null,
                                            tint = AgroGreenPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (isEnglish) "${bucket.orderCount} orders • AOV ${formatNpr(bucket.averageOrderValue)}" else "${bucket.orderCount} अर्डरहरू • औसत ${formatNpr(bucket.averageOrderValue)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = formatNpr(bucket.totalRevenue),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = AgroGreenPrimary
                                        )
                                        Text(
                                            text = if (isEnglish) "Verified: ${formatNpr(bucket.verifiedRevenue)}" else "प्रमाणित: ${formatNpr(bucket.verifiedRevenue)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF00796B),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Share of Peak Progress Bar
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp),
                                    color = AgroGreenPrimary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (isEnglish) "Share of peak revenue" else "उच्चतम महिनाको तुलना",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${(progress * 100).toInt()}%",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AgroGreenPrimary
                                    )
                                }
                            }

                            // Expandable list of database orders for this month
                            AnimatedVisibility(visible = isExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    Text(
                                        text = if (isEnglish) "Orders in $label (${bucket.orders.size}):" else "$label का अर्डरहरू (${bucket.orders.size} वटा):",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    bucket.orders.forEach { order ->
                                        MonthOrderDetailItem(
                                            order = order,
                                            isEnglish = isEnglish,
                                            formatNpr = ::formatNpr
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Action Buttons Footer
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (onNavigateToPastOrders != null) {
                        OutlinedButton(
                            onClick = onNavigateToPastOrders,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("revenue_view_all_orders_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AgroGreenPrimary)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isEnglish) "Past Orders List" else "विगतका अर्डरहरू")
                        }
                    }

                    if (onNavigateToCreateOrder != null) {
                        Button(
                            onClick = onNavigateToCreateOrder,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("revenue_create_new_order_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isEnglish) "Create Order" else "नयाँ अर्डर फारम")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Top KPI Summary Card
 */
@Composable
private fun KpiSummaryCard(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * AndroidView WebView embedding D3.js and Recharts asset
 */
@Composable
private fun D3RechartsWebView(
    jsonPayload: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier.testTag("d3_recharts_webview"),
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
                        view?.evaluateJavascript("window.updateRevenueData(\"$escaped\");", null)
                    }
                }
                loadUrl("file:///android_asset/monthly_revenue_chart.html")
            }
        },
        update = { webView ->
            val escaped = jsonPayload.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ")
            webView.evaluateJavascript("window.updateRevenueData(\"$escaped\");", null)
        }
    )
}

/**
 * Native Jetpack Compose Canvas Bar Chart
 * Renders monthly revenue bars, average trendline, and touch-sensitive tooltips
 */
@Composable
private fun NativeMonthlyRevenueCanvas(
    buckets: List<MonthRevenueBucket>,
    isEnglish: Boolean,
    peakRevenue: Double,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (buckets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(30.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isEnglish) "No monthly orders to display in canvas" else "क्यानभासमा देखाउन कुनै मासिक अर्डर छैन",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(buckets) {
                    detectTapGestures { offset ->
                        val n = buckets.size
                        val slotWidth = size.width / n
                        val idx = (offset.x / slotWidth).toInt().coerceIn(0, n - 1)
                        selectedIndex = if (selectedIndex == idx) null else idx
                    }
                }
        ) {
            val w = size.width
            val h = size.height
            val bottomPadding = 36f
            val topPadding = 24f
            val chartH = h - bottomPadding - topPadding
            val n = buckets.size
            val slotW = w / n
            val barW = (slotW * 0.55f).coerceIn(12f, 40f)

            val maxRev = (peakRevenue * 1.15).coerceAtLeast(1000.0).toFloat()
            val avgRev = buckets.map { it.totalRevenue }.average().toFloat()

            // Horizontal average dashed line
            if (avgRev > 0) {
                val avgY = topPadding + chartH * (1f - (avgRev / maxRev))
                drawLine(
                    color = Color(0xFFFFB300),
                    start = Offset(0f, avgY),
                    end = Offset(w, avgY),
                    strokeWidth = 2f
                )
            }

            // Draw bars
            buckets.forEachIndexed { i, b ->
                val cx = i * slotW + slotW / 2f
                val barH = ((b.totalRevenue.toFloat() / maxRev) * chartH).coerceAtLeast(4f)
                val barTop = topPadding + chartH - barH
                val isSelected = selectedIndex == i

                val barBrush = if (isSelected) {
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFB300), Color(0xFFF57F17)),
                        startY = barTop,
                        endY = topPadding + chartH
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF43A047), Color(0xFF1B5E20)),
                        startY = barTop,
                        endY = topPadding + chartH
                    )
                }

                drawRoundRect(
                    brush = barBrush,
                    topLeft = Offset(cx - barW / 2f, barTop),
                    size = Size(barW, barH),
                    cornerRadius = CornerRadius(6f, 6f)
                )

                // Bottom baseline
                drawLine(
                    color = Color(0xFFCFD8DC),
                    start = Offset(0f, topPadding + chartH),
                    end = Offset(w, topPadding + chartH),
                    strokeWidth = 1.5f
                )
            }
        }

        // Month labels row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            buckets.forEachIndexed { i, b ->
                val label = if (isEnglish) b.monthLabelEn else b.monthLabelNp
                val isSelected = selectedIndex == i
                Text(
                    text = label.split(" ").firstOrNull() ?: label,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                    color = if (isSelected) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Tap inspection box
        selectedIndex?.let { idx ->
            val b = buckets.getOrNull(idx)
            if (b != null) {
                val label = if (isEnglish) b.monthLabelEn else b.monthLabelNp
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = AgroGreenPrimary.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, AgroGreenPrimary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$label: ${b.orderCount} orders",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "रू ${NumberFormat.getNumberInstance().format(b.totalRevenue.toLong())}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            color = AgroGreenPrimary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual Order Row inside expanded month card
 */
@Composable
private fun MonthOrderDetailItem(
    order: Order,
    isEnglish: Boolean,
    formatNpr: (Double) -> String
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = order.customerName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = order.orderDate.ifBlank { "Date not recorded" },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 9.sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatNpr(order.totalAmount),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = AgroGreenPrimary
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = when (order.status) {
                        "VERIFIED" -> Color(0xFF2E7D32).copy(alpha = 0.15f)
                        "COMPLETED" -> Color(0xFF00796B).copy(alpha = 0.15f)
                        else -> Color(0xFFF57F17).copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = order.status,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (order.status) {
                            "VERIFIED" -> Color(0xFF1B5E20)
                            "COMPLETED" -> Color(0xFF004D40)
                            else -> Color(0xFFE65100)
                        },
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
