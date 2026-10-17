package com.example.data

import android.content.Context
import com.example.R
import com.example.model.BankTransferDetails
import com.example.model.CartItem
import com.example.model.FarmOrder
import com.example.model.OrderStatus
import com.example.model.PriceUnit
import com.example.model.Product
import com.example.model.AttendanceRecord
import com.example.model.AttendanceStatus
import com.example.model.Employee
import com.example.model.FarmBalanceSheetLedger
import com.example.model.FarmTransaction
import com.example.model.ProductCategory
import com.example.model.TransactionCategory
import com.example.model.TransactionPaymentMode
import com.example.model.TransactionType
import com.example.model.UserProfile
import com.example.model.UserRole
import com.example.model.DefaultOfficialDocuments
import com.example.model.DocumentCategory
import com.example.model.DocumentStatus
import com.example.model.OfficialDocument
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class FirestoreFarmRepository(
    private val db: FirebaseFirestore
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // Real-time observation of products catalog
    fun observeProducts(): Flow<List<Product>> {
        val path = "products"
        return db.collection(path)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        val categoryStr = doc.getString("category") ?: "ALL"
                        val category = try {
                            ProductCategory.valueOf(categoryStr)
                        } catch (e: Exception) {
                            ProductCategory.DAIRY
                        }

                        val unitStr = doc.getString("unit") ?: "KG"
                        val unit = try {
                            PriceUnit.valueOf(unitStr)
                        } catch (e: Exception) {
                            PriceUnit.KG
                        }

                        Product(
                            id = doc.id,
                            nameNp = doc.getString("nameNp") ?: "",
                            nameEn = doc.getString("nameEn") ?: "",
                            category = category,
                            retailPrice = doc.getDouble("retailPrice") ?: 0.0,
                            wholesalePrice = doc.getDouble("wholesalePrice") ?: 0.0,
                            unit = unit,
                            stockQuantity = doc.getDouble("stockQuantity") ?: 0.0,
                            minStockThreshold = doc.getDouble("minStockThreshold") ?: 10.0,
                            descriptionNp = doc.getString("descriptionNp") ?: "",
                            descriptionEn = doc.getString("descriptionEn") ?: "",
                            sku = doc.getString("sku") ?: "UAF-PROD",
                            isOrganicCertified = doc.getBoolean("isOrganicCertified") ?: true
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    // Real-time observation of user orders
    fun observeUserOrders(userId: String): Flow<List<FarmOrder>> {
        val path = "orders"
        return db.collection(path)
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        val statusStr = doc.getString("status") ?: "PENDING_VERIFICATION"
                        val status = try {
                            OrderStatus.valueOf(statusStr)
                        } catch (e: Exception) {
                            OrderStatus.PENDING_VERIFICATION
                        }

                        val bankTransferMap = doc.get("bankTransfer") as? Map<String, Any>
                        val bankTransfer = bankTransferMap?.let { map ->
                            BankTransferDetails(
                                senderBankName = map["senderBankName"] as? String ?: "",
                                senderAccountName = map["senderAccountName"] as? String ?: "",
                                transactionRefNumber = map["transactionRefNumber"] as? String ?: "",
                                transferredAmount = (map["transferredAmount"] as? Number)?.toDouble() ?: 0.0,
                                transferDate = map["transferDate"] as? String ?: "",
                                remarks = map["remarks"] as? String ?: ""
                            )
                        }

                        val itemsListRaw = doc.get("items") as? List<Map<String, Any>>
                        val items = itemsListRaw?.mapNotNull { itemMap ->
                            val prodMap = itemMap["product"] as? Map<String, Any> ?: return@mapNotNull null
                            val catStr = prodMap["category"] as? String ?: "DAIRY"
                            val unitStr = prodMap["unit"] as? String ?: "KG"
                            val product = Product(
                                id = prodMap["id"] as? String ?: "",
                                nameNp = prodMap["nameNp"] as? String ?: "",
                                nameEn = prodMap["nameEn"] as? String ?: "",
                                category = try { ProductCategory.valueOf(catStr) } catch (_: Exception) { ProductCategory.DAIRY },
                                retailPrice = (prodMap["retailPrice"] as? Number)?.toDouble() ?: 0.0,
                                wholesalePrice = (prodMap["wholesalePrice"] as? Number)?.toDouble() ?: 0.0,
                                unit = try { PriceUnit.valueOf(unitStr) } catch (_: Exception) { PriceUnit.KG },
                                stockQuantity = (prodMap["stockQuantity"] as? Number)?.toDouble() ?: 0.0,
                                minStockThreshold = (prodMap["minStockThreshold"] as? Number)?.toDouble() ?: 10.0,
                                descriptionNp = prodMap["descriptionNp"] as? String ?: "",
                                descriptionEn = prodMap["descriptionEn"] as? String ?: "",
                                sku = prodMap["sku"] as? String ?: "",
                                isOrganicCertified = prodMap["isOrganicCertified"] as? Boolean ?: true
                            )
                            val qty = (itemMap["quantity"] as? Number)?.toDouble() ?: 1.0
                            CartItem(product, qty)
                        } ?: emptyList()

                        FarmOrder(
                            orderId = doc.id,
                            customerName = doc.getString("customerName") ?: "",
                            customerPhone = doc.getString("customerPhone") ?: "",
                            deliveryAddress = doc.getString("deliveryAddress") ?: "",
                            items = items,
                            totalAmount = doc.getDouble("totalAmount") ?: 0.0,
                            orderDate = doc.getString("orderDate") ?: "",
                            status = status,
                            bankTransfer = bankTransfer,
                            notes = doc.getString("notes") ?: ""
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun addProduct(product: Product) {
        val uid = requireUserId()
        val docId = if (product.id.isNotBlank() && !product.id.startsWith("PROD-")) product.id else "prod_${UUID.randomUUID()}"
        val docRef = db.collection("products").document(docId)

        val payload = mapOf(
            "nameNp" to product.nameNp,
            "nameEn" to product.nameEn,
            "category" to product.category.name,
            "retailPrice" to product.retailPrice,
            "wholesalePrice" to product.wholesalePrice,
            "unit" to product.unit.name,
            "stockQuantity" to product.stockQuantity,
            "minStockThreshold" to product.minStockThreshold,
            "descriptionNp" to product.descriptionNp,
            "descriptionEn" to product.descriptionEn,
            "sku" to product.sku,
            "isOrganicCertified" to product.isOrganicCertified,
            "userId" to uid,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.set(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateStock(productId: String, newStock: Double) {
        val uid = requireUserId()
        val docRef = db.collection("products").document(productId)
        try {
            docRef.update(
                mapOf(
                    "stockQuantity" to newStock.coerceAtLeast(0.0),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun updateProductPriceAndStock(productId: String, newRetailPrice: Double, newWholesalePrice: Double, newStock: Double) {
        val uid = requireUserId()
        val docRef = db.collection("products").document(productId)
        try {
            docRef.update(
                mapOf(
                    "retailPrice" to newRetailPrice.coerceAtLeast(0.0),
                    "wholesalePrice" to newWholesalePrice.coerceAtLeast(0.0),
                    "stockQuantity" to newStock.coerceAtLeast(0.0),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteProduct(productId: String) {
        val uid = requireUserId()
        val docRef = db.collection("products").document(productId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    suspend fun createOrder(
        customerName: String,
        customerPhone: String,
        deliveryAddress: String,
        cartItems: List<CartItem>,
        bankTransfer: BankTransferDetails?,
        notes: String
    ): FarmOrder {
        val uid = requireUserId()
        val totalAmount = cartItems.sumOf { it.totalAmount }
        val orderId = "ord_${UUID.randomUUID().toString().take(8)}"
        val docRef = db.collection("orders").document(orderId)

        val formatter = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
        val dateString = formatter.format(Date())

        val itemsPayload = cartItems.map { item ->
            mapOf(
                "quantity" to item.quantity,
                "totalAmount" to item.totalAmount,
                "product" to mapOf(
                    "id" to item.product.id,
                    "nameNp" to item.product.nameNp,
                    "nameEn" to item.product.nameEn,
                    "category" to item.product.category.name,
                    "retailPrice" to item.product.retailPrice,
                    "wholesalePrice" to item.product.wholesalePrice,
                    "unit" to item.product.unit.name,
                    "sku" to item.product.sku,
                    "stockQuantity" to item.product.stockQuantity
                )
            )
        }

        val bankTransferMap = bankTransfer?.let {
            mapOf(
                "senderBankName" to it.senderBankName,
                "senderAccountName" to it.senderAccountName,
                "transactionRefNumber" to it.transactionRefNumber,
                "transferredAmount" to it.transferredAmount,
                "transferDate" to it.transferDate,
                "remarks" to it.remarks
            )
        }

        val payload = mapOf(
            "userId" to uid,
            "customerName" to customerName,
            "customerPhone" to customerPhone,
            "deliveryAddress" to deliveryAddress,
            "totalAmount" to totalAmount,
            "orderDate" to dateString,
            "status" to OrderStatus.PENDING_VERIFICATION.name,
            "notes" to notes,
            "items" to itemsPayload,
            "bankTransfer" to (bankTransferMap ?: emptyMap<String, Any>()),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.set(payload).await()
            for (cartItem in cartItems) {
                try {
                    if (cartItem.product.id.isNotBlank()) {
                        val pRef = db.collection("products").document(cartItem.product.id)
                        val newStock = (cartItem.product.stockQuantity - cartItem.quantity).coerceAtLeast(0.0)
                        pRef.update(
                            mapOf(
                                "stockQuantity" to newStock,
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        ).await()
                    }
                } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }

        return FarmOrder(
            orderId = orderId,
            customerName = customerName,
            customerPhone = customerPhone,
            deliveryAddress = deliveryAddress,
            items = cartItems,
            totalAmount = totalAmount,
            orderDate = dateString,
            status = OrderStatus.PENDING_VERIFICATION,
            bankTransfer = bankTransfer,
            notes = notes
        )
    }

    suspend fun seedInitialProductsIfEmpty(initialProducts: List<Product>) {
        val uid = auth.currentUser?.uid ?: return
        try {
            val existing = db.collection("products").limit(1).get().await()
            if (existing.isEmpty) {
                initialProducts.forEach { prod ->
                    addProduct(prod)
                }
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, "products")
        }
    }

    suspend fun saveOrUpdateUserProfile(role: UserRole = UserRole.STAFF): UserProfile? {
        val user = auth.currentUser ?: return null
        val uid = user.uid
        val docRef = db.collection("users").document(uid)
        val name = user.displayName?.ifBlank { null } ?: ("Farmer " + uid.take(5))
        val email = user.email ?: ""
        val phone = user.phoneNumber ?: ""

        try {
            val existing = docRef.get().await()
            if (!existing.exists()) {
                val payload = mutableMapOf<String, Any>(
                    "userId" to uid,
                    "displayName" to name,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "role" to role.name
                )
                if (email.isNotBlank()) payload["email"] = email
                if (phone.isNotBlank()) payload["phone"] = phone

                docRef.set(payload).await()
            } else {
                val updates = mutableMapOf<String, Any>(
                    "displayName" to name,
                    "role" to role.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (email.isNotBlank()) updates["email"] = email
                if (phone.isNotBlank()) updates["phone"] = phone

                docRef.update(updates).await()
            }
            return UserProfile(
                userId = uid,
                displayName = name,
                email = email,
                phone = phone,
                role = role
            )
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            return UserProfile(
                userId = uid,
                displayName = name,
                email = email,
                phone = phone,
                role = role
            )
        }
    }

    suspend fun fetchUserProfile(): UserProfile? {
        val uid = auth.currentUser?.uid ?: return null
        val docRef = db.collection("users").document(uid)
        return try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                val roleStr = snapshot.getString("role") ?: UserRole.STAFF.name
                val role = try { UserRole.valueOf(roleStr) } catch (_: Exception) { UserRole.STAFF }
                UserProfile(
                    userId = uid,
                    displayName = snapshot.getString("displayName") ?: "Farmer",
                    email = snapshot.getString("email") ?: "",
                    phone = snapshot.getString("phone") ?: "",
                    role = role
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // --- EMPLOYEES ---
    fun observeEmployees(): Flow<List<Employee>> {
        val path = "employees"
        return db.collection(path)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        Employee(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            role = doc.getString("role") ?: "Farm Staff",
                            phone = doc.getString("phone") ?: "",
                            dailyWage = doc.getDouble("dailyWage") ?: 800.0,
                            monthlySalary = doc.getDouble("monthlySalary") ?: 24000.0,
                            joinDate = doc.getString("joinDate") ?: "2024-01-01",
                            isActive = doc.getBoolean("isActive") ?: true
                        )
                    } catch (e: Exception) {
                        null
                    }
                }.sortedBy { it.name }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun addEmployee(employee: Employee) {
        val uid = requireUserId()
        val docId = if (employee.id.isNotBlank() && !employee.id.startsWith("TEMP_")) employee.id else "emp_${UUID.randomUUID().toString().take(8)}"
        val docRef = db.collection("employees").document(docId)
        val payload = mapOf(
            "name" to employee.name,
            "role" to employee.role,
            "phone" to employee.phone,
            "dailyWage" to employee.dailyWage,
            "monthlySalary" to employee.monthlySalary,
            "joinDate" to employee.joinDate,
            "isActive" to employee.isActive,
            "userId" to uid,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateEmployee(employee: Employee) {
        val uid = requireUserId()
        val docRef = db.collection("employees").document(employee.id)
        try {
            docRef.update(
                mapOf(
                    "name" to employee.name,
                    "role" to employee.role,
                    "phone" to employee.phone,
                    "dailyWage" to employee.dailyWage,
                    "monthlySalary" to employee.monthlySalary,
                    "isActive" to employee.isActive,
                    "joinDate" to employee.joinDate,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteEmployee(employeeId: String) {
        val docRef = db.collection("employees").document(employeeId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- ATTENDANCE ---
    fun observeDailyAttendance(date: String): Flow<List<AttendanceRecord>> {
        val path = "attendances"
        return db.collection(path)
            .whereEqualTo("date", date)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        val statusStr = doc.getString("status") ?: "PRESENT"
                        val status = try { AttendanceStatus.valueOf(statusStr) } catch (_: Exception) { AttendanceStatus.PRESENT }
                        AttendanceRecord(
                            id = doc.id,
                            employeeId = doc.getString("employeeId") ?: "",
                            employeeName = doc.getString("employeeName") ?: "",
                            date = doc.getString("date") ?: date,
                            monthYear = doc.getString("monthYear") ?: date.take(7),
                            status = status,
                            checkInTime = doc.getString("checkInTime") ?: "07:00 AM",
                            checkOutTime = doc.getString("checkOutTime") ?: "05:00 PM",
                            remarks = doc.getString("remarks") ?: "",
                            recordedBy = doc.getString("recordedBy") ?: ""
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    fun observeMonthlyAttendance(monthYear: String): Flow<List<AttendanceRecord>> {
        val path = "attendances"
        return db.collection(path)
            .whereEqualTo("monthYear", monthYear)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        val statusStr = doc.getString("status") ?: "PRESENT"
                        val status = try { AttendanceStatus.valueOf(statusStr) } catch (_: Exception) { AttendanceStatus.PRESENT }
                        AttendanceRecord(
                            id = doc.id,
                            employeeId = doc.getString("employeeId") ?: "",
                            employeeName = doc.getString("employeeName") ?: "",
                            date = doc.getString("date") ?: "",
                            monthYear = doc.getString("monthYear") ?: monthYear,
                            status = status,
                            checkInTime = doc.getString("checkInTime") ?: "07:00 AM",
                            checkOutTime = doc.getString("checkOutTime") ?: "05:00 PM",
                            remarks = doc.getString("remarks") ?: "",
                            recordedBy = doc.getString("recordedBy") ?: ""
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun recordAttendance(record: AttendanceRecord) {
        val uid = requireUserId()
        val docId = if (record.id.isNotBlank() && !record.id.startsWith("TEMP_")) record.id else "att_${record.employeeId}_${record.date.replace("-", "")}"
        val docRef = db.collection("attendances").document(docId)
        val payload = mapOf(
            "employeeId" to record.employeeId,
            "employeeName" to record.employeeName,
            "date" to record.date,
            "monthYear" to record.monthYear,
            "status" to record.status.name,
            "checkInTime" to record.checkInTime,
            "checkOutTime" to record.checkOutTime,
            "remarks" to record.remarks,
            "recordedBy" to record.recordedBy,
            "userId" to uid,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    // --- TRANSACTIONS (INCOME & EXPENSE) ---
    fun observeTransactions(): Flow<List<FarmTransaction>> {
        val path = "transactions"
        return db.collection(path)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        val typeStr = doc.getString("type") ?: "INCOME"
                        val type = try { TransactionType.valueOf(typeStr) } catch (_: Exception) { TransactionType.INCOME }
                        val catStr = doc.getString("category") ?: "OTHER_INCOME"
                        val category = try { TransactionCategory.valueOf(catStr) } catch (_: Exception) {
                            if (type == TransactionType.INCOME) TransactionCategory.OTHER_INCOME else TransactionCategory.MISC_EXPENSE
                        }
                        val payStr = doc.getString("paymentMode") ?: "CASH"
                        val paymentMode = try { TransactionPaymentMode.valueOf(payStr) } catch (_: Exception) { TransactionPaymentMode.CASH }

                        FarmTransaction(
                            id = doc.id,
                            type = type,
                            category = category,
                            amount = doc.getDouble("amount") ?: 0.0,
                            date = doc.getString("date") ?: "",
                            monthYear = doc.getString("monthYear") ?: "",
                            paymentMode = paymentMode,
                            partyName = doc.getString("partyName") ?: "",
                            referenceNo = doc.getString("referenceNo") ?: "",
                            description = doc.getString("description") ?: "",
                            recordedBy = doc.getString("recordedBy") ?: ""
                        )
                    } catch (e: Exception) {
                        null
                    }
                }.sortedByDescending { it.date }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun addTransaction(transaction: FarmTransaction) {
        val uid = requireUserId()
        val docId = if (transaction.id.isNotBlank()) transaction.id else "tx_${UUID.randomUUID().toString().take(8)}"
        val docRef = db.collection("transactions").document(docId)
        val payload = mapOf(
            "type" to transaction.type.name,
            "category" to transaction.category.name,
            "amount" to transaction.amount,
            "date" to transaction.date,
            "monthYear" to transaction.monthYear,
            "paymentMode" to transaction.paymentMode.name,
            "partyName" to transaction.partyName,
            "referenceNo" to transaction.referenceNo,
            "description" to transaction.description,
            "recordedBy" to transaction.recordedBy,
            "userId" to uid,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteTransaction(transactionId: String) {
        val docRef = db.collection("transactions").document(transactionId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- BALANCE SHEET LEDGER ---
    fun observeBalanceSheetLedger(): Flow<FarmBalanceSheetLedger> {
        val path = "balance_sheet"
        val docRef = db.collection(path).document("primary_ledger")
        return docRef.snapshots()
            .map { doc ->
                if (doc.exists()) {
                    FarmBalanceSheetLedger(
                        id = doc.id,
                        cashInHand = doc.getDouble("cashInHand") ?: 85000.0,
                        bankBalanceMuktinath = doc.getDouble("bankBalanceMuktinath") ?: 425000.0,
                        esewaBalance = doc.getDouble("esewaBalance") ?: 35000.0,
                        accountsReceivable = doc.getDouble("accountsReceivable") ?: 48000.0,
                        fixedAssetsEquipment = doc.getDouble("fixedAssetsEquipment") ?: 650000.0,
                        fixedAssetsLandShed = doc.getDouble("fixedAssetsLandShed") ?: 1200000.0,
                        livestockValuation = doc.getDouble("livestockValuation") ?: 850000.0,
                        accountsPayable = doc.getDouble("accountsPayable") ?: 65000.0,
                        accruedSalaries = doc.getDouble("accruedSalaries") ?: 45000.0,
                        shortTermLoans = doc.getDouble("shortTermLoans") ?: 250000.0,
                        ownerCapital = doc.getDouble("ownerCapital") ?: 2500000.0,
                        asOfDate = doc.getString("asOfDate") ?: "2026-10-14"
                    )
                } else {
                    FarmBalanceSheetLedger()
                }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.GET, docRef.path)
                emit(FarmBalanceSheetLedger())
            }
    }

    suspend fun updateBalanceSheetLedger(ledger: FarmBalanceSheetLedger) {
        val uid = requireUserId()
        val docRef = db.collection("balance_sheet").document("primary_ledger")
        val payload = mapOf(
            "cashInHand" to ledger.cashInHand,
            "bankBalanceMuktinath" to ledger.bankBalanceMuktinath,
            "esewaBalance" to ledger.esewaBalance,
            "accountsReceivable" to ledger.accountsReceivable,
            "fixedAssetsEquipment" to ledger.fixedAssetsEquipment,
            "fixedAssetsLandShed" to ledger.fixedAssetsLandShed,
            "livestockValuation" to ledger.livestockValuation,
            "accountsPayable" to ledger.accountsPayable,
            "accruedSalaries" to ledger.accruedSalaries,
            "shortTermLoans" to ledger.shortTermLoans,
            "ownerCapital" to ledger.ownerCapital,
            "asOfDate" to ledger.asOfDate,
            "userId" to uid,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- OFFICIAL DOCUMENTS & PERMITS DATABASE ---
    fun observeDocuments(): Flow<List<OfficialDocument>> {
        val path = "documents"
        return db.collection(path)
            .snapshots()
            .map { snapshot ->
                if (snapshot.isEmpty) {
                    emptyList()
                } else {
                    snapshot.documents.mapNotNull { doc ->
                        try {
                            val catStr = doc.getString("category") ?: "REGISTRATION"
                            val category = try { DocumentCategory.valueOf(catStr) } catch (_: Exception) { DocumentCategory.REGISTRATION }
                            val statusStr = doc.getString("status") ?: "ACTIVE"
                            val status = try { DocumentStatus.valueOf(statusStr) } catch (_: Exception) { DocumentStatus.ACTIVE }
                            @Suppress("UNCHECKED_CAST")
                            val pointsNp = (doc.get("summaryPointsNp") as? List<String>) ?: emptyList()
                            @Suppress("UNCHECKED_CAST")
                            val pointsEn = (doc.get("summaryPointsEn") as? List<String>) ?: emptyList()

                            OfficialDocument(
                                id = doc.id,
                                titleNp = doc.getString("titleNp") ?: "",
                                titleEn = doc.getString("titleEn") ?: "",
                                documentNumber = doc.getString("documentNumber") ?: "",
                                category = category,
                                issuingAuthorityNp = doc.getString("issuingAuthorityNp") ?: "",
                                issuingAuthorityEn = doc.getString("issuingAuthorityEn") ?: "",
                                issueDate = doc.getString("issueDate") ?: "",
                                expiryDate = doc.getString("expiryDate") ?: "स्थायी / आजीवन",
                                status = status,
                                isVerified = doc.getBoolean("isVerified") ?: true,
                                digitalRefCode = doc.getString("digitalRefCode") ?: "",
                                summaryPointsNp = pointsNp,
                                summaryPointsEn = pointsEn,
                                contactOfficer = doc.getString("contactOfficer") ?: "",
                                notes = doc.getString("notes") ?: "",
                                userId = doc.getString("userId") ?: "",
                                createdAt = doc.getString("createdAt") ?: "",
                                updatedAt = doc.getString("updatedAt") ?: ""
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun addDocument(document: OfficialDocument) {
        val uid = requireUserId()
        val docId = if (document.id.isNotBlank() && !document.id.startsWith("TEMP_")) document.id else "doc_${UUID.randomUUID().toString().take(8)}"
        val docRef = db.collection("documents").document(docId)
        val payload = mapOf(
            "titleNp" to document.titleNp,
            "titleEn" to document.titleEn,
            "documentNumber" to document.documentNumber,
            "category" to document.category.name,
            "issuingAuthorityNp" to document.issuingAuthorityNp,
            "issuingAuthorityEn" to document.issuingAuthorityEn,
            "issueDate" to document.issueDate,
            "expiryDate" to document.expiryDate,
            "status" to document.status.name,
            "isVerified" to document.isVerified,
            "digitalRefCode" to document.digitalRefCode,
            "summaryPointsNp" to document.summaryPointsNp,
            "summaryPointsEn" to document.summaryPointsEn,
            "contactOfficer" to document.contactOfficer,
            "notes" to document.notes,
            "userId" to uid,
            "createdAt" to SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
            "updatedAt" to SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        )
        try {
            docRef.set(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateDocument(document: OfficialDocument) {
        requireUserId()
        val docRef = db.collection("documents").document(document.id)
        val payload = mapOf(
            "titleNp" to document.titleNp,
            "titleEn" to document.titleEn,
            "documentNumber" to document.documentNumber,
            "category" to document.category.name,
            "issuingAuthorityNp" to document.issuingAuthorityNp,
            "issuingAuthorityEn" to document.issuingAuthorityEn,
            "issueDate" to document.issueDate,
            "expiryDate" to document.expiryDate,
            "status" to document.status.name,
            "isVerified" to document.isVerified,
            "digitalRefCode" to document.digitalRefCode,
            "summaryPointsNp" to document.summaryPointsNp,
            "summaryPointsEn" to document.summaryPointsEn,
            "contactOfficer" to document.contactOfficer,
            "notes" to document.notes,
            "updatedAt" to SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        )
        try {
            docRef.update(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteDocument(documentId: String) {
        requireUserId()
        val docRef = db.collection("documents").document(documentId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    suspend fun seedInitialEmployeesAndFinanceIfEmpty() {
        val uid = auth.currentUser?.uid ?: return
        try {
            val existingEmployees = db.collection("employees").limit(1).get().await()
            if (existingEmployees.isEmpty) {
                val initialStaff = listOf(
                    Employee("emp_1", "राम बहादुर थापा (Ram Bahadur Thapa)", "दूध तथा गाईगोठ व्यवस्थापक (Dairy Manager)", "9841123456", 900.0, 27000.0, "2023-03-15", true),
                    Employee("emp_2", "सीता शर्मा (Sita Sharma)", "कुखुरा तथा अण्डा रेखदेख (Poultry Attendant)", "9841654321", 850.0, 25500.0, "2023-06-01", true),
                    Employee("emp_3", "कृष्ण बिक (Krishna BK)", "अग्र्यानिक तरकारी तथा टनेल (Vegetable Specialist)", "9841789012", 800.0, 24000.0, "2023-09-10", true),
                    Employee("emp_4", "हरि प्रसाद आचार्य (Hari Acharya)", "ढुवानी तथा डेलिभरी चालक (Logistics & Driver)", "9841345678", 850.0, 25500.0, "2024-01-05", true),
                    Employee("emp_5", "कमला तामाङ (Kamala Tamang)", "जैविक मल तथा सरसफाइ (Bio-Fertilizer & San.)", "9841901234", 750.0, 22500.0, "2024-02-15", true)
                )
                for (emp in initialStaff) {
                    addEmployee(emp)
                }

                // Seed today's initial attendance
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val monthYear = today.take(7)
                for (emp in initialStaff) {
                    recordAttendance(
                        AttendanceRecord(
                            id = "att_${emp.id}_${today.replace("-", "")}",
                            employeeId = emp.id,
                            employeeName = emp.name,
                            date = today,
                            monthYear = monthYear,
                            status = AttendanceStatus.PRESENT,
                            checkInTime = "07:00 AM",
                            checkOutTime = "05:00 PM",
                            remarks = "बिहानी सिफ्ट नियमित उपस्थिति",
                            recordedBy = "United Agro Admin"
                        )
                    )
                }
            }

            val existingTxCount = db.collection("transactions").limit(6).get().await().size()
            if (existingTxCount < 4) {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val monthYear = today.take(7)
                val initialTx = listOf(
                    FarmTransaction(
                        id = "tx_init_1",
                        type = TransactionType.INCOME,
                        category = TransactionCategory.MILK_SALES,
                        amount = 14500.0,
                        date = "$monthYear-01",
                        monthYear = monthYear,
                        paymentMode = TransactionPaymentMode.BANK_TRANSFER,
                        partyName = "पाटन डेरी को-अपरेटिभ",
                        referenceNo = "MNB-TX-9901",
                        description = "१३० लिटर गाईको दूध संकलन भुक्तानी"
                    ),
                    FarmTransaction(
                        id = "tx_init_2",
                        type = TransactionType.EXPENSE,
                        category = TransactionCategory.FEED_FORAGE,
                        amount = 7200.0,
                        date = "$monthYear-01",
                        monthYear = monthYear,
                        paymentMode = TransactionPaymentMode.BANK_TRANSFER,
                        partyName = "अन्नपूर्णा फिड इन्डस्ट्रिज",
                        referenceNo = "MNB-BILL-331",
                        description = "गाईको सन्तुलित दाना ४ बोरा"
                    ),
                    FarmTransaction(
                        id = "tx_init_3",
                        type = TransactionType.INCOME,
                        category = TransactionCategory.POULTRY_EGGS,
                        amount = 9600.0,
                        date = "$monthYear-03",
                        monthYear = monthYear,
                        paymentMode = TransactionPaymentMode.CASH,
                        partyName = "जावलाखेल लोकल मार्ट",
                        referenceNo = "INV-042",
                        description = "२४ क्रेट ताजा लोकल अण्डा बिक्री"
                    ),
                    FarmTransaction(
                        id = "tx_init_4",
                        type = TransactionType.EXPENSE,
                        category = TransactionCategory.VET_MEDICINE,
                        amount = 2100.0,
                        date = "$monthYear-03",
                        monthYear = monthYear,
                        paymentMode = TransactionPaymentMode.ESEWA,
                        partyName = "भ्याली भेट क्लिनिक",
                        referenceNo = "ESW-VET-09",
                        description = "पशु भ्याक्सिन तथा जुकाको औषधि"
                    ),
                    FarmTransaction(
                        id = "tx_init_5",
                        type = TransactionType.INCOME,
                        category = TransactionCategory.VEGGIE_SALES,
                        amount = 18200.0,
                        date = "$monthYear-05",
                        monthYear = monthYear,
                        paymentMode = TransactionPaymentMode.BANK_TRANSFER,
                        partyName = "अग्र्यानिक बजार ललितपुर",
                        referenceNo = "MNB-TX-102",
                        description = "डल्ले खुर्सानी र ताजा तरकारी संकलन"
                    ),
                    FarmTransaction(
                        id = "tx_init_6",
                        type = TransactionType.EXPENSE,
                        category = TransactionCategory.FUEL_TRANSPORT,
                        amount = 8400.0,
                        date = "$monthYear-05",
                        monthYear = monthYear,
                        paymentMode = TransactionPaymentMode.CASH,
                        partyName = "साझा यातायात ढुवानी",
                        referenceNo = "BILL-098",
                        description = "कृषि मल तथा घाँस ढुवानी भाडा"
                    ),
                    FarmTransaction(
                        id = "tx_init_7",
                        type = TransactionType.INCOME,
                        category = TransactionCategory.LIVESTOCK_SALES,
                        amount = 28000.0,
                        date = "$monthYear-07",
                        monthYear = monthYear,
                        paymentMode = TransactionPaymentMode.BANK_TRANSFER,
                        partyName = "भक्तपुर मिट सेन्टर",
                        referenceNo = "MNB-TX-440",
                        description = "बोयर क्रस जीवित खसी बिक्री"
                    ),
                    FarmTransaction(
                        id = "tx_init_8",
                        type = TransactionType.EXPENSE,
                        category = TransactionCategory.MAINTENANCE,
                        amount = 4500.0,
                        date = "$monthYear-07",
                        monthYear = monthYear,
                        paymentMode = TransactionPaymentMode.CASH,
                        partyName = "ललितपुर वेल्डिङ वर्कसप",
                        referenceNo = "REC-44",
                        description = "गाईगोठ पाइपलाइन र टनेल मर्मत"
                    ),
                    FarmTransaction(
                        id = "tx_init_9",
                        type = TransactionType.INCOME,
                        category = TransactionCategory.MILK_SALES,
                        amount = 14200.0,
                        date = "$monthYear-09",
                        monthYear = monthYear,
                        paymentMode = TransactionPaymentMode.BANK_TRANSFER,
                        partyName = "पाटन डेरी को-अपरेटिभ",
                        referenceNo = "MNB-TX-994",
                        description = "दैनिक दूध संकलन भुक्तानी"
                    ),
                    FarmTransaction(
                        id = "tx_init_10",
                        type = TransactionType.EXPENSE,
                        category = TransactionCategory.ELECTRICITY_WATER,
                        amount = 9800.0,
                        date = "$monthYear-09",
                        monthYear = monthYear,
                        paymentMode = TransactionPaymentMode.ESEWA,
                        partyName = "नेपाल विद्युत प्राधिकरण",
                        referenceNo = "ESW-NEA-88",
                        description = "मिल्किङ मेशिन र चिलिङ भ्याट बिजुली महसुल"
                    ),
                    FarmTransaction(
                        id = "tx_init_11",
                        type = TransactionType.INCOME,
                        category = TransactionCategory.POULTRY_EGGS,
                        amount = 16800.0,
                        date = "$monthYear-11",
                        monthYear = monthYear,
                        paymentMode = TransactionPaymentMode.CASH,
                        partyName = "लगनखेल ग्रोसरी",
                        referenceNo = "INV-071",
                        description = "ताजा लोकल अण्डा ४० क्रेट"
                    ),
                    FarmTransaction(
                        id = "tx_init_12",
                        type = TransactionType.EXPENSE,
                        category = TransactionCategory.SEEDS_AGRO,
                        amount = 6200.0,
                        date = "$monthYear-11",
                        monthYear = monthYear,
                        paymentMode = TransactionPaymentMode.BANK_TRANSFER,
                        partyName = "किसान एग्रोभेट सेन्टर",
                        referenceNo = "MNB-TX-512",
                        description = "प्राङ्गारिक बीउ तथा जैविक विषादी"
                    ),
                    FarmTransaction(
                        id = "tx_init_13",
                        type = TransactionType.INCOME,
                        category = TransactionCategory.MILK_SALES,
                        amount = 13100.0,
                        date = "$monthYear-13",
                        monthYear = monthYear,
                        paymentMode = TransactionPaymentMode.BANK_TRANSFER,
                        partyName = "पाटन डेरी को-अपरेटिभ",
                        referenceNo = "MNB-TX-1002",
                        description = "गाई र भैंसीको दूध संकलन"
                    ),
                    FarmTransaction(
                        id = "tx_init_14",
                        type = TransactionType.EXPENSE,
                        category = TransactionCategory.WAGES_SALARY,
                        amount = 12400.0,
                        date = "$monthYear-13",
                        monthYear = monthYear,
                        paymentMode = TransactionPaymentMode.CASH,
                        partyName = "फार्म कामदार ज्याला",
                        referenceNo = "WAGE-02",
                        description = "दैनिक कामदार तथा घाँस कटानी ज्याला"
                    ),
                    FarmTransaction(
                        id = "tx_init_15",
                        type = TransactionType.INCOME,
                        category = TransactionCategory.FERTILIZER_SALES,
                        amount = 24100.0,
                        date = today,
                        monthYear = monthYear,
                        paymentMode = TransactionPaymentMode.BANK_TRANSFER,
                        partyName = "हरियाली नर्सरी तथा कृषि फार्म",
                        referenceNo = "MNB-TX-8809",
                        description = "भर्मिकम्पोस्ट मल र घिउ बिक्री"
                    ),
                    FarmTransaction(
                        id = "tx_init_16",
                        type = TransactionType.EXPENSE,
                        category = TransactionCategory.FEED_FORAGE,
                        amount = 17800.0,
                        date = today,
                        monthYear = monthYear,
                        paymentMode = TransactionPaymentMode.BANK_TRANSFER,
                        partyName = "अन्नपूर्णा फिड इन्डस्ट्रिज",
                        referenceNo = "MNB-BILL-401",
                        description = "कुखुराको लेयर्स दाना र गाईको चोकर"
                    )
                )
                for (tx in initialTx) {
                    addTransaction(tx)
                }

                // Seed balance sheet
                updateBalanceSheetLedger(
                    FarmBalanceSheetLedger(
                        id = "primary_ledger",
                        cashInHand = 92000.0,
                        bankBalanceMuktinath = 432500.0,
                        esewaBalance = 41200.0,
                        accountsReceivable = 48000.0,
                        fixedAssetsEquipment = 680000.0,
                        fixedAssetsLandShed = 1250000.0,
                        livestockValuation = 890000.0,
                        accountsPayable = 58000.0,
                        accruedSalaries = 42000.0,
                        shortTermLoans = 220000.0,
                        ownerCapital = 2800000.0,
                        asOfDate = today
                    )
                )
            }

            // Seed official documents and permits database if empty
            val existingDocs = db.collection("documents").limit(1).get().await()
            if (existingDocs.isEmpty) {
                for (doc in DefaultOfficialDocuments.documents) {
                    addDocument(doc)
                }
            }
        } catch (_: Exception) {}
    }
}

