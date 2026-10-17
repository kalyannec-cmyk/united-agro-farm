package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for InventoryItem entity to perform CRUD operations
 * on the inventory_items table in Room database.
 */
@Dao
interface InventoryItemDao {

    // --- CREATE ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<InventoryItem>)

    // --- READ ---
    @Query("SELECT * FROM inventory_items ORDER BY product_name ASC")
    fun getAllItems(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: String): InventoryItem?

    @Query("SELECT * FROM inventory_items WHERE sku = :sku LIMIT 1")
    suspend fun getItemBySku(sku: String): InventoryItem?

    @Query("SELECT * FROM inventory_items WHERE category = :category ORDER BY product_name ASC")
    fun getItemsByCategory(category: String): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE current_quantity <= min_stock_threshold ORDER BY current_quantity ASC")
    fun getLowStockItems(): Flow<List<InventoryItem>>

    @Query("SELECT SUM(current_quantity * unit_price) FROM inventory_items")
    fun getTotalStockValue(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM inventory_items")
    fun getTotalItemCount(): Flow<Int>

    // --- UPDATE ---
    @Update
    suspend fun updateItem(item: InventoryItem)

    @Query("UPDATE inventory_items SET current_quantity = :newQuantity, last_updated = :timestamp WHERE id = :id")
    suspend fun updateQuantity(
        id: String,
        newQuantity: Double,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("UPDATE inventory_items SET unit_price = :newUnitPrice, last_updated = :timestamp WHERE id = :id")
    suspend fun updateUnitPrice(
        id: String,
        newUnitPrice: Double,
        timestamp: Long = System.currentTimeMillis()
    )

    // --- DELETE ---
    @Delete
    suspend fun deleteItem(item: InventoryItem)

    @Query("DELETE FROM inventory_items WHERE id = :id")
    suspend fun deleteItemById(id: String)

    @Query("DELETE FROM inventory_items")
    suspend fun deleteAll()
}
