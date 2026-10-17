package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for farm inventory items in Room.
 * Follows reactive Flow pattern for queries and suspend functions for writes.
 */
@Dao
interface FarmInventoryDao {

    @Query("SELECT * FROM farm_inventory ORDER BY product_name ASC")
    fun getAllInventory(): Flow<List<FarmInventoryEntity>>

    @Query("SELECT * FROM farm_inventory WHERE category = :category ORDER BY product_name ASC")
    fun getInventoryByCategory(category: String): Flow<List<FarmInventoryEntity>>

    @Query("SELECT * FROM farm_inventory WHERE current_quantity <= min_stock_threshold ORDER BY current_quantity ASC")
    fun getLowStockItems(): Flow<List<FarmInventoryEntity>>

    @Query("SELECT * FROM farm_inventory WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: String): FarmInventoryEntity?

    @Query("SELECT * FROM farm_inventory WHERE sku = :sku LIMIT 1")
    suspend fun getItemBySku(sku: String): FarmInventoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: FarmInventoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<FarmInventoryEntity>)

    @Update
    suspend fun updateItem(item: FarmInventoryEntity)

    @Query("UPDATE farm_inventory SET current_quantity = :newQuantity, last_updated = :timestamp WHERE id = :id")
    suspend fun updateQuantity(
        id: String,
        newQuantity: Double,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("UPDATE farm_inventory SET unit_price = :newUnitPrice, last_updated = :timestamp WHERE id = :id")
    suspend fun updateUnitPrice(
        id: String,
        newUnitPrice: Double,
        timestamp: Long = System.currentTimeMillis()
    )

    @Delete
    suspend fun deleteItem(item: FarmInventoryEntity)

    @Query("DELETE FROM farm_inventory WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM farm_inventory")
    suspend fun deleteAll()

    @Query("SELECT SUM(current_quantity * unit_price) FROM farm_inventory")
    fun getTotalStockValue(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM farm_inventory")
    fun getTotalItemCount(): Flow<Int>
}
