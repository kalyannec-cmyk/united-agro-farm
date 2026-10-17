package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.BalanceSheetSummary
import com.example.model.FarmBalanceSheetLedger
import com.example.model.FarmOfficialInfo
import com.example.model.FarmTransaction
import com.example.model.MonthlyDashboardSummary
import com.example.ui.theme.AgroGreenPrimary
import com.example.util.PdfReportGenerator
import com.example.util.PdfReportItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class PdfReportTypeSelection {
    BALANCE_SHEET,
    FINANCIAL_SUMMARY,
    COMPLETE_AUDIT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfExportDialog(
    initialType: PdfReportTypeSelection = PdfReportTypeSelection.BALANCE_SHEET,
    balanceSheet: BalanceSheetSummary,
    balanceSheetLedger: FarmBalanceSheetLedger,
    dashboardSummary: MonthlyDashboardSummary,
    transactions: List<FarmTransaction>,
    isEnglish: Boolean = false,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedReportType by remember { mutableStateOf(initialType) }
    var isGenerating by remember { mutableStateOf(false) }
    var lastGeneratedFile by remember { mutableStateOf<File?>(null) }
    var savedReports by remember { mutableStateOf<List<PdfReportItem>>(emptyList()) }

    fun refreshSavedReports() {
        savedReports = PdfReportGenerator.getSavedReports(context)
    }

    LaunchedEffect(Unit) {
        refreshSavedReports()
    }

    Dialog(
        onDismissRequest = { if (!isGenerating) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxSize(0.92f)
                .testTag("pdf_export_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Dialog Header
                Surface(
                    color = AgroGreenPrimary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD54F),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isEnglish) "Financial PDF Reports" else "वित्तीय प्रतिवेदन (PDF Export)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${FarmOfficialInfo.FARM_NAME_NP} | PAN: ${FarmOfficialInfo.PAN_VAT_NO}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("btn_close_pdf_dialog")
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                // Sub-tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = AgroGreenPrimary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isEnglish) "Export Report" else "नयाँ प्रतिवेदन निकाल्नुहोस्",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        },
                        modifier = Modifier.testTag("tab_export_new_pdf")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            refreshSavedReports()
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isEnglish) "Saved Records (${savedReports.size})" else "सुरक्षित प्रतिवेदनहरू (${savedReports.size})",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        },
                        modifier = Modifier.testTag("tab_saved_pdf_history")
                    )
                }

                if (selectedTab == 0) {
                    // TAB 0: EXPORT NEW REPORT
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Text(
                                text = if (isEnglish) "Select Report Document to Generate:" else "कुन प्रतिवेदन PDF मा निर्यात गर्न चाहनुहुन्छ छान्नुहोस्:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B3B22)
                            )
                        }

                        // Option 1: Balance Sheet
                        item {
                            ReportTypeOptionCard(
                                title = if (isEnglish) "Official Balance Sheet Statement" else "कम्पनी वासलात विवरण (Balance Sheet)",
                                nepaliDesc = "चालु सम्पत्ति, स्थिर सम्पत्ति, पशुधन, दायित्व, पुँजी तथा लेखापरीक्षण सन्तुलन विवरण",
                                badge = "As of ${balanceSheet.asOfDate}",
                                icon = Icons.Default.AccountBalance,
                                isSelected = selectedReportType == PdfReportTypeSelection.BALANCE_SHEET,
                                primaryColor = Color(0xFF1B5E20),
                                onClick = { selectedReportType = PdfReportTypeSelection.BALANCE_SHEET },
                                testTag = "option_balance_sheet"
                            )
                        }

                        // Option 2: Monthly Financial Summary & P&L
                        item {
                            ReportTypeOptionCard(
                                title = if (isEnglish) "Monthly Financial & P&L Summary" else "मासिक वित्तीय समीक्षा तथा नाफा-नोक्सान",
                                nepaliDesc = "कुल आम्दानी (स्रोत अनुसार), कुल खर्च (शीर्षक अनुसार), खुद बचत र दैनिक आय-व्यय गतिशीलता",
                                badge = "महिना: ${dashboardSummary.monthLabelNp}",
                                icon = Icons.Default.TrendingUp,
                                isSelected = selectedReportType == PdfReportTypeSelection.FINANCIAL_SUMMARY,
                                primaryColor = Color(0xFF0277BD),
                                onClick = { selectedReportType = PdfReportTypeSelection.FINANCIAL_SUMMARY },
                                testTag = "option_financial_summary"
                            )
                        }

                        // Option 3: Complete Audit Pack
                        item {
                            ReportTypeOptionCard(
                                title = if (isEnglish) "Complete Executive Master Audit (2 Pages)" else "समग्र वित्तीय प्रतिवेदन तथा कम्पनी वासलात (Master Audit)",
                                nepaliDesc = "वासलात (पृष्ठ १) र नाफा-नोक्सान तथा दैनिक आय-व्यय तालिका (पृष्ठ २) एकैसाथ आधिकारिक प्याक",
                                badge = "२ पृष्ठ आधिकारिक दस्तावेज",
                                icon = Icons.Default.PictureAsPdf,
                                isSelected = selectedReportType == PdfReportTypeSelection.COMPLETE_AUDIT,
                                primaryColor = Color(0xFF6A1B9A),
                                onClick = { selectedReportType = PdfReportTypeSelection.COMPLETE_AUDIT },
                                testTag = "option_complete_audit"
                            )
                        }

                        // Recent Generation Success Card
                        if (lastGeneratedFile != null) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                    border = BorderStroke(1.5.dp, Color(0xFF4CAF50)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Color(0xFF2E7D32),
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = if (isEnglish) "PDF Document Ready!" else "प्रतिवेदन सफलतापूर्वक तयार भयो!",
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF1B5E20)
                                                )
                                                Text(
                                                    text = lastGeneratedFile?.name ?: "",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color(0xFF2E7D32),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    lastGeneratedFile?.let { file ->
                                                        PdfReportGenerator.openPdf(context, file)
                                                    }
                                                },
                                                modifier = Modifier.weight(1f).testTag("btn_open_generated_pdf"),
                                                colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(text = if (isEnglish) "Open PDF" else "खोल्नुहोस्")
                                            }

                                            Button(
                                                onClick = {
                                                    lastGeneratedFile?.let { file ->
                                                        PdfReportGenerator.sharePdf(context, file)
                                                    }
                                                },
                                                modifier = Modifier.weight(1f).testTag("btn_share_generated_pdf"),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0277BD)),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(text = if (isEnglish) "Share PDF" else "साझा गर्नुहोस्")
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Info details
                        item {
                            Surface(
                                color = Color(0xFFF5F7F5),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Print,
                                        contentDescription = null,
                                        tint = AgroGreenPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = if (isEnglish)
                                            "All PDF exports adhere to official A4 standards with signature blocks for Accountant, Auditor, and Managing Director verification."
                                        else
                                            "सबै PDF प्रतिवेदनहरू नेपालको मानक A4 साइजमा फर्मट गरिएका छन् जसमा लेखापाल, आन्तरिक लेखापरीक्षक र सञ्चालकको हस्ताक्षर ब्लक समावेश छ।",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }
                    }

                    // Bottom Action Bar
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(text = if (isEnglish) "Cancel" else "बन्द गर्नुहोस्")
                            }

                            Button(
                                onClick = {
                                    if (isGenerating) return@Button
                                    isGenerating = true
                                    scope.launch {
                                        val file = withContext(Dispatchers.IO) {
                                            try {
                                                when (selectedReportType) {
                                                    PdfReportTypeSelection.BALANCE_SHEET -> {
                                                        PdfReportGenerator.generateBalanceSheetPdf(
                                                            context = context,
                                                            balanceSheet = balanceSheet,
                                                            ledger = balanceSheetLedger,
                                                            isEnglish = isEnglish
                                                        )
                                                    }
                                                    PdfReportTypeSelection.FINANCIAL_SUMMARY -> {
                                                        PdfReportGenerator.generateFinancialSummaryPdf(
                                                            context = context,
                                                            summary = dashboardSummary,
                                                            transactions = transactions,
                                                            isEnglish = isEnglish
                                                        )
                                                    }
                                                    PdfReportTypeSelection.COMPLETE_AUDIT -> {
                                                        PdfReportGenerator.generateCompleteAuditReportPdf(
                                                            context = context,
                                                            balanceSheet = balanceSheet,
                                                            ledger = balanceSheetLedger,
                                                            summary = dashboardSummary,
                                                            transactions = transactions,
                                                            isEnglish = isEnglish
                                                        )
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                null
                                            }
                                        }

                                        isGenerating = false
                                        if (file != null && file.exists()) {
                                            lastGeneratedFile = file
                                            refreshSavedReports()
                                            Toast.makeText(context, "PDF सफलतापूर्वक सिर्जना भयो! (${file.name})", Toast.LENGTH_SHORT).show()
                                            // Auto-open or prompt
                                            PdfReportGenerator.openPdf(context, file)
                                        } else {
                                            Toast.makeText(context, "PDF सिर्जना गर्न समस्या आयो। कृपया पुन: प्रयास गर्नुहोस्।", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                enabled = !isGenerating,
                                modifier = Modifier.weight(1.6f).testTag("btn_generate_pdf_now"),
                                colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                if (isGenerating) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "तयार हुँदैछ...")
                                } else {
                                    Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isEnglish) "Generate & View PDF" else "PDF बनाउनुहोस्",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // TAB 1: SAVED PDF HISTORY / RECORD-KEEPING
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isEnglish) "Saved Financial Records (Device Storage):" else "फार्मको सुरक्षित वित्तीय प्रतिवेदनहरू (आन्तरिक भण्डारण):",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B3B22)
                            )
                            IconButton(
                                onClick = { refreshSavedReports() },
                                modifier = Modifier.testTag("btn_refresh_pdf_history")
                            ) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", tint = AgroGreenPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (savedReports.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = Color.LightGray,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = if (isEnglish) "No PDF reports exported yet" else "हालसम्म कुनै प्रतिवेदन सुरक्षित गरिएको छैन",
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = if (isEnglish) "Export your first report from the tab above." else "माथिको ट्याबबाट पहिलो वासलात वा नाफा-नोक्सान PDF निकाल्नुहोस्।",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.LightGray
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(savedReports) { reportItem ->
                                    SavedReportCard(
                                        item = reportItem,
                                        isEnglish = isEnglish,
                                        onOpen = { PdfReportGenerator.openPdf(context, reportItem.file) },
                                        onShare = { PdfReportGenerator.sharePdf(context, reportItem.file) },
                                        onDelete = {
                                            PdfReportGenerator.deleteReport(reportItem.file)
                                            refreshSavedReports()
                                            Toast.makeText(context, "फाइल हटाइयो", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportTypeOptionCard(
    title: String,
    nepaliDesc: String,
    badge: String,
    icon: ImageVector,
    isSelected: Boolean,
    primaryColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) primaryColor.copy(alpha = 0.08f) else Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) primaryColor else Color(0xFFE0E0E0)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(if (isSelected) primaryColor else Color(0xFFF0F0F0), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else primaryColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) primaryColor else Color(0xFF212121)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = nepaliDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    color = primaryColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = primaryColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedReportCard(
    item: PdfReportItem,
    isEnglish: Boolean,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("saved_report_card_${item.name}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color(0xFFE8E8E8))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(Color(0xFFFFEBEE), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    tint = Color(0xFFC62828),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOpen() }
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B3B22),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${item.dateCreated}  •  ${item.sizeFormatted}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onOpen,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = "Open",
                        tint = AgroGreenPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onShare,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color(0xFF0277BD),
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
