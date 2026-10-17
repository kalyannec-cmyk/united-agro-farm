package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import com.example.model.BankTransferDetails
import com.example.model.CartItem
import com.example.model.FarmOrder
import com.example.model.OrderStatus
import com.example.model.Product
import com.example.ui.components.OrderProcessingDialog
import com.example.ui.theme.AgroGoldTertiary
import com.example.ui.theme.AgroGreenPrimary

@Composable
fun OrdersScreen(
    orders: List<FarmOrder>,
    products: List<Product> = emptyList(),
    isEnglish: Boolean = false,
    onSelectOrder: (FarmOrder) -> Unit,
    onVerifyOrder: (String) -> Unit,
    onNavigateToCreateOrder: (() -> Unit)? = null,
    onNavigateToPastOrders: (() -> Unit)? = null,
    onCreateOrder: ((
        customerName: String,
        customerPhone: String,
        deliveryAddress: String,
        items: List<CartItem>,
        bankTransfer: BankTransferDetails?,
        notes: String
    ) -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }
    var showOrderProcessingDialog by remember { mutableStateOf(false) }

    val filteredOrders = orders.filter { order ->
        val matchesQuery = if (searchQuery.isBlank()) true else {
            val q = searchQuery.trim().lowercase()
            order.customerName.lowercase().contains(q) ||
            order.orderId.lowercase().contains(q) ||
            order.customerPhone.contains(q) ||
            (order.bankTransfer?.transactionRefNumber?.lowercase()?.contains(q) == true)
        }

        val matchesFilter = when (selectedFilter) {
            "PENDING" -> order.status == OrderStatus.PENDING_VERIFICATION
            "VERIFIED" -> order.status == OrderStatus.VERIFIED || order.status == OrderStatus.COMPLETED
            else -> true
        }

        matchesQuery && matchesFilter
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("orders_screen")
    ) {
        // Top Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isEnglish) "Orders & Invoices" else "अर्डर तथा भुक्तानी रसिद",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = AgroGreenPrimary
                        )
                        Text(
                            text = if (isEnglish) "Order history & bank verification" else "ललितपुर युनाइटेड एग्रो फार्मका सम्पूर्ण अर्डरहरू",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (onNavigateToPastOrders != null) {
                            OutlinedButton(
                                onClick = onNavigateToPastOrders,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, AgroGreenPrimary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("orders_screen_past_orders_button")
                            ) {
                                Icon(imageVector = Icons.Default.Receipt, contentDescription = null, tint = AgroGreenPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isEnglish) "Past Orders" else "विगतका अर्डरहरू", fontSize = 12.sp, color = AgroGreenPrimary)
                            }
                        }

                        if (onNavigateToCreateOrder != null) {
                            Button(
                                onClick = onNavigateToCreateOrder,
                                colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("orders_screen_create_order_button")
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isEnglish) "New Order" else "नयाँ अर्डर", fontSize = 12.sp)
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = AgroGreenPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, tint = AgroGreenPrimary, modifier = Modifier.size(22.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(if (isEnglish) "Search by Customer, Order ID, Phone..." else "ग्राहक, अर्डर नम्बर वा फोनबाट खोज्नुहोस्...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("orders_search_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text(if (isEnglish) "All (${orders.size})" else "सबै (${orders.size})") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AgroGreenPrimary, selectedLabelColor = Color.White)
                    )

                    val pendingCount = orders.count { it.status == OrderStatus.PENDING_VERIFICATION }
                    FilterChip(
                        selected = selectedFilter == "PENDING",
                        onClick = { selectedFilter = "PENDING" },
                        label = { Text(if (isEnglish) "Pending ($pendingCount)" else "प्रमाणीकरण बाँकी ($pendingCount)") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AgroGoldTertiary, selectedLabelColor = Color.White)
                    )

                    val verifiedCount = orders.count { it.status == OrderStatus.VERIFIED || it.status == OrderStatus.COMPLETED }
                    FilterChip(
                        selected = selectedFilter == "VERIFIED",
                        onClick = { selectedFilter = "VERIFIED" },
                        label = { Text(if (isEnglish) "Verified ($verifiedCount)" else "प्रमाणीकृत ($verifiedCount)") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF2E7D32), selectedLabelColor = Color.White)
                    )
                }
            }
        }

        // Orders List
        if (filteredOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isEnglish) "No orders found." else "कुनै पनि अर्डर भेटिएन।",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredOrders, key = { it.orderId }) { order ->
                    OrderCard(
                        order = order,
                        isEnglish = isEnglish,
                        onViewReceipt = { onSelectOrder(order) },
                        onVerify = { onVerifyOrder(order.orderId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderCard(
    order: FarmOrder,
    isEnglish: Boolean = false,
    onViewReceipt: () -> Unit,
    onVerify: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("order_card_${order.orderId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Order ID & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.orderId,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = AgroGreenPrimary
                )

                val isVerified = order.status == OrderStatus.VERIFIED || order.status == OrderStatus.COMPLETED
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isVerified) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                ) {
                    Text(
                        text = if (isVerified) (if (isEnglish) "Payment Verified" else "भुक्तानी प्रमाणित") else (if (isEnglish) "Pending Verification" else "प्रमाणीकरण बाँकी"),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isVerified) Color(0xFF2E7D32) else Color(0xFFEF6C00),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Customer Name & Date
            Text(
                text = order.customerName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${order.orderDate} | ${order.customerPhone}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )

            // Items count or summary
            if (order.items.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                val itemsSummary = order.items.joinToString(", ") {
                    "${it.product.nameNp} (${it.quantity.toInt()} ${it.product.unit.labelNp.split(" ").lastOrNull() ?: ""})"
                }
                Text(
                    text = itemsSummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            // Bank Transfer Ref Box if available
            order.bankTransfer?.let { transfer ->
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color(0xFFF1F8E9),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null, tint = AgroGreenPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "मुक्तिनाथ बैंक Ref: ${transfer.transactionRefNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            color = AgroGreenPrimary
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            // Total & Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = if (isEnglish) "Total Amount" else "कुल रकम", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = "रू. ${"%.2f".format(order.totalAmount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = AgroGreenPrimary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (order.status == OrderStatus.PENDING_VERIFICATION) {
                        OutlinedButton(
                            onClick = onVerify,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (isEnglish) "Verify" else "रुजु")
                        }
                    }

                    Button(
                        onClick = onViewReceipt,
                        colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = if (isEnglish) "Receipt" else "रसिद")
                    }
                }
            }
        }
    }
}
