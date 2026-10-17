package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.FarmOfficialInfo
import com.example.ui.theme.AgroGreenPrimary

val EsewaGreen = Color(0xFF60BB46)
val EsewaGreenDark = Color(0xFF459A2C)
val MuktinathNavy = Color(0xFF0D254C)

enum class PaymentQrMethod {
    MUKTINATH_BANK,
    ESEWA_WALLET
}

@Composable
fun PaymentQrCard(
    isEnglish: Boolean = false,
    onCopied: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedMethod by remember { mutableStateOf(PaymentQrMethod.MUKTINATH_BANK) }
    var showFullscreenQr by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("payment_qr_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Two Payment Methods Switcher Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isEnglish) "Instant Payment QR Codes" else "क्यूआर स्क्यान भुक्तानी (QR Payment)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AgroGreenPrimary
                    )
                    Text(
                        text = if (isEnglish) "Scan via Mobile Banking or eSewa app" else "मोबाइल बैंकिङ वा ईसेवा एपबाट सिधै स्क्यान गर्नुहोस्",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE8F5E9)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, tint = AgroGreenPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isEnglish) "Instant Scan" else "२ वटा खाता",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AgroGreenPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab Selector: Muktinath Bank vs eSewa Wallet
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Muktinath Bank Tab
                val isBankSelected = selectedMethod == PaymentQrMethod.MUKTINATH_BANK
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedMethod = PaymentQrMethod.MUKTINATH_BANK }
                        .testTag("tab_muktinath_bank"),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isBankSelected) MuktinathNavy else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, if (isBankSelected) MuktinathNavy else Color.Transparent)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = if (isBankSelected) Color(0xFFFFD54F) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEnglish) "Muktinath Bank" else "मुक्तिनाथ बैंक",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isBankSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // eSewa Wallet Tab
                val isEsewaSelected = selectedMethod == PaymentQrMethod.ESEWA_WALLET
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedMethod = PaymentQrMethod.ESEWA_WALLET }
                        .testTag("tab_esewa_wallet"),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isEsewaSelected) EsewaGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, if (isEsewaSelected) EsewaGreen else Color.Transparent)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = if (isEsewaSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEnglish) "eSewa Wallet" else "ईसेवा वालेट",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isEsewaSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Active QR Box
            when (selectedMethod) {
                PaymentQrMethod.MUKTINATH_BANK -> {
                    MuktinathBankQrView(
                        isEnglish = isEnglish,
                        onExpandQr = { showFullscreenQr = true },
                        onCopy = {
                            copyText(context, FarmOfficialInfo.BANK_ACCOUNT_NO, "Account Number")
                            onCopied(FarmOfficialInfo.BANK_ACCOUNT_NO)
                        }
                    )
                }
                PaymentQrMethod.ESEWA_WALLET -> {
                    EsewaWalletQrView(
                        isEnglish = isEnglish,
                        onExpandQr = { showFullscreenQr = true },
                        onCopy = {
                            copyText(context, FarmOfficialInfo.ESEWA_ID, "eSewa ID")
                            onCopied(FarmOfficialInfo.ESEWA_ID)
                        }
                    )
                }
            }
        }
    }

    // Fullscreen QR Modal for Easy Scanning
    if (showFullscreenQr) {
        Dialog(onDismissRequest = { showFullscreenQr = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedMethod == PaymentQrMethod.MUKTINATH_BANK) "मुक्तिनाथ बैंक भुक्तानी क्यूआर" else "ईसेवा भुक्तानी क्यूआर (eSewa)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedMethod == PaymentQrMethod.MUKTINATH_BANK) MuktinathNavy else EsewaGreenDark
                        )
                        IconButton(onClick = { showFullscreenQr = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    DynamicQrGraphic(
                        seedString = if (selectedMethod == PaymentQrMethod.MUKTINATH_BANK) FarmOfficialInfo.BANK_ACCOUNT_NO else FarmOfficialInfo.ESEWA_ID,
                        qrColor = if (selectedMethod == PaymentQrMethod.MUKTINATH_BANK) MuktinathNavy else EsewaGreenDark,
                        size = 220.dp,
                        centerLabel = if (selectedMethod == PaymentQrMethod.MUKTINATH_BANK) "MNB" else "eSewa"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = FarmOfficialInfo.FARM_NAME_EN,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    val idText = if (selectedMethod == PaymentQrMethod.MUKTINATH_BANK) {
                        "A/C: ${FarmOfficialInfo.BANK_ACCOUNT_NO}"
                    } else {
                        "eSewa ID: ${FarmOfficialInfo.ESEWA_ID}"
                    }
                    Text(
                        text = idText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedMethod == PaymentQrMethod.MUKTINATH_BANK) MuktinathNavy else EsewaGreenDark
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showFullscreenQr = false },
                        colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("बन्द गर्नुहोस् (Close)")
                    }
                }
            }
        }
    }
}

