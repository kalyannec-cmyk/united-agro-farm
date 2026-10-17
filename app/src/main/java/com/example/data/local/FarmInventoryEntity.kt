package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room Entity representing a farm inventory item.
 *
 * Requirements:
 * - Product Name (product_name / productName)
 * - Current Quantity (current_quantity / currentQuantity)
 * - Unit Price (unit_price / unitPrice)
 *
 * Additional supporting fields:
 * - Unit (e.g. Liter, Kg, Crate, Dozen)
 * - Category (e.g. DAIRY, POULTRY, LIVESTOCK, VEGETABLES, FERTILIZER_FEED)
 * - Minimum stock threshold for alert warnings
 * - Last updated timestamp
 */
@Entity(tableName = "farm_inventory")
data class FarmInventoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "product_name")
    val productName: String,

    @ColumnInfo(name = "product_name_np")
    val productNameNp: String = "",

    @ColumnInfo(name = "current_quantity")
    val currentQuantity: Double = 0.0,

    @ColumnInfo(name = "unit_price")
    val unitPrice: Double = 0.0,

    @ColumnInfo(name = "wholesale_price")
    val wholesalePrice: Double = 0.0,

    @ColumnInfo(name = "unit")
    val unit: String = "kg",

    @ColumnInfo(name = "category")
    val category: String = "DAIRY",

    @ColumnInfo(name = "min_stock_threshold")
    val minStockThreshold: Double = 10.0,

    @ColumnInfo(name = "sku")
    val sku: String = "",

    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val isLowStock: Boolean
        get() = currentQuantity <= minStockThreshold && currentQuantity > 0

    val isOutOfStock: Boolean
        get() = currentQuantity <= 0.0

    val totalStockValue: Double
        get() = currentQuantity * unitPrice
}
