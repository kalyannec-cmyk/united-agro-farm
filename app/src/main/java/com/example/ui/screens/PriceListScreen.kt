package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.BankTransferDetails
import com.example.model.FarmOfficialInfo
import com.example.model.Product
import com.example.model.ProductCategory
import com.example.ui.components.EditProductPriceDialog
import com.example.ui.components.ProductCard
import com.example.ui.theme.AgroGoldTertiary
import com.example.ui.theme.AgroGreenPrimary
import com.example.ui.viewmodel.CartUiState
import com.example.ui.viewmodel.PriceSortOrder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceListScreen(
    products: List<Product>,
    cart: CartUiState,
    searchQuery: String,
    selectedCategory: ProductCategory,
    sortOrder: PriceSortOrder,
    isEnglish: Boolean = false,
    onSearchChange: (String) -> Unit,
    onCategoryChange: (ProductCategory) -> Unit,
    onSortChange: (PriceSortOrder) -> Unit,
    onAddToCart: (Product) -> Unit,
    onUpdateCartQty: (String, Double) -> Unit,
    onClearCart: () -> Unit,
    onCheckoutSubmit: (name: String, phone: String, address: String, bankTransfer: BankTransferDetails?, notes: String) -> Unit,
    onUpdateProductPriceAndStock: ((productId: String, newRetailPrice: Double, newWholesalePrice: Double, newStock: Double) -> Unit)? = null
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("price_list_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Search & Sort bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchChange,
                            placeholder = { Text(if (isEnglish) "Search products, milk, eggs..." else "उत्पादन खोज्नुहोस् (दूध, अण्डा, कुखुरा...)") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { onSearchChange("") }) {
                                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("product_search_input")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Sort Button with dropdown
                        Box {
                            OutlinedButton(
                                onClick = { sortMenuExpanded = true },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("sort_order_button")
                            ) {
                                Icon(imageVector = Icons.Default.Sort, contentDescription = "Sort", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isEnglish) "Sort" else "क्रम")
                            }

                            DropdownMenu(
                                expanded = sortMenuExpanded,
                                onDismissRequest = { sortMenuExpanded = false }
                            ) {
                                PriceSortOrder.values().forEach { sort ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (sort == sortOrder) {
                                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = AgroGreenPrimary, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                }
                                                Text(if (isEnglish) sort.labelEn else sort.labelNp)
                                            }
                                        },
                                        onClick = {
                                            onSortChange(sort)
                                            sortMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Horizontal Category Filter Pills
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProductCategory.values().forEach { category ->
                            val isSelected = category == selectedCategory
                            FilterChip(
                                selected = isSelected,
                                onClick = { onCategoryChange(category) },
                                label = {
                                    Text(
                                        text = "${category.iconEmoji} ${if (isEnglish) category.displayNameEn else category.displayNameNp}",
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AgroGreenPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }

            // Product List
            if (products.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isEnglish) "No products found matching criteria." else "खोजिएको उत्पादन फेला परेन।",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = if (cart.items.isNotEmpty()) 120.dp else 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            isEnglish = isEnglish,
                            onAddToCart = onAddToCart,
                            onEditPriceClick = if (onUpdateProductPriceAndStock != null) {
                                { productToEdit = it }
                            } else null
                        )
                    }
                }
            }
        }

        // Sticky Bottom Cart & Bank Checkout Banner
        if (cart.items.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("floating_cart_banner"),
                shape = RoundedCornerShape(16.dp),
                color = AgroGreenPrimary,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${cart.totalItemCount} ${if (isEnglish) "items in order" else "प्रकार सामग्री"}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFC8E6C9)
                            )
                        }
                        Text(
                            text = "कुल: रू. ${"%.0f".format(cart.totalAmount)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = { showCheckoutDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD54F),
                            contentColor = Color(0xFF1B3B22)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("open_checkout_dialog_button")
                    ) {
                        Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEnglish) "Bank Pay & Order" else "बैंक ट्रान्सफर अर्डर",
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }

    // Checkout & Bank Transfer Dialog
    if (showCheckoutDialog) {
        CheckoutAndBankTransferDialog(
            cart = cart,
            isEnglish = isEnglish,
            onDismiss = { showCheckoutDialog = false },
            onUpdateQty = onUpdateCartQty,
            onClearCart = onClearCart,
            onSubmit = { name, phone, address, bankTransfer, notes ->
                onCheckoutSubmit(name, phone, address, bankTransfer, notes)
                showCheckoutDialog = false
            }
        )
    }

    // Admin Edit Price and Stock Dialog
    if (productToEdit != null && onUpdateProductPriceAndStock != null) {
        EditProductPriceDialog(
            product = productToEdit!!,
            isEnglish = isEnglish,
            onDismiss = { productToEdit = null },
            onSavePriceAndStock = { newRetail, newWholesale, newStock ->
                onUpdateProductPriceAndStock(productToEdit!!.id, newRetail, newWholesale, newStock)
                productToEdit = null
            }
        )
    }
}

