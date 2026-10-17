package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.Order
import com.example.model.FarmOfficialInfo
import com.example.ui.theme.AgroGoldTertiary
import com.example.ui.theme.AgroGreenPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Composable interface to initiate a payment request using Muktinath Bikas Bank details for an order,
 * including a confirmation status display.
 */
@Composable
fun MuktinathPaymentRequestCard(
    order: Order,
    isEnglish: Boolean = false,
    onPaymentSubmitted: (senderName: String, refNumber: String, amount: Double, voucherUri: String?) -> Unit,
    onVerifyPayment: (orderId: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var senderBankName by remember { mutableStateOf("मुक्तिनाथ विकास बैंक (Muktinath Bikas Bank)") }
    var senderAccountName by remember { mutableStateOf(order.customerName) }
    var transactionRefNumber by remember { mutableStateOf(order.transactionRefNumber) }
    var voucherUriString by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    // Status state
    var currentStatus by remember(order.status) { mutableStateOf(order.status) }

    // Voucher screenshot picker
    val voucherPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            voucherUriString = uri.toString()
            Toast.makeText(
                context,
                if (isEnglish) "Voucher screenshot selected!" else "भुक्तानी भौचर फोटो छानियो!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("muktinath_payment_request_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with Muktinath Bikas Bank Official Branding
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFE65100), Color(0xFFFF9800))
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = FarmOfficialInfo.BANK_NAME_NP,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF1F1F1F)
                        )
                        Text(
                            text = FarmOfficialInfo.BANK_NAME_EN,
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Live Confirmation Status Badge
                ConfirmationStatusBadge(
                    status = currentStatus,
                    isEnglish = isEnglish
                )
            }

            HorizontalDivider()

            // Bank Account Details Card with Copy buttons
            Surface(
                color = Color(0xFFFFF8E1),
                border = BorderStroke(1.dp, Color(0xFFFFD54F)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = if (isEnglish) "OFFICIAL BENEFICIARY ACCOUNT" else "आधिकारिक भुक्तानी खाता विवरण",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Account Name
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = FarmOfficialInfo.BANK_ACCOUNT_NAME_EN,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF1B3B22)
                        )
                    }

                    // Account Number with Copy Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "A/C: ${FarmOfficialInfo.BANK_ACCOUNT_NO}",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = Color(0xFFB71C1C)
                        )

                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Account Number", FarmOfficialInfo.BANK_ACCOUNT_NO)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(
                                    context,
                                    if (isEnglish) "Account number copied!" else "खाता नम्बर कपी गरियो!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Account Number",
                                tint = Color(0xFFE65100),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Amount Payable
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEnglish) "Payable Amount:" else "तिर्नुपर्ने रकम:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                        Text(
                            text = "रु ${String.format(Locale.getDefault(), "%,.2f", order.totalAmount)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AgroGreenPrimary
                        )
                    }
                }
            }

            // QR Code Scan Visual & Quick Info
            Surface(
                color = Color(0xFFF1F8E9),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, AgroGreenPrimary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Payment,
                        contentDescription = null,
                        tint = AgroGreenPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEnglish)
                            "Transfer via Muktinath Mobile Banking app, Fonepay, or connectIPS, then enter the reference below."
                        else
                            "मुक्तिनाथ मोबाइल बैंकिङ, फोनपे वा कनेक्टIPS बाट रकम पठाएर तलको फारममा ट्रान्ज्याक्सन नम्बर राख्नुहोस्।",
                        fontSize = 11.sp,
                        color = Color(0xFF2E7D32),
                        lineHeight = 15.sp
                    )
                }
            }

            // Payment Request Submission Form
            Text(
                text = if (isEnglish) "Initiate Payment Request" else "भुक्तानी अनुरोध पेश गर्नुहोस्",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F1F1F)
            )

            // Sender Account Name
            OutlinedTextField(
                value = senderAccountName,
                onValueChange = { senderAccountName = it },
                label = { Text(if (isEnglish) "Sender Account Name *" else "रकम पठाउनेको नाम *") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sender_account_name_input")
            )

            // Transaction Reference / Voucher Number
            OutlinedTextField(
                value = transactionRefNumber,
                onValueChange = { transactionRefNumber = it },
                label = { Text(if (isEnglish) "Bank Transaction Ref / Voucher No. *" else "ट्रान्ज्याक्सन रेफरन्स / भौचर नं. *") },
                placeholder = { Text("e.g. MNB-987654321") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transaction_ref_input")
            )

            // Optional Voucher Screenshot Attachment
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = {
                        voucherPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("attach_voucher_button")
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (voucherUriString != null) {
                            if (isEnglish) "Voucher Attached ✓" else "भौचर फोटो संलग्न ✓"
                        } else {
                            if (isEnglish) "Attach Voucher / Screenshot" else "भौचर फोटो संलग्न गर्नुहोस्"
                        },
                        fontSize = 12.sp
                    )
                }

                if (voucherUriString != null) {
                    Text(
                        text = if (isEnglish) "File Selected" else "छानियो",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AgroGreenPrimary
                    )
                }
            }

            // Submit Payment Request Button
            Button(
                onClick = {
                    if (senderAccountName.isBlank()) {
                        Toast.makeText(context, if (isEnglish) "Please enter sender account name" else "कृपया पठाउनेको नाम लेख्नुहोस्", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (transactionRefNumber.isBlank()) {
                        Toast.makeText(context, if (isEnglish) "Please enter transaction reference number" else "कृपया ट्रान्ज्याक्सन रेफरन्स नम्बर लेख्नुहोस्", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isSubmitting = true
                    currentStatus = "PENDING_VERIFICATION"
                    onPaymentSubmitted(
                        senderAccountName.trim(),
                        transactionRefNumber.trim(),
                        order.totalAmount,
                        voucherUriString
                    )
                    isSubmitting = false
                    Toast.makeText(
                        context,
                        if (isEnglish) "Muktinath Bank payment request submitted!" else "मुक्तिनाथ बैंक भुक्तानी अनुरोध पेश गरियो!",
                        Toast.LENGTH_LONG
                    ).show()
                },
                enabled = !isSubmitting && transactionRefNumber.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("submit_payment_request_button")
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Payment, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEnglish) "Submit Payment Request" else "भुक्तानी अनुरोध पेश गर्नुहोस्",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Confirmation Status Display Card
            ConfirmationStatusCard(
                order = order,
                status = currentStatus,
                isEnglish = isEnglish,
                onVerifyClick = {
                    currentStatus = "VERIFIED"
                    onVerifyPayment(order.orderId)
                }
            )
        }
    }
}

@Composable
private fun ConfirmationStatusBadge(
    status: String,
    isEnglish: Boolean
) {
    val (bg, text, tint) = when (status) {
        "VERIFIED" -> Triple(Color(0xFFE8F5E9), if (isEnglish) "Verified ✓" else "प्रमाणित ✓", AgroGreenPrimary)
        "PROCESSING" -> Triple(Color(0xFFE3F2FD), if (isEnglish) "Processing" else "प्रक्रियामा", Color(0xFF1565C0))
        else -> Triple(Color(0xFFFFF3E0), if (isEnglish) "Pending Verification" else "प्रमाणीकरण बाँकी", Color(0xFFE65100))
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (status == "VERIFIED") Icons.Default.Verified else Icons.Default.HourglassTop,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                color = tint,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ConfirmationStatusCard(
    order: Order,
    status: String,
    isEnglish: Boolean,
    onVerifyClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (status == "VERIFIED") Color(0xFFF1F8E9) else Color(0xFFFAFAFA)
        ),
        border = BorderStroke(
            1.dp,
            if (status == "VERIFIED") AgroGreenPrimary.copy(alpha = 0.4f) else Color(0xFFE0E0E0)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("payment_confirmation_status_card")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isEnglish) "Payment Confirmation Status" else "भुक्तानी प्रमाणीकरण अवस्था",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF1B3B22)
                )

                if (status == "VERIFIED") {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified",
                        tint = AgroGreenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                text = when (status) {
                    "VERIFIED" -> if (isEnglish)
                        "Payment of NPR ${String.format(Locale.getDefault(), "%,.2f", order.totalAmount)} has been verified. Order is now confirmed and being prepared."
                    else
                        "रु ${String.format(Locale.getDefault(), "%,.2f", order.totalAmount)} को भुक्तानी प्रमाणित भयो। अर्डर तयारीमा छ।"
                    else -> if (isEnglish)
                        "Payment request recorded for Order #${order.orderId}. Bank confirmation usually takes 5-15 minutes."
                    else
                        "अर्डर #${order.orderId} को भुक्तानी अनुरोध दर्ता भयो। मुक्तिनाथ बैंक खाता रुजु हुन ५-१५ मिनेट लाग्नेछ।"
                },
                fontSize = 12.sp,
                color = if (status == "VERIFIED") Color(0xFF2E7D32) else Color.Gray,
                lineHeight = 16.sp
            )

            if (status != "VERIFIED") {
                Button(
                    onClick = onVerifyClick,
                    colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quick_verify_payment_button")
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isEnglish) "Verify Payment Now (Staff)" else "भुक्तानी प्रमाणित गर्नुहोस् (कर्मचारी)",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