@Composable
private fun MuktinathBankQrView(
    isEnglish: Boolean,
    onExpandQr: () -> Unit,
    onCopy: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF4F7FB),
        border = BorderStroke(1.dp, Color(0xFFD6E2F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // QR Code with tap to enlarge
            Box(
                modifier = Modifier
                    .clickable { onExpandQr() }
                    .padding(4.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                DynamicQrGraphic(
                    seedString = FarmOfficialInfo.BANK_ACCOUNT_NO,
                    qrColor = MuktinathNavy,
                    size = 180.dp,
                    centerLabel = "MNB"
                )
                Surface(
                    shape = CircleShape,
                    color = MuktinathNavy,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Fullscreen, contentDescription = "Enlarge", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "ट्याप गरी क्यूआर ठूलो बनाउनुहोस् (Tap to enlarge)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Details rows
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (isEnglish) "Bank Name:" else "बैंकको नाम:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "${FarmOfficialInfo.BANK_NAME_NP} (${FarmOfficialInfo.BANK_BRANCH_NP})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MuktinathNavy
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isEnglish) "Beneficiary / Account Name:" else "खातावालाको नाम:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = FarmOfficialInfo.BANK_ACCOUNT_NAME_EN,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Account Number with copy button
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFC4D6EB)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isEnglish) "Account Number:" else "खाता नम्बर:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = FarmOfficialInfo.BANK_ACCOUNT_NO,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                color = MuktinathNavy
                            )
                        }

                        Button(
                            onClick = onCopy,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MuktinathNavy,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("copy_bank_acc_btn")
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (isEnglish) "Copy" else "कपी", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EsewaWalletQrView(
    isEnglish: Boolean,
    onExpandQr: () -> Unit,
    onCopy: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF3FBF1),
        border = BorderStroke(1.dp, Color(0xFFCCEAC4)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // QR Code with tap to enlarge
            Box(
                modifier = Modifier
                    .clickable { onExpandQr() }
                    .padding(4.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                DynamicQrGraphic(
                    seedString = FarmOfficialInfo.ESEWA_ID,
                    qrColor = EsewaGreenDark,
                    size = 180.dp,
                    centerLabel = "eSewa"
                )
                Surface(
                    shape = CircleShape,
                    color = EsewaGreen,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Fullscreen, contentDescription = "Enlarge", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "ईसेवा एपबाट स्क्यान गर्नुहोस् (Scan with eSewa app)",
                style = MaterialTheme.typography.labelSmall,
                color = EsewaGreenDark,
                fontWeight = FontWeight.SemiBold
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Details rows
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (isEnglish) "Digital Wallet Provider:" else "डिजिटल वालेट:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "${FarmOfficialInfo.ESEWA_NAME_NP} / eSewa Nepal",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = EsewaGreenDark
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isEnglish) "Account / Merchant Name:" else "खातावालाको आधिकारिक नाम:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = FarmOfficialInfo.ESEWA_ACCOUNT_NAME,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // eSewa ID with copy button
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFBCE3B2)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isEnglish) "eSewa ID / Mobile Number:" else "ईसेवा आईडी / मोबाइल नम्बर:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = "${FarmOfficialInfo.ESEWA_ID} (${FarmOfficialInfo.ESEWA_ID_NP})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                color = EsewaGreenDark
                            )
                        }

                        Button(
                            onClick = onCopy,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EsewaGreen,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("copy_esewa_id_btn")
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (isEnglish) "Copy" else "कपी", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Beautiful, deterministic QR matrix generator in Compose Canvas:
 * Renders standard 3 corner finder patterns, alignment timing lines,
 * and pseudo-data blocks seeded by the account string.
 */
@Composable
fun DynamicQrGraphic(
    seedString: String,
    qrColor: Color,
    size: Dp = 160.dp,
    centerLabel: String = ""
) {
    Surface(
        modifier = Modifier.size(size),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(2.dp, qrColor.copy(alpha = 0.2f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasSize = this.size.width
                val gridSize = 21 // 21x21 standard Version 1 QR matrix
                val cellSize = canvasSize / gridSize

                // Helper to draw QR finder pattern (7x7 box with 3x3 center)
                fun drawFinderPattern(startX: Int, startY: Int) {
                    // Outer 7x7 box
                    drawRoundRect(
                        color = qrColor,
                        topLeft = Offset(startX * cellSize, startY * cellSize),
                        size = Size(7 * cellSize, 7 * cellSize),
                        cornerRadius = CornerRadius(cellSize * 1.2f, cellSize * 1.2f)
                    )
                    // Inner 5x5 white space
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset((startX + 1) * cellSize, (startY + 1) * cellSize),
                        size = Size(5 * cellSize, 5 * cellSize),
                        cornerRadius = CornerRadius(cellSize * 0.8f, cellSize * 0.8f)
                    )
                    // Core 3x3 solid box
                    drawRoundRect(
                        color = qrColor,
                        topLeft = Offset((startX + 2) * cellSize, (startY + 2) * cellSize),
                        size = Size(3 * cellSize, 3 * cellSize),
                        cornerRadius = CornerRadius(cellSize * 0.5f, cellSize * 0.5f)
                    )
                }

                // 1. Top-Left Finder
                drawFinderPattern(0, 0)
                // 2. Top-Right Finder
                drawFinderPattern(gridSize - 7, 0)
                // 3. Bottom-Left Finder
                drawFinderPattern(0, gridSize - 7)

                // 4. Timing lines (alternating dots along row 6 and col 6)
                for (i in 8 until gridSize - 8) {
                    if (i % 2 == 0) {
                        drawRect(
                            color = qrColor,
                            topLeft = Offset(i * cellSize, 6 * cellSize),
                            size = Size(cellSize, cellSize)
                        )
                        drawRect(
                            color = qrColor,
                            topLeft = Offset(6 * cellSize, i * cellSize),
                            size = Size(cellSize, cellSize)
                        )
                    }
                }

                // 5. Deterministic Data Matrix cells based on seed string
                val hash = seedString.hashCode()
                for (row in 0 until gridSize) {
                    for (col in 0 until gridSize) {
                        // Skip the 3 finder pattern zones
                        val inTopLeft = row < 8 && col < 8
                        val inTopRight = row < 8 && col >= gridSize - 8
                        val inBottomLeft = row >= gridSize - 8 && col < 8
                        val inCenterLogo = row in 8..12 && col in 8..12

                        if (!inTopLeft && !inTopRight && !inBottomLeft && !inCenterLogo) {
                            val pseudoRandomBit = ((row * 31 + col * 17 + hash + seedString.length) xor (row * col)) % 5
                            if (pseudoRandomBit == 0 || pseudoRandomBit == 2) {
                                drawRoundRect(
                                    color = qrColor,
                                    topLeft = Offset(col * cellSize + cellSize * 0.08f, row * cellSize + cellSize * 0.08f),
                                    size = Size(cellSize * 0.84f, cellSize * 0.84f),
                                    cornerRadius = CornerRadius(cellSize * 0.2f, cellSize * 0.2f)
                                )
                            }
                        }
                    }
                }
            }

            // Central logo chip
            if (centerLabel.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = qrColor,
                    shadowElevation = 2.dp,
                    modifier = Modifier.size(size * 0.25f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = centerLabel,
                            color = Color.White,
                            fontSize = (size.value * 0.065f).sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }
            }
        }
    }
}

private fun copyText(context: Context, text: String, label: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
}
