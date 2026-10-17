package com.example

import com.example.data.FarmRepository
import com.example.model.BankTransferDetails
import com.example.model.CartItem
import com.example.model.FarmOfficialInfo
import com.example.model.OrderStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testBankOfficialDetailsMatchUserRequirement() {
        assertEquals("09610201478560000001", FarmOfficialInfo.BANK_ACCOUNT_NO)
        assertEquals("Lalitpur United Agro Farm Pvt. Ltd.", FarmOfficialInfo.BANK_ACCOUNT_NAME_EN)
        assertEquals("ललितपुर युनाइटेड एग्रो फार्म प्रा. लि.", FarmOfficialInfo.BANK_ACCOUNT_NAME_NP)
        assertTrue(FarmOfficialInfo.BANK_NAME_NP.contains("मुक्तिनाथ विकास बैंक"))
        assertTrue(FarmOfficialInfo.BANK_NAME_EN.contains("Muktinath Bikas Bank"))
    }

    @Test
    fun testProductsLoadedSuccessfully() {
        val repository = FarmRepository()
        val products = repository.products.value
        assertTrue("Products list should not be empty", products.isNotEmpty())

        val milkProduct = products.firstOrNull { it.sku == "UAF-MILK-COW" }
        assertNotNull("Cow milk product should exist", milkProduct)
        assertEquals(110.0, milkProduct!!.retailPrice, 0.01)
    }

    @Test
    fun testStockAdjustment() {
        val repository = FarmRepository()
        val firstProduct = repository.products.value.first()
        val originalStock = firstProduct.stockQuantity

        repository.adjustStock(firstProduct.id, 10.0)
        val updatedStock = repository.products.value.first { it.id == firstProduct.id }.stockQuantity
        assertEquals(originalStock + 10.0, updatedStock, 0.01)

        repository.adjustStock(firstProduct.id, -5.0)
        val decreasedStock = repository.products.value.first { it.id == firstProduct.id }.stockQuantity
        assertEquals(originalStock + 5.0, decreasedStock, 0.01)
    }

    @Test
    fun testDirectBankTransferSubmission() {
        val repository = FarmRepository()
        val order = repository.submitDirectBankTransferPayment(
            customerName = "सरोज पौडेल (Saroj Poudel)",
            customerPhone = "9841000001",
            deliveryAddress = "ललितपुर",
            senderBankName = "मुक्तिनाथ विकास बैंक",
            senderAccountName = "Saroj Poudel",
            transactionRef = "MNB-TXN-998822",
            amount = 3500.0,
            remarks = "दुग्धजन्य परिकार तथा घिउ खरिद"
        )

        assertNotNull(order)
        assertEquals(3500.0, order.totalAmount, 0.01)
        assertEquals(OrderStatus.PENDING_VERIFICATION, order.status)
        assertEquals("MNB-TXN-998822", order.bankTransfer?.transactionRefNumber)

        repository.updateOrderStatus(order.orderId, OrderStatus.VERIFIED)
        val verifiedOrder = repository.orders.value.first { it.orderId == order.orderId }
        assertEquals(OrderStatus.VERIFIED, verifiedOrder.status)
    }

    @Test
    fun testOrderCreationDeductsStock() {
        val repository = FarmRepository()
        val product = repository.products.value.first()
        val initialStock = product.stockQuantity

        val cartItem = CartItem(product = product, quantity = 5.0)
        val order = repository.createOrder(
            customerName = "हरि बहादुर",
            customerPhone = "9851000002",
            deliveryAddress = "पाटन",
            cartItems = listOf(cartItem),
            bankTransfer = BankTransferDetails(
                senderBankName = "मुक्तिनाथ विकास बैंक",
                senderAccountName = "Hari Bahadur",
                transactionRefNumber = "MNB-554411",
                transferredAmount = cartItem.totalAmount,
                transferDate = "2026-10-14"
            )
        )

        val updatedProduct = repository.products.value.first { it.id == product.id }
        assertEquals(initialStock - 5.0, updatedProduct.stockQuantity, 0.01)
        assertEquals(cartItem.totalAmount, order.totalAmount, 0.01)
    }

    @Test
    fun testBalanceSheetAccountingEquation() {
        val balanceSheet = com.example.model.BalanceSheetSummary(
            asOfDate = "2026-10-14",
            cashInHand = 100000.0,
            bankBalanceMuktinath = 400000.0,
            esewaBalance = 50000.0,
            accountsReceivable = 50000.0,
            inventoryStockValue = 200000.0,
            fixedAssetsEquipment = 600000.0,
            fixedAssetsLandShed = 1000000.0,
            livestockValuation = 800000.0,
            accountsPayable = 60000.0,
            accruedSalaries = 40000.0,
            shortTermLoans = 200000.0,
            ownerCapital = 2800000.0,
            netFarmProfit = 100000.0
        )

        // Assets = 100k + 400k + 50k + 50k + 200k + 600k + 1000k + 800k = 3,200,000
        assertEquals(800000.0, balanceSheet.totalCurrentAssets, 0.01)
        assertEquals(2400000.0, balanceSheet.totalFixedAssets, 0.01)
        assertEquals(3200000.0, balanceSheet.totalAssets, 0.01)

        // Liabilities = 60k + 40k + 200k = 300,000
        assertEquals(300000.0, balanceSheet.totalLiabilities, 0.01)

        // Equity = 2,800,000 + 100,000 = 2,900,000
        assertEquals(2900000.0, balanceSheet.totalEquity, 0.01)

        // Equation balanced: 3,200,000 == 300,000 + 2,900,000
        assertTrue("Accounting equation must balance", balanceSheet.isBalanced)
    }

    @Test
    fun testMainNavigationBarSections() {
        val primaryScreens = listOf(
            com.example.ui.viewmodel.FarmScreen.INVENTORY,
            com.example.ui.viewmodel.FarmScreen.ORDERS,
            com.example.ui.viewmodel.FarmScreen.PRICE_LIST
        )
        assertEquals(3, primaryScreens.size)
        assertEquals("Inventory", com.example.ui.viewmodel.FarmScreen.INVENTORY.labelEn)
        assertEquals("Orders & Receipts", com.example.ui.viewmodel.FarmScreen.ORDERS.labelEn)
        assertEquals("Price List", com.example.ui.viewmodel.FarmScreen.PRICE_LIST.labelEn)
    }
}

