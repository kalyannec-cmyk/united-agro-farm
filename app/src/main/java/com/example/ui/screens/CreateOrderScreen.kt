package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.InventoryItem
import com.example.data.local.Order
import com.example.model.FarmOfficialInfo
import com.example.ui.theme.AgroGreenPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class OrderSelectedItem(
    val item: InventoryItem,
    var quantity: Double
) {
    val subtotal: Double
        get() = quantity * item.unitPrice
}

/**
 * Composable screen that displays a form for creating new orders,
 * including fields for customer name, items selected from inventory,
 * and total calculated price.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrderScreen(
    inventoryItems: List<InventoryItem>,
    isEnglish: Boolean = false,
    onOrderCreated: (Order, paymentMode: String) -> Unit,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Customer Form State
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var deliveryAddress by remember { mutableStateOf("") }
    var orderNotes by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("MUKTINATH_BANK") } // or CASH_ON_DELIVERY

    // Inventory selection state (itemId -> quantity)
    val selectedItemQuantities = remember { mutableStateMapOf<String, Double>() }
    var inventorySearchQuery by remember { mutableStateOf("") }

    // Derived total calculated price
    val totalCalculatedPrice by remember {
        derivedStateOf {
            selectedItemQuantities.entries.sumOf { (itemId, qty) ->
                val item = inventoryItems.firstOrNull { it.id == itemId }
                (item?.unitPrice ?: 0.0) * qty
            }
        }
    }

    val totalSelectedItemsCount by remember {
        derivedStateOf { selectedItemQuantities.count { it.value > 0.0 } }
    }

    val availableInventoryItems = remember(inventoryItems, inventorySearchQuery) {
        if (inventorySearchQuery.isBlank()) inventoryItems
        else inventoryItems.filter {
            it.productName.contains(inventorySearchQuery, ignoreCase = true) ||
                    it.productNameNp.contains(inventorySearchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("create_order_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEnglish) "Create New Farm Order" else "नयाँ कृषि अर्डर फारम",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("create_order_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AgroGreenPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            // Bottom Sticky Checkout Bar showing Total Calculated Price and Submit button
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isEnglish) "Total Calculated Price:" else "कुल हिसाब मूल्य:",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "रु ${String.format(Locale.getDefault(), "%,.2f", totalCalculatedPrice)}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AgroGreenPrimary,
                            modifier = Modifier.testTag("total_calculated_price")
                        )
                        Text(
                            text = if (isEnglish) "$totalSelectedItemsCount item(s) selected" else "$totalSelectedItemsCount प्रकारका वस्तु छानियो",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }

                    Button(
                        onClick = {
                            if (customerName.isBlank()) {
                                Toast.makeText(
                                    context,
                                    if (isEnglish) "Please enter customer name" else "कृपया ग्राहकको नाम लेख्नुहोस्",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }
                            if (customerPhone.length < 7) {
                                Toast.makeText(
                                    context,
                                    if (isEnglish) "Please enter valid phone number" else "कृपया मान्य फोन नम्बर राख्नुहोस्",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }
                            if (totalCalculatedPrice <= 0.0 || totalSelectedItemsCount == 0) {
                                Toast.makeText(
                                    context,
                                    if (isEnglish) "Please select at least one item from inventory" else "कृपया इन्भेन्ट्रिबाट कम्तीमा एउटा वस्तु छान्नुहोस्",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }

                            val newOrder = Order(
                                orderId = "ORD-${SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date())}",
                                customerName = customerName.trim(),
                                customerPhone = customerPhone.trim(),
                                deliveryAddress = deliveryAddress.trim().ifBlank { "Lalitpur" },
                                totalAmount = totalCalculatedPrice,
                                status = "PENDING_VERIFICATION",
                                paymentMode = paymentMethod,
                                orderDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
                                notes = orderNotes.trim(),
                                senderBankName = if (paymentMethod == "MUKTINATH_BANK") FarmOfficialInfo.BANK_NAME_EN else "CASH",
                                transferredAmount = if (paymentMethod == "MUKTINATH_BANK") totalCalculatedPrice else 0.0
                            )

                            onOrderCreated(newOrder, paymentMethod)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("submit_create_order_button")
                    ) {
                        Icon(Icons.Default.ShoppingCartCheckout, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (paymentMethod == "MUKTINATH_BANK") {
                                if (isEnglish) "Proceed to Muktinath Pay" else "मुक्तिनाथ भुक्तानी गर्नुहोस्"
                            } else {
                                if (isEnglish) "Confirm Order" else "अर्डर निश्चित गर्नुहोस्"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("create_order_form_column"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Customer Details Form
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(AgroGreenPrimary.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = AgroGreenPrimary, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isEnglish) "Customer Information" else "ग्राहक विवरण",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Customer Name Field (Mandatory per requirement)
                        OutlinedTextField(
                            value = customerName,
                            onValueChange = { customerName = it },
                            label = { Text(if (isEnglish) "Customer Name *" else "ग्राहकको नाम *") },
                            placeholder = { Text("e.g. सरोज पौडेल (Saroj Poudel)") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("order_customer_name_input")
                        )

                        // Customer Phone
                        OutlinedTextField(
                            value = customerPhone,
                            onValueChange = { customerPhone = it },
                            label = { Text(if (isEnglish) "Phone Number *" else "सम्पर्क फोन नम्बर *") },
                            placeholder = { Text("98XXXXXXXX") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Gray) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("order_customer_phone_input")
                        )

                        // Delivery Address
                        OutlinedTextField(
                            value = deliveryAddress,
                            onValueChange = { deliveryAddress = it },
                            label = { Text(if (isEnglish) "Delivery Address" else "डेलिभरी ठेगाना") },
                            placeholder = { Text("e.g. ललितपुर, सानेपा / पाटन") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("order_delivery_address_input")
                        )
                    }
                }
            }

            // Section 2: Items Selected from Inventory
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(AgroGreenPrimary.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Inventory2, contentDescription = null, tint = AgroGreenPrimary, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isEnglish) "Select Items from Inventory" else "इन्भेन्ट्रिबाट वस्तुहरू छान्नुहोस्",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (totalSelectedItemsCount > 0) {
                                Surface(
                                    color = AgroGreenPrimary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "$totalSelectedItemsCount ${if (isEnglish) "Selected" else "छानिएको"}",
                                        color = AgroGreenPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Inventory Search Filter
                        OutlinedTextField(
                            value = inventorySearchQuery,
                            onValueChange = { inventorySearchQuery = it },
                            placeholder = { Text(if (isEnglish) "Search inventory products..." else "उत्पादन खोज्नुहोस्...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Items list from inventory with quantity controls
            items(availableInventoryItems, key = { it.id }) { item ->
                val selectedQty = selectedItemQuantities[item.id] ?: 0.0
                val isSelected = selectedQty > 0.0

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) AgroGreenPrimary.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) AgroGreenPrimary else Color(0xFFE0E0E0)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("order_item_card_${item.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isEnglish) item.productName else item.productNameNp.ifBlank { item.productName },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B3B22)
                            )
                            Text(
                                text = "रु ${String.format(Locale.getDefault(), "%,.2f", item.unitPrice)} / ${item.unit}",
                                fontSize = 12.sp,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${if (isEnglish) "Stock Available: " else "मौज्दात उपलब्ध: "}${String.format(Locale.getDefault(), "%,.1f", item.currentQuantity)} ${item.unit}",
                                fontSize = 11.sp,
                                color = if (item.isLowStock || item.isOutOfStock) Color(0xFFD32F2F) else Color.Gray
                            )

                            if (isSelected) {
                                val itemSubtotal = selectedQty * item.unitPrice
                                Text(
                                    text = "${if (isEnglish) "Subtotal: NPR " else "जम्मा: रु "}${String.format(Locale.getDefault(), "%,.2f", itemSubtotal)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AgroGreenPrimary,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        // Quantity adjustment controls (- / +)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (selectedQty > 0.0) {
                                IconButton(
                                    onClick = {
                                        val newQty = (selectedQty - 1.0).coerceAtLeast(0.0)
                                        if (newQty <= 0.0) {
                                            selectedItemQuantities.remove(item.id)
                                        } else {
                                            selectedItemQuantities[item.id] = newQty
                                        }
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFFEEEEEE), CircleShape)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                                }

                                Text(
                                    text = String.format(Locale.getDefault(), "%.0f", selectedQty),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    val newQty = selectedQty + 1.0
                                    selectedItemQuantities[item.id] = newQty
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(AgroGreenPrimary, CircleShape)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Section 3: Payment Method (Muktinath Bikas Bank or Cash)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0xFFE65100).copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isEnglish) "Payment Method" else "भुक्तानी विधि",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Muktinath Bikas Bank option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { paymentMethod = "MUKTINATH_BANK" }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = paymentMethod == "MUKTINATH_BANK",
                                onClick = { paymentMethod = "MUKTINATH_BANK" },
                                colors = RadioButtonDefaults.colors(selectedColor = AgroGreenPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isEnglish) "Muktinath Bikas Bank Direct Transfer / QR" else "मुक्तिनाथ विकास बैंक सिधा खाता ट्रान्सफर / QR",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "A/C: ${FarmOfficialInfo.BANK_ACCOUNT_NO} (${FarmOfficialInfo.BANK_ACCOUNT_NAME_EN})",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        // Cash on delivery option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { paymentMethod = "CASH_ON_DELIVERY" }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = paymentMethod == "CASH_ON_DELIVERY",
                                onClick = { paymentMethod = "CASH_ON_DELIVERY" },
                                colors = RadioButtonDefaults.colors(selectedColor = AgroGreenPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isEnglish) "Cash on Delivery" else "डेलिभरीमा नगद भुक्तानी (Cash)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (isEnglish) "Pay upon delivery of fresh farm goods" else "ताजा उत्पादन घरमा प्राप्त भएपछि भुक्तानी",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Optional Order Notes
                        OutlinedTextField(
                            value = orderNotes,
                            onValueChange = { orderNotes = it },
                            label = { Text(if (isEnglish) "Order Notes / Instructions" else "अर्डर सम्बन्धी विशेष निर्देशन") },
                            placeholder = { Text(if (isEnglish) "e.g. Deliver before 8 AM" else "जस्तै: बिहान ८ बजे अगाडि डेलिभरी गरिदिनुहोला") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("order_notes_input")
                        )
                    }
                }
            }

            // Bottom padding for sticky checkout bar
            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}
