package com.example.model

enum class ProductCategory(val displayNameNp: String, val displayNameEn: String, val iconEmoji: String) {
    ALL("सबै", "All", "🌾"),
    DAIRY("दुग्धजन्य", "Dairy", "🥛"),
    POULTRY("कुखुरा तथा अण्डा", "Poultry & Eggs", "🥚"),
    LIVESTOCK("बाख्रा तथा पशु", "Goat & Livestock", "🐐"),
    VEGETABLES("ताजा तरकारी", "Organic Veggies", "🥬"),
    FERTILIZER_FEED("मल तथा दाना", "Bio-Fertilizer & Feed", "🌱")
}

enum class PriceUnit(val labelNp: String, val labelEn: String) {
    LITRE("प्रति लिटर", "per Litre"),
    KG("प्रति केजी", "per Kg"),
    CRATE("प्रति क्रेट (३० वटा)", "per Crate (30 pcs)"),
    PIECE("प्रति गोटा", "per Piece"),
    BAG_50KG("प्रति ५० केजी बोरा", "per 50kg Bag"),
    BAG_25KG("प्रति २५ केजी बोरा", "per 25kg Bag"),
    PAIR("प्रति जोडी", "per Pair")
}

data class Product(
    val id: String,
    val nameNp: String,
    val nameEn: String,
    val category: ProductCategory,
    val retailPrice: Double,
    val wholesalePrice: Double,
    val unit: PriceUnit,
    val stockQuantity: Double,
    val minStockThreshold: Double = 10.0,
    val descriptionNp: String,
    val descriptionEn: String,
    val sku: String,
    val isOrganicCertified: Boolean = true
) {
    val isLowStock: Boolean
        get() = stockQuantity > 0 && stockQuantity <= minStockThreshold

    val isOutOfStock: Boolean
        get() = stockQuantity <= 0
}

enum class OrderStatus(val labelNp: String, val labelEn: String) {
    PENDING_VERIFICATION("प्रमाणीकरण बाँकी", "Pending Verification"),
    VERIFIED("भुक्तानी प्रमाणीकृत", "Payment Verified"),
    PROCESSING("तयारी हुँदै", "Processing"),
    DISPATCHED("डेलिभरीमा छ", "Dispatched"),
    COMPLETED("सम्पन्न भयो", "Completed")
}

data class CartItem(
    val product: Product,
    val quantity: Double
) {
    val totalAmount: Double
        get() = product.retailPrice * quantity
}

data class BankTransferDetails(
    val senderBankName: String,
    val senderAccountName: String,
    val transactionRefNumber: String,
    val transferredAmount: Double,
    val transferDate: String,
    val remarks: String = ""
)

data class FarmOrder(
    val orderId: String,
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val items: List<CartItem>,
    val totalAmount: Double,
    val orderDate: String,
    val status: OrderStatus,
    val bankTransfer: BankTransferDetails? = null,
    val notes: String = ""
)

object FarmOfficialInfo {
    const val FARM_NAME_NP = "ललितपुर युनाइटेड एग्रो फार्म प्रा. लि."
    const val FARM_NAME_EN = "Lalitpur United Agro Farm Pvt. Ltd."
    const val FARM_SHORT_NAME = "युनाइटेड एग्रो फार्म (United Agro Farm)"
    const val LOCATION_NP = "ललितपुर, बागमती प्रदेश, नेपाल"
    const val LOCATION_EN = "Lalitpur, Bagmati Province, Nepal"
    const val PHONE_PRIMARY = "+977 9851000000"
    const val EMAIL = "kalyan.nec@gmail.com"
    const val REGISTRATION_NO = "249870/077/078"
    const val PAN_VAT_NO = "609823451"

    // Official Bank Information as requested
    const val BANK_NAME_NP = "मुक्तिनाथ विकास बैंक लिमिटेड"
    const val BANK_NAME_EN = "Muktinath Bikas Bank Ltd."
    const val BANK_ACCOUNT_NAME_NP = "ललितपुर युनाइटेड एग्रो फार्म प्रा. लि."
    const val BANK_ACCOUNT_NAME_EN = "Lalitpur United Agro Farm Pvt. Ltd."
    const val BANK_ACCOUNT_NO = "09610201478560000001"
    const val BANK_ACCOUNT_NO_NP = "०९६१०२०१४७८५६००००००१"
    const val BANK_BRANCH_NP = "ललितपुर शाखा (Lalitpur Market)"
    const val BANK_BRANCH_EN = "Lalitpur Branch"
    const val BANK_SWIFT = "MNBBNPKM"

    // Official eSewa Wallet Information
    const val ESEWA_NAME_NP = "ईसेवा वालेट (eSewa)"
    const val ESEWA_NAME_EN = "eSewa Digital Wallet"
    const val ESEWA_ID = "9848092790"
    const val ESEWA_ID_NP = "९८४८०९२७९०"
    const val ESEWA_ACCOUNT_NAME = "Lalitpur United Agro Farm Pvt. Ltd."
    const val ESTD_YEAR = "Estd. 2021"
}

