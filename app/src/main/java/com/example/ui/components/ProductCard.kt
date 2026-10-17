package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Product
import com.example.ui.theme.AgroGoldTertiary
import com.example.ui.theme.AgroGreenPrimary

@Composable
fun ProductCard(
    product: Product,
    isEnglish: Boolean = false,
    onAddToCart: (Product) -> Unit,
    onEditPriceClick: ((Product) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Category & Badge Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AgroGreenPrimary.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = product.category.iconEmoji,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isEnglish) product.category.displayNameEn else product.category.displayNameNp,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AgroGreenPrimary
                        )
                    }
                }

                // Stock Badge
                val (stockBg, stockFg, stockText) = when {
                    product.isOutOfStock -> Triple(
                        Color(0xFFFFEBEE),
                        Color(0xFFC62828),
                        if (isEnglish) "Out of Stock" else "सकिएको"
                    )
                    product.isLowStock -> Triple(
                        Color(0xFFFFF3E0),
                        Color(0xFFEF6C00),
                        if (isEnglish) "Low Stock (${product.stockQuantity.toInt()})" else "थोरै बाँकी (${product.stockQuantity.toInt()})"
                    )
                    else -> Triple(
                        Color(0xFFE8F5E9),
                        Color(0xFF2E7D32),
                        if (isEnglish) "In Stock (${product.stockQuantity.toInt()})" else "उपलब्ध (${product.stockQuantity.toInt()})"
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = stockBg
                ) {
                    Text(
                        text = stockText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = stockFg,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title & Description
            Text(
                text = if (isEnglish) product.nameEn else product.nameNp,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!isEnglish) {
                Text(
                    text = product.nameEn,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isEnglish) product.descriptionEn else product.descriptionNp,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Pricing and Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "रू. ${"%.0f".format(product.retailPrice)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = AgroGreenPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "/ ${if (isEnglish) product.unit.labelEn else product.unit.labelNp}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }

                    Text(
                        text = "${if (isEnglish) "Wholesale" else "थोक मूल्य"}: रू. ${"%.0f".format(product.wholesalePrice)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = AgroGoldTertiary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (onEditPriceClick != null) {
                        OutlinedButton(
                            onClick = { onEditPriceClick(product) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("edit_price_btn_${product.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Price",
                                modifier = Modifier.size(16.dp),
                                tint = AgroGreenPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isEnglish) "Price" else "मूल्य",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Button(
                        onClick = { onAddToCart(product) },
                        enabled = !product.isOutOfStock,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AgroGreenPrimary,
                            disabledContainerColor = Color.LightGray
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_to_cart_${product.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isEnglish) "Order" else "अर्डर",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
