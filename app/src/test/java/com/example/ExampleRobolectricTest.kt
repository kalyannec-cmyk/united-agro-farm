package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlinx.coroutines.flow.first

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("United Agro Farm", appName)

    val bankAccountNo = context.getString(R.string.bank_account_no)
    assertEquals("09610201478560000001", bankAccountNo)

    val bankAccountName = context.getString(R.string.bank_account_name)
    assertEquals("Lalitpur United Agro Farm Pvt. Ltd.", bankAccountName)
  }

  @Test
  fun `test farm inventory room database schema`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = androidx.room.Room.inMemoryDatabaseBuilder(
        context,
        com.example.data.local.FarmDatabase::class.java
    ).allowMainThreadQueries().build()

    val dao = database.inventoryDao()

    val item = com.example.data.local.FarmInventoryEntity(
        id = "INV-001",
        productName = "Fresh Cow Milk",
        productNameNp = "ताजा गाईको दूध",
        currentQuantity = 50.0,
        unitPrice = 110.0,
        wholesalePrice = 95.0,
        unit = "Liter",
        category = "DAIRY",
        minStockThreshold = 10.0
    )

    dao.insertItem(item)

    val fetched = dao.getItemById("INV-001")
    org.junit.Assert.assertNotNull(fetched)
    assertEquals("Fresh Cow Milk", fetched?.productName)
    assertEquals(50.0, fetched?.currentQuantity ?: 0.0, 0.001)
    assertEquals(110.0, fetched?.unitPrice ?: 0.0, 0.001)
    assertEquals(5500.0, fetched?.totalStockValue ?: 0.0, 0.001)

    // Update quantity and unit price
    dao.updateQuantity("INV-001", 65.0)
    dao.updateUnitPrice("INV-001", 115.0)

    val updated = dao.getItemById("INV-001")
    assertEquals(65.0, updated?.currentQuantity ?: 0.0, 0.001)
    assertEquals(115.0, updated?.unitPrice ?: 0.0, 0.001)

    database.close()
  }

  @Test
  fun `test InventoryItem, Order, and PriceItem Room entities`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = androidx.room.Room.inMemoryDatabaseBuilder(
        context,
        com.example.data.local.FarmDatabase::class.java
    ).allowMainThreadQueries().build()

    // 1. Test Order entity & DAO
    val orderDao = database.orderDao()
    val order = com.example.data.local.Order(
        orderId = "ORD-TEST-001",
        customerName = "राम शर्मा",
        customerPhone = "9841234567",
        deliveryAddress = "ललितपुर, नेपाल",
        totalAmount = 2500.0,
        status = "PENDING_VERIFICATION",
        paymentMode = "BANK_TRANSFER",
        senderBankName = "मुक्तिनाथ विकास बैंक",
        senderAccountName = "Ram Sharma",
        transactionRefNumber = "MNB-TXN-112233",
        transferredAmount = 2500.0
    )
    orderDao.insertOrder(order)
    val fetchedOrder = orderDao.getOrderById("ORD-TEST-001")
    org.junit.Assert.assertNotNull(fetchedOrder)
    assertEquals("राम शर्मा", fetchedOrder?.customerName)
    assertEquals(2500.0, fetchedOrder?.totalAmount ?: 0.0, 0.001)

    orderDao.updateOrderStatus("ORD-TEST-001", "VERIFIED")
    val updatedOrder = orderDao.getOrderById("ORD-TEST-001")
    assertEquals("VERIFIED", updatedOrder?.status)

    // 2. Test PriceItem entity & DAO
    val priceItemDao = database.priceItemDao()
    val priceItem = com.example.data.local.PriceItem(
        id = "PRICE-001",
        productName = "Pure Cow Ghee",
        productNameNp = "शुद्ध गाईको घिउ",
        retailPrice = 1400.0,
        wholesalePrice = 1250.0,
        unit = "kg",
        category = "DAIRY"
    )
    priceItemDao.insertPriceItem(priceItem)
    val fetchedPrice = priceItemDao.getPriceItemById("PRICE-001")
    org.junit.Assert.assertNotNull(fetchedPrice)
    assertEquals("Pure Cow Ghee", fetchedPrice?.productName)
    assertEquals(1400.0, fetchedPrice?.retailPrice ?: 0.0, 0.001)

    database.close()
  }

  @Test
  fun `test InventoryItemDao CRUD operations`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = androidx.room.Room.inMemoryDatabaseBuilder(
        context,
        com.example.data.local.FarmDatabase::class.java
    ).allowMainThreadQueries().build()

    val inventoryDao = database.inventoryItemDao()

    // Create
    val item = com.example.data.local.InventoryItem(
        id = "INV-ITEM-01",
        productName = "Organic Goat Cheese",
        productNameNp = "अर्ग्यानिक बाख्राको चिज",
        currentQuantity = 20.0,
        unitPrice = 1800.0,
        unit = "kg",
        category = "DAIRY",
        minStockThreshold = 5.0
    )
    inventoryDao.insertItem(item)

    // Read
    val fetched = inventoryDao.getItemById("INV-ITEM-01")
    org.junit.Assert.assertNotNull(fetched)
    assertEquals("Organic Goat Cheese", fetched?.productName)
    assertEquals(20.0, fetched?.currentQuantity ?: 0.0, 0.001)
    assertEquals(1800.0, fetched?.unitPrice ?: 0.0, 0.001)

    // Update
    inventoryDao.updateQuantity("INV-ITEM-01", 35.0)
    inventoryDao.updateUnitPrice("INV-ITEM-01", 1900.0)
    val updated = inventoryDao.getItemById("INV-ITEM-01")
    assertEquals(35.0, updated?.currentQuantity ?: 0.0, 0.001)
    assertEquals(1900.0, updated?.unitPrice ?: 0.0, 0.001)

    // Delete
    inventoryDao.deleteItemById("INV-ITEM-01")
    val deleted = inventoryDao.getItemById("INV-ITEM-01")
    org.junit.Assert.assertNull(deleted)

    database.close()
  }

  @Test
  fun `test past orders query from database displays customer name, date, and total`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = androidx.room.Room.inMemoryDatabaseBuilder(
        context,
        com.example.data.local.FarmDatabase::class.java
    ).allowMainThreadQueries().build()

    val orderDao = database.orderDao()

    val order1 = com.example.data.local.Order(
        orderId = "ORD-001",
        customerName = "राजेश श्रेष्ठ (Rajesh Shrestha)",
        customerPhone = "9841234567",
        totalAmount = 3850.0,
        status = "VERIFIED",
        orderDate = "2026-10-15 11:30 AM",
        paymentMode = "BANK_TRANSFER"
    )
    val order2 = com.example.data.local.Order(
        orderId = "ORD-002",
        customerName = "सुनिता थापा (Sunita Thapa)",
        customerPhone = "9851098765",
        totalAmount = 1600.0,
        status = "COMPLETED",
        orderDate = "2026-10-14 02:15 PM",
        paymentMode = "CASH_ON_DELIVERY"
    )

    orderDao.insertOrders(listOf(order1, order2))

    val allOrders = orderDao.getAllOrders().first()
    org.junit.Assert.assertEquals(2, allOrders.size)

    val fetched1 = allOrders.first { it.orderId == "ORD-001" }
    assertEquals("राजेश श्रेष्ठ (Rajesh Shrestha)", fetched1.customerName)
    assertEquals("2026-10-15 11:30 AM", fetched1.orderDate)
    assertEquals(3850.0, fetched1.totalAmount, 0.001)

    val fetched2 = allOrders.first { it.orderId == "ORD-002" }
    assertEquals("सुनिता थापा (Sunita Thapa)", fetched2.customerName)
    assertEquals("2026-10-14 02:15 PM", fetched2.orderDate)
    assertEquals(1600.0, fetched2.totalAmount, 0.001)

    database.close()
  }

  @Test
  fun `test monthly revenue aggregation groups orders by month and computes totals`() = kotlinx.coroutines.runBlocking {
    val orders = listOf(
        com.example.data.local.Order(
            orderId = "ORD-AUG-01",
            customerName = "राम शरण",
            customerPhone = "9841000000",
            totalAmount = 5000.0,
            status = "VERIFIED",
            orderDate = "2026-08-15 10:00 AM",
            paymentMode = "BANK_TRANSFER"
        ),
        com.example.data.local.Order(
            orderId = "ORD-SEP-01",
            customerName = "सरिता श्रेष्ठ",
            customerPhone = "9851000000",
            totalAmount = 8000.0,
            status = "COMPLETED",
            orderDate = "2026-09-20 02:00 PM",
            paymentMode = "BANK_TRANSFER"
        ),
        com.example.data.local.Order(
            orderId = "ORD-OCT-01",
            customerName = "राजेश श्रेष्ठ",
            customerPhone = "9841234567",
            totalAmount = 4000.0,
            status = "VERIFIED",
            orderDate = "2026-10-15 11:30 AM",
            paymentMode = "BANK_TRANSFER"
        ),
        com.example.data.local.Order(
            orderId = "ORD-OCT-02",
            customerName = "हरि खत्री",
            customerPhone = "9818000000",
            totalAmount = 6000.0,
            status = "PENDING_VERIFICATION",
            orderDate = "2026-10-16 09:00 AM",
            paymentMode = "BANK_TRANSFER"
        ),
        com.example.data.local.Order(
            orderId = "ORD-OCT-03",
            customerName = "रद्द अर्डर",
            customerPhone = "9800000000",
            totalAmount = 2000.0,
            status = "CANCELLED",
            orderDate = "2026-10-17 01:00 PM",
            paymentMode = "CASH_ON_DELIVERY"
        )
    )

    val analytics = com.example.ui.screens.aggregateOrdersByMonth(orders)

    // Non-cancelled total: 5000 + 8000 + 4000 + 6000 = 23000
    assertEquals(23000.0, analytics.totalRevenue, 0.001)
    // Verified + Completed: 5000 + 8000 + 4000 = 17000
    assertEquals(17000.0, analytics.verifiedRevenue, 0.001)
    // Pending: 6000
    assertEquals(6000.0, analytics.pendingRevenue, 0.001)
    assertEquals(5, analytics.totalOrders)

    // 3 distinct months: 2026-08, 2026-09, 2026-10
    assertEquals(3, analytics.monthlyBuckets.size)

    val augBucket = analytics.monthlyBuckets.first { it.monthKey == "2026-08" }
    assertEquals(5000.0, augBucket.totalRevenue, 0.001)
    assertEquals(1, augBucket.orderCount)

    val sepBucket = analytics.monthlyBuckets.first { it.monthKey == "2026-09" }
    assertEquals(8000.0, sepBucket.totalRevenue, 0.001)

    val octBucket = analytics.monthlyBuckets.first { it.monthKey == "2026-10" }
    // In October: 4000 + 6000 = 10000 non-cancelled
    assertEquals(10000.0, octBucket.totalRevenue, 0.001)
    assertEquals(3, octBucket.orders.size)

    // Test JSON payload for D3 / Recharts
    val json = com.example.ui.screens.buildRevenueJsonPayload(analytics, isEnglish = true, orders)
    org.junit.Assert.assertTrue(json.contains("\"totalRevenue\":23000.0"))
    org.junit.Assert.assertTrue(json.contains("\"verifiedRevenue\":17000.0"))
    org.junit.Assert.assertTrue(json.contains("\"months\":["))
    org.junit.Assert.assertTrue(json.contains("\"statusBreakdown\":["))
    org.junit.Assert.assertTrue(json.contains("\"paymentModeBreakdown\":["))
  }
}