enum class UserRole(val labelNp: String, val labelEn: String) {
    STAFF("कर्मचारी / व्यवस्थापक", "Farm Staff / Manager"),
    CUSTOMER("किसान तथा ग्राहक", "Farmer / Customer"),
    ADMIN("प्रमुख व्यवस्थापक", "Farm Administrator")
}

data class UserProfile(
    val userId: String,
    val displayName: String,
    val email: String,
    val phone: String = "",
    val role: UserRole = UserRole.STAFF
)

enum class AttendanceStatus(val labelNp: String, val labelEn: String, val colorHex: Long) {
    PRESENT("उपस्थित", "Present", 0xFF2E7D32),
    ABSENT("अनुपस्थित", "Absent", 0xFFD32F2F),
    HALF_DAY("आधा दिन", "Half Day", 0xFFF57C00),
    PAID_LEAVE("बिदा", "Paid Leave", 0xFF1976D2),
    OVERTIME("ओभरटाइम", "Overtime", 0xFF7B1FA2)
}

data class Employee(
    val id: String,
    val name: String,
    val role: String,
    val phone: String,
    val dailyWage: Double = 800.0,
    val monthlySalary: Double = 24000.0,
    val joinDate: String = "2024-01-01",
    val isActive: Boolean = true
)

data class AttendanceRecord(
    val id: String,
    val employeeId: String,
    val employeeName: String,
    val date: String, // "YYYY-MM-DD"
    val monthYear: String, // "YYYY-MM"
    val status: AttendanceStatus = AttendanceStatus.PRESENT,
    val checkInTime: String = "07:00 AM",
    val checkOutTime: String = "05:00 PM",
    val remarks: String = "",
    val recordedBy: String = ""
)

data class EmployeeMonthlySummary(
    val employee: Employee,
    val monthYear: String,
    val totalWorkingDays: Int,
    val presentCount: Int,
    val halfDayCount: Int,
    val absentCount: Int,
    val leaveCount: Int,
    val overtimeCount: Int,
    val calculatedPay: Double
) {
    val attendancePercentage: Double
        get() = if (totalWorkingDays > 0) {
            ((presentCount + (halfDayCount * 0.5) + leaveCount) / totalWorkingDays) * 100.0
        } else 0.0
}

enum class TransactionType(val labelNp: String, val labelEn: String) {
    INCOME("आम्दानी", "Income"),
    EXPENSE("खर्च", "Expense")
}

enum class TransactionCategory(val type: TransactionType, val labelNp: String, val labelEn: String, val emoji: String) {
    // Incomes
    MILK_SALES(TransactionType.INCOME, "दूध बिक्री", "Milk Sales", "🥛"),
    POULTRY_EGGS(TransactionType.INCOME, "कुखुरा तथा अण्डा बिक्री", "Poultry & Eggs", "🥚"),
    LIVESTOCK_SALES(TransactionType.INCOME, "बाख्रा तथा पशु बिक्री", "Livestock Sales", "🐐"),
    VEGGIE_SALES(TransactionType.INCOME, "ताजा तरकारी बिक्री", "Veggies Sales", "🥬"),
    FERTILIZER_SALES(TransactionType.INCOME, "जैविक मल तथा दाना", "Bio-Fertilizer", "🌱"),
    ORDER_PAYMENT(TransactionType.INCOME, "ग्राहक अर्डर भुक्तानी", "Customer Order Payment", "🛒"),
    OTHER_INCOME(TransactionType.INCOME, "अन्य आम्दानी", "Other Revenue", "💰"),

    // Expenses
    FEED_FORAGE(TransactionType.EXPENSE, "दाना तथा घाँस खर्च", "Feed & Forage", "🌾"),
    VET_MEDICINE(TransactionType.EXPENSE, "औषधि तथा खोप", "Veterinary & Medicine", "💉"),
    WAGES_SALARY(TransactionType.EXPENSE, "कर्मचारी तलब तथा ज्याला", "Staff Wages", "👥"),
    ELECTRICITY_WATER(TransactionType.EXPENSE, "बिजुली तथा सिँचाइ", "Electricity & Water", "💡"),
    FUEL_TRANSPORT(TransactionType.EXPENSE, "इन्धन तथा ढुवानी", "Fuel & Transport", "🚚"),
    MAINTENANCE(TransactionType.EXPENSE, "मेशिनरी तथा गोठ मर्मत", "Farm Maintenance", "🔧"),
    SEEDS_AGRO(TransactionType.EXPENSE, "बिउ तथा कृषि सामाग्री", "Seeds & Agro Inputs", "🌱"),
    MISC_EXPENSE(TransactionType.EXPENSE, "विविध फार्म खर्च", "Miscellaneous", "📝")
}

enum class TransactionPaymentMode(val labelNp: String, val labelEn: String) {
    CASH("नगद", "Cash"),
    BANK_TRANSFER("मुक्तिनाथ विकास बैंक", "Muktinath Bikas Bank"),
    ESEWA("ईसेवा वालेट", "eSewa Wallet"),
    OTHER("अन्य", "Other")
}

