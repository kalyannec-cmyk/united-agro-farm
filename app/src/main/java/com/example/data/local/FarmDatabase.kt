package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Main Room Database for United Agro Farm local persistence, containing tables for:
 * - InventoryItem ("inventory_items")
 * - Order ("orders")
 * - PriceItem ("price_items")
 * - FarmInventoryEntity ("farm_inventory")
 */
@Database(
    entities = [
        InventoryItem::class,
        Order::class,
        PriceItem::class,
        FarmInventoryEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class FarmDatabase : RoomDatabase() {

    abstract fun inventoryDao(): FarmInventoryDao
    abstract fun inventoryItemDao(): InventoryItemDao
    abstract fun orderDao(): OrderDao
    abstract fun priceItemDao(): PriceItemDao

    companion object {
        @Volatile
        private var INSTANCE: FarmDatabase? = null

        fun getInstance(context: Context): FarmDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FarmDatabase::class.java,
                    "united_agro_farm.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
