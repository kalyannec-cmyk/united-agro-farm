package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.BankTransferDetails
import com.example.model.CartItem
import com.example.model.FarmOfficialInfo
import com.example.model.Product
import com.example.model.ProductCategory
import com.example.ui.theme.AgroGoldTertiary
import com.example.ui.theme.AgroGreenPrimary

enum class OrderPaymentMethod(val labelNp: String, val labelEn: String) {
    CASH_ON_DELIVERY("नगद भुक्तानी (Cash)", "Cash on Delivery"),
    MUKTINATH_BANK("मुक्तिनाथ विकास बैंक (Bank Transfer)", "Muktinath Bikas Bank"),
    ESEWA_WALLET("ईसेवा वालेट (eSewa)", "eSewa Digital Wallet")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderProcessingDialog(
    products: List<Product>,
    isEnglish: Boolean = false,
    initialSelectedProduct: Product? = null,
    onDismiss: () -> Unit,
    onFinalizeOrder: (
        customerName: String,
        customerPhone: String,
        deliveryAddress: String,
        items: List<CartItem>,
        bankTransfer: BankTransferDetails?,
        notes: String
    ) -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }

    // Selected Items Map: productId -> quantity
    val selectedQuantities = remember {
        mutableStateMapOf<String, Double>().apply {
            if (initialSelectedProduct != null) {
                put(initialSelectedProduct.id, 1.0)
            }
        }
    }

    // Customer details
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var deliveryAddress by remember { mutableStateOf("पाटन, ललितपुर") }
    var deliveryNotes by remember { mutableStateOf("") }

    // Payment details
    var selectedPaymentMethod by remember { mutableStateOf(OrderPaymentMethod.MUKTINATH_BANK) }
    var senderBankName by remember { mutableStateOf("") }
    var senderAccountName by remember { mutableStateOf("") }
    var transactionRefNumber by remember { mutableStateOf("") }

