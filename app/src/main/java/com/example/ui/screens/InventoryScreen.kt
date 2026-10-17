package com.example.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
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
import com.example.model.Product
import com.example.ui.components.AddNewProductDialog
import com.example.ui.components.EditProductPriceDialog
import com.example.ui.components.StockAdjustDialog
import com.example.ui.theme.AgroGoldTertiary
import com.example.ui.theme.AgroGreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    products: List<Product>,
    allProducts: List<Product>,
    searchQuery: String,
    filter: String,
    isEnglish: Boolean = false,
    onSearchChange: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    onStockAdjust: (productId: String, delta: Double) -> Unit,
    onStockSet: (productId: String, newStock: Double) -> Unit,
    onAddProduct: (Product) -> Unit,
    onUpdateProductPriceAndStock: (productId: String, retailPrice: Double, wholesalePrice: Double, stock: Double) -> Unit = { _, _, _, _ -> },
    onDeleteProduct: (productId: String) -> Unit = {},
    onNavigateToRoomInventory: (() -> Unit)? = null
) {
    var selectedProductForEdit by remember { mutableStateOf<Product?>(null) }
    var showAddProductDialog by remember { mutableStateOf(false) }

    val totalStockSum = allProducts.sumOf { it.stockQuantity }
    val lowStockCount = allProducts.count { it.isLowStock }
    val outOfStockCount = allProducts.count { it.isOutOfStock }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("inventory_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Stats Cards
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
                                text = if (isEnglish) "Farm Stock Inventory" else "फार्म इन्भेन्ट्रि तथा मौज्दात",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = AgroGreenPrimary
                            )
                            Text(
                                text = if (isEnglish) "Lalitpur United Agro Farm stock tracking" else "दैनिक उत्पादन संकलन तथा मौज्दात अद्यावधिक",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (onNavigateToRoomInventory != null) {
                                OutlinedButton(
                                    onClick = onNavigateToRoomInventory,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("open_room_inventory_button")
                                ) {
                                    Icon(imageVector = Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isEnglish) "Room Table" else "Room मौज्दात", fontSize = 12.sp)
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = AgroGreenPrimary.copy(alpha = 0.12f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Inventory,
                                        contentDescription = null,
                                        tint = AgroGreenPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Metrics Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        InventoryMetricBox(
                            label = if (isEnglish) "Total Units" else "कुल एकाइ",
                            value = "${totalStockSum.toInt()}",
                            color = AgroGreenPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        InventoryMetricBox(
                            label = if (isEnglish) "Low Stock" else "कम मौज्दात",
                            value = "$lowStockCount",
                            color = AgroGoldTertiary,
                            modifier = Modifier.weight(1f)
                        )
                        InventoryMetricBox(
                            label = if (isEnglish) "Out of Stock" else "सकिएको",
                            value = "$outOfStockCount",
                            color = Color(0xFFD32F2F),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Search input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = { Text(if (isEnglish) "Search by product name or SKU..." else "उत्पादनको नाम वा SKU बाट खोज्नुहोस्...") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("inventory_search_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Filters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = filter == "ALL",
                            onClick = { onFilterChange("ALL") },
                            label = { Text(if (isEnglish) "All (${allProducts.size})" else "सबै (${allProducts.size})") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AgroGreenPrimary, selectedLabelColor = Color.White)
                        )
                        FilterChip(
                            selected = filter == "LOW_STOCK",
                            onClick = { onFilterChange("LOW_STOCK") },
                            label = { Text(if (isEnglish) "Low Stock ($lowStockCount)" else "कम मौज्दात ($lowStockCount)") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AgroGoldTertiary, selectedLabelColor = Color.White)
                        )
                        FilterChip(
                            selected = filter == "OUT_OF_STOCK",
                            onClick = { onFilterChange("OUT_OF_STOCK") },
                            label = { Text(if (isEnglish) "Out of Stock ($outOfStockCount)" else "सकिएको ($outOfStockCount)") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFD32F2F), selectedLabelColor = Color.White)
                        )
                    }
                }
            }

            // Products Table List
            if (products.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isEnglish) "No inventory records found." else "कुनै पनि मौज्दात रेकर्ड फेला परेन।",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        InventoryItemRow(
                            product = product,
                            isEnglish = isEnglish,
                            onMinusClick = { onStockAdjust(product.id, -1.0) },
                            onPlusClick = { onStockAdjust(product.id, 1.0) },
                            onEditClick = { selectedProductForEdit = product }
                        )
                    }
                }
            }
        }

        // Floating Action Button to Add New Product
        FloatingActionButton(
            onClick = { showAddProductDialog = true },
            containerColor = AgroGreenPrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_product_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Product")
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isEnglish) "Add Product" else "नयाँ उत्पादन",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Dialog for editing product prices, stock, or deleting product
    selectedProductForEdit?.let { product ->
        EditProductPriceDialog(
            product = product,
            isEnglish = isEnglish,
            onDismiss = { selectedProductForEdit = null },
            onSavePriceAndStock = { rPrice, wPrice, newStock ->
                onUpdateProductPriceAndStock(product.id, rPrice, wPrice, newStock)
                selectedProductForEdit = null
            },
            onDeleteProduct = {
                onDeleteProduct(product.id)
                selectedProductForEdit = null
            }
        )
    }

    // Dialog for adding new product
    if (showAddProductDialog) {
        AddNewProductDialog(
            isEnglish = isEnglish,
            onDismiss = { showAddProductDialog = false },
            onProductAdded = { newProduct ->
                onAddProduct(newProduct)
                showAddProductDialog = false
            }
        )
    }
}

@Composable
private fun InventoryMetricBox(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InventoryItemRow(
    product: Product,
    isEnglish: Boolean = false,
    onMinusClick: () -> Unit,
    onPlusClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("inventory_item_${product.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = product.sku,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = product.category.iconEmoji,
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = if (isEnglish) product.nameEn else product.nameNp,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "रू. ${"%.0f".format(product.retailPrice)} / ${if (isEnglish) product.unit.labelEn else product.unit.labelNp}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Stock Count & Status Pill
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.clickable { onEditClick() }
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = if (product.stockQuantity % 1.0 == 0.0) "${product.stockQuantity.toInt()}" else "%.1f".format(product.stockQuantity),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = when {
                            product.isOutOfStock -> Color(0xFFD32F2F)
                            product.isLowStock -> AgroGoldTertiary
                            else -> AgroGreenPrimary
                        }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isEnglish) product.unit.name.take(3) else product.unit.labelNp.split(" ").lastOrNull() ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                val statusText = when {
                    product.isOutOfStock -> if (isEnglish) "Out of Stock" else "सकिएको"
                    product.isLowStock -> if (isEnglish) "Low Stock" else "कम मौज्दात"
                    else -> if (isEnglish) "In Stock" else "उपलब्ध"
                }
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = when {
                        product.isOutOfStock -> Color(0xFFD32F2F)
                        product.isLowStock -> AgroGoldTertiary
                        else -> AgroGreenPrimary
                    }
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Quick Adjust Buttons [-] [+]
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onMinusClick() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Surface(
                    shape = CircleShape,
                    color = AgroGreenPrimary,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onPlusClick() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}
