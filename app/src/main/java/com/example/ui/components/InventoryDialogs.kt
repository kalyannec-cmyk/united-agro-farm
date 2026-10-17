package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.model.PriceUnit
import com.example.model.Product
import com.example.model.ProductCategory
import com.example.ui.theme.AgroGreenPrimary
import java.util.UUID

@Composable
fun StockAdjustDialog(
    product: Product,
    isEnglish: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var stockText by remember { mutableStateOf(product.stockQuantity.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("stock_adjust_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = if (isEnglish) "Update Stock Quantity" else "मौज्दात (स्टक) परिवर्तन गर्नुहोस्",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isEnglish) product.nameEn else product.nameNp,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AgroGreenPrimary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    FilledTonalButton(
                        onClick = {
                            val current = stockText.toDoubleOrNull() ?: product.stockQuantity
                            val newStock = (current - 5.0).coerceAtLeast(0.0)
                            stockText = if (newStock % 1 == 0.0) newStock.toInt().toString() else newStock.toString()
                        }
                    ) {
                        Text("-5")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    FilledTonalButton(
                        onClick = {
                            val current = stockText.toDoubleOrNull() ?: product.stockQuantity
                            val newStock = (current - 1.0).coerceAtLeast(0.0)
                            stockText = if (newStock % 1 == 0.0) newStock.toInt().toString() else newStock.toString()
                        }
                    ) {
                        Text("-1")
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    OutlinedTextField(
                        value = stockText,
                        onValueChange = { stockText = it },
                        label = { Text(if (isEnglish) "Quantity" else "परिमाण") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .width(110.dp)
                            .testTag("stock_quantity_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    FilledTonalButton(
                        onClick = {
                            val current = stockText.toDoubleOrNull() ?: product.stockQuantity
                            val newStock = current + 1.0
                            stockText = if (newStock % 1 == 0.0) newStock.toInt().toString() else newStock.toString()
                        }
                    ) {
                        Text("+1")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    FilledTonalButton(
                        onClick = {
                            val current = stockText.toDoubleOrNull() ?: product.stockQuantity
                            val newStock = current + 5.0
                            stockText = if (newStock % 1 == 0.0) newStock.toInt().toString() else newStock.toString()
                        }
                    ) {
                        Text("+5")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "एकाइ: ${if (isEnglish) product.unit.labelEn else product.unit.labelNp}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(20.dp))

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
                            val qty = stockText.toDoubleOrNull() ?: product.stockQuantity
                            onSave(qty)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary)
                    ) {
                        Text(if (isEnglish) "Save" else "सुरक्षित गर्नुहोस्")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewProductDialog(
    isEnglish: Boolean = false,
    onDismiss: () -> Unit,
    onProductAdded: (Product) -> Unit
) {
    var nameNp by remember { mutableStateOf("") }
    var nameEn by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ProductCategory.DAIRY) }
    var categoryExpanded by remember { mutableStateOf(false) }

    var retailPriceText by remember { mutableStateOf("") }
    var wholesalePriceText by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf(PriceUnit.KG) }
    var unitExpanded by remember { mutableStateOf(false) }

    var initialStockText by remember { mutableStateOf("10") }
    var descriptionNp by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("add_new_product_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = if (isEnglish) "Add New Product to Inventory" else "नयाँ उत्पादन इन्भेन्ट्रिमा थप्नुहोस्",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AgroGreenPrimary
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = nameNp,
                    onValueChange = { nameNp = it },
                    label = { Text("उत्पादनको नाम (नेपाली)*") },
                    placeholder = { Text("उदा. ताजा काउली, शुद्ध घिउ") },
                    modifier = Modifier.fillMaxWidth().testTag("product_name_np_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text("Product Name (English)") },
                    placeholder = { Text("e.g. Fresh Cauliflower, Pure Ghee") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = if (isEnglish) selectedCategory.displayNameEn else "${selectedCategory.iconEmoji} ${selectedCategory.displayNameNp}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isEnglish) "Category" else "वर्ग (Category)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        ProductCategory.values().filter { it != ProductCategory.ALL }.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text("${cat.iconEmoji} ${if (isEnglish) cat.displayNameEn else cat.displayNameNp}") },
                                onClick = {
                                    selectedCategory = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Price Inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = retailPriceText,
                        onValueChange = { retailPriceText = it },
                        label = { Text("खुद्रे मूल्य (रु.)*") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("product_retail_price_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = wholesalePriceText,
                        onValueChange = { wholesalePriceText = it },
                        label = { Text("थोक मूल्य (रु.)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Unit & Initial Stock
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = unitExpanded,
                        onExpandedChange = { unitExpanded = !unitExpanded },
                        modifier = Modifier.weight(1.2f)
                    ) {
                        OutlinedTextField(
                            value = if (isEnglish) selectedUnit.labelEn else selectedUnit.labelNp,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (isEnglish) "Unit" else "एकाइ (Unit)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = unitExpanded,
                            onDismissRequest = { unitExpanded = false }
                        ) {
                            PriceUnit.values().forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(if (isEnglish) unit.labelEn else unit.labelNp) },
                                    onClick = {
                                        selectedUnit = unit
                                        unitExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = initialStockText,
                        onValueChange = { initialStockText = it },
                        label = { Text("सुरु मौज्दात") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.8f).testTag("product_initial_stock_input"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = descriptionNp,
                    onValueChange = { descriptionNp = it },
                    label = { Text("छोटो विवरण (Description)") },
                    placeholder = { Text("फार्मको उत्पादन, अर्गानिक गुणस्तर...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(20.dp))

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
                            val rPrice = retailPriceText.toDoubleOrNull() ?: 0.0
                            val wPrice = wholesalePriceText.toDoubleOrNull() ?: (rPrice * 0.9)
                            val stock = initialStockText.toDoubleOrNull() ?: 0.0
                            if (nameNp.isNotBlank() && rPrice > 0.0) {
                                val newProd = Product(
                                    id = "PROD-${UUID.randomUUID().toString().take(6).uppercase()}",
                                    nameNp = nameNp.trim(),
                                    nameEn = if (nameEn.isNotBlank()) nameEn.trim() else nameNp.trim(),
                                    category = selectedCategory,
                                    retailPrice = rPrice,
                                    wholesalePrice = wPrice,
                                    unit = selectedUnit,
                                    stockQuantity = stock,
                                    descriptionNp = if (descriptionNp.isNotBlank()) descriptionNp.trim() else "ललितपुर युनाइटेड एग्रो फार्मको उत्पादन",
                                    descriptionEn = if (nameEn.isNotBlank()) "Fresh production from Lalitpur United Agro Farm" else descriptionNp,
                                    sku = "UAF-${selectedCategory.name.take(3)}-${(100..999).random()}"
                                )
                                onProductAdded(newProd)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary),
                        modifier = Modifier.testTag("save_new_product_button")
                    ) {
                        Text(if (isEnglish) "Add Product" else "उत्पादन थप्नुहोस्")
                    }
                }
            }
        }
    }
}
