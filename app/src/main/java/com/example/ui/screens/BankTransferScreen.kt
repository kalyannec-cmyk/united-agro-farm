package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FarmOfficialInfo
import com.example.model.FarmOrder
import com.example.model.OrderStatus
import com.example.ui.components.CompanyLogo
import com.example.ui.components.PaymentQrCard
import com.example.ui.theme.AgroGoldTertiary
import com.example.ui.theme.AgroGreenPrimary

@Composable
fun BankTransferScreen(
    orders: List<FarmOrder>,
    isEnglish: Boolean = false,
    onToast: (String) -> Unit,
    onSubmitDirectTransfer: (
        customerName: String,
        customerPhone: String,
        address: String,
        senderBank: String,
        senderName: String,
        txnRef: String,
        amount: Double,
        remarks: String
    ) -> Unit,
    onOpenReceipt: (FarmOrder) -> Unit
) {
    val context = LocalContext.current

    // Form inputs
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var deliveryAddress by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var senderBankName by remember { mutableStateOf("मुक्तिनाथ विकास बैंक (Muktinath Bank)") }
    var senderAccountName by remember { mutableStateOf("") }
    var transactionRef by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }

    val recentBankOrders = orders.filter { it.bankTransfer != null }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("bank_transfer_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title banner with Company Logo
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CompanyLogo(
                        size = 48.dp,
                        showSubtext = false,
                        modifier = Modifier.testTag("transfer_screen_logo")
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isEnglish) "Payment QR & Bank Transfer" else "भुक्तानी क्यूआर तथा बैंक ट्रान्सफर",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = AgroGreenPrimary
                        )
                        Text(
                            text = if (isEnglish) "Muktinath Bank & eSewa Wallet: 9848092790" else "मुक्तिनाथ विकास बैंक तथा ईसेवा वालेट: ९८४८०९२७९०",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }

        // Instant Payment QR Display (Muktinath Bank & eSewa Wallet with Fullscreen Zoom)
        item {
            PaymentQrCard(
                isEnglish = isEnglish,
                onCopied = { text ->
                    onToast("$text कपी गरियो!")
                }
            )
        }

        // Step-by-Step Bank Transfer Guide (कसरी गर्ने?)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = null,
                            tint = AgroGreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEnglish) "How to do Bank Transfer? (Step-by-Step)" else "बैंक ट्रान्सफर कसरी गर्ने? (चरणबद्ध तरिका)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TransferStepItem(
                        stepNumber = "१",
                        title = if (isEnglish) "Open Mobile Banking / ConnectIPS" else "आफ्नो मोबाइल बैंकिङ वा ConnectIPS खोल्नुहोस्",
                        desc = if (isEnglish) "Log in to Muktinath Smart or any commercial bank app." else "मुक्तिनाथ विकास बैंक वा अन्य कुनै पनि बैंकको एप खोल्नुहोस्।"
                    )

                    TransferStepItem(
                        stepNumber = "२",
                        title = if (isEnglish) "Select Inter-Bank Transfer / Send Money" else "'Send Money' वा 'Interbank Transfer' रोज्नुहोस्",
                        desc = if (isEnglish) "Choose Fund Transfer option from your bank menu." else "बैंकिङ मेनुबाट फन्ड ट्रान्सफर विकल्प रोज्नुहोस्।"
                    )

                    TransferStepItem(
                        stepNumber = "३",
                        title = if (isEnglish) "Enter Muktinath Bikas Bank & Account No." else "मुक्तिनाथ बैंक तथा खाता नम्बर राख्नुहोस्",
                        desc = "खाता नम्बर: ${FarmOfficialInfo.BANK_ACCOUNT_NO} (${FarmOfficialInfo.BANK_ACCOUNT_NO_NP})"
                    )

                    TransferStepItem(
                        stepNumber = "४",
                        title = if (isEnglish) "Confirm Account Holder Name" else "खातावालाको नाम रुजु गर्नुहोस्",
                        desc = "${FarmOfficialInfo.FARM_NAME_EN} (${FarmOfficialInfo.FARM_NAME_NP})"
                    )

                    TransferStepItem(
                        stepNumber = "५",
                        title = if (isEnglish) "Submit Voucher Details Below" else "भुक्तानी सम्पन्न गरी भौचर विवरण तल पेश गर्नुहोस्",
                        desc = if (isEnglish) "Enter transaction reference number to instantly verify your payment." else "भौचर वा ट्रान्जेक्सन रिफरेन्स नम्बर प्रविष्ट गरी तुरन्तै रसिद प्राप्त गर्नुहोस्।"
                    )
                }
            }
        }

        // Direct Payment Voucher Submission Form
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, AgroGreenPrimary.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bank_transfer_form")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Payment,
                                contentDescription = null,
                                tint = AgroGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isEnglish) "Submit Payment Voucher" else "बैंक ट्रान्सफर भौचर विवरण पेश गर्नुहोस्",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AgroGreenPrimary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AgroGreenPrimary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = if (isEnglish) "Secure" else "सुरक्षित",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AgroGreenPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = if (isEnglish) "Fill this form after completing transfer to receive official verified invoice" else "मुक्तिनाथ विकास बैंकमा रकम पठाएपछि यो फारम भरेर आधिकारिक बिल रसिद पाउनुहोस्।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text(if (isEnglish) "Customer Full Name *" else "ग्राहकको पूरा नाम *") },
                        placeholder = { Text("उदा. राम कुमार श्रेष्ठ") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transfer_customer_name_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = customerPhone,
                            onValueChange = { customerPhone = it },
                            label = { Text(if (isEnglish) "Phone No. *" else "सम्पर्क फोन *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("transfer_phone_input"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = it },
                            label = { Text(if (isEnglish) "Amount (NPR) *" else "पठाएको रकम (रु.) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("transfer_amount_input"),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = deliveryAddress,
                        onValueChange = { deliveryAddress = it },
                        label = { Text(if (isEnglish) "Delivery / Farm Pickup Location *" else "डेलिभरी वा संकलन ठेगाना *") },
                        placeholder = { Text("उदा. ललितपुर महालक्ष्मी, इमाडोल / गोदावरी") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transfer_address_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = transactionRef,
                        onValueChange = { transactionRef = it },
                        label = { Text(if (isEnglish) "Transaction Ref / Voucher No. *" else "ट्रान्जेक्सन रिफरेन्स / भौचर नम्बर *") },
                        placeholder = { Text("उदा. MNB-7489201 वा UTR Ref") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transfer_txn_ref_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Payment Method Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val methods = listOf("मुक्तिनाथ बैंक", "ईसेवा वालेट", "ConnectIPS")
                        methods.forEach { method ->
                            val isSelected = senderBankName.contains(method)
                            OutlinedButton(
                                onClick = {
                                    senderBankName = when (method) {
                                        "मुक्तिनाथ बैंक" -> "मुक्तिनाथ विकास बैंक (Muktinath Bank)"
                                        "ईसेवा वालेट" -> "ईसेवा वालेट (eSewa - 9848092790)"
                                        else -> "ConnectIPS Nepal"
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) AgroGreenPrimary.copy(alpha = 0.1f) else Color.Transparent
                                )
                            ) {
                                Text(method, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = senderBankName,
                            onValueChange = { senderBankName = it },
                            label = { Text(if (isEnglish) "Payment Channel / App" else "भुक्तानी माध्यम / बैंक / ईसेवा") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = senderAccountName,
                            onValueChange = { senderAccountName = it },
                            label = { Text(if (isEnglish) "Sender A/C Name" else "खातावालाको नाम") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { remarks = it },
                        label = { Text(if (isEnglish) "Purchased Product / Purpose" else "खरिद गरिएको सामान वा प्रयोजन") },
                        placeholder = { Text("उदा. दूध, घिउ, गिरिराज कुखुरा, गँड्यौला मल भुक्तानी") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            if (customerName.isNotBlank() && customerPhone.isNotBlank() && transactionRef.isNotBlank() && amt > 0.0) {
                                onSubmitDirectTransfer(
                                    customerName,
                                    customerPhone,
                                    deliveryAddress,
                                    senderBankName,
                                    senderAccountName.ifBlank { customerName },
                                    transactionRef,
                                    amt,
                                    remarks.ifBlank { "Direct Muktinath Bank Transfer" }
                                )
                                // Clear form
                                customerName = ""
                                customerPhone = ""
                                deliveryAddress = ""
                                amountText = ""
                                transactionRef = ""
                                remarks = ""
                            } else {
                                onToast("कृपया नाम, फोन, रकम र भौचर नम्बर अनिवार्य भर्नुहोस्।")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_bank_transfer_button")
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEnglish) "Submit Bank Transfer & Generate Receipt" else "बैंक भुक्तानी पेश गरी रसिद हेर्नुहोस्",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        // Recent Bank Transfers List
        if (recentBankOrders.isNotEmpty()) {
            item {
                Text(
                    text = if (isEnglish) "Submitted Bank Transfers" else "पेश गरिएका बैंक ट्रान्सफर विवरणहरू",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(recentBankOrders, key = { it.orderId }) { order ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenReceipt(order) }
                        .testTag("recent_transfer_${order.orderId}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE8F5E9),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = null,
                                    tint = AgroGreenPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = order.customerName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Ref: ${order.bankTransfer?.transactionRefNumber ?: "N/A"}",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = order.orderDate,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "रू. ${"%.0f".format(order.totalAmount)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = AgroGreenPrimary
                            )

                            val isVerified = order.status == OrderStatus.VERIFIED || order.status == OrderStatus.COMPLETED
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isVerified) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                            ) {
                                Text(
                                    text = if (isVerified) "प्रमाणित" else "प्रमाणीकरण बाँकी",
                                    color = if (isVerified) Color(0xFF2E7D32) else Color(0xFFEF6C00),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransferStepItem(
    stepNumber: String,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = AgroGreenPrimary,
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = stepNumber,
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