data class FarmTransaction(
    val id: String,
    val type: TransactionType,
    val category: TransactionCategory,
    val amount: Double,
    val date: String, // "YYYY-MM-DD"
    val monthYear: String, // "YYYY-MM"
    val paymentMode: TransactionPaymentMode = TransactionPaymentMode.CASH,
    val partyName: String = "",
    val referenceNo: String = "",
    val description: String = "",
    val recordedBy: String = ""
)

data class FarmBalanceSheetLedger(
    val id: String = "primary_ledger",
    val cashInHand: Double = 85000.0,
    val bankBalanceMuktinath: Double = 425000.0,
    val esewaBalance: Double = 35000.0,
    val accountsReceivable: Double = 48000.0,
    val fixedAssetsEquipment: Double = 650000.0, // Chiller, Milking machines, generator
    val fixedAssetsLandShed: Double = 1200000.0, // Greenhouse, sheds, water storage
    val livestockValuation: Double = 850000.0, // Dairy cows, goats, poultry birds
    val accountsPayable: Double = 65000.0, // Feed supplier credits
    val accruedSalaries: Double = 45000.0, // Monthly salaries pending
    val shortTermLoans: Double = 250000.0, // Agro cooperative loan
    val ownerCapital: Double = 2500000.0, // Initial founding equity
    val asOfDate: String = "2026-10-14"
)

data class BalanceSheetSummary(
    val asOfDate: String,
    val cashInHand: Double,
    val bankBalanceMuktinath: Double,
    val esewaBalance: Double,
    val accountsReceivable: Double,
    val inventoryStockValue: Double,
    val fixedAssetsEquipment: Double,
    val fixedAssetsLandShed: Double,
    val livestockValuation: Double,
    val accountsPayable: Double,
    val accruedSalaries: Double,
    val shortTermLoans: Double,
    val ownerCapital: Double,
    val netFarmProfit: Double
) {
    val totalCurrentAssets: Double
        get() = cashInHand + bankBalanceMuktinath + esewaBalance + accountsReceivable + inventoryStockValue

    val totalFixedAssets: Double
        get() = fixedAssetsEquipment + fixedAssetsLandShed + livestockValuation

    val totalAssets: Double
        get() = totalCurrentAssets + totalFixedAssets

    val totalLiabilities: Double
        get() = accountsPayable + accruedSalaries + shortTermLoans

    val totalEquity: Double
        get() = ownerCapital + netFarmProfit

    val isBalanced: Boolean
        get() = kotlin.math.abs(totalAssets - (totalLiabilities + totalEquity)) < 1.0
}

data class DailyTrendPoint(
    val day: Int,
    val dayLabel: String,
    val dateStr: String,
    val income: Double,
    val expense: Double,
    val net: Double,
    val inventoryValue: Double
)

data class MonthlyDashboardSummary(
    val monthYear: String,
    val monthLabelNp: String,
    val monthLabelEn: String,
    val totalIncome: Double,
    val totalExpense: Double,
    val netCashFlow: Double,
    val currentInventoryValue: Double,
    val startInventoryValue: Double = 0.0,
    val inventoryValueChange: Double = 0.0,
    val totalStockUnits: Double = 0.0,
    val lowStockCount: Int = 0,
    val dailyTrends: List<DailyTrendPoint> = emptyList(),
    val topIncomeCategory: String = "",
    val topExpenseCategory: String = "",
    val highestIncomeDay: String = ""
)

enum class DocumentCategory(val labelNp: String, val labelEn: String) {
    REGISTRATION("कम्पनी दर्ता", "Company Registration"),
    TAX_VAT("कर तथा प्यान/भ्याट", "Tax & PAN/VAT"),
    MUNICIPAL_PERMIT("स्थानीय तह इजाजत", "Municipal Operating Permit"),
    AGRICULTURE_LIVESTOCK("कृषि तथा पशुपन्छी पालन", "Agro & Livestock License"),
    FOOD_SAFETY_DFTQC("खाद्य गुणस्तर अनुज्ञापत्र", "Food Safety (DFTQC)"),
    BANKING_FINANCE("बैंकिङ तथा वित्तीय अख्तियारी", "Banking & Finance Mandates"),
    ENVIRONMENTAL("वातावरणीय स्वीकृति", "Environmental Clearance"),
    LAND_LEASE("जग्गा तथा फार्म लिज", "Land & Shed Lease")
}

enum class DocumentStatus(val labelNp: String, val labelEn: String, val colorHex: Long) {
    ACTIVE("सक्रिय / मान्य", "Active & Valid", 0xFF2E7D32),
    RENEWAL_DUE("नवीकरण नजिकिएको", "Renewal Due", 0xFFE65100),
    PERMANENT("स्थायी / आजीवन", "Lifetime Valid", 0xFF1565C0),
    EXPIRED("म्याद समाप्त", "Expired", 0xFFC62828)
}

