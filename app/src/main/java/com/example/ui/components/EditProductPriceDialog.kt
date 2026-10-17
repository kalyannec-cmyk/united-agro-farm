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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.model.Product
import com.example.ui.theme.AgroGoldTertiary
import com.example.ui.theme.AgroGreenPrimary

@Composable
fun EditProductPriceDialog(
    product: Product,
    isEnglish: Boolean = false,
    onDismiss: () -> Unit,
    onSavePriceAndStock: (newRetailPrice: Double, newWholesalePrice: Double, newStock: Double) -> Unit,
    onDeleteProduct: (() -> Unit)? = null
) {
    var retailPriceText by remember { mutableStateOf(product.retailPrice.toInt().toString()) }
    var wholesalePriceText by remember { mutableStateOf(product.wholesalePrice.toInt().toString()) }
    var stockText by remember { mutableStateOf(product.stockQuantity.toInt().toString()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("edit_product_price_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PriceChange,
                            contentDescription = null,
                            tint = AgroGreenPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEnglish) "Edit Price & Stock" else "मूल्य तथा स्टक सम्पादन",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AgroGreenPrimary
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "${product.nameNp} (${product.nameEn})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "एकाइ: ${if (isEnglish) product.unit.labelEn else product.unit.labelNp} | SKU: ${product.sku}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Retail Price Field
                Text(
                    text = if (isEnglish) "Retail Price (NPR)*" else "खुद्रे मूल्य (रु.) *",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = retailPriceText,
                    onValueChange = { retailPriceText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_retail_price_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Quick percentage modifiers for retail price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            val cur = retailPriceText.toDoubleOrNull() ?: product.retailPrice
                            retailPriceText = "%.0f".format((cur * 1.05))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+5%", style = MaterialTheme.typography.labelSmall)
                    }

                    FilledTonalButton(
                        onClick = {
                            val cur = retailPriceText.toDoubleOrNull() ?: product.retailPrice
                            retailPriceText = "%.0f".format((cur * 0.95))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("-5%", style = MaterialTheme.typography.labelSmall)
                    }

                    FilledTonalButton(
                        onClick = {
                            val cur = retailPriceText.toDoubleOrNull() ?: product.retailPrice
                            retailPriceText = "%.0f".format((cur + 50))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+50", style = MaterialTheme.typography.labelSmall)
                    }

                    FilledTonalButton(
                        onClick = {
                            val cur = retailPriceText.toDoubleOrNull() ?: product.retailPrice
                            retailPriceText = "%.0f".format((cur - 50).coerceAtLeast(0.0))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("-50", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Wholesale Price Field
                Text(
                    text = if (isEnglish) "Wholesale Price (NPR)" else "थोक मूल्य (रु.)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = wholesalePriceText,
                    onValueChange = { wholesalePriceText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_wholesale_price_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Available Stock Field
                Text(
                    text = if (isEnglish) "Available Stock Quantity" else "मौज्दात परिमाण (Stock Quantity)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = stockText,
                    onValueChange = { stockText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_stock_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onDeleteProduct != null) {
                        IconButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.testTag("delete_product_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = if (isEnglish) "Delete Product" else "उत्पादन हटाउनुहोस्",
                                tint = Color(0xFFD32F2F)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(onClick = onDismiss) {
                            Text(if (isEnglish) "Cancel" else "रद्द")
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                val rPrice = retailPriceText.toDoubleOrNull() ?: product.retailPrice
                                val wPrice = wholesalePriceText.toDoubleOrNull() ?: product.wholesalePrice
                                val stock = stockText.toDoubleOrNull() ?: product.stockQuantity
                                onSavePriceAndStock(rPrice, wPrice, stock)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary),
                            modifier = Modifier.testTag("save_price_changes_btn")
                        ) {
                            Text(if (isEnglish) "Save Changes" else "मूल्य सुरक्षित गर्नुहोस्")
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text(
                    text = if (isEnglish) "Remove Product?" else "उत्पादन हटाउने?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isEnglish) "Are you sure you want to remove '${product.nameEn}' from inventory and Firestore?"
                    else "के तपाईं '${product.nameNp}' लाई मौज्दात र फायरस्टोरबाट हटाउन चाहनुहुन्छ?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteProduct?.invoke()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    modifier = Modifier.testTag("confirm_delete_product_btn")
                ) {
                    Text(if (isEnglish) "Delete" else "हटाउनुहोस्")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                    Text(if (isEnglish) "Cancel" else "रद्द")
                }
            }
        )
    }
}
