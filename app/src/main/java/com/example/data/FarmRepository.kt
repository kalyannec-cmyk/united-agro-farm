package com.example.data

import com.example.model.BankTransferDetails
import com.example.model.CartItem
import com.example.model.FarmOrder
import com.example.model.OrderStatus
import com.example.model.PriceUnit
import com.example.model.Product
import com.example.model.ProductCategory
import com.example.model.OfficialDocument
import com.example.model.DefaultOfficialDocuments
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class FarmRepository {

    private val initialProducts = listOf(
        // Dairy
        Product(
            id = "PROD-001",
            nameNp = "गाएको शुद्ध ताजा दूध",
            nameEn = "Fresh Pure Cow Milk",
            category = ProductCategory.DAIRY,
            retailPrice = 110.0,
            wholesalePrice = 95.0,
            unit = PriceUnit.LITRE,
            stockQuantity = 140.0,
            minStockThreshold = 25.0,
            descriptionNp = "हाम्रो आफ्नै फार्मको होलस्टिन र जर्सी गाईको ताजा, फ्याटयुक्त शुद्ध दूध। विहान र बेलुकाको ताजा संकलन।",
            descriptionEn = "Farm fresh milk from Holstein and Jersey cows. Pure, unadulterated, collected twice daily.",
            sku = "UAF-MILK-COW",
            isOrganicCertified = true
        ),
        Product(
            id = "PROD-002",
            nameNp = "भैँसीको बाक्लो दूध",
            nameEn = "Fresh Buffalo Milk",
            category = ProductCategory.DAIRY,
            retailPrice = 135.0,
            wholesalePrice = 120.0,
            unit = PriceUnit.LITRE,
            stockQuantity = 85.0,
            minStockThreshold = 20.0,
            descriptionNp = "मुर्रा भैँसीको बाक्लो, उच्च फ्याट भएको परम्परागत शुद्ध दूध। चिया, दही र मिठाईको लागि उत्तम।",
            descriptionEn = "Rich, thick buffalo milk with high cream content, perfect for curd and dairy sweets.",
            sku = "UAF-MILK-BUF",
            isOrganicCertified = true
        ),
        Product(
            id = "PROD-003",
            nameNp = "शुद्ध दानेदार घिउ",
            nameEn = "Pure Farm Ghee (Danedar)",
            category = ProductCategory.DAIRY,
            retailPrice = 1450.0,
            wholesalePrice = 1300.0,
            unit = PriceUnit.KG,
            stockQuantity = 42.0,
            minStockThreshold = 10.0,
            descriptionNp = "परम्परागत मदानीबाट तयार पारिएको बास्नादार दानेदार शुद्ध घिउ। कुनै रसायन वा मिसावट नभएको।",
            descriptionEn = "Traditional churned golden pure ghee. Rich aroma and granulated texture.",
            sku = "UAF-GHEE-01",
            isOrganicCertified = true
        ),
        Product(
            id = "PROD-004",
            nameNp = "ताजा नरम पनिर",
            nameEn = "Fresh Farm Paneer",
            category = ProductCategory.DAIRY,
            retailPrice = 750.0,
            wholesalePrice = 680.0,
            unit = PriceUnit.KG,
            stockQuantity = 18.0,
            minStockThreshold = 8.0,
            descriptionNp = "ताजा दूधबाट दैनिक तयार पारिने स्पन्जी र नरम प्राङ्गारिक पनिर।",
            descriptionEn = "Soft, spongy cottage cheese prepared fresh daily from whole cow milk.",
            sku = "UAF-PANEER-01",
            isOrganicCertified = true
        ),
        Product(
            id = "PROD-005",
            nameNp = "स्थानीय मटका दही",
            nameEn = "Traditional Curd / Dahi",
            category = ProductCategory.DAIRY,
            retailPrice = 160.0,
            wholesalePrice = 140.0,
            unit = PriceUnit.LITRE,
            stockQuantity = 35.0,
            minStockThreshold = 10.0,
            descriptionNp = "माटोको भाँडोमा जमाइएको स्वादिलो र बाक्लो परम्परागत दही।",
            descriptionEn = "Thick, naturally cultured curd set in clay pots.",
            sku = "UAF-DAHI-01",
            isOrganicCertified = true
        ),

        // Poultry & Eggs
        Product(
            id = "PROD-006",
            nameNp = "अर्गानिक लोकल कुखुराको अण्डा",
            nameEn = "Organic Free-Range Local Eggs",
            category = ProductCategory.POULTRY,
            retailPrice = 650.0,
            wholesalePrice = 580.0,
            unit = PriceUnit.CRATE,
            stockQuantity = 55.0,
            minStockThreshold = 15.0,
            descriptionNp = "खुल्ला चौरमा चरेका लोकल कुखुराका पोषणयुक्त अर्गानिक अण्डा। (प्रति क्रेट ३० वटा)।",
            descriptionEn = "Nutrient-rich free-range local chicken eggs. Pack of 30 eggs per crate.",
            sku = "UAF-EGG-LOC",
            isOrganicCertified = true
        ),
        Product(
            id = "PROD-007",
            nameNp = "फार्म लेयर्स अण्डा (ग्रेड A)",
            nameEn = "Fresh Layer Eggs (Grade A)",
            category = ProductCategory.POULTRY,
            retailPrice = 490.0,
            wholesalePrice = 450.0,
            unit = PriceUnit.CRATE,
            stockQuantity = 120.0,
            minStockThreshold = 30.0,
            descriptionNp = "ताजा र ठूलो साइजका लेयर्स अण्डा, दैनिक संकलित। थोक तथा खुद्रा बिक्री।",
            descriptionEn = "Fresh large-size Grade A layer eggs, sorted and crate-packed.",
            sku = "UAF-EGG-LAY",
            isOrganicCertified = true
        ),
        Product(
            id = "PROD-008",
            nameNp = "गिरिराज जीवित कुखुरा",
            nameEn = "Giriraja Free-Range Chicken (Live)",
            category = ProductCategory.POULTRY,
            retailPrice = 480.0,
            wholesalePrice = 420.0,
            unit = PriceUnit.KG,
            stockQuantity = 220.0,
            minStockThreshold = 40.0,
            descriptionNp = "प्राकृतिक वातावरणमा हुर्काइएका स्वादिला गिरिराज कुखुरा। रोगमुक्त र स्वस्थ।",
            descriptionEn = "Healthy free-range Giriraja poultry birds, raised on natural farm grains.",
            sku = "UAF-CHK-GIRI",
            isOrganicCertified = true
        ),
        Product(
            id = "PROD-009",
            nameNp = "कालो कडकनाथ कुखुरा",
            nameEn = "Kadaknath Black Chicken",
            category = ProductCategory.POULTRY,
            retailPrice = 950.0,
            wholesalePrice = 850.0,
            unit = PriceUnit.KG,
            stockQuantity = 45.0,
            minStockThreshold = 10.0,
            descriptionNp = "औषधीय गुणयुक्त, उच्च प्रोटिन र कम चिल्लो भएको असली कडकनाथ कुखुरा।",
            descriptionEn = "Prized black meat poultry rich in protein and low in cholesterol.",
            sku = "UAF-CHK-KADAK",
            isOrganicCertified = true
        ),
        Product(
            id = "PROD-010",
            nameNp = "१ दिने स्वस्थ गिरिराज चल्ला",
            nameEn = "Day-Old Giriraja Chicks",
            category = ProductCategory.POULTRY,
            retailPrice = 75.0,
            wholesalePrice = 65.0,
            unit = PriceUnit.PIECE,
            stockQuantity = 500.0,
            minStockThreshold = 100.0,
            descriptionNp = "ह्याचरीबाट उत्पादित खोप लगाइएका १ दिने स्वस्थ र तन्दुरुस्त चल्लाहरू।",
            descriptionEn = "Vaccinated, active day-old chicks directly from farm incubator.",
            sku = "UAF-CHK-CHICK",
            isOrganicCertified = true
        ),

        // Livestock & Goats
        Product(
            id = "PROD-011",
            nameNp = "बोयर क्रस जीवित खसी",
            nameEn = "Boer Cross Goat (Live Khasi)",
            category = ProductCategory.LIVESTOCK,
            retailPrice = 780.0,
            wholesalePrice = 720.0,
            unit = PriceUnit.KG,
            stockQuantity = 38.0,
            minStockThreshold = 8.0,
            descriptionNp = "वैज्ञानिक पद्धति र हरियो घाँसमा हुर्काइएका मोटो र स्वस्थ बोयर क्रस खसी।",
            descriptionEn = "Well-fed Boer cross male goats fed on green silage and farm fodder.",
            sku = "UAF-GOAT-BOER",
            isOrganicCertified = true
        ),
        Product(
            id = "PROD-012",
            nameNp = "बीउको लागि बोयर बोका",
            nameEn = "Breeding Boer Buck (Pure/Cross)",
            category = ProductCategory.LIVESTOCK,
            retailPrice = 35000.0,
            wholesalePrice = 32000.0,
            unit = PriceUnit.PIECE,
            stockQuantity = 5.0,
            minStockThreshold = 2.0,
            descriptionNp = "प्रजननका लागि उच्च गुणस्तरीय, प्रमाणित वंशावली भएको बोयर बोका।",
            descriptionEn = "Certified breeding buck for herd improvement and genetic vigor.",
            sku = "UAF-GOAT-BREED",
            isOrganicCertified = true
        ),

        // Fresh Organic Veggies
        Product(
            id = "PROD-013",
            nameNp = "अर्गानिक डल्ले अकबरे खुर्सानी",
            nameEn = "Organic Akabare Chilli (Dalle)",
            category = ProductCategory.VEGETABLES,
            retailPrice = 450.0,
            wholesalePrice = 380.0,
            unit = PriceUnit.KG,
            stockQuantity = 60.0,
            minStockThreshold = 15.0,
            descriptionNp = "ललितपुरको पहाडी हावापानीमा बिना रासायनिक विषादी फलाइएको पिरो अकबरे खुर्सानी।",
            descriptionEn = "Fiery red Dalle round chillies grown with 100% bio-inputs.",
            sku = "UAF-VEG-AKAB",
            isOrganicCertified = true
        ),
        Product(
            id = "PROD-014",
            nameNp = "ताजा पालुङ्गो साग",
            nameEn = "Fresh Organic Spinach (Palungo)",
            category = ProductCategory.VEGETABLES,
            retailPrice = 90.0,
            wholesalePrice = 75.0,
            unit = PriceUnit.KG,
            stockQuantity = 40.0,
            minStockThreshold = 10.0,
            descriptionNp = "बिहान टिपिएको कलिलो र हरियो अर्गानिक पालुङ्गो साग।",
            descriptionEn = "Crisp, tender organic spinach picked fresh at dawn.",
            sku = "UAF-VEG-SPIN",
            isOrganicCertified = true
        ),
        Product(
            id = "PROD-015",
            nameNp = "अर्गानिक स्थानीय गोलभेँडा",
            nameEn = "Organic Farm Tomatoes",
            category = ProductCategory.VEGETABLES,
            retailPrice = 85.0,
            wholesalePrice = 70.0,
            unit = PriceUnit.KG,
            stockQuantity = 110.0,
            minStockThreshold = 25.0,
            descriptionNp = "टनलमा प्राङ्गारिक मल प्रयोग गरी फलाइएको स्वादिलो लोकल गोलभेँडा।",
            descriptionEn = "Juicy vine-ripened organic tomatoes grown in farm polyhouses.",
            sku = "UAF-VEG-TOM",
            isOrganicCertified = true
        ),

        // Fertilizer & Feed
        Product(
            id = "PROD-016",
            nameNp = "गँड्यौला मल (भर्मिकम्पोस्ट)",
            nameEn = "Vermicompost Organic Fertilizer",
            category = ProductCategory.FERTILIZER_FEED,
            retailPrice = 900.0,
            wholesalePrice = 780.0,
            unit = PriceUnit.BAG_50KG,
            stockQuantity = 75.0,
            minStockThreshold = 20.0,
            descriptionNp = "उच्च गुणस्तरको पोषक तत्वले भरिपूर्ण गँड्यौला मल। ५० केजीको सिलबन्दी बोरा।",
            descriptionEn = "Premium vermicompost bio-fertilizer packed in durable 50kg bags.",
            sku = "UAF-FERT-VERM50",
            isOrganicCertified = true
        ),
        Product(
            id = "PROD-017",
            nameNp = "सडेको गाईको प्राङ्गारिक कम्पोस्ट",
            nameEn = "Decomposed Cow Manure (Compost)",
            category = ProductCategory.FERTILIZER_FEED,
            retailPrice = 450.0,
            wholesalePrice = 390.0,
            unit = PriceUnit.BAG_25KG,
            stockQuantity = 90.0,
            minStockThreshold = 20.0,
            descriptionNp = "पूर्ण रूपमा सडेको, गन्ध नआउने उच्च प्राङ्गारिक पदार्थयुक्त २५ केजी मल।",
            descriptionEn = "Fully decomposed farm manure compost, odor-free, 25kg packaging.",
            sku = "UAF-FERT-MAN25",
            isOrganicCertified = true
        ),
        Product(
            id = "PROD-018",
            nameNp = "कुखुराको सन्तुलित दाना (B-2 फिनिसर)",
            nameEn = "Balanced Poultry Feed (B-2 Finisher)",
            category = ProductCategory.FERTILIZER_FEED,
            retailPrice = 3600.0,
            wholesalePrice = 3350.0,
            unit = PriceUnit.BAG_50KG,
            stockQuantity = 32.0,
            minStockThreshold = 10.0,
            descriptionNp = "मकै, भटमास र मिनरल्स मिसाइएको उच्च पोषक तत्वयुक्त कुखुराको दाना।",
            descriptionEn = "Quality formulated finisher poultry feed with balanced vitamins & minerals.",
            sku = "UAF-FEED-B2",
            isOrganicCertified = false
        )
    )

    private val initialOrders = listOf(
        FarmOrder(
            orderId = "UAF-ORD-9021",
            customerName = "राम शरण श्रेष्ठ (Ram Sharan)",
            customerPhone = "9841234567",
            deliveryAddress = "पाटन ढोका, ललितपुर (Patan Dhoka, Lalitpur)",
            items = listOf(
                CartItem(
                    product = initialProducts[0], // Cow Milk
                    quantity = 10.0
                ),
                CartItem(
                    product = initialProducts[2], // Pure Ghee
                    quantity = 2.0
                ),
                CartItem(
                    product = initialProducts[5], // Organic Eggs
                    quantity = 2.0
                )
            ),
            totalAmount = 5300.0,
            orderDate = "2026-10-12 09:30 AM",
            status = OrderStatus.VERIFIED,
            bankTransfer = BankTransferDetails(
                senderBankName = "मुक्तिनाथ विकास बैंक (Muktinath Bikas Bank)",
                senderAccountName = "Ram Sharan Shrestha",
                transactionRefNumber = "MNB-TXN-8849201",
                transferredAmount = 5300.0,
                transferDate = "2026-10-12 09:40 AM",
                remarks = "Paid via Muktinath Mobile Banking for milk & ghee"
            ),
            notes = "बिहान ७ बजे भित्र डेलिभरी गर्नुपर्ने"
        ),
        FarmOrder(
            orderId = "UAF-ORD-9022",
            customerName = "सुनिता थापा (Sunita Thapa)",
            customerPhone = "9851123498",
            deliveryAddress = "इमाडोल, महालक्ष्मी न.पा., ललितपुर",
            items = listOf(
                CartItem(
                    product = initialProducts[12], // Akabare Chilli
                    quantity = 3.0
                ),
                CartItem(
                    product = initialProducts[15], // Vermicompost
                    quantity = 4.0
                )
            ),
            totalAmount = 4950.0,
            orderDate = "2026-10-13 02:15 PM",
            status = OrderStatus.PENDING_VERIFICATION,
            bankTransfer = BankTransferDetails(
                senderBankName = "ConnectIPS / Nabil Bank",
                senderAccountName = "Sunita Thapa",
                transactionRefNumber = "CIPS-94821034",
                transferredAmount = 4950.0,
                transferDate = "2026-10-13 02:22 PM",
                remarks = "Order #UAF-ORD-9022 bank transfer"
            ),
            notes = "भर्मिकम्पोस्ट घरको छतमा बगैँचाको लागि"
        )
    )

    private val _products = MutableStateFlow<List<Product>>(initialProducts)
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _orders = MutableStateFlow<List<FarmOrder>>(initialOrders)
    val orders: StateFlow<List<FarmOrder>> = _orders.asStateFlow()

    fun updateStock(productId: String, newQuantity: Double) {
        _products.value = _products.value.map { prod ->
            if (prod.id == productId) {
                prod.copy(stockQuantity = newQuantity.coerceAtLeast(0.0))
            } else {
                prod
            }
        }
    }

    fun updateProductPriceAndStock(productId: String, newRetailPrice: Double, newWholesalePrice: Double, newStock: Double) {
        _products.value = _products.value.map { prod ->
            if (prod.id == productId) {
                prod.copy(
                    retailPrice = newRetailPrice.coerceAtLeast(0.0),
                    wholesalePrice = newWholesalePrice.coerceAtLeast(0.0),
                    stockQuantity = newStock.coerceAtLeast(0.0)
                )
            } else {
                prod
            }
        }
    }

    fun adjustStock(productId: String, delta: Double) {
        _products.value = _products.value.map { prod ->
            if (prod.id == productId) {
                val updated = (prod.stockQuantity + delta).coerceAtLeast(0.0)
                prod.copy(stockQuantity = updated)
            } else {
                prod
            }
        }
    }

    fun addProduct(product: Product) {
        _products.value = listOf(product) + _products.value
    }

    fun deleteProduct(productId: String) {
        _products.value = _products.value.filterNot { it.id == productId }
    }

    fun createOrder(
        customerName: String,
        customerPhone: String,
        deliveryAddress: String,
        cartItems: List<CartItem>,
        bankTransfer: BankTransferDetails?,
        notes: String = ""
    ): FarmOrder {
        val totalAmount = cartItems.sumOf { it.totalAmount }
        val formatter = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
        val dateString = formatter.format(Date())
        val orderId = "UAF-ORD-${(1000..9999).random()}"

        val order = FarmOrder(
            orderId = orderId,
            customerName = customerName,
            customerPhone = customerPhone,
            deliveryAddress = deliveryAddress,
            items = cartItems,
            totalAmount = totalAmount,
            orderDate = dateString,
            status = if (bankTransfer != null) OrderStatus.PENDING_VERIFICATION else OrderStatus.PENDING_VERIFICATION,
            bankTransfer = bankTransfer,
            notes = notes
        )

        _orders.value = listOf(order) + _orders.value

        // Deduct inventory stock automatically
        cartItems.forEach { item ->
            adjustStock(item.product.id, -item.quantity)
        }

        return order
    }

    fun submitDirectBankTransferPayment(
        customerName: String,
        customerPhone: String,
        deliveryAddress: String,
        senderBankName: String,
        senderAccountName: String,
        transactionRef: String,
        amount: Double,
        remarks: String,
        associatedItems: List<CartItem> = emptyList()
    ): FarmOrder {
        val formatter = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
        val dateString = formatter.format(Date())

        val bankTransfer = BankTransferDetails(
            senderBankName = senderBankName,
            senderAccountName = senderAccountName,
            transactionRefNumber = transactionRef,
            transferredAmount = amount,
            transferDate = dateString,
            remarks = remarks
        )

        val orderId = "UAF-TXN-${(1000..9999).random()}"
        val order = FarmOrder(
            orderId = orderId,
            customerName = customerName,
            customerPhone = customerPhone,
            deliveryAddress = deliveryAddress,
            items = associatedItems,
            totalAmount = amount,
            orderDate = dateString,
            status = OrderStatus.PENDING_VERIFICATION,
            bankTransfer = bankTransfer,
            notes = "Direct Bank Transfer Submission: $remarks"
        )

        _orders.value = listOf(order) + _orders.value
        return order
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        _orders.value = _orders.value.map { order ->
            if (order.orderId == orderId) {
                order.copy(status = newStatus)
            } else {
                order
            }
        }
    }

    // --- OFFICIAL DOCUMENTS & PERMITS DATABASE ---
    private val _documents = MutableStateFlow<List<OfficialDocument>>(DefaultOfficialDocuments.documents)
    val documents: StateFlow<List<OfficialDocument>> = _documents.asStateFlow()

    fun addDocument(document: OfficialDocument) {
        _documents.value = listOf(document) + _documents.value
    }

    fun updateDocument(document: OfficialDocument) {
        _documents.value = _documents.value.map {
            if (it.id == document.id) document else it
        }
    }

    fun deleteDocument(documentId: String) {
        _documents.value = _documents.value.filter { it.id != documentId }
    }
}