    // Inventory search and filter
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf(ProductCategory.ALL) }
    var isSubmitting by remember { mutableStateOf(false) }

    // Build cart items from selected quantities
    val selectedCartItems = products.mapNotNull { prod ->
        val qty = selectedQuantities[prod.id] ?: 0.0
        if (qty > 0.0) CartItem(prod, qty) else null
    }
    val grandTotal = selectedCartItems.sumOf { it.totalAmount }
    val totalUnits = selectedCartItems.sumOf { it.quantity }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .testTag("order_processing_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Header Bar
                Surface(
                    color = AgroGreenPrimary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isEnglish) "Order Processing Interface" else "नयाँ अर्डर प्रशोधन तथा दर्ता",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = FarmOfficialInfo.FARM_NAME_NP,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFC8E6C9)
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("btn_close_order_processing")
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                // Step Progress Tabs
                SecondaryTabRow(
                    selectedTabIndex = currentStep,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    contentColor = AgroGreenPrimary,
                    modifier = Modifier.testTag("order_step_tabs")
                ) {
                    Tab(
                        selected = currentStep == 0,
                        onClick = { currentStep = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isEnglish) "1. Select Items" else "१. उत्पादन छनौट",
                                    fontWeight = if (currentStep == 0) FontWeight.Bold else FontWeight.Normal
                                )
                                if (selectedCartItems.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Badge(containerColor = AgroGreenPrimary) {
                                        Text("${selectedCartItems.size}")
                                    }
                                }
                            }
                        },
                        modifier = Modifier.testTag("tab_step_select_items")
                    )

                    Tab(
                        selected = currentStep == 1,
                        onClick = { currentStep = 1 },
                        text = {
                            Text(
                                text = if (isEnglish) "2. Customer Info" else "२. ग्राहक विवरण",
                                fontWeight = if (currentStep == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("tab_step_customer_info")
                    )

                    Tab(
                        selected = currentStep == 2,
                        onClick = { currentStep = 2 },
                        text = {
                            Text(
                                text = if (isEnglish) "3. Finalize & Save" else "३. अर्डर समीक्षा",
                                fontWeight = if (currentStep == 2) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("tab_step_finalize")
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when (currentStep) {
                        0 -> {
                            // ==========================================
                            // STEP 1: INVENTORY ITEM SELECTION & QUANTITIES
                            // ==========================================
                            val filteredProducts = products.filter { prod ->
                                val matchesCategory = selectedCategoryFilter == ProductCategory.ALL || prod.category == selectedCategoryFilter
                                val matchesSearch = if (searchQuery.isBlank()) true else {
                                    val q = searchQuery.trim().lowercase()
                                    prod.nameNp.lowercase().contains(q) || prod.nameEn.lowercase().contains(q) || prod.sku.lowercase().contains(q)
                                }
                                matchesCategory && matchesSearch
                            }

                            Column(modifier = Modifier.fillMaxSize()) {
                                // Search bar & Category filters
                                Surface(
                                    color = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 1.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                                        OutlinedTextField(
                                            value = searchQuery,
                                            onValueChange = { searchQuery = it },
                                            placeholder = { Text(if (isEnglish) "Search farm stock (milk, eggs, meat, veggies)..." else "मौज्दात उत्पादन खोज्नुहोस् (दूध, अण्डा, कुखुरा, तरकारी)...") },
                                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AgroGreenPrimary) },
                                            trailingIcon = {
                                                if (searchQuery.isNotEmpty()) {
                                                    IconButton(onClick = { searchQuery = "" }) {
                                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                                    }
                                                }
                                            },
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth().testTag("order_item_search")
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Category Filter Chips
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            ProductCategory.values().take(4).forEach { cat ->
                                                FilterChip(
                                                    selected = selectedCategoryFilter == cat,
                                                    onClick = { selectedCategoryFilter = cat },
                                                    label = { Text("${cat.iconEmoji} ${if (isEnglish) cat.displayNameEn else cat.displayNameNp}", fontSize = 11.sp) },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = AgroGreenPrimary.copy(alpha = 0.15f),
                                                        selectedLabelColor = AgroGreenPrimary
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }

                                // Product inventory selection list
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(filteredProducts, key = { it.id }) { product ->
                                        val qty = selectedQuantities[product.id] ?: 0.0

                                        InventoryItemSelectionCard(
                                            product = product,
                                            quantity = qty,
                                            isEnglish = isEnglish,
                                            onQuantityChange = { newQty ->
                                                if (newQty <= 0.0) {
                                                    selectedQuantities.remove(product.id)
                                                } else {
                                                    selectedQuantities[product.id] = newQty
                                                }
                                            }
                                        )
                                    }
                                }

                                // Bottom Sticky Summary Bar
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    tonalElevation = 4.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "${selectedCartItems.size} ${if (isEnglish) "items selected" else "प्रकार सामग्री"}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.DarkGray
                                            )
                                            Text(
                                                text = "रु. ${grandTotal.toInt()}",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = AgroGreenPrimary
                                            )
                                        }

                                        Button(
                                            onClick = { currentStep = 1 },
                                            enabled = selectedCartItems.isNotEmpty(),
                                            colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.testTag("btn_proceed_to_customer")
                                        ) {
                                            Text(text = if (isEnglish) "Next: Customer Info" else "ग्राहक विवरण भर्नुहोस्")
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }

                        1 -> {
                            // ==========================================
                            // STEP 2: CUSTOMER DETAILS & PAYMENT METHOD
                            // ==========================================
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                item {
                                    Text(
                                        text = if (isEnglish) "Customer & Delivery Information" else "ग्राहक तथा डेलिभरी विवरण",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1B3B22)
                                    )
                                    Text(
                                        text = if (isEnglish) "Record buyer contact to link with order invoice" else "बिल तथा डेलिभरीका लागि ग्राहकको नाम तथा फोन नम्बर राख्नुहोस्",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }

                                item {
                                    OutlinedTextField(
                                        value = customerName,
                                        onValueChange = { customerName = it },
                                        label = { Text(if (isEnglish) "Customer Full Name *" else "ग्राहकको पूरा नाम *") },
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = AgroGreenPrimary) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("input_order_customer_name")
                                    )
                                }

                                item {
                                    OutlinedTextField(
                                        value = customerPhone,
                                        onValueChange = { customerPhone = it },
                                        label = { Text(if (isEnglish) "Customer Phone Number *" else "सम्पर्क फोन नम्बर (९८...) *") },
                                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = AgroGreenPrimary) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("input_order_customer_phone")
                                    )
                                }

                                item {
                                    OutlinedTextField(
                                        value = deliveryAddress,
                                        onValueChange = { deliveryAddress = it },
                                        label = { Text(if (isEnglish) "Delivery / Pickup Address *" else "डेलिभरी वा संकलन ठेगाना *") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("input_order_address")
                                    )
                                }

                                item {
                                    OutlinedTextField(
                                        value = deliveryNotes,
                                        onValueChange = { deliveryNotes = it },
                                        label = { Text(if (isEnglish) "Special Delivery Instructions / Notes" else "विशेष डेलिभरी निर्देशन वा कैफियत") },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                item {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (isEnglish) "Select Payment Method" else "भुक्तानी माध्यम छनौट गर्नुहोस्",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1B3B22)
                                    )
                                }

                                item {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OrderPaymentMethod.values().forEach { method ->
                                            val isSelected = selectedPaymentMethod == method
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { selectedPaymentMethod = method },
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isSelected) AgroGreenPrimary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                                                ),
                                                border = BorderStroke(
                                                    width = if (isSelected) 2.dp else 1.dp,
                                                    color = if (isSelected) AgroGreenPrimary else Color(0xFFE0E0E0)
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = when (method) {
                                                            OrderPaymentMethod.MUKTINATH_BANK -> Icons.Default.AccountBalance
                                                            OrderPaymentMethod.ESEWA_WALLET -> Icons.Default.Payments
                                                            OrderPaymentMethod.CASH_ON_DELIVERY -> Icons.Default.ReceiptLong
                                                        },
                                                        contentDescription = null,
                                                        tint = if (isSelected) AgroGreenPrimary else Color.Gray,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = if (isEnglish) method.labelEn else method.labelNp,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = if (isSelected) AgroGreenPrimary else Color.Black
                                                        )
                                                        if (method == OrderPaymentMethod.MUKTINATH_BANK) {
                                                            Text(
                                                                text = "A/C: ${FarmOfficialInfo.BANK_ACCOUNT_NO} (${FarmOfficialInfo.BANK_ACCOUNT_NAME_NP})",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = Color.DarkGray
                                                            )
                                                        } else if (method == OrderPaymentMethod.ESEWA_WALLET) {
                                                            Text(
                                                                text = "eSewa ID: ${FarmOfficialInfo.ESEWA_ID} (${FarmOfficialInfo.ESEWA_ACCOUNT_NAME})",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = Color.DarkGray
                                                            )
                                                        }
                                                    }
                                                    if (isSelected) {
                                                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = AgroGreenPrimary)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (selectedPaymentMethod == OrderPaymentMethod.MUKTINATH_BANK) {
                                    item {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Text(text = "बैंक ट्रान्सफर भौचर विवरण (वैकल्पिक)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                                OutlinedTextField(
                                                    value = senderBankName,
                                                    onValueChange = { senderBankName = it },
                                                    label = { Text("पठाउने बैंक (Sender Bank)") },
                                                    singleLine = true,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                OutlinedTextField(
                                                    value = transactionRefNumber,
                                                    onValueChange = { transactionRefNumber = it },
                                                    label = { Text("भौचर वा कारोबार नम्बर (Txn / Voucher Ref #)") },
                                                    singleLine = true,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        }
                                    }
                                }

                                item {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { currentStep = 0 },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(text = if (isEnglish) "Back to Items" else "पछाडि")
                                        }

                                        Button(
                                            onClick = { currentStep = 2 },
                                            enabled = customerName.isNotBlank() && customerPhone.isNotBlank(),
                                            colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1.3f).testTag("btn_proceed_to_review")
                                        ) {
                                            Text(text = if (isEnglish) "Review & Finalize" else "अर्डर समीक्षा गर्नुहोस्")
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }

                        2 -> {
                            // ==========================================
                            // STEP 3: FINALIZE ORDER & SAVE TO FIRESTORE
                            // ==========================================
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                item {
                                    Surface(
                                        color = AgroGreenPrimary.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text(
                                                text = if (isEnglish) "Order Invoice Preview" else "अर्डर तथा रसिद पूर्वावलोकन",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = AgroGreenPrimary
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(text = "ग्राहक: $customerName (${customerPhone})", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                            Text(text = "ठेगाना: $deliveryAddress", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                            Text(text = "भुक्तानी माध्यम: ${selectedPaymentMethod.labelNp}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF2E7D32))
                                            if (deliveryNotes.isNotBlank()) {
                                                Text(text = "कैफियत: $deliveryNotes", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                            }
                                        }
                                    }
                                }

                                item {
                                    Text(
                                        text = if (isEnglish) "Selected Items Breakdown" else "छनौट गरिएका सामग्रीहरू",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                items(selectedCartItems) { item ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "${item.product.category.iconEmoji} ${item.product.nameNp}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "${item.quantity} ${item.product.unit.labelNp} × रु. ${item.product.retailPrice.toInt()}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.Gray
                                                )
                                            }

                                            Text(
                                                text = "रु. ${item.totalAmount.toInt()}",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = AgroGreenPrimary
                                            )
                                        }
                                    }
                                }

                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (isEnglish) "Grand Total (कुल रकम):" else "कुल भुक्तानी रकम:",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "रु. ${grandTotal.toInt()}",
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF1B5E20)
                                            )
                                        }
                                    }
                                }

                                item {
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = {
                                            if (selectedCartItems.isNotEmpty() && customerName.isNotBlank() && !isSubmitting) {
                                                isSubmitting = true
                                                val bankTransfer = if (selectedPaymentMethod == OrderPaymentMethod.MUKTINATH_BANK) {
                                                    BankTransferDetails(
                                                        senderBankName = senderBankName.ifBlank { "मुक्तिनाथ विकास बैंक" },
                                                        senderAccountName = senderAccountName.ifBlank { customerName },
                                                        transactionRefNumber = transactionRefNumber.ifBlank { "DIRECT-TX-${System.currentTimeMillis().toString().takeLast(6)}" },
                                                        transferredAmount = grandTotal,
                                                        transferDate = "",
                                                        remarks = deliveryNotes
                                                    )
                                                } else null

                                                onFinalizeOrder(
                                                    customerName.trim(),
                                                    customerPhone.trim(),
                                                    deliveryAddress.trim(),
                                                    selectedCartItems,
                                                    bankTransfer,
                                                    deliveryNotes.trim()
                                                )
                                                onDismiss()
                                            }
                                        },
                                        enabled = selectedCartItems.isNotEmpty() && customerName.isNotBlank() && !isSubmitting,
                                        colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp)
                                            .testTag("btn_finalize_save_order")
                                    ) {
                                        if (isSubmitting) {
                                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = if (isEnglish) "Saving to Firestore..." else "Firestore मा सेभ हुँदैछ...")
                                        } else {
                                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (isEnglish) "Finalize & Save Order to Firestore" else "अर्डर पक्का गर्नुहोस् र Firestore मा सेभ गर्नुहोस्",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedButton(
                                        onClick = { currentStep = 1 },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(text = if (isEnglish) "Edit Customer Details" else "ग्राहक विवरण सच्याउनुहोस्")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryItemSelectionCard(
    product: Product,
    quantity: Double,
    isEnglish: Boolean,
    onQuantityChange: (Double) -> Unit
) {
    val isSelected = quantity > 0.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("order_item_card_${product.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AgroGreenPrimary.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) AgroGreenPrimary else Color(0xFFE0E0E0)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = product.category.iconEmoji, fontSize = 18.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isEnglish && product.nameEn.isNotBlank()) product.nameEn else product.nameNp,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B3B22)
                        )
                        Text(
                            text = "रु. ${product.retailPrice.toInt()} ${product.unit.labelNp}",
                            style = MaterialTheme.typography.bodySmall,
                            color = AgroGreenPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Available Stock Badge
                Surface(
                    color = when {
                        product.isOutOfStock -> Color(0xFFFFEBEE)
                        product.isLowStock -> Color(0xFFFFF8E1)
                        else -> Color(0xFFE8F5E9)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (product.isOutOfStock) "स्टक सकियो" else "मौज्दात: ${product.stockQuantity.toInt()} ${product.unit.labelNp}",
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            product.isOutOfStock -> Color(0xFFC62828)
                            product.isLowStock -> Color(0xFFF57F17)
                            else -> Color(0xFF2E7D32)
                        },
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quantity Control Stepper
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelected) {
                    Text(
                        text = "उप-रकम: रु. ${(product.retailPrice * quantity).toInt()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = AgroGreenPrimary
                    )
                } else {
                    Text(
                        text = "परिमाण चयन गर्नुहोस्",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onQuantityChange((quantity - 1.0).coerceAtLeast(0.0)) },
                        enabled = quantity > 0.0,
                        modifier = Modifier
                            .size(32.dp)
                            .background(if (quantity > 0.0) Color(0xFFEEEEEE) else Color.Transparent, CircleShape)
                            .testTag("btn_minus_${product.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = if (quantity > 0.0) "${quantity.toInt()} ${product.unit.labelNp}" else "०",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) AgroGreenPrimary else Color.Gray,
                        modifier = Modifier.testTag("qty_label_${product.id}")
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    IconButton(
                        onClick = {
                            val nextQty = if (quantity <= 0.0) 1.0 else quantity + 1.0
                            onQuantityChange(nextQty)
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .background(AgroGreenPrimary, CircleShape)
                            .testTag("btn_plus_${product.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