data class OfficialDocument(
    val id: String,
    val titleNp: String,
    val titleEn: String,
    val documentNumber: String,
    val category: DocumentCategory,
    val issuingAuthorityNp: String,
    val issuingAuthorityEn: String,
    val issueDate: String,
    val expiryDate: String = "स्थायी / आजीवन",
    val status: DocumentStatus = DocumentStatus.ACTIVE,
    val isVerified: Boolean = true,
    val digitalRefCode: String = "",
    val summaryPointsNp: List<String> = emptyList(),
    val summaryPointsEn: List<String> = emptyList(),
    val contactOfficer: String = "",
    val notes: String = "",
    val userId: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
)

object DefaultOfficialDocuments {
    val documents = listOf(
        OfficialDocument(
            id = "DOC-REG-01",
            titleNp = "कम्पनी दर्ता प्रमाणपत्र",
            titleEn = "Certificate of Company Incorporation",
            documentNumber = "249870/077/078",
            category = DocumentCategory.REGISTRATION,
            issuingAuthorityNp = "नेपाल सरकार, उद्योग, वाणिज्य तथा आपूर्ति मन्त्रालय, कम्पनी रजिष्ट्रारको कार्यालय, त्रिपुरेश्वर, काठमाडौं",
            issuingAuthorityEn = "Government of Nepal, Ministry of Industry, Commerce and Supplies, Office of the Company Registrar, Tripureshwor, Kathmandu",
            issueDate = "२०७७/०८/१५ (2020-11-30)",
            expiryDate = "आजीवन / स्थायी (Permanent)",
            status = DocumentStatus.PERMANENT,
            isVerified = true,
            digitalRefCode = "OCR-NEP-249870-LAL",
            summaryPointsNp = listOf(
                "कम्पनी ऐन, २०६३ को दफा ५ (१) बमोजिम प्रा. लि. कम्पनीको रूपमा दर्ता।",
                "अधिकृत पूँजी: रु. १,५०,००,०००/- (एक करोड पचास लाख)।",
                "जारी पूँजी: रु. १,००,००,०००/- (एक करोड)।",
                "व्यावसायिक उद्देश्य: उच्च गुणस्तरको उन्नत गाई-भैँसी पालन, शुद्ध दुग्ध उत्पादन, कुखुरा, बाख्रा तथा अर्गानिक तरकारी खेती।"
            ),
            summaryPointsEn = listOf(
                "Incorporated as a Private Limited Company under Section 5(1) of Company Act 2063.",
                "Authorized Capital: NPR 15,000,000 | Issued Capital: NPR 10,000,000.",
                "Principal Activities: Modern commercial dairy farming, poultry, livestock breeding, and organic agricultural production."
            ),
            contactOfficer = "रजिष्ट्रार अधिकृत - कम्पनी रजिष्ट्रार कार्यालय",
            notes = "कम्पनीको मूल दर्ता प्रमाणपत्र। स्थायी मान्यता प्राप्त।"
        ),
        OfficialDocument(
            id = "DOC-PAN-02",
            titleNp = "स्थायी लेखा नम्बर (PAN) तथा कर दर्ता प्रमाणपत्र",
            titleEn = "Permanent Account Number (PAN) Certificate",
            documentNumber = "609823451",
            category = DocumentCategory.TAX_VAT,
            issuingAuthorityNp = "नेपाल सरकार, अर्थ मन्त्रालय, आन्तरिक राजस्व विभाग, आन्तरिक राजस्व कार्यालय, ललितपुर",
            issuingAuthorityEn = "Government of Nepal, Ministry of Finance, Inland Revenue Department, IRO Lalitpur",
            issueDate = "२०७७/०८/२२ (2020-12-07)",
            expiryDate = "स्थायी / आजीवन (Permanent)",
            status = DocumentStatus.PERMANENT,
            isVerified = true,
            digitalRefCode = "IRD-LAL-PAN-609823451",
            summaryPointsNp = listOf(
                "आन्तरिक राजस्व ऐन अनुसार करदाता पहिचान नं. ६०९८२३४५१ प्रदान गरिएको।",
                "दर्ता भएको नाम: ललितपुर युनाइटेड एग्रो फार्म प्रा. लि.।",
                "आयकर ऐन, २०५८ तथा मूल्य अभिवृद्धि कर ऐन बमोजिम दर्ता प्रमाणित।",
                "कृषि उत्पादन तथा दुग्ध बिक्रीतर्फ प्रचलित कानुन बमोजिम कर छुट सुविधा अन्तर्गत।"
            ),
            summaryPointsEn = listOf(
                "Taxpayer Identification PAN 609823451 issued under Nepal Income Tax Act 2058.",
                "Registered Entity: Lalitpur United Agro Farm Pvt. Ltd.",
                "Agricultural farm produce & raw dairy tax concession entitlement active."
            ),
            contactOfficer = "कर अधिकृत - आन्तरिक राजस्व कार्यालय, ललितपुर",
            notes = "नियमित कर विवरण तथा अडिट प्रतिवेदन दाखिला गरिएको छ।"
        ),
        OfficialDocument(
            id = "DOC-MUN-03",
            titleNp = "ललितपुर महानगरपालिका व्यवसाय दर्ता तथा सञ्चालन इजाजत",
            titleEn = "Lalitpur Metropolitan City Business Operating Permit",
            documentNumber = "LMC-WRD21-884/078",
            category = DocumentCategory.MUNICIPAL_PERMIT,
            issuingAuthorityNp = "ललितपुर महानगरपालिका नगर कार्यपालिकाको कार्यालय, वडा नं. २१ वडा कार्यालय, ललितपुर",
            issuingAuthorityEn = "Lalitpur Metropolitan City Office of Municipal Executive, Ward No. 21 Office, Lalitpur",
            issueDate = "२०७८/०१/१२ (2021-04-25)",
            expiryDate = "२०८२/०३/३१ (2025-07-15)",
            status = DocumentStatus.RENEWAL_DUE,
            isVerified = true,
            digitalRefCode = "LMC-PERMIT-21-0884",
            summaryPointsNp = listOf(
                "ललितपुर महानगरपालिका स्थानीय सरकार सञ्चालन ऐन बमोजिम व्यावसायिक फार्म सञ्चालन इजाजत।",
                "स्थान: वडा नं. २१, ललितपुर (साविक खोकना/चापागाउँ कृषि क्षेत्र)।",
                "व्यवसाय वर्गीकरण: ठूला कृषि तथा पशुपालन उद्योग।",
                "आर्थिक वर्ष २०८१/०८२ सम्मको व्यवसाय कर तथा स्थानीय शुल्क चुक्ता गरिएको।"
            ),
            summaryPointsEn = listOf(
                "Authorized Commercial Agro Farm Operating Permit under Local Government Operation Act.",
                "Location: Ward 21, Lalitpur Metropolitan City.",
                "Annual Municipal Business Tax up-to-date through fiscal year 2081/82."
            ),
            contactOfficer = "वडा सचिव तथा राजस्व अधिकृत, वडा नं. २१, ललितपुर",
            notes = "प्रत्येक आर्थिक वर्षको मसान्तभित्र स्थानीय वडा कार्यालयमा नवीकरण गराउनुपर्ने।"
        ),
        OfficialDocument(
            id = "DOC-AGR-04",
            titleNp = "व्यावसायिक गाई-भैँसी तथा पशुपन्छी फार्म सञ्चालन अनुमति पत्र",
            titleEn = "Commercial Livestock & Poultry Farming License",
            documentNumber = "DLS-BAG-2078-412",
            category = DocumentCategory.AGRICULTURE_LIVESTOCK,
            issuingAuthorityNp = "बागमती प्रदेश सरकार, कृषि तथा पशुपन्छी विकास मन्त्रालय, पशुपन्छी विकास निर्देशनालय, हेटौंडा / ललितपुर",
            issuingAuthorityEn = "Bagmati Province Government, Ministry of Agriculture and Livestock Development, Directorate of Livestock Development",
            issueDate = "२०७८/०२/२० (2021-06-03)",
            expiryDate = "२०८३/०२/१९ (2026-06-02)",
            status = DocumentStatus.ACTIVE,
            isVerified = true,
            digitalRefCode = "MOALD-BAG-AGR-0412",
            summaryPointsNp = listOf(
                "पशु स्वास्थ्य तथा पशु सेवा ऐन, २०५५ अनुसार व्यावसायिक फार्म सञ्चालन अनुमति।",
                "अनुमोदित क्षमता: उन्नत गाई (होलस्टिन र जर्सी) ४० वटा, मुर्रा भैँसी ३० वटा, ब्रोइलर/लोकल कुखुरा ३,००० गोटा, बाख्रा ६० गोटा।",
                "जैव-सुरक्षा (Bio-security) मापदण्ड तथा नियमित भ्याक्सिनेसन अनुगमन तालिका स्वीकृत।",
                "आधुनिक वैज्ञानिक गोठ तथा ड्रेनेज मापदण्ड पालना गरिएको प्रमाणित।"
            ),
            summaryPointsEn = listOf(
                "Commercial livestock farm license under Animal Health & Livestock Services Act 2055.",
                "Approved Capacity: 40 Holstein/Jersey cattle, 30 Murrah buffaloes, 3000 poultry, 60 goats.",
                "Full compliance with biosecurity protocols and bio-waste isolation standards."
            ),
            contactOfficer = "वरिष्ठ पशु विकास अधिकृत - बागमती पशुपन्छी निर्देशनालय",
            notes = "नियमित पशु स्वास्थ्य परीक्षण प्रतिवेदन जिल्ला पशु सेवा कार्यालयमा पेश गरिँदै आएको।"
        ),
        OfficialDocument(
            id = "DOC-DFTQC-05",
            titleNp = "दुग्ध संकलन, चिस्यान तथा प्रशोधन अनुज्ञापत्र (DFTQC License)",
            titleEn = "Dairy Processing & Milk Quality License (DFTQC)",
            documentNumber = "DFTQC-LAL-MILK-0881",
            category = DocumentCategory.FOOD_SAFETY_DFTQC,
            issuingAuthorityNp = "नेपाल सरकार, कृषि तथा पशुपन्छी विकास मन्त्रालय, खाद्य प्रविधि तथा गुणस्तर नियन्त्रण विभाग, बबरमहल, काठमाडौं",
            issuingAuthorityEn = "Government of Nepal, Ministry of Agriculture, Department of Food Technology and Quality Control (DFTQC)",
            issueDate = "२०७८/०४/०५ (2021-07-20)",
            expiryDate = "२०८३/०४/०४ (2026-07-19)",
            status = DocumentStatus.ACTIVE,
            isVerified = true,
            digitalRefCode = "DFTQC-CERT-0881-DAIRY",
            summaryPointsNp = listOf(
                "खाद्य ऐन, २०२३ तथा खाद्य नियमावली, २०२७ बमोजिम दुग्ध प्रशोधन तथा वितरण अनुज्ञापत्र।",
                "उत्पादनहरू: पास्चुराइज्ड शुद्ध गाई-भैँसीको दूध, दानेदार शुद्ध घिउ, पनिर, खुवा र मटका दही।",
                "फ्याट (FAT) तथा एस.एन.एफ (SNF) ल्याब परीक्षण मापदण्ड पूर्ण पालना।",
                "कुनै पनि रसायन वा बाह्य मिसावट नभएको शुद्ध खाद्य उत्पादनको प्रत्याभूति।"
            ),
            summaryPointsEn = listOf(
                "Dairy processing and food hygienic distribution license under Nepal Food Act 2023.",
                "Covers fresh milk, traditional ghee, artisanal paneer, and clay-pot curd.",
                "Mandatory batch testing for fat content, SNF, and zero-adulteration compliance."
            ),
            contactOfficer = "खाद्य अनुसन्धान अधिकृत - खाद्य प्रविधि तथा गुणस्तर विभाग",
            notes = "प्रत्येक महिना दूध र घिउको नमुना परीक्षण रिपोर्ट अभिलेख राखिएको छ।"
        ),
        OfficialDocument(
            id = "DOC-ORG-06",
            titleNp = "नेपाल राष्ट्रिय प्राङ्गारिक कृषि उत्पादन प्रमाणीकरण प्रमाणपत्र",
            titleEn = "Nepal National Organic Agriculture Production Certificate",
            documentNumber = "ORG-NEP-2022-094",
            category = DocumentCategory.AGRICULTURE_LIVESTOCK,
            issuingAuthorityNp = "नेपाल सरकार, राष्ट्रिय प्राङ्गारिक कृषि प्रमाणीकरण समिति तथा जैविक प्रमाणीकरण बोर्ड, हरिहरभवन, ललितपुर",
            issuingAuthorityEn = "National Organic Agriculture Certification Board & Organic Standard Inspection Agency, Nepal",
            issueDate = "२०७८/११/१० (2022-02-22)",
            expiryDate = "२०८३/११/०९ (2027-02-21)",
            status = DocumentStatus.ACTIVE,
            isVerified = true,
            digitalRefCode = "NPL-ORGANIC-2022-094",
            summaryPointsNp = listOf(
                "अर्गानिक कृषि उत्पादन मापदण्ड अनुसार रासायनिक मल र विषादीरहित उत्पादन प्रमाणित।",
                "फार्मको आफ्नै कम्पोस्ट मल, बायोग्यास स्लरी तथा जैविक कीटनाशकको प्रयोग।",
                "प्रमाणित बाली: डल्ले अकबरे खुर्सानी, अर्गानिक सागपात, मौसमी तरकारी तथा शुद्ध मह।",
                "माटो तथा भू-स्वास्थ्य परीक्षणमा शून्य रासायनिक अवशेष प्रमाणित।"
            ),
            summaryPointsEn = listOf(
                "100% Organic farming certification free from synthetic pesticides and artificial chemical fertilizers.",
                "Utilizes on-farm vermicompost, cow dung biogas slurry, and botanical pest control.",
                "Covers Akabare chili, organic leafy greens, seasonal vegetables, and honey."
            ),
            contactOfficer = "वरिष्ठ कृषि विज्ञ - अर्गानिक बोर्ड प्रमाणीकरण शाखा",
            notes = "अर्गानिक उत्पादनको लेवलिङ तथा बारकोड प्रयोग गर्न अनुमति प्राप्त।"
        ),
        OfficialDocument(
            id = "DOC-BANK-07",
            titleNp = "मुक्तिनाथ विकास बैंक लिमिटेड आधिकारिक संस्थागत खाता सञ्चालन अख्तियारी",
            titleEn = "Muktinath Bikas Bank Corporate Account Mandate & Authorization",
            documentNumber = "MNB-LAL-CORP-09610201478560000001",
            category = DocumentCategory.BANKING_FINANCE,
            issuingAuthorityNp = "मुक्तिनाथ विकास बैंक लिमिटेड, ललितपुर शाखा कार्यालय, ललितपुर (SWIFT: MNBBNPKM)",
            issuingAuthorityEn = "Muktinath Bikas Bank Ltd., Lalitpur Branch, Lalitpur Market, Nepal (SWIFT: MNBBNPKM)",
            issueDate = "२०७७/०९/०१ (2020-12-16)",
            expiryDate = "स्थायी / आजीवन (Permanent)",
            status = DocumentStatus.PERMANENT,
            isVerified = true,
            digitalRefCode = "MNB-LAL-0961-AUTH",
            summaryPointsNp = listOf(
                "कम्पनीको नाम: ललितपुर युनाइटेड एग्रो फार्म प्रा. लि.।",
                "खाता नम्बर: ०९६१०२०१४७८५६००००००१ (09610201478560000001)।",
                "खाताको प्रकार: कर्पोरेट चल्ती खाता (Current Account)।",
                "सञ्चालक समितिको निर्णय बमोजिम आधिकारिक हस्ताक्षरकर्ता प्रमाणीकरण तथा क्यूआर भुक्तानी सुविधा सक्रिय।"
            ),
            summaryPointsEn = listOf(
                "Corporate Current Account Mandate for Lalitpur United Agro Farm Pvt. Ltd.",
                "Account No: 09610201478560000001 | Branch: Lalitpur Market (MNBBNPKM).",
                "Authorized Board Resolution signatory mandate & merchant QR settlement active."
            ),
            contactOfficer = "शाखा प्रबन्धक - मुक्तिनाथ विकास बैंक लि., ललितपुर शाखा",
            notes = "ग्राहकहरूबाट दूध, अण्डा, कुखुरा तथा मल बिक्रीको सिधा बैंकिङ भुक्तानी प्राप्त हुने आधिकारिक खाता।"
        ),
        OfficialDocument(
            id = "DOC-ESEWA-08",
            titleNp = "ईसेवा वालेट आधिकारिक व्यावसायिक मर्चेन्ट सम्झौता पत्र",
            titleEn = "eSewa Corporate Merchant Digital Payment Agreement",
            documentNumber = "ESEWA-MER-8891-AGRO",
            category = DocumentCategory.BANKING_FINANCE,
            issuingAuthorityNp = "ईसेवा लिमिटेड (eSewa Ltd.), एफवान सफ्ट ग्रुप, पुल्चोक, ललितपुर",
            issuingAuthorityEn = "eSewa Limited, F1Soft International Group, Pulchowk, Lalitpur, Nepal",
            issueDate = "२०७८/०१/०५ (2021-04-18)",
            expiryDate = "स्थायी / आजीवन (Permanent)",
            status = DocumentStatus.PERMANENT,
            isVerified = true,
            digitalRefCode = "ESEWA-9848092790-MER",
            summaryPointsNp = listOf(
                "आधिकारिक ईसेवा वालेट आईडी: ९८४८०९२७९० (9848092790)।",
                "खाताको नाम: Lalitpur United Agro Farm Pvt. Ltd.।",
                "नेपाल राष्ट्र बैंकको भुक्तानी प्रणाली निर्देशिका बमोजिम क-वर्गको डिजिटल मर्चेन्ट प्रमाणीकरण।",
                "दैनिक तथा मासिक असीमित व्यावसायिक डिजिटल कारोबार संकलन अनुमति।"
            ),
            summaryPointsEn = listOf(
                "Official eSewa Corporate Merchant ID: 9848092790.",
                "Registered Account: Lalitpur United Agro Farm Pvt. Ltd.",
                "Licensed Nepal Rastra Bank digital wallet settlement contract for farm sales."
            ),
            contactOfficer = "मर्चेन्ट रिलेसन म्यानेजर - ईसेवा लिमिटेड",
            notes = "द्रुत भुक्तानीको लागि फार्मको काउन्टर तथा वेबसाइट/एपमा डाइरेक्ट क्यूआर जोडिएको छ।"
        ),
        OfficialDocument(
            id = "DOC-LAND-09",
            titleNp = "व्यावसायिक कृषि फार्म जग्गा तथा शेड लिज सम्झौता (१५ वर्षे)",
            titleEn = "Commercial Farm Land & Cattle Shed 15-Year Lease Deed",
            documentNumber = "REG-LAL-LAND-15YR-077",
            category = DocumentCategory.LAND_LEASE,
            issuingAuthorityNp = "जग्गाधनी तथा ललितपुर युनाइटेड एग्रो फार्म प्रा. लि. बीच वडा कार्यालयको रोहवरमा प्रमाणित",
            issuingAuthorityEn = "Certified Notarized Lease Contract before Lalitpur Ward Executive & Land Revenue Notary",
            issueDate = "२०७७/०७/१५ (2020-10-31)",
            expiryDate = "२०९२/०७/१४ (2035-10-30)",
            status = DocumentStatus.ACTIVE,
            isVerified = true,
            digitalRefCode = "LAL-LEASE-15YR-077",
            summaryPointsNp = listOf(
                "कुल जग्गा क्षेत्रफल: ३५ रोपनी कृषि भू-भाग।",
                "संरचना: आधुनिक गाई-भैँसी गोठ (Cow Sheds), साइलेज पिट, दाना भण्डारण कक्ष, कामदार आवास तथा अफिस भवन।",
                "भाडा अवधि: १५ वर्ष (२०७७ देखि २०९२ सम्म नवीकरणीय)।",
                "दीर्घकालीन कृषि विकास र लगानी संरक्षणको लागि कानुनी सम्झौता दर्ता।"
            ),
            summaryPointsEn = listOf(
                "Total Agricultural Area: 35 Ropanis commercial farmland.",
                "Includes modern ventilated cattle sheds, silage pits, feed storage, staff quarters, and administrative office.",
                "15-year lease term registered through 2035 AD with renewal option."
            ),
            contactOfficer = "कानुनी सल्लाहकार तथा नोटरी पब्लिक अधिकृत",
            notes = "वार्षिक भाडा भुक्तानी प्रत्येक वर्षको श्रावण मसान्तभित्र चुक्ता गरिने सम्झौता।"
        ),
        OfficialDocument(
            id = "DOC-VET-10",
            titleNp = "पशु स्वास्थ्य, खोप तथा रोगमुक्त फार्म प्रमाणीकरण पत्र",
            titleEn = "Veterinary Herd Health & Disease-Free Certification",
            documentNumber = "VET-CL-LAL-2024-11",
            category = DocumentCategory.AGRICULTURE_LIVESTOCK,
            issuingAuthorityNp = "जिल्ला भेटेरिनरी अस्पताल तथा पशु सेवा विज्ञ केन्द्र, लगनखेल, ललितपुर",
            issuingAuthorityEn = "District Veterinary Hospital & Livestock Service Expert Center, Lagankhel, Lalitpur",
            issueDate = "२०८१/०१/१० (2024-04-22)",
            expiryDate = "२०८२/०१/०९ (2025-04-21)",
            status = DocumentStatus.ACTIVE,
            isVerified = true,
            digitalRefCode = "VET-LAL-HERD-2024",
            summaryPointsNp = listOf(
                "फार्मका सम्पूर्ण गाई तथा भैँसीहरूमा खोरेत (FMD), लम्पि स्किन (LSD) र ब्रुसेलोसिस विरुद्ध शतप्रतिशत खोप लगाइएको।",
                "नियमित जुकाको औषधि तथा टिबी (Tuberculosis) स्क्रिनिङ नेगेटिभ प्रमाणित।",
                "दूध परीक्षणमा एन्टिबायोटिक अवशेष शून्य (Zero Antibiotic Residue)।",
                "पशु स्वास्थ्य तथा फार्म स्वच्छता उच्च श्रेणीमा रहेको प्रमाणीकरण।"
            ),
            summaryPointsEn = listOf(
                "100% vaccinated against Foot & Mouth Disease (FMD), Lumpy Skin Disease (LSD), and Brucellosis.",
                "Regular deworming and negative TB herd screening certified.",
                "Zero antibiotic residues confirmed in daily raw milk sampling."
            ),
            contactOfficer = "वरिष्ठ पशु चिकित्सक (Senior Veterinary Officer), ललितपुर",
            notes = "वार्षिक अनिवार्य स्वास्थ्य अनुगमन प्रतिवेदन। अर्को निरीक्षण २०८२ वैशाखमा हुने।"
        ),
        OfficialDocument(
            id = "DOC-ENV-11",
            titleNp = "प्रारम्भिक वातावरणीय परीक्षण (IEE) तथा जैविक फोहोर व्यवस्थापन स्वीकृति",
            titleEn = "Initial Environmental Examination (IEE) & Bio-Waste Clearance",
            documentNumber = "ENV-BAG-2079-05",
            category = DocumentCategory.ENVIRONMENTAL,
            issuingAuthorityNp = "बागमती प्रदेश सरकार, उद्योग, पर्यटन, वन तथा वातावरण मन्त्रालय, हेटौंडा",
            issuingAuthorityEn = "Bagmati Province Government, Ministry of Industry, Tourism, Forest and Environment",
            issueDate = "२०७९/०३/१८ (2022-07-02)",
            expiryDate = "२०८४/०३/१७ (2027-07-01)",
            status = DocumentStatus.ACTIVE,
            isVerified = true,
            digitalRefCode = "MOFE-BAG-ENV-079",
            summaryPointsNp = listOf(
                "वातावरण संरक्षण ऐन, २०७६ बमोजिम प्रारम्भिक वातावरणीय परीक्षण (IEE) प्रतिवेदन स्वीकृत।",
                "गोबर तथा पिसाब व्यवस्थापनको लागि १०० घनमिटरको आधुनिक बायोग्यास प्लान्ट सञ्चालनमा।",
                "वरपरको बस्ती र पानीको स्रोतमा कुनै प्रतिकूल असर नपर्ने गरी स्लरी व्यवस्थापन प्रणाली जडान।",
                "जैविक मल (Organic Compost) उत्पादन गरी स्थानीय किसानहरूलाई वितरण गर्न स्वीकृत।"
            ),
            summaryPointsEn = listOf(
                "Initial Environmental Examination (IEE) cleared under Environment Protection Act 2076.",
                "100 cubic meter bio-gas plant installed for daily manure processing & energy generation.",
                "Full zero-discharge slurry treatment preserving community water tables."
            ),
            contactOfficer = "वातावरण निरीक्षक - उद्योग तथा वातावरण मन्त्रालय",
            notes = "वातावरणीय मापदण्डहरूको वार्षिक अडिट सन्तोषजनक पाइएको छ।"
        )
    )
}