@Composable
fun CheckoutAndBankTransferDialog(
    cart: CartUiState,
    isEnglish: Boolean = false,
    onDismiss: () -> Unit,
    onUpdateQty: (String, Double) -> Unit,
    onClearCart: () -> Unit,
    onSubmit: (name: String, phone: String, address: String, bankTransfer: BankTransferDetails?, notes: String) -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var deliveryAddress by remember { mutableStateOf("") }
    var orderNotes by remember { mutableStateOf("") }

    // Bank Transfer Details
    var isBankTransferSelected by remember { mutableStateOf(true) }
    var senderBankName by remember { mutableStateOf("मुक्तिनाथ विकास बैंक (Muktinath Bank)") }
    var senderAccountName by remember { mutableStateOf("") }
    var transactionRef by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("checkout_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEnglish) "Order & Bank Transfer" else "अर्डर तथा बैंक ट्रान्सफर भुक्तानी",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AgroGreenPrimary
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Cart Items summary
                Text(
                    text = if (isEnglish) "Order Summary:" else "अर्डर गरिएका वस्तुहरू:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                cart.items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isEnglish) item.product.nameEn else item.product.nameNp,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${item.quantity.toInt()} ${if (isEnglish) item.product.unit.labelEn else item.product.unit.labelNp} x रू. ${item.product.retailPrice}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Text(
                            text = "रू. ${"%.0f".format(item.totalAmount)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Total Payable
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEnglish) "Total Payable:" else "कुल तिर्नुपर्ने रकम:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "रू. ${"%.2f".format(cart.totalAmount)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = AgroGreenPrimary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Official Bank & eSewa Account Details
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F7FB)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFC4D6EB))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null, tint = AgroGreenPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "भुक्तानी खाताहरू (Muktinath Bank & eSewa)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = AgroGreenPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "१. ${FarmOfficialInfo.BANK_NAME_NP}: ${FarmOfficialInfo.BANK_ACCOUNT_NO}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF0D254C)
                        )
                        Text(
                            text = "२. ईसेवा वालेट (eSewa ID): ${FarmOfficialInfo.ESEWA_ID} (${FarmOfficialInfo.ESEWA_ID_NP})",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF459A2C)
                        )
                        Text(
                            text = "खातावाला: ${FarmOfficialInfo.FARM_NAME_EN}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Customer Info Inputs
                Text(
                    text = if (isEnglish) "Customer Information:" else "ग्राहकको विवरण:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text(if (isEnglish) "Full Name *" else "पूरा नाम *") },
                    modifier = Modifier.fillMaxWidth().testTag("checkout_name_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { customerPhone = it },
                    label = { Text(if (isEnglish) "Mobile Number *" else "सम्पर्क नम्बर *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().testTag("checkout_phone_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = deliveryAddress,
                    onValueChange = { deliveryAddress = it },
                    label = { Text(if (isEnglish) "Delivery Location *" else "डेलिभरी ठेगाना *") },
                    placeholder = { Text("उदा. पाटन, लगनखेल, सातदोबाटो, इमाडोल") },
                    modifier = Modifier.fillMaxWidth().testTag("checkout_address_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Bank Transfer Reference Inputs
                Text(
                    text = if (isEnglish) "Bank Transfer Verification:" else "बैंक ट्रान्सफर भुक्तानी विवरण:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isEnglish) "Enter Mobile Banking / ConnectIPS payment reference" else "मुक्तिनाथ वा अन्य मोबाइल बैंकिङबाट रकम पठाई भौचर/रिफरेन्स नम्बर राख्नुहोस्",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = transactionRef,
                    onValueChange = { transactionRef = it },
                    label = { Text(if (isEnglish) "Transaction Ref / Voucher No. *" else "ट्रान्जेक्सन रिफरेन्स / भौचर नम्बर *") },
                    placeholder = { Text("उदा. MNB-9402123 वा UTR Ref") },
                    modifier = Modifier.fillMaxWidth().testTag("checkout_txn_ref_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = senderBankName,
                    onValueChange = { senderBankName = it },
                    label = { Text(if (isEnglish) "Your Bank / App" else "पठाएको बैंक वा माध्यम") },
                    placeholder = { Text("मुक्तिनाथ बैंकिङ, ConnectIPS, eSewa...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = senderAccountName,
                    onValueChange = { senderAccountName = it },
                    label = { Text(if (isEnglish) "Sender Account Name" else "रकम पठाउनेको नाम (Sender Name)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text(if (isEnglish) "Cancel" else "रद्द")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (customerName.isNotBlank() && customerPhone.isNotBlank()) {
                                val bankDetails = if (transactionRef.isNotBlank()) {
                                    BankTransferDetails(
                                        senderBankName = senderBankName.ifBlank { "मुक्तिनाथ विकास बैंक" },
                                        senderAccountName = senderAccountName.ifBlank { customerName },
                                        transactionRefNumber = transactionRef,
                                        transferredAmount = cart.totalAmount,
                                        transferDate = "",
                                        remarks = "Order Payment via Bank Transfer"
                                    )
                                } else null

                                onSubmit(customerName, customerPhone, deliveryAddress, bankDetails, orderNotes)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary),
                        modifier = Modifier.testTag("submit_order_and_payment_button")
                    ) {
                        Text(if (isEnglish) "Submit Order & Pay" else "अर्डर तथा भुक्तानी पेश")
                    }
                }
            }
        }
    }
}
