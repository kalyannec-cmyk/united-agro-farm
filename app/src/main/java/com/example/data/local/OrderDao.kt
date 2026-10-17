package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for Order entity to perform CRUD operations
 * on the orders table in Room database.
 */
@Dao
interface OrderDao {

    // --- CREATE ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<Order>)

    // --- READ ---
    @Query("SELECT * FROM orders ORDER BY created_at DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE status = :status ORDER BY created_at DESC")
    fun getOrdersByStatus(status: String): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE order_id = :orderId LIMIT 1")
    suspend fun getOrderById(orderId: String): Order?

    @Query("SELECT COUNT(*) FROM orders")
    fun getOrderCount(): Flow<Int>

    @Query("SELECT SUM(total_amount) FROM orders WHERE status != 'CANCELLED'")
    fun getTotalRevenue(): Flow<Double?>

    // --- UPDATE ---
    @Update
    suspend fun updateOrder(order: Order)

    @Query("UPDATE orders SET status = :status WHERE order_id = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String)

    @Query("UPDATE orders SET notes = :notes WHERE order_id = :orderId")
    suspend fun updateOrderNotes(orderId: String, notes: String)

    // --- DELETE ---
    @Delete
    suspend fun deleteOrder(order: Order)

    @Query("DELETE FROM orders WHERE order_id = :orderId")
    suspend fun deleteOrderById(orderId: String)

    @Query("DELETE FROM orders")
    suspend fun deleteAllOrders()
}
