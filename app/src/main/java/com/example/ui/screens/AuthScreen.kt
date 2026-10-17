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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.GoogleSignInButton
import com.example.data.FirestoreFarmRepository
import com.example.model.FarmOfficialInfo
import com.example.model.UserRole
import com.example.ui.components.CompanyLogo
import com.example.ui.theme.AgroGoldTertiary
import com.example.ui.theme.AgroGreenDark
import com.example.ui.theme.AgroGreenLight
import com.example.ui.theme.AgroGreenPrimary
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var isEnglish by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(UserRole.STAFF) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showInfoDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1B4D24),
                        Color(0xFF153B1C),
                        Color(0xFF0D2411)
                    )
                )
            )
            .testTag("auth_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Top Quick Action Bar: Tag, Info, Language Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEnglish) "Reg. No: 249870" else "दर्ता नं: २४९८७०",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showInfoDialog = true },
                        modifier = Modifier
                            .testTag("auth_info_button")
                            .size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Farm Legal Info",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                        modifier = Modifier.clickable { isEnglish = !isEnglish }
                    ) {
                        Text(
                            text = if (isEnglish) "नेपाली" else "English",
                            color = Color(0xFFFFD54F),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Hero Emblem & Branding
            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.size(88.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CompanyLogo(
                        size = 72.dp,
                        showSubtext = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (isEnglish) FarmOfficialInfo.FARM_NAME_EN else FarmOfficialInfo.FARM_NAME_NP,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (isEnglish) "Muktinath Bikas Bank Secured Agro Portal" else "मुक्तिनाथ विकास बैंक सुरक्षित कृषि पोर्टल",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFC8E6C9),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isEnglish) "Bagmati Province, Lalitpur-28, Harisiddhi" else "ललितपुर-२८, हरिसिद्धि, बागमती प्रदेश, नेपाल",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Access Level / Role Selection Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isEnglish) "Select Your Portal Access" else "प्रवेश गर्ने पोर्टल छनौट गर्नुहोस्",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFFD54F)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RoleSelectionChip(
                            title = if (isEnglish) "Staff / Admin" else "व्यवस्थापक / स्टाफ",
                            subtitle = if (isEnglish) "Full Control" else "पूर्ण व्यवस्थापन",
                            icon = Icons.Default.AdminPanelSettings,
                            isSelected = selectedRole == UserRole.STAFF,
                            onClick = { selectedRole = UserRole.STAFF },
                            modifier = Modifier.weight(1f)
                        )

                        RoleSelectionChip(
                            title = if (isEnglish) "Customer / Buyer" else "किसान तथा ग्राहक",
                            subtitle = if (isEnglish) "Orders & Bill" else "अर्डर र भुक्तानी",
                            icon = Icons.Default.Person,
                            isSelected = selectedRole == UserRole.CUSTOMER,
                            onClick = { selectedRole = UserRole.CUSTOMER },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (selectedRole == UserRole.STAFF) {
                            if (isEnglish)
                                "Staff Portal: Manage real-time inventory, live rate list, product catalog, and verify bank transfer receipts."
                            else
                                "व्यवस्थापक पोर्टल: लाइभ इन्भेन्ट्रि मौज्दात नियन्त्रण, दैनिक मूल्य सूची अपडेट, र मुक्तिनाथ विकास बैंक भौचर प्रमाणीकरण।"
                        } else {
                            if (isEnglish)
                                "Customer Portal: Browse farm-fresh dairy, poultry & veggies, place wholesale/retail orders, and pay via Muktinath Bikas Bank."
                            else
                                "ग्राहक पोर्टल: ताजा दुध, अण्डा, कुखुरा र तरकारीको दरभाउ अवलोकन, प्रत्यक्ष खरिद र मुक्तिनाथ विकास बैंकमार्फत सजिलो भुक्तानी।"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Login Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_login_card")
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = AgroGreenPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = AgroGreenPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isEnglish) "Firebase Authentication" else "फायरबेस अथेन्टिकेसन",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = AgroGreenPrimary
                            )
                            Text(
                                text = if (isEnglish) "Google Identity Services Protected" else "गुगल आइडेन्टिटी सुरक्षित प्रणाली",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = Color(0xFFEEEEEE))

                    Spacer(modifier = Modifier.height(16.dp))

                    // Selected role badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (selectedRole == UserRole.STAFF) Color(0xFFE8F5E9) else Color(0xFFFFF8E1),
                        border = BorderStroke(
                            1.dp,
                            if (selectedRole == UserRole.STAFF) AgroGreenPrimary.copy(alpha = 0.4f) else Color(0xFFFFB300)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (selectedRole == UserRole.STAFF) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                contentDescription = null,
                                tint = if (selectedRole == UserRole.STAFF) AgroGreenPrimary else Color(0xFFE65100),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEnglish) {
                                    "Access: ${if (selectedRole == UserRole.STAFF) "Farm Staff / Manager" else "Customer / Buyer"}"
                                } else {
                                    "पहुँच स्तर: ${if (selectedRole == UserRole.STAFF) "फार्म व्यवस्थापक / कर्मचारी" else "किसान तथा ग्राहक"}"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedRole == UserRole.STAFF) AgroGreenDark else Color(0xFFBF360C)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isEnglish)
                            "Sign in with your Google account to access cloud inventory, update pricing, and manage Muktinath Bikas Bank orders."
                        else
                            "फार्मको क्लाउड इन्भेन्ट्रि, दैनिक मूल्य सूची र मुक्तिनाथ विकास बैंकमार्फत अर्डर व्यवस्थापन गर्न आफ्नो Google खाताबाट प्रवेश गर्नुहोस्।",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF555555),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    GoogleSignInButton(
                        onAuthSuccess = {
                            // Asynchronously sync or update user profile with chosen role
                            coroutineScope.launch {
                                try {
                                    FirestoreFarmRepository(context).saveOrUpdateUserProfile(selectedRole)
                                } catch (_: Exception) {}
                                onAuthSuccess()
                            }
                        },
                        onAuthError = { err ->
                            errorMessage = err
                        },
                        buttonText = if (isEnglish) "Sign in with Google" else "Google खाताबाट प्रवेश गर्नुहोस्",
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFFEBEE),
                            border = BorderStroke(1.dp, Color(0xFFFFCDD2)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFFC62828),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = errorMessage ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFC62828),
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { errorMessage = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = Color(0xFFC62828),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Secured Management Features Showcase
            Text(
                text = if (isEnglish) "Secured Farm Features" else "सुरक्षित कृषि व्यवस्थापन सुविधाहरू",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FeatureHighlightRow(
                    icon = Icons.Default.Inventory2,
                    title = if (isEnglish) "Real-Time Inventory Control" else "लाइभ इन्भेन्ट्रि नियन्त्रण",
                    description = if (isEnglish)
                        "Milk, poultry, goats, organic vegetables, and bio-feed stock synced to Firestore with low-stock alerts."
                    else
                        "दुध, कुखुरा, बाख्रा, ताजा तरकारी र जैविक मल/दानाको मौज्दात क्लाउडमा प्रत्यक्ष अद्यावधिक।"
                )

                FeatureHighlightRow(
                    icon = Icons.Default.PriceChange,
                    title = if (isEnglish) "Official Price Management" else "दैनिक मूल्य सूची निर्धारण",
                    description = if (isEnglish)
                        "Real-time update of retail and wholesale prices per Litre, Kg, Crate, Piece, and 50Kg bags."
                    else
                        "प्रति लिटर, केजी, क्रेट, गोटा र ५० केजी बोराको थोक तथा खुद्रा दरभाउ सिधा व्यवस्थापन।"
                )

                FeatureHighlightRow(
                    icon = Icons.Default.AccountBalance,
                    title = if (isEnglish) "Muktinath Bikas Bank Direct Transfer" else "मुक्तिनाथ विकास बैंक भुक्तानी",
                    description = if (isEnglish)
                        "Verified bank transfer to Account No. 09610201478560000001 with transaction vouchers & QR."
                    else
                        "खाता नं. ०९६१०२०१४७८५६००००००१ मा सिधा रकम जम्मा, भौचर अपलोड र क्युआर भुक्तानी प्रमाणीकरण।"
                )

                FeatureHighlightRow(
                    icon = Icons.Default.ReceiptLong,
                    title = if (isEnglish) "Customer Orders & Official Bills" else "डिजिटल अर्डर तथा रसिद",
                    description = if (isEnglish)
                        "End-to-end order processing, invoice printing, and delivery status tracking."
                    else
                        "ग्राहक अर्डर प्राप्ति, आधिकारिक बिल रसिद उत्पादन, र डेलिभरी अवस्था ट्र्याकिङ।"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer with zero-trust security & farm registry
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color(0xFFA5D6A7),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isEnglish) "Zero-Trust Architecture • Firestore Security Rules" else "शून्य-विश्वास सुरक्षा (Zero-Trust) • फायरबेस सेक्युरिटी रुल्स",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFA5D6A7)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isEnglish)
                        "PAN / VAT: 609823451 • Estd. 2021 • All Rights Reserved"
                    else
                        "भ्याट / प्यान: ६०९८२३४५१ • स्थापना: २०७८ • सर्वाधिकार सुरक्षित",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Official Farm & Bank Details Information Dialog
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Agriculture,
                        contentDescription = null,
                        tint = AgroGreenPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isEnglish) "About United Agro Farm" else "युनाइटेड एग्रो फार्म विवरण",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    InfoRow(
                        label = if (isEnglish) "Registered Name" else "दर्ता भएको नाम",
                        value = "${FarmOfficialInfo.FARM_NAME_NP}\n(${FarmOfficialInfo.FARM_NAME_EN})"
                    )

                    InfoRow(
                        label = if (isEnglish) "Registration No." else "दर्ता नम्बर",
                        value = "${FarmOfficialInfo.REGISTRATION_NO} (नेपाल सरकार)"
                    )

                    InfoRow(
                        label = if (isEnglish) "PAN / VAT No." else "स्थायी लेखा नं (PAN)",
                        value = FarmOfficialInfo.PAN_VAT_NO
                    )

                    InfoRow(
                        label = if (isEnglish) "Farm Location" else "ठेगाना",
                        value = "${FarmOfficialInfo.LOCATION_NP}\n(Lalitpur-28, Harisiddhi)"
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Text(
                        text = if (isEnglish) "Official Bank Details:" else "आधिकारिक बैंक खाता विवरण:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = AgroGreenPrimary
                    )

                    InfoRow(
                        label = if (isEnglish) "Bank Name" else "बैंकको नाम",
                        value = "${FarmOfficialInfo.BANK_NAME_NP} (${FarmOfficialInfo.BANK_NAME_EN})"
                    )

                    InfoRow(
                        label = if (isEnglish) "Account Name" else "खातावालाको नाम",
                        value = FarmOfficialInfo.BANK_ACCOUNT_NAME_NP
                    )

                    InfoRow(
                        label = if (isEnglish) "Account Number" else "खाता नम्बर",
                        value = "${FarmOfficialInfo.BANK_ACCOUNT_NO} (${FarmOfficialInfo.BANK_ACCOUNT_NO_NP})"
                    )

                    InfoRow(
                        label = if (isEnglish) "Branch & SWIFT" else "शाखा र स्विफ्ट",
                        value = "${FarmOfficialInfo.BANK_BRANCH_NP} | SWIFT: ${FarmOfficialInfo.BANK_SWIFT}"
                    )

                    InfoRow(
                        label = if (isEnglish) "eSewa ID" else "ईसेवा आईडी",
                        value = "${FarmOfficialInfo.ESEWA_ID} (${FarmOfficialInfo.ESEWA_ACCOUNT_NAME})"
                    )

                    InfoRow(
                        label = if (isEnglish) "Contact Phone" else "सम्पर्क फोन",
                        value = FarmOfficialInfo.PHONE_PRIMARY
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showInfoDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary)
                ) {
                    Text(if (isEnglish) "Close" else "बन्द गर्नुहोस्")
                }
            }
        )
    }
}

@Composable
private fun RoleSelectionChip(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.08f),
        border = BorderStroke(
            1.5.dp,
            if (isSelected) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.15f)
        ),
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (isSelected) AgroGreenPrimary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.12f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) AgroGreenPrimary else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color(0xFF1B4D24) else Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) Color(0xFF2E7D32) else Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun FeatureHighlightRow(
    icon: ImageVector,
    title: String,
    description: String
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.15f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF1F1F1F)
        )
    }
}
