package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for PriceItem entity to perform CRUD operations
 * on the price_items table in Room database.
 */
@Dao
interface PriceItemDao {

    // --- CREATE ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceItem(item: PriceItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceItems(items: List<PriceItem>)

    // --- READ ---
    @Query("SELECT * FROM price_items ORDER BY product_name ASC")
    fun getAllPriceItems(): Flow<List<PriceItem>>

    @Query("SELECT * FROM price_items WHERE category = :category ORDER BY product_name ASC")
    fun getPriceItemsByCategory(category: String): Flow<List<PriceItem>>

    @Query("SELECT * FROM price_items WHERE id = :id LIMIT 1")
    suspend fun getPriceItemById(id: String): PriceItem?

    @Query("SELECT COUNT(*) FROM price_items")
    fun getPriceItemCount(): Flow<Int>

    // --- UPDATE ---
    @Update
    suspend fun updatePriceItem(item: PriceItem)

    @Query("UPDATE price_items SET retail_price = :retailPrice, wholesale_price = :wholesalePrice, last_updated = :timestamp WHERE id = :id")
    suspend fun updatePrices(
        id: String,
        retailPrice: Double,
        wholesalePrice: Double,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("UPDATE price_items SET retail_price = :retailPrice, last_updated = :timestamp WHERE id = :id")
    suspend fun updateRetailPrice(
        id: String,
        retailPrice: Double,
        timestamp: Long = System.currentTimeMillis()
    )

    // --- DELETE ---
    @Delete
    suspend fun deletePriceItem(item: PriceItem)

    @Query("DELETE FROM price_items WHERE id = :id")
    suspend fun deletePriceItemById(id: String)

    @Query("DELETE FROM price_items")
    suspend fun deleteAllPriceItems()
}
