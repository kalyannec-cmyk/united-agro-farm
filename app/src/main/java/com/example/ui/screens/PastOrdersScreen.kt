package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.Order
import com.example.model.FarmOfficialInfo
import com.example.ui.theme.AgroGoldTertiary
import com.example.ui.theme.AgroGreenPrimary
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp

/**
 * PastOrdersScreen displays a list of past orders from the Room database,
 * explicitly showing customer name, date, and order total, along with
 * verification statuses, Muktinath Bikas Bank payment details, and search/filtering.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastOrdersScreen(
    orders: List<Order>,
    isEnglish: Boolean = false,
    onSelectOrder: ((Order) -> Unit)? = null,
    onUpdateStatus: ((orderId: String, status: String) -> Unit)? = null,
    onDeleteOrder: ((orderId: String) -> Unit)? = null,
    onNavigateToCreateOrder: (() -> Unit)? = null,
    onNavigateToMuktinathPayment: ((Order) -> Unit)? = null,
    onNavigateToRevenueChart: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = onBack != null) {
        onBack?.invoke()
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("ALL") }
    var selectedOrderForDetail by remember { mutableStateOf<Order?>(null) }
    var orderToDelete by remember { mutableStateOf<Order?>(null) }

    // Filter orders by search query and status filter
    val filteredOrders = orders.filter { order ->
        val matchesQuery = if (searchQuery.isBlank()) true else {
            val q = searchQuery.trim().lowercase()
            order.customerName.lowercase().contains(q) ||
            order.orderId.lowercase().contains(q) ||
            order.customerPhone.contains(q) ||
            order.orderDate.lowercase().contains(q) ||
            order.senderBankName.lowercase().contains(q) ||
            order.transactionRefNumber.lowercase().contains(q)
        }

        val matchesFilter = when (selectedStatusFilter) {
            "PENDING" -> order.status == "PENDING_VERIFICATION"
            "VERIFIED" -> order.status == "VERIFIED"
            "COMPLETED" -> order.status == "COMPLETED"
            else -> true
        }

        matchesQuery && matchesFilter
    }

    // Key statistics from database orders
    val totalRevenue = orders.filter { it.status != "CANCELLED" }.sumOf { it.totalAmount }
    val pendingCount = orders.count { it.status == "PENDING_VERIFICATION" }
    val verifiedCount = orders.count { it.status == "VERIFIED" || it.status == "COMPLETED" }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("past_orders_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isEnglish) "Past Orders (Room Database)" else "विगतका अर्डरहरू (Room DB)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (isEnglish) "${orders.size} Total Orders in Database" else "कुल ${orders.size} वटा अर्डरहरू",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("past_orders_back_button")
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
                    if (onNavigateToRevenueChart != null) {
                        IconButton(
                            onClick = onNavigateToRevenueChart,
                            modifier = Modifier.testTag("past_orders_goto_revenue_chart_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = if (isEnglish) "Monthly Revenue Chart (D3 & Recharts)" else "मासिक आम्दानी चार्ट",
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
        },
        floatingActionButton = {
            if (onNavigateToCreateOrder != null) {
                FloatingActionButton(
                    onClick = onNavigateToCreateOrder,
                    containerColor = AgroGreenPrimary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("create_order_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Create New Order")
                        Text(
                            text = if (isEnglish) "New Order" else "नयाँ अर्डर",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Summary Header Card
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Summary stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Total Orders
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isEnglish) "Total Orders" else "कुल अर्डर",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "${orders.size}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Total Revenue
                        Card(
                            modifier = Modifier.weight(1.3f),
                            colors = CardDefaults.cardColors(containerColor = AgroGreenPrimary.copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isEnglish) "Total Amount" else "कुल रकम",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AgroGreenPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = formatNepaliCurrency(totalRevenue),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AgroGreenPrimary
                                )
                            }
                        }

                        // Pending Status
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = if (pendingCount > 0) Color(0xFFFFF3E0) else Color(0xFFE8F5E9)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isEnglish) "Pending" else "प्रमाणीकरण बाँकी",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (pendingCount > 0) Color(0xFFE65100) else Color(0xFF2E7D32)
                                )
                                Text(
                                    text = "$pendingCount",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (pendingCount > 0) Color(0xFFE65100) else Color(0xFF2E7D32)
                                )
                            }
                        }
                    }

                    if (onNavigateToRevenueChart != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onNavigateToRevenueChart,
                            colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("past_orders_banner_goto_revenue_chart")
                        ) {
                            Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isEnglish) "View Monthly Revenue (D3 & Recharts)" else "मासिक आम्दानी चार्ट हेर्नुहोस् (D3/Recharts)",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Search input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("past_orders_search_input"),
                        placeholder = {
                            Text(if (isEnglish) "Search customer name, date, total, phone..." else "ग्राहकको नाम, मिति, रकम खोज्नुहोस्...")
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = AgroGreenPrimary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Filter Chips: ALL, PENDING, VERIFIED, COMPLETED
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedStatusFilter == "ALL",
                                onClick = { selectedStatusFilter = "ALL" },
                                label = { Text(if (isEnglish) "All (${orders.size})" else "सबै (${orders.size})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AgroGreenPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("filter_chip_all")
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedStatusFilter == "PENDING",
                                onClick = { selectedStatusFilter = "PENDING" },
                                label = { Text(if (isEnglish) "Pending ($pendingCount)" else "प्रमाणीकरण बाँकी ($pendingCount)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFEF6C00),
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("filter_chip_pending")
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedStatusFilter == "VERIFIED",
                                onClick = { selectedStatusFilter = "VERIFIED" },
                                label = { Text(if (isEnglish) "Verified ($verifiedCount)" else "प्रमाणित ($verifiedCount)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF2E7D32),
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("filter_chip_verified")
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedStatusFilter == "COMPLETED",
                                onClick = { selectedStatusFilter = "COMPLETED" },
                                label = { Text(if (isEnglish) "Completed" else "सम्पन्न") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AgroGoldTertiary,
                                    selectedLabelColor = Color.Black
                                ),
                                modifier = Modifier.testTag("filter_chip_completed")
                            )
                        }
                    }
                }
            }

            // Orders List
            if (filteredOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isEnglish) "No Past Orders Found" else "कुनै विगतका अर्डरहरू भेटिएनन्",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isEnglish) {
                                if (searchQuery.isNotBlank()) "No orders match your search criteria" else "Create a new order from the inventory table"
                            } else {
                                if (searchQuery.isNotBlank()) "तपाईंको खोजी अनुसार कुनै अर्डर भेटिएन" else "मौज्दात सूचीबाट नयाँ अर्डर दर्ता गर्नुहोस्"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        if (onNavigateToCreateOrder != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onNavigateToCreateOrder,
                                colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isEnglish) "Create First Order" else "पहिलो अर्डर सिर्जना गर्नुहोस्")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("past_orders_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredOrders, key = { it.orderId }) { order ->
                        PastOrderItemCard(
                            order = order,
                            isEnglish = isEnglish,
                            onCardClick = {
                                selectedOrderForDetail = order
                                onSelectOrder?.invoke(order)
                            },
                            onVerifyClick = {
                                onUpdateStatus?.invoke(order.orderId, "VERIFIED")
                            },
                            onPaymentClick = {
                                onNavigateToMuktinathPayment?.invoke(order)
                            },
                            onDeleteClick = {
                                orderToDelete = order
                            }
                        )
                    }
                    // Bottom spacing for FAB
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }

    // Order Detail & Receipt Dialog
    selectedOrderForDetail?.let { order ->
        PastOrderDetailDialog(
            order = order,
            isEnglish = isEnglish,
            onDismiss = { selectedOrderForDetail = null },
            onUpdateStatus = { status ->
                onUpdateStatus?.invoke(order.orderId, status)
                selectedOrderForDetail = order.copy(status = status)
            },
            onNavigateToMuktinathPayment = {
                selectedOrderForDetail = null
                onNavigateToMuktinathPayment?.invoke(order)
            }
        )
    }

    // Delete Confirmation Dialog
    orderToDelete?.let { order ->
        AlertDialog(
            onDismissRequest = { orderToDelete = null },
            title = {
                Text(if (isEnglish) "Delete Order?" else "अर्डर मेटाउने?")
            },
            text = {
                Text(
                    if (isEnglish)
                        "Are you sure you want to delete order #${order.orderId.takeLast(8)} for customer ${order.customerName}?"
                    else
                        "के तपाईं ग्राहक ${order.customerName} को अर्डर #${order.orderId.takeLast(8)} मेटाउन निश्चित हुनुहुन्छ?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteOrder?.invoke(order.orderId)
                        orderToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isEnglish) "Delete" else "मेटाउनुहोस्")
                }
            },
            dismissButton = {
                TextButton(onClick = { orderToDelete = null }) {
                    Text(if (isEnglish) "Cancel" else "रद्द गर्नुहोस्")
                }
            }
        )
    }
}

/**
 * Individual Card displaying the past order from the Room database,
 * explicitly highlighting Customer Name, Date, and Order Total.
 */
