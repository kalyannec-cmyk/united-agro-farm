package com.example.data.local

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface and implementation providing clean separation of concerns
 * between Room DAO and ViewModel/UI layer.
 */
class LocalInventoryRepository(
    private val inventoryDao: FarmInventoryDao
) {
    val allInventory: Flow<List<FarmInventoryEntity>> = inventoryDao.getAllInventory()
    val lowStockItems: Flow<List<FarmInventoryEntity>> = inventoryDao.getLowStockItems()
    val totalStockValue: Flow<Double?> = inventoryDao.getTotalStockValue()
    val totalItemCount: Flow<Int> = inventoryDao.getTotalItemCount()

    fun getInventoryByCategory(category: String): Flow<List<FarmInventoryEntity>> {
        return inventoryDao.getInventoryByCategory(category)
    }

    suspend fun getItemById(id: String): FarmInventoryEntity? {
        return inventoryDao.getItemById(id)
    }

    suspend fun insertItem(item: FarmInventoryEntity) {
        inventoryDao.insertItem(item)
    }

    suspend fun insertAll(items: List<FarmInventoryEntity>) {
        inventoryDao.insertAll(items)
    }

    suspend fun updateItem(item: FarmInventoryEntity) {
        inventoryDao.updateItem(item)
    }

    suspend fun updateStockQuantity(id: String, newQuantity: Double) {
        inventoryDao.updateQuantity(id, newQuantity)
    }

    suspend fun updateUnitPrice(id: String, newUnitPrice: Double) {
        inventoryDao.updateUnitPrice(id, newUnitPrice)
    }

    suspend fun deleteItem(item: FarmInventoryEntity) {
        inventoryDao.deleteItem(item)
    }

    suspend fun deleteById(id: String) {
        inventoryDao.deleteById(id)
    }

    suspend fun clearAll() {
        inventoryDao.deleteAll()
    }
}
