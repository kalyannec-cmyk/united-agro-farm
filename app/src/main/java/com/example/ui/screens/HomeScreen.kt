package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FarmOfficialInfo
import com.example.model.Product
import com.example.ui.components.BankDetailsCard
import com.example.ui.components.CompanyLogo
import com.example.ui.components.PaymentQrCard
import com.example.ui.theme.AgroGoldTertiary
import com.example.ui.theme.AgroGreenPrimary
import com.example.ui.viewmodel.FarmScreen

@Composable
fun HomeScreen(
    products: List<Product>,
    isEnglish: Boolean = false,
    onNavigate: (FarmScreen) -> Unit,
    onToast: (String) -> Unit
) {
    val totalStockCount = products.sumOf { it.stockQuantity }
    val lowStockCount = products.count { it.isLowStock }
    val availableCount = products.count { !it.isOutOfStock }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Hero Farm Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                AgroGreenPrimary,
                                Color(0xFF2E7D32)
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    // Official Company Logo Header (Lalitpur United Agro Farm)
                    CompanyLogo(
                        size = 80.dp,
                        showSubtext = false,
                        modifier = Modifier.testTag("hero_company_logo")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = FarmOfficialInfo.FARM_NAME_NP,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = FarmOfficialInfo.FARM_NAME_EN,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFC8E6C9),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isEnglish) FarmOfficialInfo.LOCATION_EN else FarmOfficialInfo.LOCATION_NP,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onNavigate(FarmScreen.DASHBOARD) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFD54F),
                                contentColor = Color(0xFF1B3B22)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.2f).testTag("home_dashboard_button")
                        ) {
                            Icon(imageVector = Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = if (isEnglish) "Recharts" else "ड्यासबोर्ड", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onNavigate(FarmScreen.PRICE_LIST) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.9f),
                                contentColor = AgroGreenPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("home_price_list_button")
                        ) {
                            Icon(imageVector = Icons.Default.PriceChange, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (isEnglish) "Prices" else "मूल्य", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onNavigate(FarmScreen.BANK_TRANSFER) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.9f),
                                contentColor = AgroGreenPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("home_bank_transfer_button")
                        ) {
                            Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (isEnglish) "Bank" else "बैंक", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Quick Stats Strip
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = if (isEnglish) "Total Products" else "कुल उत्पादनहरू",
                    value = "${products.size}",
                    subtitle = if (isEnglish) "Active items" else "५ विभिन्न वर्ग",
                    icon = Icons.Default.PriceChange,
                    modifier = Modifier.weight(1f),
                    color = AgroGreenPrimary,
                    onClick = { onNavigate(FarmScreen.PRICE_LIST) }
                )

                StatCard(
                    title = if (isEnglish) "In-Stock Items" else "उपलब्ध मौज्दात",
                    value = "$availableCount",
                    subtitle = if (lowStockCount > 0) "$lowStockCount कम मौज्दात" else "पर्याप्त स्टक",
                    icon = Icons.Default.Inventory2,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF2E7D32),
                    onClick = { onNavigate(FarmScreen.INVENTORY) }
                )
            }
        }

        // Bank Details Section with Direct Copy & Guide
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                PaymentQrCard(
                    isEnglish = isEnglish,
                    onCopied = { accNo ->
                        onToast("$accNo कपी गरियो!")
                    }
                )
            }
        }

        // Navigation Quick Action Tiles
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isEnglish) "Farm Modules" else "फार्म व्यवस्थापन तथा सेवाहरू",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                ModuleActionRow(
                    title = if (isEnglish) "Recharts Financial & Stock Dashboard" else "Recharts आय-व्यय तथा मौज्दात ड्यासबोर्ड",
                    subtitle = if (isEnglish) "Daily income vs expenses & monthly inventory value trends" else "दैनिक आम्दानी-खर्च बार चार्ट तथा कुल स्टक मूल्य विश्लेषण ग्राफ",
                    icon = Icons.Default.BarChart,
                    badgeText = "Recharts",
                    onClick = { onNavigate(FarmScreen.DASHBOARD) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                ModuleActionRow(
                    title = if (isEnglish) "Product Catalog & Price List" else "उत्पादन सूची तथा मूल्य सूची",
                    subtitle = if (isEnglish) "Categorized rates (Milk, Eggs, Meat, Veggies, Fertilizer)" else "दूध, अण्डा, कुखुरा, खसी, अकबरे, मलको मूल्य तथा अर्डर",
                    icon = Icons.Default.PriceChange,
                    badgeText = "${products.size} Items",
                    onClick = { onNavigate(FarmScreen.PRICE_LIST) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                ModuleActionRow(
                    title = if (isEnglish) "Stock & Inventory Management" else "इन्भेन्ट्रि तथा मौज्दात व्यवस्थापन",
                    subtitle = if (isEnglish) "Real-time stock tracking, quick adjustments (+ / -)" else "स्टक अपडेट, नयाँ उत्पादन थप्ने तथा मौज्दात अलर्ट",
                    icon = Icons.Default.Inventory2,
                    badgeText = "${totalStockCount.toInt()} Total",
                    onClick = { onNavigate(FarmScreen.INVENTORY) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                ModuleActionRow(
                    title = if (isEnglish) "Employee Attendance (Day & Monthly)" else "कर्मचारी हाजिरी तथा मासिक विवरण",
                    subtitle = if (isEnglish) "Daily check-in, overtime, leave & monthly wage payroll" else "दैनिक हाजिरी, बिदा, ओभरटाइम तथा मासिक तलब विवरण",
                    icon = Icons.Default.Groups,
                    badgeText = if (isEnglish) "HR / Staff" else "हाजिरी",
                    onClick = { onNavigate(FarmScreen.ATTENDANCE) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                ModuleActionRow(
                    title = if (isEnglish) "Income, Expenses, Balance Sheet & PDF Reports" else "दैनिक आय-व्यय, वासलात तथा PDF प्रतिवेदन",
                    subtitle = if (isEnglish) "Cash book, balance sheet, P&L statement & official PDF export" else "दैनिक नगद खाता, कम्पनी वासलात, नाफा-नोक्सान तथा PDF निर्यात",
                    icon = Icons.Default.MonetizationOn,
                    badgeText = if (isEnglish) "PDF Export" else "वासलात + PDF",
                    onClick = { onNavigate(FarmScreen.FINANCE) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                ModuleActionRow(
                    title = if (isEnglish) "Official Documents & Permits Database" else "आधिकारिक कागजात तथा अनुमतिपत्र डाटाबेस",
                    subtitle = if (isEnglish) "Company reg. (249870), PAN (609823451), municipal permit, DFTQC & organic licenses" else "कम्पनी दर्ता, प्यान/भ्याट, ललितपुर महानगरपालिका इजाजत, DFTQC तथा जैविक लाइसेन्स",
                    icon = Icons.Default.VerifiedUser,
                    badgeText = if (isEnglish) "11 Verified" else "११ दर्ता प्रमाण",
                    onClick = { onNavigate(FarmScreen.DOCUMENTS) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                ModuleActionRow(
                    title = if (isEnglish) "Direct Bank Payment Gateway" else "बैंक ट्रान्सफर भुक्तानी तथा प्रमाण",
                    subtitle = if (isEnglish) "Step-by-step transfer guide & submit voucher" else "मुक्तिनाथ विकास बैंकमा भौचर पेश र बिल प्रमाणीकरण",
                    icon = Icons.Default.AccountBalance,
                    badgeText = "A/C: 0961...",
                    onClick = { onNavigate(FarmScreen.BANK_TRANSFER) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                ModuleActionRow(
                    title = if (isEnglish) "Customer Orders & Invoices" else "ग्राहक अर्डर तथा डिजिटल रसिद",
                    subtitle = if (isEnglish) "Order history, payment verification stamps" else "सम्पूर्ण अर्डरहरू, भुक्तानी स्थिति तथा आधिकारिक बिल",
                    icon = Icons.Default.Receipt,
                    badgeText = "Orders",
                    onClick = { onNavigate(FarmScreen.ORDERS) }
                )
            }
        }

        // About the Farm & Contact Information
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = AgroGreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEnglish) "About Lalitpur United Agro Farm" else "युनाइटेड एग्रो फार्मको बारेमा",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isEnglish) {
                            "Lalitpur United Agro Farm Pvt. Ltd. is dedicated to sustainable organic agriculture, high-yield dairy farming, free-range poultry, Boer goat breeding, and bio-fertilizer production in Lalitpur, Nepal."
                        } else {
                            "ललितपुर युनाइटेड एग्रो फार्म प्रा. लि. गुणस्तरीय शुद्ध दुग्ध उत्पादन, उन्नत जातका बोयर बाख्रा तथा गिरिराज/कडकनाथ कुखुरा पालन, अग्र्यानिक डल्ले अकबरे खुर्सानी तथा प्राङ्गारिक गँड्यौला मल उत्पादनमा समर्पित कृषि उद्यम हो।"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "दर्ता नं. (Reg):", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text(text = FarmOfficialInfo.REGISTRATION_NO, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        }
                        Column {
                            Text(text = "पान नं. (PAN/VAT):", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text(text = FarmOfficialInfo.PAN_VAT_NO, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        }
                        Column {
                            Text(text = "इमेल (Email):", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text(text = FarmOfficialInfo.EMAIL, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = color.copy(alpha = 0.12f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                    }
                }

                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun ModuleActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badgeText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = AgroGreenPrimary.copy(alpha = 0.12f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = AgroGreenPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
