package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.InventoryItem
import com.example.ui.theme.AgroGoldTertiary
import com.example.ui.theme.AgroGreenPrimary
import java.util.Locale

/**
 * Composable screen that uses a LazyColumn to list items from the InventoryItem Room table,
 * including a FloatingActionButton to add new items.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryItemListScreen(
    items: List<InventoryItem>,
    isEnglish: Boolean = false,
    onAddItem: (InventoryItem) -> Unit,
    onUpdateQuantity: (id: String, newQuantity: Double) -> Unit,
    onDeleteItem: (id: String) -> Unit,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var showOnlyLowStock by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    val categories = remember {
        listOf("ALL", "DAIRY", "POULTRY", "LIVESTOCK", "VEGETABLES", "FERTILIZER_FEED")
    }

    val filteredItems = items.filter { item ->
        val matchesSearch = item.productName.contains(searchQuery, ignoreCase = true) ||
                item.productNameNp.contains(searchQuery, ignoreCase = true) ||
                item.sku.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategoryFilter == "ALL" || item.category.equals(selectedCategoryFilter, ignoreCase = true)
        val matchesStock = !showOnlyLowStock || item.isLowStock || item.isOutOfStock
        matchesSearch && matchesCategory && matchesStock
    }

    val totalItemsCount = items.size
    val totalStockSum = items.sumOf { it.currentQuantity }
    val totalStockValuation = items.sumOf { it.totalStockValue }
    val lowStockCount = items.count { it.isLowStock || it.isOutOfStock }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("inventory_item_list_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isEnglish) "Room Inventory Database" else "इन्भेन्ट्रि मौज्दात सूची (Room)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (isEnglish) "$totalItemsCount items • Total NPR ${String.format(Locale.getDefault(), "%,.0f", totalStockValuation)}"
                            else "$totalItemsCount प्रकारका वस्तु • जम्मा रु. ${String.format(Locale.getDefault(), "%,.0f", totalStockValuation)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFC8E6C9)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("inventory_list_back_button")
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = AgroGreenPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("add_inventory_item_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (isEnglish) "Add New Inventory Item" else "नयाँ वस्तु थप्नुहोस्"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Summary Banner
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    // Quick Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatBadge(
                            label = if (isEnglish) "Total Stock" else "कुल मौज्दात",
                            value = "${String.format(Locale.getDefault(), "%,.1f", totalStockSum)}",
                            color = AgroGreenPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        StatBadge(
                            label = if (isEnglish) "Stock Value" else "कुल मूल्यांकन",
                            value = "रु ${String.format(Locale.getDefault(), "%,.0f", totalStockValuation)}",
                            color = Color(0xFF1565C0),
                            modifier = Modifier.weight(1.3f)
                        )
                        StatBadge(
                            label = if (isEnglish) "Low Stock" else "न्यून मौज्दात",
                            value = "$lowStockCount",
                            color = if (lowStockCount > 0) Color(0xFFD32F2F) else AgroGreenPrimary,
                            modifier = Modifier.weight(0.9f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Search input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(if (isEnglish) "Search by product name or SKU..." else "वस्तुको नाम वा SKU खोज्नुहोस्...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("inventory_search_input")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Filter Chips Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = showOnlyLowStock,
                                onClick = { showOnlyLowStock = !showOnlyLowStock },
                                label = { Text(if (isEnglish) "Low Stock Alert" else "न्यून मौज्दात") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFFF3E0),
                                    selectedLabelColor = Color(0xFFE65100)
                                ),
                                modifier = Modifier.testTag("filter_low_stock_chip")
                            )
                        }

                        items(categories) { category ->
                            FilterChip(
                                selected = selectedCategoryFilter == category,
                                onClick = { selectedCategoryFilter = category },
                                label = {
                                    Text(
                                        when (category) {
                                            "ALL" -> if (isEnglish) "All" else "सबै"
                                            "DAIRY" -> if (isEnglish) "Dairy" else "दूध/घिउ"
                                            "POULTRY" -> if (isEnglish) "Poultry" else "कुखुरा/अण्डा"
                                            "LIVESTOCK" -> if (isEnglish) "Livestock" else "बाख्रा/पशु"
                                            "VEGETABLES" -> if (isEnglish) "Veggies" else "तरकारी"
                                            "FERTILIZER_FEED" -> if (isEnglish) "Feed/Bio" else "दाना/मल"
                                            else -> category
                                        }
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AgroGreenPrimary.copy(alpha = 0.15f),
                                    selectedLabelColor = AgroGreenPrimary
                                )
                            )
                        }
                    }
                }
            }

            // LazyColumn to list items from InventoryItem Room table
            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isEnglish) "No inventory items found." else "कुनै मौज्दात वस्तु फेला परेन।",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isEnglish) "Tap the '+' button below to add your first item." else "नयाँ वस्तु थप्न तलको '+' बटन थिच्नुहोस्।",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("inventory_item_lazy_column"),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        InventoryItemCard(
                            item = item,
                            isEnglish = isEnglish,
                            onAdjustQuantity = { delta ->
                                val newQty = (item.currentQuantity + delta).coerceAtLeast(0.0)
                                onUpdateQuantity(item.id, newQty)
                            },
                            onDelete = { onDeleteItem(item.id) },
                            modifier = Modifier.testTag("inventory_item_row_${item.id}")
                        )
                    }
                    // Bottom spacer for FAB
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }

    // Dialog for adding a new item
    if (showAddDialog) {
        AddInventoryItemDialog(
            isEnglish = isEnglish,
            onDismiss = { showAddDialog = false },
            onConfirm = { newItem ->
                onAddItem(newItem)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun InventoryItemCard(
    item: InventoryItem,
    isEnglish: Boolean,
    onAdjustQuantity: (Double) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header row with Name and Stock Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isEnglish) item.productName else item.productNameNp.ifBlank { item.productName },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B3B22)
                    )
                    if (item.productNameNp.isNotBlank() && item.productName.isNotBlank() && item.productName != item.productNameNp) {
                        Text(
                            text = if (isEnglish) item.productNameNp else item.productName,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                // Stock Badge
                val (badgeBg, badgeText, badgeColor) = when {
                    item.isOutOfStock -> Triple(Color(0xFFFFEBEE), if (isEnglish) "Out of Stock" else "सकियो", Color(0xFFD32F2F))
                    item.isLowStock -> Triple(Color(0xFFFFF3E0), if (isEnglish) "Low Stock" else "कम छ", Color(0xFFEF6C00))
                    else -> Triple(Color(0xFFE8F5E9), if (isEnglish) "In Stock" else "उपलब्ध", AgroGreenPrimary)
                }

                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details Row: Price, Current Quantity, Stock Value
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF9FBF9), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isEnglish) "Unit Price" else "प्रति एकाइ मूल्य",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "रु ${String.format(Locale.getDefault(), "%,.2f", item.unitPrice)} / ${item.unit}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF2E7D32)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isEnglish) "Current Quantity" else "मौज्दात परिमाण",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "${String.format(Locale.getDefault(), "%,.1f", item.currentQuantity)} ${item.unit}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (item.isOutOfStock) Color(0xFFD32F2F) else Color(0xFF1B3B22)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isEnglish) "Total Valuation" else "कुल मूल्य",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "रु ${String.format(Locale.getDefault(), "%,.0f", item.totalStockValue)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF1565C0)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row: Quick stock adjust (-5, -1, +1, +5) and delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    OutlinedButton(
                        onClick = { onAdjustQuantity(-1.0) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease 1", modifier = Modifier.size(14.dp))
                        Text("1", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = { onAdjustQuantity(1.0) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase 1", modifier = Modifier.size(14.dp))
                        Text("1", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = { onAdjustQuantity(5.0) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("+5", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Item",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatBadge(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 10.sp, color = Color.Gray)
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun AddInventoryItemDialog(
    isEnglish: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (InventoryItem) -> Unit
) {
    var nameEn by remember { mutableStateOf("") }
    var nameNp by remember { mutableStateOf("") }
    var quantityStr by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf("kg") }
    var selectedCategory by remember { mutableStateOf("DAIRY") }
    var minThresholdStr by remember { mutableStateOf("10") }
    var skuStr by remember { mutableStateOf("") }

    val units = listOf("kg", "liter", "crate", "piece", "dozen", "bag_50kg", "pair")
    val categories = listOf("DAIRY", "POULTRY", "LIVESTOCK", "VEGETABLES", "FERTILIZER_FEED")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = null, tint = AgroGreenPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEnglish) "Add New Inventory Item" else "नयाँ इन्भेन्ट्रि वस्तु थप्नुहोस्",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text(if (isEnglish) "Product Name (English)" else "वस्तुको नाम (अंग्रेजी)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_item_name_input")
                )

                OutlinedTextField(
                    value = nameNp,
                    onValueChange = { nameNp = it },
                    label = { Text(if (isEnglish) "Product Name (Nepali)" else "वस्तुको नाम (नेपाली)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_item_name_np_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        label = { Text(if (isEnglish) "Quantity" else "परिमाण") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("add_item_quantity_input")
                    )

                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text(if (isEnglish) "Price (NPR)" else "मूल्य (रु)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("add_item_unit_price_input")
                    )
                }

                // Category selection chips
                Text(
                    text = if (isEnglish) "Category:" else "विधा:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AgroGreenPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = AgroGreenPrimary
                            )
                        )
                    }
                }

                // Unit selection chips
                Text(
                    text = if (isEnglish) "Unit:" else "एकाइ:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(units) { u ->
                        FilterChip(
                            selected = selectedUnit == u,
                            onClick = { selectedUnit = u },
                            label = { Text(u, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AgroGoldTertiary.copy(alpha = 0.3f),
                                selectedLabelColor = Color(0xFF1B3B22)
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityStr.toDoubleOrNull() ?: 0.0
                    val price = priceStr.toDoubleOrNull() ?: 0.0
                    val minStock = minThresholdStr.toDoubleOrNull() ?: 10.0
                    val finalNameEn = nameEn.ifBlank { nameNp }
                    val finalNameNp = nameNp.ifBlank { nameEn }

                    if (finalNameEn.isNotBlank()) {
                        val newItem = InventoryItem(
                            productName = finalNameEn,
                            productNameNp = finalNameNp,
                            currentQuantity = qty,
                            unitPrice = price,
                            unit = selectedUnit,
                            category = selectedCategory,
                            minStockThreshold = minStock,
                            sku = skuStr.ifBlank { "UAF-${System.currentTimeMillis() % 10000}" }
                        )
                        onConfirm(newItem)
                    }
                },
                enabled = (nameEn.isNotBlank() || nameNp.isNotBlank()) && priceStr.toDoubleOrNull() != null,
                colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary),
                modifier = Modifier.testTag("add_item_dialog_confirm_button")
            ) {
                Text(if (isEnglish) "Add Item" else "थप्नुहोस्")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isEnglish) "Cancel" else "रद्द")
            }
        }
    )
}