@Composable
private fun PastOrderItemCard(
    order: Order,
    isEnglish: Boolean,
    onCardClick: () -> Unit,
    onVerifyClick: () -> Unit,
    onPaymentClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("order_item_${order.orderId}")
            .clickable { onCardClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Customer Name and Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = AgroGreenPrimary.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = AgroGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        // 1. CUSTOMER NAME (prominently displayed)
                        Text(
                            text = order.customerName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("order_customer_name_${order.orderId}")
                        )
                        Text(
                            text = "ID: #${order.orderId.takeLast(8).uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Status Badge
                OrderStatusBadge(status = order.status, isEnglish = isEnglish)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))

            // Middle Row: Date & Order Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 2. ORDER DATE
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Column {
                        Text(
                            text = if (isEnglish) "Order Date" else "अर्डर मिति",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        Text(
                            text = order.orderDate.ifBlank { formatTimestamp(order.createdAt) },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("order_date_${order.orderId}")
                        )
                    }
                }

                // 3. ORDER TOTAL (bold, prominent, formatted in Nepali currency)
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isEnglish) "Order Total" else "जम्मा रकम",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = formatNepaliCurrency(order.totalAmount),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = AgroGreenPrimary,
                        modifier = Modifier.testTag("order_total_${order.orderId}")
                    )
                }
            }

            // Customer Phone and Bank / Payment info
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = order.customerPhone,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                if (order.senderBankName.isNotBlank() || order.transactionRefNumber.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = Color(0xFF1E88E5),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (order.transactionRefNumber.isNotBlank()) {
                                "Ref: ${order.transactionRefNumber}"
                            } else {
                                order.senderBankName
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF1E88E5),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Delivery address / notes if present
            if (order.deliveryAddress.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = order.deliveryAddress,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Delete button
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Order",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Muktinath Payment details button
                OutlinedButton(
                    onClick = onPaymentClick,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, AgroGreenPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = AgroGreenPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isEnglish) "Bank Info" else "बैंक विवरण",
                        style = MaterialTheme.typography.labelMedium,
                        color = AgroGreenPrimary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Verify Button (if pending) or Details button
                if (order.status == "PENDING_VERIFICATION") {
                    Button(
                        onClick = onVerifyClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isEnglish) "Verify" else "प्रमाणित गर्नुहोस्",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = onCardClick,
                        colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isEnglish) "View Receipt" else "रसिद हेर्नुहोस्",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

/**
 * Clean status pill badge for Order status.
 */
@Composable
private fun OrderStatusBadge(
    status: String,
    isEnglish: Boolean
) {
    val (label, bgColor, textColor, icon) = when (status) {
        "VERIFIED" -> Quadruple(
            if (isEnglish) "VERIFIED" else "प्रमाणित",
            Color(0xFFE8F5E9),
            Color(0xFF2E7D32),
            Icons.Default.Verified
        )
        "COMPLETED" -> Quadruple(
            if (isEnglish) "COMPLETED" else "सम्पन्न",
            Color(0xFFE0F2F1),
            Color(0xFF00695C),
            Icons.Default.CheckCircle
        )
        "CANCELLED" -> Quadruple(
            if (isEnglish) "CANCELLED" else "रद्द गरिएको",
            Color(0xFFFFEBEE),
            Color(0xFFC62828),
            Icons.Default.Clear
        )
        else -> Quadruple(
            if (isEnglish) "PENDING" else "प्रमाणीकरण बाँकी",
            Color(0xFFFFF3E0),
            Color(0xFFE65100),
            Icons.Default.Pending
        )
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Detailed Order Receipt Modal Dialog.
 */
@Composable
private fun PastOrderDetailDialog(
    order: Order,
    isEnglish: Boolean,
    onDismiss: () -> Unit,
    onUpdateStatus: (String) -> Unit,
    onNavigateToMuktinathPayment: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary)
            ) {
                Text(if (isEnglish) "Close" else "बन्द गर्नुहोस्")
            }
        },
        dismissButton = {
            if (order.status == "PENDING_VERIFICATION") {
                Button(
                    onClick = {
                        onUpdateStatus("VERIFIED")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isEnglish) "Mark Verified" else "प्रमाणित गर्नुहोस्")
                }
            } else {
                OutlinedButton(onClick = onNavigateToMuktinathPayment) {
                    Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isEnglish) "Muktinath Bank Info" else "मुक्तिनाथ बैंक विवरण")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // Farm Header
                Text(
                    text = if (isEnglish) FarmOfficialInfo.FARM_NAME_EN else FarmOfficialInfo.FARM_NAME_NP,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AgroGreenPrimary
                )
                Text(
                    text = if (isEnglish) FarmOfficialInfo.LOCATION_EN else FarmOfficialInfo.LOCATION_NP,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = "PAN: ${FarmOfficialInfo.PAN_VAT_NO} | Reg: ${FarmOfficialInfo.REGISTRATION_NO}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                // Order Meta Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isEnglish) "Order ID:" else "अर्डर नं.:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "#${order.orderId.takeLast(10).uppercase()}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isEnglish) "Customer Name:" else "ग्राहकको नाम:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = order.customerName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isEnglish) "Contact Phone:" else "सम्पर्क फोन:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = order.customerPhone,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isEnglish) "Order Date:" else "अर्डर मिति:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = order.orderDate.ifBlank { formatTimestamp(order.createdAt) },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (order.deliveryAddress.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isEnglish) "Delivery Address:" else "ठेगाना:",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Text(
                            text = order.deliveryAddress,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                // Payment & Bank Details
                Text(
                    text = if (isEnglish) "Payment Information" else "भुक्तानी विवरण",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = AgroGreenPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isEnglish) "Payment Mode:" else "भुक्तानी माध्यम:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = order.paymentMode,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (order.senderBankName.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isEnglish) "Bank Name:" else "बैंक:",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Text(
                            text = order.senderBankName,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                if (order.transactionRefNumber.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isEnglish) "Txn Reference:" else "रसिद / भौचर नं.:",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Text(
                            text = order.transactionRefNumber,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E88E5)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = AgroGreenPrimary.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEnglish) "TOTAL AMOUNT:" else "जम्मा रकम:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AgroGreenPrimary
                        )
                        Text(
                            text = formatNepaliCurrency(order.totalAmount),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = AgroGreenPrimary
                        )
                    }
                }

                if (order.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${if (isEnglish) "Notes: " else "कैफियत: "}${order.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    )
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

private fun formatNepaliCurrency(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("ne", "NP"))
    return "रु ${formatter.format(amount)}"
}

private fun formatTimestamp(timestamp: Long): String {
    if (timestamp <= 0L) return ""
    val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
