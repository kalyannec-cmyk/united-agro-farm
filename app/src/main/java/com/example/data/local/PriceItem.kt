package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room Entity representing a product price item in the price list table.
 */
@Entity(tableName = "price_items")
data class PriceItem(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "product_name")
    val productName: String,

    @ColumnInfo(name = "product_name_np")
    val productNameNp: String = "",

    @ColumnInfo(name = "retail_price")
    val retailPrice: Double,

    @ColumnInfo(name = "wholesale_price")
    val wholesalePrice: Double = 0.0,

    @ColumnInfo(name = "unit")
    val unit: String = "kg",

    @ColumnInfo(name = "category")
    val category: String = "DAIRY",

    @ColumnInfo(name = "description")
    val description: String = "",

    @ColumnInfo(name = "is_organic_certified")
    val isOrganicCertified: Boolean = true,

    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long = System.currentTimeMillis()
)
