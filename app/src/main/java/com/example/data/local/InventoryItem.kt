package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room Entity representing an inventory item in the farm inventory table.
 */
@Entity(tableName = "inventory_items")
data class InventoryItem(
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
        get() = currentQuantity in 0.001..minStockThreshold

    val isOutOfStock: Boolean
        get() = currentQuantity <= 0.0

    val totalStockValue: Double
        get() = currentQuantity * unitPrice
}
