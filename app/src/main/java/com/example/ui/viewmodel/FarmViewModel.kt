package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FarmRepository
import com.example.data.FirestoreFarmRepository
import com.example.data.local.FarmDatabase
import com.example.data.local.FarmInventoryEntity
import com.example.data.local.InventoryItem
import com.example.data.local.LocalInventoryRepository
import com.example.data.local.Order
import com.example.data.local.PriceItem
import com.example.ui.components.AttachedDocumentItem
import com.example.model.BankTransferDetails
import com.example.model.CartItem
import com.example.model.FarmOrder
import com.example.model.OrderStatus
import com.example.model.Product
import com.example.model.AttendanceRecord
import com.example.model.AttendanceStatus
import com.example.model.BalanceSheetSummary
import com.example.model.DailyTrendPoint
import com.example.model.Employee
import com.example.model.EmployeeMonthlySummary
import com.example.model.FarmBalanceSheetLedger
import com.example.model.FarmTransaction
import com.example.model.MonthlyDashboardSummary
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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class FarmScreen(val labelNp: String, val labelEn: String) {
    HOME("गृह पृष्ठ", "Home"),
    DASHBOARD("ड्यासबोर्ड (Recharts)", "Dashboard"),
    PRICE_LIST("मूल्य सूची", "Price List"),
    INVENTORY("इन्भेन्ट्रि", "Inventory"),
    ROOM_INVENTORY("इन्भेन्ट्रि सूची (Room)", "Inventory Room"),
    CREATE_ORDER("नयाँ अर्डर फारम", "Create Order"),
    MUKTINATH_PAYMENT("मुक्तिनाथ भुक्तानी", "Muktinath Payment"),
    PAST_ORDERS("विगतका अर्डरहरू (Room)", "Past Orders"),
    MONTHLY_REVENUE("मासिक आम्दानी (D3/Recharts)", "Monthly Revenue"),
    ATTENDANCE("हाजिरी", "Attendance"),
    FINANCE("आय-व्यय तथा ब्यालेन्स सिट", "Finance"),
    BANK_TRANSFER("बैंक ट्रान्सफर", "Bank Transfer"),
    ORDERS("अर्डर तथा रसिद", "Orders & Receipts"),
    DOCUMENTS("कागजात तथा इजाजतपत्र", "Official Documents & Permits")
}

enum class PriceSortOrder(val labelNp: String, val labelEn: String) {
    DEFAULT("क्रमबद्ध (Default)", "Categorized"),
    PRICE_LOW_TO_HIGH("मूल्य: कमबाट बढी", "Price: Low to High"),
    PRICE_HIGH_TO_LOW("मूल्य: बढीबाट कम", "Price: High to Low"),
    NAME_A_TO_Z("नाम (क-ज्ञ)", "Name (A-Z)"),
    STOCK_LEVEL("मौज्दात (धेरैबाट थोरै)", "Stock (High to Low)")
}

data class CartUiState(
    val items: List<CartItem> = emptyList()
) {
    val totalAmount: Double
        get() = items.sumOf { it.totalAmount }

    val totalItemCount: Int
        get() = items.size
}

class FarmViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val localFallbackRepo = FarmRepository()
    private val firestoreRepo = FirestoreFarmRepository(application)
    val roomDatabase = FarmDatabase.getInstance(application)
    val roomInventoryRepo = LocalInventoryRepository(roomDatabase.inventoryDao())

    val roomInventory: StateFlow<List<FarmInventoryEntity>> = roomInventoryRepo.allInventory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val inventoryItems: StateFlow<List<InventoryItem>> = roomDatabase.inventoryItemDao().getAllItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val roomOrders: StateFlow<List<Order>> = roomDatabase.orderDao().getAllOrders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _activePaymentOrder = MutableStateFlow<Order?>(null)
    val activePaymentOrder: StateFlow<Order?> = _activePaymentOrder.asStateFlow()

    private val _attachedDocuments = MutableStateFlow<List<AttachedDocumentItem>>(emptyList())
    val attachedDocuments: StateFlow<List<AttachedDocumentItem>> = _attachedDocuments.asStateFlow()

    private val _currentScreen = MutableStateFlow(FarmScreen.INVENTORY)
    val currentScreen: StateFlow<FarmScreen> = _currentScreen.asStateFlow()

    private val _isEnglishMode = MutableStateFlow(false)
    val isEnglishMode: StateFlow<Boolean> = _isEnglishMode.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(ProductCategory.ALL)
    val selectedCategory: StateFlow<ProductCategory> = _selectedCategory.asStateFlow()

    private val _sortOrder = MutableStateFlow(PriceSortOrder.DEFAULT)
    val sortOrder: StateFlow<PriceSortOrder> = _sortOrder.asStateFlow()

    private val _inventoryFilter = MutableStateFlow("ALL")
    val inventoryFilter: StateFlow<String> = _inventoryFilter.asStateFlow()

    private val _cart = MutableStateFlow(CartUiState())
    val cart: StateFlow<CartUiState> = _cart.asStateFlow()

    private val _selectedOrderForReceipt = MutableStateFlow<FarmOrder?>(null)
    val selectedOrderForReceipt: StateFlow<FarmOrder?> = _selectedOrderForReceipt.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    // Real-time Firestore products with fallback
    private val _firestoreProducts = MutableStateFlow<List<Product>>(localFallbackRepo.products.value)
    val products: StateFlow<List<Product>> = _firestoreProducts.asStateFlow()

    // Real-time Firestore orders with fallback
    private val _firestoreOrders = MutableStateFlow<List<FarmOrder>>(localFallbackRepo.orders.value)
    val orders: StateFlow<List<FarmOrder>> = _firestoreOrders.asStateFlow()

    init {
        // Observe Firestore products in real-time
        viewModelScope.launch {
            try {
                firestoreRepo.observeProducts()
                    .catch { e ->
                        Log.w("FarmVM", "Firestore products stream fallback to local: ${e.message}")
                    }
                    .collect { list ->
                        if (list.isNotEmpty()) {
                            _firestoreProducts.value = list
                        } else {
                            // Seed if Firestore collection is initially empty
                            firestoreRepo.seedInitialProductsIfEmpty(localFallbackRepo.products.value)
                        }
                    }
            } catch (e: Exception) {
                Log.w("FarmVM", "Error starting product sync: ${e.message}")
            }
        }

        // Sync products into local Room database for offline persistence
        viewModelScope.launch {
            products.collect { prods ->
                if (prods.isNotEmpty()) {
                    val entities = prods.map { p ->
                        FarmInventoryEntity(
                            id = p.id,
                            productName = p.nameEn.ifEmpty { p.nameNp },
                            productNameNp = p.nameNp,
                            currentQuantity = p.stockQuantity,
                            unitPrice = p.retailPrice,
                            wholesalePrice = p.wholesalePrice,
                            unit = p.unit.labelEn,
                            category = p.category.name,
                            minStockThreshold = p.minStockThreshold,
                            sku = p.sku
                        )
                    }
                    val inventoryEntities = prods.map { p ->
                        InventoryItem(
                            id = p.id,
                            productName = p.nameEn.ifEmpty { p.nameNp },
                            productNameNp = p.nameNp,
                            currentQuantity = p.stockQuantity,
                            unitPrice = p.retailPrice,
                            unit = p.unit.labelEn,
                            category = p.category.name,
                            minStockThreshold = p.minStockThreshold,
                            sku = p.sku
                        )
                    }
                    try {
                        roomInventoryRepo.insertAll(entities)
                        roomDatabase.inventoryItemDao().insertItems(inventoryEntities)
                    } catch (e: Exception) {
                        Log.w("FarmVM", "Error syncing products to Room: ${e.message}")
                    }
                }
            }
        }

        // Seed initial past orders in Room database if table is empty
        viewModelScope.launch {
            try {
                val currentOrders = roomDatabase.orderDao().getAllOrders().first()
                if (currentOrders.isEmpty()) {
                    val initialSeedOrders = listOf(
                        Order(
                            orderId = "ORD-20260515-01",
                            customerName = "राम शरण महर्जन (Ram Sharan)",
                            customerPhone = "9841122334",
                            deliveryAddress = "ललितपुर, पाटनढोका",
                            totalAmount = 4800.0,
                            status = "COMPLETED",
                            paymentMode = "BANK_TRANSFER",
                            senderBankName = "मुक्तिनाथ विकास बैंक (Muktinath Bikas Bank)",
                            senderAccountName = "Ram Sharan Maharjan",
                            transactionRefNumber = "MNB-TXN-110294",
                            transferredAmount = 4800.0,
                            orderDate = "2026-05-15 10:30 AM",
                            notes = "अर्ग्यानिक तरकारी तथा कालिज अण्डा"
                        ),
                        Order(
                            orderId = "ORD-20260618-02",
                            customerName = "सरिता श्रेष्ठ (Sarita Shrestha)",
                            customerPhone = "9851239845",
                            deliveryAddress = "काठमाडौं, महाराजगञ्ज",
                            totalAmount = 5200.0,
                            status = "VERIFIED",
                            paymentMode = "BANK_TRANSFER",
                            senderBankName = "मुक्तिनाथ विकास बैंक",
                            senderAccountName = "Sarita Shrestha",
                            transactionRefNumber = "MNB-TXN-229481",
                            transferredAmount = 5200.0,
                            orderDate = "2026-06-18 02:45 PM",
                            notes = "शुद्ध घिउ र मह"
                        ),
                        Order(
                            orderId = "ORD-20260720-03",
                            customerName = "दिनेश पौडेल (Dinesh Poudel)",
                            customerPhone = "9812983746",
                            deliveryAddress = "भक्तपुर, सल्लाघारी",
                            totalAmount = 6400.0,
                            status = "COMPLETED",
                            paymentMode = "CASH_ON_DELIVERY",
                            orderDate = "2026-07-20 11:15 AM",
                            notes = "स्थानीय कुखुरा र अण्डा बास्केट"
                        ),
                        Order(
                            orderId = "ORD-20260814-04",
                            customerName = "ममता गुरुङ (Mamata Gurung)",
                            customerPhone = "9861029384",
                            deliveryAddress = "ललितपुर, झम्सिखेल",
                            totalAmount = 7600.0,
                            status = "VERIFIED",
                            paymentMode = "BANK_TRANSFER",
                            senderBankName = "मुक्तिनाथ विकास बैंक",
                            senderAccountName = "Mamata Gurung",
                            transactionRefNumber = "MNB-TXN-559124",
                            transferredAmount = 7600.0,
                            orderDate = "2026-08-14 03:20 PM",
                            notes = "बाख्राको दूध र पनिर"
                        ),
                        Order(
                            orderId = "ORD-20260922-05",
                            customerName = "सुरेश लामा (Suresh Lama)",
                            customerPhone = "9849201938",
                            deliveryAddress = "काठमाडौं, बानेश्वर",
                            totalAmount = 9800.0,
                            status = "VERIFIED",
                            paymentMode = "BANK_TRANSFER",
                            senderBankName = "मुक्तिनाथ विकास बैंक",
                            senderAccountName = "Suresh Lama",
                            transactionRefNumber = "MNB-TXN-882194",
                            transferredAmount = 9800.0,
                            orderDate = "2026-09-22 09:30 AM",
                            notes = "दशैं अर्डर: खसीको मासु र घिउ"
                        ),
                        Order(
                            orderId = "ORD-20261015-01",
                            customerName = "राजेश श्रेष्ठ (Rajesh Shrestha)",
                            customerPhone = "9841234567",
                            deliveryAddress = "ललितपुर-१४, सानेपा",
                            totalAmount = 3850.0,
                            status = "VERIFIED",
                            paymentMode = "BANK_TRANSFER",
                            senderBankName = "मुक्तिनाथ विकास बैंक (Muktinath Bikas Bank)",
                            senderAccountName = "Rajesh Shrestha",
                            transactionRefNumber = "MNB-TXN-982144",
                            transferredAmount = 3850.0,
                            orderDate = "2026-10-15 11:30 AM",
                            notes = "शुद्ध गाईको घिउ र मह डेलिभरी"
                        ),
                        Order(
                            orderId = "ORD-20261014-02",
                            customerName = "सुनिता थापा (Sunita Thapa)",
                            customerPhone = "9851098765",
                            deliveryAddress = "काठमाडौं, नयाँ बानेश्वर",
                            totalAmount = 1600.0,
                            status = "COMPLETED",
                            paymentMode = "CASH_ON_DELIVERY",
                            orderDate = "2026-10-14 02:15 PM",
                            notes = "अर्ग्यानिक तरकारी बास्केट"
                        ),
                        Order(
                            orderId = "ORD-20261016-03",
                            customerName = "हरि बहादुर खत्री (Hari B. Khatri)",
                            customerPhone = "9818765432",
                            deliveryAddress = "भक्तपुर, सूर्यविनायक",
                            totalAmount = 5200.0,
                            status = "PENDING_VERIFICATION",
                            paymentMode = "BANK_TRANSFER",
                            senderBankName = "मुक्तिनाथ विकास बैंक (Muktinath Bikas Bank)",
                            senderAccountName = "Hari Bahadur Khatri",
                            transactionRefNumber = "MNB-TXN-774012",
                            transferredAmount = 5200.0,
                            orderDate = "2026-10-16 09:45 AM",
                            notes = "कालिज मासु र लोकल अण्डा"
                        ),
                        Order(
                            orderId = "ORD-20261012-04",
                            customerName = "अस्मिता अधिकारी (Asmita Adhikari)",
                            customerPhone = "9860123987",
                            deliveryAddress = "ललितपुर, इमाडोल",
                            totalAmount = 2450.0,
                            status = "VERIFIED",
                            paymentMode = "BANK_TRANSFER",
                            senderBankName = "मुक्तिनाथ विकास बैंक",
                            senderAccountName = "Asmita Adhikari",
                            transactionRefNumber = "MNB-TXN-330912",
                            transferredAmount = 2450.0,
                            orderDate = "2026-10-12 04:20 PM",
                            notes = "बाख्राको शुद्ध दूध र पनिर"
                        )
                    )
                    roomDatabase.orderDao().insertOrders(initialSeedOrders)
                }
            } catch (e: Exception) {
                Log.w("FarmVM", "Seed room orders fallback: ${e.message}")
            }
        }

        // Observe user orders if authenticated
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            loadUserProfile()
            viewModelScope.launch {
                try {
                    firestoreRepo.observeUserOrders(uid)
                        .catch { e ->
                            Log.w("FarmVM", "Firestore orders stream fallback: ${e.message}")
                        }
                        .collect { list ->
                            if (list.isNotEmpty()) {
                                _firestoreOrders.value = list
                            }
                        }
                } catch (e: Exception) {
                    Log.w("FarmVM", "Error starting order sync: ${e.message}")
                }
            }

            // Seed initial employees and finances if database is fresh
            viewModelScope.launch {
                try {
                    firestoreRepo.seedInitialEmployeesAndFinanceIfEmpty()
                } catch (e: Exception) {
                    Log.w("FarmVM", "Seed employees/finance error: ${e.message}")
                }
            }
        }
    }

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    fun syncUserProfile(role: UserRole = UserRole.STAFF) {
        viewModelScope.launch {
            try {
                val profile = firestoreRepo.saveOrUpdateUserProfile(role)
                _userProfile.value = profile
            } catch (e: Exception) {
                Log.w("FarmVM", "Error syncing user profile: ${e.message}")
            }
        }
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                val profile = firestoreRepo.fetchUserProfile()
                _userProfile.value = profile
            } catch (e: Exception) {
                Log.w("FarmVM", "Error loading user profile: ${e.message}")
            }
        }
    }


    // Filtered and sorted products for Price List
    val filteredProducts: StateFlow<List<Product>> = combine(
        products,
        _searchQuery,
        _selectedCategory,
        _sortOrder
    ) { allProducts, query, category, sort ->
        var list = allProducts

        if (category != ProductCategory.ALL) {
            list = list.filter { it.category == category }
        }

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.nameNp.lowercase().contains(q) ||
                it.nameEn.lowercase().contains(q) ||
                it.sku.lowercase().contains(q) ||
                it.descriptionNp.lowercase().contains(q)
            }
        }

        when (sort) {
            PriceSortOrder.DEFAULT -> list
            PriceSortOrder.PRICE_LOW_TO_HIGH -> list.sortedBy { it.retailPrice }
            PriceSortOrder.PRICE_HIGH_TO_LOW -> list.sortedByDescending { it.retailPrice }
            PriceSortOrder.NAME_A_TO_Z -> list.sortedBy { it.nameNp }
            PriceSortOrder.STOCK_LEVEL -> list.sortedByDescending { it.stockQuantity }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered for Inventory management screen
    val inventoryFilteredProducts: StateFlow<List<Product>> = combine(
        products,
        _searchQuery,
        _inventoryFilter
    ) { allProducts, query, filter ->
        var list = allProducts

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.nameNp.lowercase().contains(q) ||
                it.nameEn.lowercase().contains(q) ||
                it.sku.lowercase().contains(q)
            }
        }

        when (filter) {
            "LOW_STOCK" -> list.filter { it.isLowStock }
            "OUT_OF_STOCK" -> list.filter { it.isOutOfStock }
            else -> list
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun navigateTo(screen: FarmScreen) {
        _currentScreen.value = screen
    }

    fun toggleLanguage() {
        _isEnglishMode.value = !_isEnglishMode.value
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: ProductCategory) {
        _selectedCategory.value = category
    }

    fun setSortOrder(sort: PriceSortOrder) {
        _sortOrder.value = sort
    }

    fun setInventoryFilter(filter: String) {
        _inventoryFilter.value = filter
    }

    fun attachDocument(doc: AttachedDocumentItem) {
        _attachedDocuments.value = listOf(doc) + _attachedDocuments.value
        showToast("संलग्न गरियो: ${doc.title}")
    }

    fun removeAttachedDocument(id: String) {
        _attachedDocuments.value = _attachedDocuments.value.filterNot { it.id == id }
    }

    fun setActivePaymentOrder(order: Order?) {
        _activePaymentOrder.value = order
    }

    fun addInventoryItem(item: InventoryItem) {
        viewModelScope.launch {
            try {
                roomDatabase.inventoryItemDao().insertItem(item)
                showToast("Item '${item.productName}' added to Room inventory")
            } catch (e: Exception) {
                showToast("Error adding item: ${e.message}")
            }
        }
    }

    fun updateInventoryItemQuantity(id: String, newQuantity: Double) {
        viewModelScope.launch {
            try {
                roomDatabase.inventoryItemDao().updateQuantity(id, newQuantity)
            } catch (e: Exception) {
                Log.w("FarmVM", "Error updating quantity: ${e.message}")
            }
        }
    }

    fun deleteInventoryItem(id: String) {
        viewModelScope.launch {
            try {
                roomDatabase.inventoryItemDao().deleteItemById(id)
                showToast("Item deleted from Room inventory")
            } catch (e: Exception) {
                showToast("Error deleting item: ${e.message}")
            }
        }
    }

    fun submitRoomOrder(order: Order) {
        viewModelScope.launch {
            try {
                roomDatabase.orderDao().insertOrder(order)
                _activePaymentOrder.value = order
                showToast("Order #${order.orderId} created successfully")
            } catch (e: Exception) {
                Log.w("FarmVM", "Error saving room order: ${e.message}")
            }
        }
    }

    fun updateRoomOrderStatus(orderId: String, status: String) {
        viewModelScope.launch {
            try {
                roomDatabase.orderDao().updateOrderStatus(orderId, status)
                if (_activePaymentOrder.value?.orderId == orderId) {
                    _activePaymentOrder.value = _activePaymentOrder.value?.copy(status = status)
                }
                showToast("Payment status: $status")
            } catch (e: Exception) {
                Log.w("FarmVM", "Error updating order status: ${e.message}")
            }
        }
    }

    fun deleteRoomOrder(orderId: String) {
        viewModelScope.launch {
            try {
                roomDatabase.orderDao().deleteOrderById(orderId)
                if (_activePaymentOrder.value?.orderId == orderId) {
                    _activePaymentOrder.value = null
                }
                showToast("Order deleted from Room database")
            } catch (e: Exception) {
                Log.w("FarmVM", "Error deleting room order: ${e.message}")
            }
        }
    }

    fun seedMultiMonthOrders() {
        viewModelScope.launch {
            try {
                val multiMonthOrders = listOf(
                    Order(
                        orderId = "ORD-20260515-01",
                        customerName = "राम शरण महर्जन (Ram Sharan)",
                        customerPhone = "9841122334",
                        deliveryAddress = "ललितपुर, पाटनढोका",
                        totalAmount = 4800.0,
                        status = "COMPLETED",
                        paymentMode = "BANK_TRANSFER",
                        senderBankName = "मुक्तिनाथ विकास बैंक (Muktinath Bikas Bank)",
                        senderAccountName = "Ram Sharan Maharjan",
                        transactionRefNumber = "MNB-TXN-110294",
                        transferredAmount = 4800.0,
                        orderDate = "2026-05-15 10:30 AM",
                        notes = "अर्ग्यानिक तरकारी तथा कालिज अण्डा"
                    ),
                    Order(
                        orderId = "ORD-20260618-02",
                        customerName = "सरिता श्रेष्ठ (Sarita Shrestha)",
                        customerPhone = "9851239845",
                        deliveryAddress = "काठमाडौं, महाराजगञ्ज",
                        totalAmount = 5200.0,
                        status = "VERIFIED",
                        paymentMode = "BANK_TRANSFER",
                        senderBankName = "मुक्तिनाथ विकास बैंक",
                        senderAccountName = "Sarita Shrestha",
                        transactionRefNumber = "MNB-TXN-229481",
                        transferredAmount = 5200.0,
                        orderDate = "2026-06-18 02:45 PM",
                        notes = "शुद्ध घिउ र मह"
                    ),
                    Order(
                        orderId = "ORD-20260720-03",
                        customerName = "दिनेश पौडेल (Dinesh Poudel)",
                        customerPhone = "9812983746",
                        deliveryAddress = "भक्तपुर, सल्लाघारी",
                        totalAmount = 6400.0,
                        status = "COMPLETED",
                        paymentMode = "CASH_ON_DELIVERY",
                        orderDate = "2026-07-20 11:15 AM",
                        notes = "स्थानीय कुखुरा र अण्डा बास्केट"
                    ),
                    Order(
                        orderId = "ORD-20260814-04",
                        customerName = "ममता गुरुङ (Mamata Gurung)",
                        customerPhone = "9861029384",
                        deliveryAddress = "ललितपुर, झम्सिखेल",
                        totalAmount = 7600.0,
                        status = "VERIFIED",
                        paymentMode = "BANK_TRANSFER",
                        senderBankName = "मुक्तिनाथ विकास बैंक",
                        senderAccountName = "Mamata Gurung",
                        transactionRefNumber = "MNB-TXN-559124",
                        transferredAmount = 7600.0,
                        orderDate = "2026-08-14 03:20 PM",
                        notes = "बाख्राको दूध र पनिर"
                    ),
                    Order(
                        orderId = "ORD-20260922-05",
                        customerName = "सुरेश लामा (Suresh Lama)",
                        customerPhone = "9849201938",
                        deliveryAddress = "काठमाडौं, बानेश्वर",
                        totalAmount = 9800.0,
                        status = "VERIFIED",
                        paymentMode = "BANK_TRANSFER",
                        senderBankName = "मुक्तिनाथ विकास बैंक",
                        senderAccountName = "Suresh Lama",
                        transactionRefNumber = "MNB-TXN-882194",
                        transferredAmount = 9800.0,
                        orderDate = "2026-09-22 09:30 AM",
                        notes = "दशैं अर्डर: खसीको मासु र घिउ"
                    )
                )
                roomDatabase.orderDao().insertOrders(multiMonthOrders)
                showToast("Multi-month historical orders added to database")
            } catch (e: Exception) {
                Log.w("FarmVM", "Error seeding multi-month orders: ${e.message}")
            }
        }
    }

    fun addToCart(product: Product, quantity: Double = 1.0) {
        val currentItems = _cart.value.items.toMutableList()
        val existingIndex = currentItems.indexOfFirst { it.product.id == product.id }
        if (existingIndex >= 0) {
            val existing = currentItems[existingIndex]
            currentItems[existingIndex] = existing.copy(quantity = existing.quantity + quantity)
        } else {
            currentItems.add(CartItem(product, quantity))
        }
        _cart.value = CartUiState(currentItems)
        showToast("${product.nameNp} अर्डरमा थपियो!")
    }

    fun updateCartQuantity(productId: String, quantity: Double) {
        val currentItems = _cart.value.items.toMutableList()
        val index = currentItems.indexOfFirst { it.productId == productId }
        if (index >= 0) {
            if (quantity <= 0.0) {
                currentItems.removeAt(index)
            } else {
                currentItems[index] = currentItems[index].copy(quantity = quantity)
            }
            _cart.value = CartUiState(currentItems)
        }
    }

    private val CartItem.productId: String get() = this.product.id

    fun clearCart() {
        _cart.value = CartUiState(emptyList())
    }

    fun updateProductStock(productId: String, newStock: Double) {
        viewModelScope.launch {
            try {
                firestoreRepo.updateStock(productId, newStock)
            } catch (e: Exception) {
                localFallbackRepo.updateStock(productId, newStock)
            }
            showToast("मौज्दात (Firestore) अद्यावधिक गरियो!")
        }
    }

    fun updateProductPriceAndStock(productId: String, newRetailPrice: Double, newWholesalePrice: Double, newStock: Double) {
        viewModelScope.launch {
            try {
                firestoreRepo.updateProductPriceAndStock(productId, newRetailPrice, newWholesalePrice, newStock)
            } catch (e: Exception) {
                localFallbackRepo.updateProductPriceAndStock(productId, newRetailPrice, newWholesalePrice, newStock)
            }
            // Update local memory state immediately
            _firestoreProducts.value = _firestoreProducts.value.map { prod ->
                if (prod.id == productId) {
                    prod.copy(
                        retailPrice = newRetailPrice.coerceAtLeast(0.0),
                        wholesalePrice = newWholesalePrice.coerceAtLeast(0.0),
                        stockQuantity = newStock.coerceAtLeast(0.0)
                    )
                } else prod
            }
            showToast("मूल्य तथा मौज्दात सफलतापूर्वक परिवर्तन गरियो!")
        }
    }

    fun adjustProductStock(productId: String, delta: Double) {
        val current = _firestoreProducts.value.firstOrNull { it.id == productId }?.stockQuantity ?: 0.0
        val newStock = (current + delta).coerceAtLeast(0.0)
        updateProductStock(productId, newStock)
    }

    fun addNewProduct(product: Product) {
        viewModelScope.launch {
            try {
                firestoreRepo.addProduct(product)
            } catch (e: Exception) {
                localFallbackRepo.addProduct(product)
            }
            showToast("नयाँ उत्पादन '${product.nameNp}' Firestore मा थपियो!")
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            try {
                firestoreRepo.deleteProduct(productId)
            } catch (e: Exception) {
                localFallbackRepo.deleteProduct(productId)
            }
            _firestoreProducts.value = _firestoreProducts.value.filterNot { it.id == productId }
            showToast("उत्पादन सफलतापूर्वक हटाइयो (Product removed)!")
        }
    }

    fun checkoutOrder(
        customerName: String,
        customerPhone: String,
        deliveryAddress: String,
        bankTransfer: BankTransferDetails?,
        notes: String
    ): FarmOrder {
        var createdOrder = localFallbackRepo.createOrder(
            customerName = customerName,
            customerPhone = customerPhone,
            deliveryAddress = deliveryAddress,
            cartItems = _cart.value.items,
            bankTransfer = bankTransfer,
            notes = notes
        )

        viewModelScope.launch {
            try {
                val firestoreOrder = firestoreRepo.createOrder(
                    customerName = customerName,
                    customerPhone = customerPhone,
                    deliveryAddress = deliveryAddress,
                    cartItems = _cart.value.items,
                    bankTransfer = bankTransfer,
                    notes = notes
                )
                createdOrder = firestoreOrder
            } catch (e: Exception) {
                Log.w("FarmVM", "Saved to local fallback: ${e.message}")
            }
        }

        clearCart()
        _selectedOrderForReceipt.value = createdOrder
        showToast("अर्डर र मुक्तिनाथ बैंक भुक्तानी विवरण Firestore मा पेश भयो! ID: ${createdOrder.orderId}")
        return createdOrder
    }

    fun processNewOrder(
        customerName: String,
        customerPhone: String,
        deliveryAddress: String,
        items: List<CartItem>,
        bankTransfer: BankTransferDetails?,
        notes: String
    ) {
        viewModelScope.launch {
            try {
                val created = firestoreRepo.createOrder(
                    customerName = customerName,
                    customerPhone = customerPhone,
                    deliveryAddress = deliveryAddress,
                    cartItems = items,
                    bankTransfer = bankTransfer,
                    notes = notes
                )
                _firestoreOrders.value = listOf(created) + _firestoreOrders.value.filterNot { it.orderId == created.orderId }
                _firestoreProducts.value = _firestoreProducts.value.map { prod ->
                    val matching = items.firstOrNull { it.product.id == prod.id }
                    if (matching != null) {
                        prod.copy(stockQuantity = (prod.stockQuantity - matching.quantity).coerceAtLeast(0.0))
                    } else prod
                }
                _selectedOrderForReceipt.value = created
                showToast("नयाँ अर्डर Firestore मा सफलतापूर्वक दर्ता भयो! अर्डर नं.: ${created.orderId}")
            } catch (e: Exception) {
                val fallback = localFallbackRepo.createOrder(
                    customerName = customerName,
                    customerPhone = customerPhone,
                    deliveryAddress = deliveryAddress,
                    cartItems = items,
                    bankTransfer = bankTransfer,
                    notes = notes
                )
                _selectedOrderForReceipt.value = fallback
                showToast("अर्डर सुरक्षित भयो! अर्डर नं.: ${fallback.orderId}")
            }
        }
    }

    fun submitDirectBankTransfer(
        customerName: String,
        customerPhone: String,
        deliveryAddress: String,
        senderBankName: String,
        senderAccountName: String,
        transactionRef: String,
        amount: Double,
        remarks: String
    ): FarmOrder {
        val bankDetails = BankTransferDetails(
            senderBankName = senderBankName,
            senderAccountName = senderAccountName,
            transactionRefNumber = transactionRef,
            transferredAmount = amount,
            transferDate = "",
            remarks = remarks
        )

        val order = checkoutOrder(
            customerName = customerName,
            customerPhone = customerPhone,
            deliveryAddress = deliveryAddress,
            bankTransfer = bankDetails,
            notes = remarks
        )
        return order
    }

    fun verifyOrderPayment(orderId: String) {
        localFallbackRepo.updateOrderStatus(orderId, OrderStatus.VERIFIED)
        _firestoreOrders.value = _firestoreOrders.value.map {
            if (it.orderId == orderId) it.copy(status = OrderStatus.VERIFIED) else it
        }
        showToast("अर्डर भुक्तानी प्रमाणीकरण सम्पन्न भयो!")
    }

    fun openReceipt(order: FarmOrder) {
        _selectedOrderForReceipt.value = order
    }

    fun closeReceipt() {
        _selectedOrderForReceipt.value = null
    }

    fun showToast(message: String) {
        viewModelScope.launch {
            _toastEvent.emit(message)
        }
    }

    // ====================================================================
    // EMPLOYEE ATTENDANCE (DAY-WISE & MONTHLY)
    // ====================================================================

    val todayDateString: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val currentMonthString: String = todayDateString.take(7)

    private val _selectedAttendanceDate = MutableStateFlow(todayDateString)
    val selectedAttendanceDate: StateFlow<String> = _selectedAttendanceDate.asStateFlow()

    private val _selectedAttendanceMonth = MutableStateFlow(currentMonthString)
    val selectedAttendanceMonth: StateFlow<String> = _selectedAttendanceMonth.asStateFlow()

    val employees: StateFlow<List<Employee>> = firestoreRepo.observeEmployees()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val dailyAttendance: StateFlow<List<AttendanceRecord>> = _selectedAttendanceDate
        .flatMapLatest { date ->
            firestoreRepo.observeDailyAttendance(date).catch { emit(emptyList()) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val monthlyAttendance: StateFlow<List<AttendanceRecord>> = _selectedAttendanceMonth
        .flatMapLatest { month ->
            firestoreRepo.observeMonthlyAttendance(month).catch { emit(emptyList()) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setAttendanceDate(date: String) {
        _selectedAttendanceDate.value = date
        if (date.length >= 7) {
            _selectedAttendanceMonth.value = date.take(7)
        }
    }

    fun setAttendanceMonth(monthYear: String) {
        _selectedAttendanceMonth.value = monthYear
    }

    fun markEmployeeAttendance(
        employee: Employee,
        status: AttendanceStatus,
        checkIn: String = "07:00 AM",
        checkOut: String = "05:00 PM",
        remarks: String = ""
    ) {
        viewModelScope.launch {
            try {
                val date = _selectedAttendanceDate.value
                val month = date.take(7)
                val user = FirebaseAuth.getInstance().currentUser
                val record = AttendanceRecord(
                    id = "att_${employee.id}_${date.replace("-", "")}",
                    employeeId = employee.id,
                    employeeName = employee.name,
                    date = date,
                    monthYear = month,
                    status = status,
                    checkInTime = checkIn,
                    checkOutTime = checkOut,
                    remarks = remarks,
                    recordedBy = user?.displayName ?: "Farm Manager"
                )
                firestoreRepo.recordAttendance(record)
                showToast("${employee.name}: ${status.labelNp} हाजिरी सुरक्षित भयो")
            } catch (e: Exception) {
                showToast("हाजिरी रेकर्ड असफल: ${e.message}")
            }
        }
    }

    fun markAllPresent(date: String = _selectedAttendanceDate.value) {
        viewModelScope.launch {
            try {
                val empList = employees.value.filter { it.isActive }
                val month = date.take(7)
                val user = FirebaseAuth.getInstance().currentUser
                for (emp in empList) {
                    val record = AttendanceRecord(
                        id = "att_${emp.id}_${date.replace("-", "")}",
                        employeeId = emp.id,
                        employeeName = emp.name,
                        date = date,
                        monthYear = month,
                        status = AttendanceStatus.PRESENT,
                        checkInTime = "07:00 AM",
                        checkOutTime = "05:00 PM",
                        remarks = "सबै उपस्थित चिन्हित",
                        recordedBy = user?.displayName ?: "Farm Manager"
                    )
                    firestoreRepo.recordAttendance(record)
                }
                showToast("सबै सक्रिय कर्मचारीको उपस्थित (${date}) हाजिरी सम्पन्न भयो!")
            } catch (e: Exception) {
                showToast("हाजिरी त्रुटि: ${e.message}")
            }
        }
    }

    fun addNewEmployee(
        name: String,
        role: String,
        phone: String,
        dailyWage: Double,
        monthlySalary: Double
    ) {
        viewModelScope.launch {
            try {
                val emp = Employee(
                    id = "emp_${System.currentTimeMillis().toString().takeLast(6)}",
                    name = name,
                    role = role,
                    phone = phone,
                    dailyWage = dailyWage,
                    monthlySalary = monthlySalary,
                    joinDate = todayDateString,
                    isActive = true
                )
                firestoreRepo.addEmployee(emp)
                showToast("नयाँ कर्मचारी '$name' सफलतापूर्वक दर्ता गरियो")
            } catch (e: Exception) {
                showToast("कर्मचारी थप्न असफल: ${e.message}")
            }
        }
    }

    fun updateEmployee(employee: Employee) {
        viewModelScope.launch {
            try {
                firestoreRepo.updateEmployee(employee)
                showToast("कर्मचारी विवरण अद्यावधिक भयो")
            } catch (e: Exception) {
                showToast("अपडेट असफल: ${e.message}")
            }
        }
    }

    fun deleteEmployee(employeeId: String) {
        viewModelScope.launch {
            try {
                firestoreRepo.deleteEmployee(employeeId)
                showToast("कर्मचारी हटाइयो")
            } catch (e: Exception) {
                showToast("हटाउन असफल: ${e.message}")
            }
        }
    }

    // ====================================================================
    // DAILY INCOME & EXPENSES AND BALANCE SHEET
    // ====================================================================

    val transactions: StateFlow<List<FarmTransaction>> = firestoreRepo.observeTransactions()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val balanceSheetLedger: StateFlow<FarmBalanceSheetLedger> = firestoreRepo.observeBalanceSheetLedger()
        .catch { emit(FarmBalanceSheetLedger()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FarmBalanceSheetLedger())

    val balanceSheetSummary: StateFlow<BalanceSheetSummary> = combine(
        balanceSheetLedger,
        products,
        transactions
    ) { ledger, prodList, txList ->
        val inventoryValue = prodList.sumOf { it.stockQuantity * it.retailPrice }
        val totalIncome = txList.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpense = txList.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val netProfit = totalIncome - totalExpense

        BalanceSheetSummary(
            asOfDate = ledger.asOfDate,
            cashInHand = ledger.cashInHand,
            bankBalanceMuktinath = ledger.bankBalanceMuktinath,
            esewaBalance = ledger.esewaBalance,
            accountsReceivable = ledger.accountsReceivable,
            inventoryStockValue = inventoryValue,
            fixedAssetsEquipment = ledger.fixedAssetsEquipment,
            fixedAssetsLandShed = ledger.fixedAssetsLandShed,
            livestockValuation = ledger.livestockValuation,
            accountsPayable = ledger.accountsPayable,
            accruedSalaries = ledger.accruedSalaries,
            shortTermLoans = ledger.shortTermLoans,
            ownerCapital = ledger.ownerCapital,
            netFarmProfit = netProfit
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        BalanceSheetSummary(
            asOfDate = todayDateString,
            cashInHand = 92000.0,
            bankBalanceMuktinath = 432500.0,
            esewaBalance = 41200.0,
            accountsReceivable = 48000.0,
            inventoryStockValue = 180000.0,
            fixedAssetsEquipment = 680000.0,
            fixedAssetsLandShed = 1250000.0,
            livestockValuation = 890000.0,
            accountsPayable = 58000.0,
            accruedSalaries = 42000.0,
            shortTermLoans = 220000.0,
            ownerCapital = 2800000.0,
            netFarmProfit = 45000.0
        )
    )

    fun recordDailyTransaction(
        type: TransactionType,
        category: TransactionCategory,
        amount: Double,
        date: String = todayDateString,
        paymentMode: TransactionPaymentMode = TransactionPaymentMode.CASH,
        partyName: String = "",
        referenceNo: String = "",
        description: String = ""
    ) {
        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                val tx = FarmTransaction(
                    id = "tx_${System.currentTimeMillis().toString().takeLast(8)}",
                    type = type,
                    category = category,
                    amount = amount,
                    date = date,
                    monthYear = date.take(7),
                    paymentMode = paymentMode,
                    partyName = partyName,
                    referenceNo = referenceNo,
                    description = description,
                    recordedBy = user?.displayName ?: "Farm Accountant"
                )
                firestoreRepo.addTransaction(tx)
                val label = if (type == TransactionType.INCOME) "आम्दानी (रु. ${amount.toInt()})" else "खर्च (रु. ${amount.toInt()})"
                showToast("$label सफलतापूर्वक दर्ता भयो")
            } catch (e: Exception) {
                showToast("लेनदेन रेकर्ड असफल: ${e.message}")
            }
        }
    }

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            try {
                firestoreRepo.deleteTransaction(transactionId)
                showToast("लेनदेन हटाइयो")
            } catch (e: Exception) {
                showToast("हटाउन असफल: ${e.message}")
            }
        }
    }

    fun updateBalanceSheetLedger(ledger: FarmBalanceSheetLedger) {
        viewModelScope.launch {
            try {
                firestoreRepo.updateBalanceSheetLedger(ledger)
                showToast("ब्यालेन्स सिट सम्पत्ति तथा दायित्व अपडेट भयो")
            } catch (e: Exception) {
                showToast("ब्यालेन्स सिट अपडेट त्रुटि: ${e.message}")
            }
        }
    }

    // ====================================================================
    // MONTHLY DASHBOARD (RECHARTS & COMPOSE)
    // ====================================================================

    private val _selectedDashboardMonth = MutableStateFlow(currentMonthString)
    val selectedDashboardMonth: StateFlow<String> = _selectedDashboardMonth.asStateFlow()

    fun setDashboardMonth(month: String) {
        _selectedDashboardMonth.value = month
    }

    val monthlyDashboardSummary: StateFlow<MonthlyDashboardSummary> = combine(
        transactions,
        products,
        _selectedDashboardMonth
    ) { txList, prodList, selMonth ->
        val currentInvVal = prodList.sumOf { it.stockQuantity * it.retailPrice }
        val totalUnits = prodList.sumOf { it.stockQuantity }
        val lowStock = prodList.count { it.isLowStock || it.isOutOfStock }

        // Filter transactions matching this month
        val monthTxs = txList.filter { it.monthYear == selMonth || it.date.startsWith(selMonth) }
        val monthIncome = monthTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val monthExpense = monthTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val netCashFlow = monthIncome - monthExpense

        // Group transactions by date
        val txsByDate = monthTxs.groupBy { it.date }

        val dates = if (txsByDate.isNotEmpty()) {
            txsByDate.keys.sorted()
        } else {
            listOf("$selMonth-01", "$selMonth-03", "$selMonth-05", "$selMonth-07", "$selMonth-10", "$selMonth-14")
        }

        val monthAbbr = getMonthAbbreviation(selMonth)
        val trendPoints = mutableListOf<DailyTrendPoint>()

        var baselineInv = if (currentInvVal > 0) currentInvVal * 0.9 else 250000.0

        for ((idx, dStr) in dates.withIndex()) {
            val dayTxs = txsByDate[dStr] ?: emptyList()
            val dayInc = dayTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val dayExp = dayTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val dayNet = dayInc - dayExp

            val dayNumber = try {
                dStr.takeLast(2).toInt()
            } catch (_: Exception) {
                idx + 1
            }

            // Valuation progression across days
            baselineInv = if (idx == dates.size - 1 && currentInvVal > 0) {
                currentInvVal
            } else {
                (baselineInv + (dayInc * 0.18) - (dayExp * 0.06) + ((idx % 3) * 1200.0)).coerceAtLeast(10000.0)
            }

            trendPoints.add(
                DailyTrendPoint(
                    day = dayNumber,
                    dayLabel = "$dayNumber $monthAbbr",
                    dateStr = dStr,
                    income = dayInc,
                    expense = dayExp,
                    net = dayNet,
                    inventoryValue = baselineInv
                )
            )
        }

        val topInc = monthTxs.filter { it.type == TransactionType.INCOME }
            .groupBy { it.category }
            .maxByOrNull { entry -> entry.value.sumOf { it.amount } }
            ?.key?.labelNp ?: "दूध बिक्री"

        val topExp = monthTxs.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .maxByOrNull { entry -> entry.value.sumOf { it.amount } }
            ?.key?.labelNp ?: "दाना तथा घाँस"

        val highestDay = trendPoints.maxByOrNull { it.income }?.dateStr ?: todayDateString

        MonthlyDashboardSummary(
            monthYear = selMonth,
            monthLabelNp = formatMonthNp(selMonth),
            monthLabelEn = formatMonthEn(selMonth),
            totalIncome = monthIncome,
            totalExpense = monthExpense,
            netCashFlow = netCashFlow,
            currentInventoryValue = currentInvVal,
            startInventoryValue = trendPoints.firstOrNull()?.inventoryValue ?: (currentInvVal * 0.9),
            inventoryValueChange = currentInvVal - (trendPoints.firstOrNull()?.inventoryValue ?: currentInvVal),
            totalStockUnits = totalUnits,
            lowStockCount = lowStock,
            dailyTrends = trendPoints,
            topIncomeCategory = topInc,
            topExpenseCategory = topExp,
            highestIncomeDay = highestDay
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MonthlyDashboardSummary(
            monthYear = currentMonthString,
            monthLabelNp = "असोज-कार्तिक २०८३ (October 2026)",
            monthLabelEn = "October 2026",
            totalIncome = 138500.0,
            totalExpense = 68400.0,
            netCashFlow = 70100.0,
            currentInventoryValue = 284200.0,
            startInventoryValue = 260000.0,
            inventoryValueChange = 24200.0,
            totalStockUnits = 345.0,
            lowStockCount = 2,
            dailyTrends = emptyList(),
            topIncomeCategory = "दूध बिक्री",
            topExpenseCategory = "दाना तथा घाँस"
        )
    )

    private fun formatMonthNp(monthYear: String): String {
        val parts = monthYear.split("-")
        if (parts.size != 2) return monthYear
        val m = parts[1].toIntOrNull() ?: 10
        val y = parts[0].toIntOrNull() ?: 2026
        val nepaliMonths = listOf(
            "पुष-माघ (Jan)", "माघ-फागुन (Feb)", "फागुन-चैत (Mar)", "चैत-बैशाख (Apr)",
            "बैशाख-जेठ (May)", "जेठ-असार (Jun)", "असार-साउन (Jul)", "साउन-भदौ (Aug)",
            "भदौ-असोज (Sep)", "असोज-कार्तिक (Oct)", "कार्तिक-मंसिर (Nov)", "मंसिर-पुष (Dec)"
        )
        val name = nepaliMonths.getOrElse(m - 1) { "महिना $m" }
        return "$name $y"
    }

    private fun formatMonthEn(monthYear: String): String {
        val parts = monthYear.split("-")
        if (parts.size != 2) return monthYear
        val m = parts[1].toIntOrNull() ?: 10
        val y = parts[0].toIntOrNull() ?: 2026
        val enMonths = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
        val name = enMonths.getOrElse(m - 1) { "Month $m" }
        return "$name $y"
    }

    private fun getMonthAbbreviation(monthYear: String): String {
        val parts = monthYear.split("-")
        if (parts.size != 2) return "Oct"
        val m = parts[1].toIntOrNull() ?: 10
        val enAbbrs = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        return enAbbrs.getOrElse(m - 1) { "M$m" }
    }

    // ====================================================================
    // OFFICIAL DOCUMENTS & PERMITS DATABASE
    // ====================================================================

    val documents: StateFlow<List<OfficialDocument>> = firestoreRepo.observeDocuments()
        .catch {
            emit(localFallbackRepo.documents.value)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            DefaultOfficialDocuments.documents
        )

    private val _documentSearchQuery = MutableStateFlow("")
    val documentSearchQuery: StateFlow<String> = _documentSearchQuery.asStateFlow()

    private val _documentCategoryFilter = MutableStateFlow<DocumentCategory?>(null)
    val documentCategoryFilter: StateFlow<DocumentCategory?> = _documentCategoryFilter.asStateFlow()

    private val _documentStatusFilter = MutableStateFlow<DocumentStatus?>(null)
    val documentStatusFilter: StateFlow<DocumentStatus?> = _documentStatusFilter.asStateFlow()

    val filteredDocuments: StateFlow<List<OfficialDocument>> = combine(
        documents,
        _documentSearchQuery,
        _documentCategoryFilter,
        _documentStatusFilter
    ) { allDocs, query, catFilter, statusFilter ->
        var list = allDocs
        if (catFilter != null) {
            list = list.filter { it.category == catFilter }
        }
        if (statusFilter != null) {
            list = list.filter { it.status == statusFilter }
        }
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter { doc ->
                doc.titleNp.lowercase().contains(q) ||
                doc.titleEn.lowercase().contains(q) ||
                doc.documentNumber.lowercase().contains(q) ||
                doc.issuingAuthorityNp.lowercase().contains(q) ||
                doc.issuingAuthorityEn.lowercase().contains(q) ||
                doc.digitalRefCode.lowercase().contains(q) ||
                doc.notes.lowercase().contains(q)
            }
        }
        list
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DefaultOfficialDocuments.documents
    )

    fun setDocumentSearch(query: String) {
        _documentSearchQuery.value = query
    }

    fun setDocumentCategoryFilter(category: DocumentCategory?) {
        _documentCategoryFilter.value = category
    }

    fun setDocumentStatusFilter(status: DocumentStatus?) {
        _documentStatusFilter.value = status
    }

    fun addOfficialDocument(
        titleNp: String,
        titleEn: String,
        documentNumber: String,
        category: DocumentCategory,
        issuingAuthorityNp: String,
        issuingAuthorityEn: String,
        issueDate: String,
        expiryDate: String,
        status: DocumentStatus,
        summaryPointsNp: List<String> = emptyList(),
        summaryPointsEn: List<String> = emptyList(),
        contactOfficer: String = "",
        notes: String = ""
    ) {
        viewModelScope.launch {
            try {
                val newDoc = OfficialDocument(
                    id = "DOC_${System.currentTimeMillis()}",
                    titleNp = titleNp,
                    titleEn = titleEn,
                    documentNumber = documentNumber,
                    category = category,
                    issuingAuthorityNp = issuingAuthorityNp,
                    issuingAuthorityEn = issuingAuthorityEn,
                    issueDate = issueDate,
                    expiryDate = if (expiryDate.isBlank()) "आजीवन / स्थायी" else expiryDate,
                    status = status,
                    isVerified = true,
                    digitalRefCode = "UAF-REF-${(1000..9999).random()}",
                    summaryPointsNp = summaryPointsNp,
                    summaryPointsEn = summaryPointsEn,
                    contactOfficer = contactOfficer,
                    notes = notes,
                    createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                )
                // Write to local fallback
                localFallbackRepo.addDocument(newDoc)
                // Write to Firestore cloud database
                firestoreRepo.addDocument(newDoc)
                showToast("आधिकारिक कागजात '$documentNumber' सफलतापूर्वक दर्ता भयो")
            } catch (e: Exception) {
                showToast("कागजात दर्ता असफल: ${e.message}")
            }
        }
    }

    fun updateOfficialDocument(document: OfficialDocument) {
        viewModelScope.launch {
            try {
                localFallbackRepo.updateDocument(document)
                firestoreRepo.updateDocument(document)
                showToast("कागजात अद्यावधिक गरियो")
            } catch (e: Exception) {
                showToast("अपडेट असफल: ${e.message}")
            }
        }
    }

    fun deleteOfficialDocument(documentId: String) {
        viewModelScope.launch {
            try {
                localFallbackRepo.deleteDocument(documentId)
                firestoreRepo.deleteDocument(documentId)
                showToast("कागजात रेकर्ड हटाइयो")
            } catch (e: Exception) {
                showToast("हटाउन असफल: ${e.message}")
            }
        }
    }

    fun renewOfficialDocument(documentId: String, newExpiryDate: String, notes: String = "") {
        viewModelScope.launch {
            try {
                val current = documents.value.find { it.id == documentId }
                if (current != null) {
                    val updated = current.copy(
                        expiryDate = newExpiryDate,
                        status = DocumentStatus.ACTIVE,
                        notes = if (notes.isNotBlank()) "${current.notes}\n[Renewed]: $notes" else current.notes,
                        updatedAt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                    )
                    localFallbackRepo.updateDocument(updated)
                    firestoreRepo.updateDocument(updated)
                    showToast("इजाजतपत्र/कागजात सफलतापूर्वक नवीकरण गरियो")
                }
            } catch (e: Exception) {
                showToast("नवीकरण असफल: ${e.message}")
            }
        }
    }
}
