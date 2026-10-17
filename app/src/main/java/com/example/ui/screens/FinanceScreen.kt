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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BalanceSheetSummary
import com.example.model.FarmBalanceSheetLedger
import com.example.model.FarmOfficialInfo
import com.example.model.FarmTransaction
import com.example.model.MonthlyDashboardSummary
import com.example.model.TransactionCategory
import com.example.model.TransactionPaymentMode
import com.example.model.TransactionType
import com.example.ui.components.PdfExportDialog
import com.example.ui.components.PdfReportTypeSelection
import com.example.ui.theme.AgroGoldTertiary
import com.example.ui.theme.AgroGreenPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
    transactions: List<FarmTransaction>,
    balanceSheet: BalanceSheetSummary,
    balanceSheetLedger: FarmBalanceSheetLedger,
    selectedDate: String,
    dashboardSummary: MonthlyDashboardSummary? = null,
    isEnglish: Boolean = false,
    onDateChange: (String) -> Unit,
    onAddTransaction: (TransactionType, TransactionCategory, Double, TransactionPaymentMode, String, String, String, String) -> Unit,
    onDeleteTransaction: (String) -> Unit,
    onUpdateLedger: (FarmBalanceSheetLedger) -> Unit,
    onOpenDashboard: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddTxDialog by remember { mutableStateOf(false) }
    var showEditLedgerDialog by remember { mutableStateOf(false) }
    var showPdfExportDialog by remember { mutableStateOf(false) }
    var pdfExportInitialType by remember { mutableStateOf(PdfReportTypeSelection.BALANCE_SHEET) }
    var transactionTypeFilter by remember { mutableStateOf<TransactionType?>(null) }

    val dailyTx = transactions.filter { it.date == selectedDate }
    val filteredDailyTx = if (transactionTypeFilter == null) {
        dailyTx
    } else {
        dailyTx.filter { it.type == transactionTypeFilter }
    }

    val dailyIncome = dailyTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val dailyExpense = dailyTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val dailyNet = dailyIncome - dailyExpense

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("finance_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SecondaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = AgroGreenPrimary,
                modifier = Modifier.testTag("finance_tabs")
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = if (isEnglish) "Daily Cash Book" else "दैनिक आय-व्यय",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("tab_daily_cashbook")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = if (isEnglish) "Balance Sheet" else "ब्यालेन्स सिट",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("tab_balance_sheet")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            text = if (isEnglish) "P&L Summary" else "नाफा-नोक्सान",
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("tab_pnl_summary")
                )
                Tab(
                    selected = false,
                    onClick = { onOpenDashboard() },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isEnglish) "Recharts" else "चार्ट (Recharts)",
                                fontWeight = FontWeight.Bold,
                                color = AgroGreenPrimary
                            )
                        }
                    },
                    modifier = Modifier.testTag("tab_recharts_dashboard")
                )
            }

            when (selectedTab) {
                0 -> {
                    // TAB 0: DAILY INCOME & EXPENSES
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        // Date Bar & Daily Totals Card
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 2.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = { onDateChange(shiftDate(selectedDate, -1)) },
                                            modifier = Modifier.testTag("btn_prev_tx_date")
                                        ) {
                                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev Day", tint = AgroGreenPrimary)
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.clickable {
                                                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                                onDateChange(today)
                                            }
                                        ) {
                                            Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = AgroGreenPrimary, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = selectedDate,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1B3B22)
                                            )
                                        }

                                        IconButton(
                                            onClick = { onDateChange(shiftDate(selectedDate, 1)) },
                                            modifier = Modifier.testTag("btn_next_tx_date")
                                        ) {
                                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Day", tint = AgroGreenPrimary)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Daily Income, Expense & Net Cards
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        FinanceStatCard(
                                            title = if (isEnglish) "Today's Income" else "आजको आम्दानी",
                                            amount = dailyIncome,
                                            isPositive = true,
                                            modifier = Modifier.weight(1f)
                                        )
                                        FinanceStatCard(
                                            title = if (isEnglish) "Today's Expense" else "आजको खर्च",
                                            amount = dailyExpense,
                                            isPositive = false,
                                            modifier = Modifier.weight(1f)
                                        )
                                        FinanceStatCard(
                                            title = if (isEnglish) "Net Balance" else "खुद बचत",
                                            amount = dailyNet,
                                            isPositive = dailyNet >= 0,
                                            isNeutral = true,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Filter Chips and PDF Export
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f, fill = false)
                                        ) {
                                            FilterChip(
                                                selected = transactionTypeFilter == null,
                                                onClick = { transactionTypeFilter = null },
                                                label = { Text(if (isEnglish) "All (${dailyTx.size})" else "सबै (${dailyTx.size})") }
                                            )
                                            FilterChip(
                                                selected = transactionTypeFilter == TransactionType.INCOME,
                                                onClick = { transactionTypeFilter = TransactionType.INCOME },
                                                label = { Text(if (isEnglish) "Income" else "आम्दानी") },
                                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFE8F5E9))
                                            )
                                            FilterChip(
                                                selected = transactionTypeFilter == TransactionType.EXPENSE,
                                                onClick = { transactionTypeFilter = TransactionType.EXPENSE },
                                                label = { Text(if (isEnglish) "Expenses" else "खर्च") },
                                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFFFEBEE))
                                            )
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                pdfExportInitialType = PdfReportTypeSelection.FINANCIAL_SUMMARY
                                                showPdfExportDialog = true
                                            },
                                            modifier = Modifier.testTag("btn_export_daily_pdf"),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFC62828))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = if (isEnglish) "PDF" else "PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        if (filteredDailyTx.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = if (isEnglish) "No transactions recorded for $selectedDate" else "$selectedDate मा कुनै आय वा व्यय प्रविष्टि छैन।",
                                            color = Color.Gray,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Button(
                                            onClick = { showAddTxDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary)
                                        ) {
                                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(text = if (isEnglish) "Record Transaction" else "नयाँ आय वा व्यय थप्नुहोस्")
                                        }
                                    }
                                }
                            }
                        } else {
                            items(filteredDailyTx, key = { it.id }) { tx ->
                                TransactionItemCard(
                                    transaction = tx,
                                    isEnglish = isEnglish,
                                    onDelete = { onDeleteTransaction(tx.id) }
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: FARM BALANCE SHEET
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        item {
                            Surface(
                                color = AgroGreenPrimary,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = FarmOfficialInfo.FARM_NAME_NP,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (isEnglish) "Official Farm Balance Sheet" else "कम्पनीको आधिकारिक वासलात (Balance Sheet)",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFFFD54F)
                                    )
                                    Text(
                                        text = "मिति (As of Date): ${balanceSheet.asOfDate}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { showEditLedgerDialog = true },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color.White,
                                                contentColor = AgroGreenPrimary
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1.1f).testTag("btn_edit_balance_sheet")
                                        ) {
                                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = if (isEnglish) "Edit Values" else "मूल्यांकन सम्पादन", maxLines = 1)
                                        }

                                        Button(
                                            onClick = {
                                                pdfExportInitialType = PdfReportTypeSelection.BALANCE_SHEET
                                                showPdfExportDialog = true
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFFFD54F),
                                                contentColor = Color(0xFF1B3B22)
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(0.9f).testTag("btn_export_balance_sheet_pdf")
                                        ) {
                                            Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = if (isEnglish) "Export PDF" else "वासलात PDF", fontWeight = FontWeight.Bold, maxLines = 1)
                                        }
                                    }
                                }
                            }
                        }

                        // Equation Balance Check Card
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (balanceSheet.isBalanced) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, if (balanceSheet.isBalanced) Color(0xFF4CAF50) else Color(0xFFFF9800))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (balanceSheet.isBalanced) Icons.Default.CheckCircle else Icons.Default.Scale,
                                        contentDescription = null,
                                        tint = if (balanceSheet.isBalanced) Color(0xFF2E7D32) else Color(0xFFE65100),
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = if (isEnglish) "Accounting Equation Balanced" else "वित्तीय सन्तुलन (Total Assets = Liabilities + Equity)",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (balanceSheet.isBalanced) Color(0xFF1B5E20) else Color(0xFFE65100)
                                        )
                                        Text(
                                            text = "कुल सम्पत्ति: रु. ${balanceSheet.totalAssets.toInt()} = दायित्व + पुँजी: रु. ${(balanceSheet.totalLiabilities + balanceSheet.totalEquity).toInt()}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.DarkGray
                                        )
                                    }
                                }
                            }
                        }

                        // SECTION 1: ASSETS
                        item {
                            BalanceSheetSectionCard(
                                sectionTitle = if (isEnglish) "1. ASSETS (सम्पत्तिहरू)" else "१. कुल सम्पत्ति (Assets)",
                                totalAmount = balanceSheet.totalAssets,
                                headerColor = Color(0xFF1B5E20)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(text = if (isEnglish) "Current Assets" else "चालु सम्पत्ति (Current Assets):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                    BalanceSheetRowItem("नगद मौज्दात (Cash in Hand)", balanceSheet.cashInHand)
                                    BalanceSheetRowItem("मुक्तिनाथ विकास बैंक (A/C: 0961...)", balanceSheet.bankBalanceMuktinath)
                                    BalanceSheetRowItem("ईसेवा वालेट (eSewa Wallet)", balanceSheet.esewaBalance)
                                    BalanceSheetRowItem("उधारो असुल हुन बाँकी (Accounts Receivable)", balanceSheet.accountsReceivable)
                                    BalanceSheetRowItem("मौज्दात उत्पादन स्टक मूल्य (Live Inventory Stock)", balanceSheet.inventoryStockValue, highlight = true)

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                    BalanceSheetRowItem("कुल चालु सम्पत्ति (Total Current Assets)", balanceSheet.totalCurrentAssets, isSubtotal = true)

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = if (isEnglish) "Fixed & Biological Assets" else "स्थिर तथा जैविक सम्पत्ति (Fixed & Livestock):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                    BalanceSheetRowItem("दूध चिलिङ भ्याट, उपकरण तथा मेशिनरी", balanceSheet.fixedAssetsEquipment)
                                    BalanceSheetRowItem("फार्म जग्गा, टनेल, गोठ तथा संरचना", balanceSheet.fixedAssetsLandShed)
                                    BalanceSheetRowItem("पशुधन मूल्यांकन (दूधालु गाई, बाख्रा, कुखुरा)", balanceSheet.livestockValuation)

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                    BalanceSheetRowItem("कुल स्थिर सम्पत्ति (Total Fixed Assets)", balanceSheet.totalFixedAssets, isSubtotal = true)
                                }
                            }
                        }

                        // SECTION 2: LIABILITIES
                        item {
                            BalanceSheetSectionCard(
                                sectionTitle = if (isEnglish) "2. LIABILITIES (दायित्वहरू)" else "२. कुल दायित्व (Liabilities)",
                                totalAmount = balanceSheet.totalLiabilities,
                                headerColor = Color(0xFFC62828)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    BalanceSheetRowItem("दाना तथा सामाग्री साहुलाई तिर्न बाँकी (Payables)", balanceSheet.accountsPayable)
                                    BalanceSheetRowItem("कर्मचारी तलब तथा ज्याला भुक्तानी बाँकी", balanceSheet.accruedSalaries)
                                    BalanceSheetRowItem("कृषि सहकारी तथा बैंक ऋण (Agro Loans)", balanceSheet.shortTermLoans)
                                }
                            }
                        }

                        // SECTION 3: OWNER'S EQUITY
                        item {
                            BalanceSheetSectionCard(
                                sectionTitle = if (isEnglish) "3. OWNER'S EQUITY (पुँजी तथा खुद नाफा)" else "३. शेयरधनी पुँजी तथा खुद बचत (Equity)",
                                totalAmount = balanceSheet.totalEquity,
                                headerColor = Color(0xFF0D47A1)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    BalanceSheetRowItem("सञ्चालकहरूको लगानी पुँजी (Owner Capital)", balanceSheet.ownerCapital)
                                    BalanceSheetRowItem("फार्म सञ्चालनबाट प्राप्त खुद बचत/नाफा (Net Profit)", balanceSheet.netFarmProfit, highlight = true)
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: P&L SUMMARY
                    val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                    val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                    val netOperatingProfit = totalIncome - totalExpense

                    val incomeByCategory = transactions.filter { it.type == TransactionType.INCOME }
                        .groupBy { it.category }
                        .mapValues { entry -> entry.value.sumOf { it.amount } }

                    val expenseByCategory = transactions.filter { it.type == TransactionType.EXPENSE }
                        .groupBy { it.category }
                        .mapValues { entry -> entry.value.sumOf { it.amount } }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 2.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = if (isEnglish) "Farm Profit & Loss Statement" else "फार्मको नाफा-नोक्सान विवरण (P&L Summary)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1B3B22)
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Card(
                                            modifier = Modifier.weight(1f),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text(text = if (isEnglish) "Total Farm Revenue" else "कुल आम्दानी", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32))
                                                Text(text = "रु. ${totalIncome.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                                            }
                                        }

                                        Card(
                                            modifier = Modifier.weight(1f),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text(text = if (isEnglish) "Total Farm Expenses" else "कुल खर्च", style = MaterialTheme.typography.labelSmall, color = Color(0xFFC62828))
                                                Text(text = "रु. ${totalExpense.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFB71C1C))
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (netOperatingProfit >= 0) Color(0xFFC8E6C9) else Color(0xFFFFCDD2)
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (isEnglish) "Net Operating Profit:" else "फार्मको कुल खुद नाफा / बचत:",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleSmall
                                            )
                                            Text(
                                                text = "रु. ${netOperatingProfit.toInt()}",
                                                fontWeight = FontWeight.ExtraBold,
                                                style = MaterialTheme.typography.titleLarge,
                                                color = if (netOperatingProfit >= 0) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            pdfExportInitialType = PdfReportTypeSelection.FINANCIAL_SUMMARY
                                            showPdfExportDialog = true
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = AgroGreenPrimary,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("btn_export_pnl_pdf")
                                    ) {
                                        Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFFFFD54F))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isEnglish) "Export Monthly Financial Summary (PDF)" else "मासिक वित्तीय समीक्षा तथा नाफा-नोक्सान PDF निकाल्नुहोस्",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Category wise Revenue breakdown
                        item {
                            CategoryBreakdownCard(
                                title = if (isEnglish) "Revenue by Farm Production (आम्दानी स्रोत)" else "उत्पादन अनुसारको आम्दानी स्रोत",
                                categoryTotals = incomeByCategory,
                                grandTotal = totalIncome,
                                isIncome = true
                            )
                        }

                        // Category wise Expense breakdown
                        item {
                            CategoryBreakdownCard(
                                title = if (isEnglish) "Operating Expenses Breakdown (खर्च विवरण)" else "सञ्चालन खर्चको विवरण",
                                categoryTotals = expenseByCategory,
                                grandTotal = totalExpense,
                                isIncome = false
                            )
                        }
                    }
                }
            }
        }

        // FAB to Record Income / Expense
        if (selectedTab == 0) {
            FloatingActionButton(
                onClick = { showAddTxDialog = true },
                containerColor = AgroGoldTertiary,
                contentColor = Color(0xFF1B3B22),
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("fab_add_transaction")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    }

    if (showAddTxDialog) {
        AddTransactionDialog(
            defaultDate = selectedDate,
            isEnglish = isEnglish,
            onDismiss = { showAddTxDialog = false },
            onSubmit = { type, category, amount, mode, party, ref, desc, date ->
                onAddTransaction(type, category, amount, mode, party, ref, desc, date)
                showAddTxDialog = false
            }
        )
    }

    if (showEditLedgerDialog) {
        EditBalanceSheetLedgerDialog(
            ledger = balanceSheetLedger,
            isEnglish = isEnglish,
            onDismiss = { showEditLedgerDialog = false },
            onSubmit = { updated ->
                onUpdateLedger(updated)
                showEditLedgerDialog = false
            }
        )
    }

    if (showPdfExportDialog) {
        PdfExportDialog(
            initialType = pdfExportInitialType,
            balanceSheet = balanceSheet,
            balanceSheetLedger = balanceSheetLedger,
            dashboardSummary = dashboardSummary ?: MonthlyDashboardSummary(
                monthYear = selectedDate.take(7),
                monthLabelNp = "चालु महिना",
                monthLabelEn = "Current Month",
                totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount },
                totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount },
                netCashFlow = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount } - transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount },
                currentInventoryValue = balanceSheet.inventoryStockValue,
                startInventoryValue = balanceSheet.inventoryStockValue,
                inventoryValueChange = 0.0,
                totalStockUnits = 0.0,
                lowStockCount = 0,
                dailyTrends = emptyList()
            ),
            transactions = transactions,
            isEnglish = isEnglish,
            onDismiss = { showPdfExportDialog = false }
        )
    }
}

@Composable
fun FinanceStatCard(
    title: String,
    amount: Double,
    isPositive: Boolean,
    isNeutral: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = when {
                isNeutral -> Color(0xFFF1F8E9)
                isPositive -> Color(0xFFE8F5E9)
                else -> Color(0xFFFFEBEE)
            }
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = Color.Gray, maxLines = 1)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "रु. ${amount.toInt()}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = when {
                    isNeutral -> Color(0xFF2E7D32)
                    isPositive -> Color(0xFF1B5E20)
                    else -> Color(0xFFC62828)
                }
            )
        }
    }
}

@Composable
fun TransactionItemCard(
    transaction: FarmTransaction,
    isEnglish: Boolean,
    onDelete: () -> Unit
) {
    val isIncome = transaction.type == TransactionType.INCOME

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 5.dp)
            .testTag("tx_card_${transaction.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = if (isIncome) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                shape = CircleShape,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = transaction.category.emoji, fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isEnglish) transaction.category.labelEn else transaction.category.labelNp,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B3B22)
                )

                if (transaction.partyName.isNotBlank() || transaction.description.isNotBlank()) {
                    Text(
                        text = listOf(transaction.partyName, transaction.description).filter { it.isNotBlank() }.joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isEnglish) transaction.paymentMode.labelEn else transaction.paymentMode.labelNp,
                        style = MaterialTheme.typography.labelSmall,
                        color = AgroGreenPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    if (transaction.referenceNo.isNotBlank()) {
                        Text(text = " (भौचर: ${transaction.referenceNo})", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isIncome) "+" else "-"} रु. ${transaction.amount.toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isIncome) Color(0xFF1B5E20) else Color(0xFFC62828)
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun BalanceSheetSectionCard(
    sectionTitle: String,
    totalAmount: Double,
    headerColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sectionTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = headerColor
                )
                Text(
                    text = "रु. ${totalAmount.toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = headerColor
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            content()
        }
    }
}

@Composable
fun BalanceSheetRowItem(
    title: String,
    amount: Double,
    highlight: Boolean = false,
    isSubtotal: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = if (isSubtotal) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
            fontWeight = if (isSubtotal || highlight) FontWeight.Bold else FontWeight.Normal,
            color = if (highlight) AgroGreenPrimary else Color.DarkGray
        )
        Text(
            text = "रु. ${amount.toInt()}",
            style = if (isSubtotal) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
            fontWeight = if (isSubtotal || highlight) FontWeight.Bold else FontWeight.SemiBold,
            color = if (highlight) AgroGreenPrimary else Color.Black
        )
    }
}

@Composable
fun CategoryBreakdownCard(
    title: String,
    categoryTotals: Map<TransactionCategory, Double>,
    grandTotal: Double,
    isIncome: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            if (categoryTotals.isEmpty()) {
                Text(text = "कुनै विवरण रेकर्ड भएको छैन।", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            } else {
                categoryTotals.entries.sortedByDescending { it.value }.forEach { (cat, amount) ->
                    val percentage = if (grandTotal > 0) (amount / grandTotal) * 100.0 else 0.0
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "${cat.emoji} ${cat.labelNp}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                            Text(text = "रु. ${amount.toInt()} (%.1f%%)".format(percentage), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { (percentage / 100.0).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp),
                            color = if (isIncome) AgroGreenPrimary else Color(0xFFC62828),
                            trackColor = Color(0xFFEEEEEE),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    defaultDate: String,
    isEnglish: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (TransactionType, TransactionCategory, Double, TransactionPaymentMode, String, String, String, String) -> Unit
) {
    var selectedType by remember { mutableStateOf(TransactionType.INCOME) }
    val availableCategories = TransactionCategory.values().filter { it.type == selectedType }
    var selectedCategory by remember { mutableStateOf(availableCategories.first()) }
    var amount by remember { mutableStateOf("") }
    var selectedPaymentMode by remember { mutableStateOf(TransactionPaymentMode.BANK_TRANSFER) }
    var partyName by remember { mutableStateOf("") }
    var refNo by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(defaultDate) }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var paymentDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEnglish) "Record Farm Transaction" else "दैनिक आय वा व्यय दर्ता",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Type selector (Income vs Expense)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            selectedType = TransactionType.INCOME
                            selectedCategory = TransactionCategory.values().first { it.type == TransactionType.INCOME }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedType == TransactionType.INCOME) Color(0xFF2E7D32) else Color(0xFFE0E0E0),
                            contentColor = if (selectedType == TransactionType.INCOME) Color.White else Color.Black
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isEnglish) "Income" else "आम्दानी")
                    }

                    Button(
                        onClick = {
                            selectedType = TransactionType.EXPENSE
                            selectedCategory = TransactionCategory.values().first { it.type == TransactionType.EXPENSE }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedType == TransactionType.EXPENSE) Color(0xFFC62828) else Color(0xFFE0E0E0),
                            contentColor = if (selectedType == TransactionType.EXPENSE) Color.White else Color.Black
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isEnglish) "Expense" else "खर्च")
                    }
                }

                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = "${selectedCategory.emoji} ${if (isEnglish) selectedCategory.labelEn else selectedCategory.labelNp}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isEnglish) "Category" else "वर्ग / शीर्षक") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        availableCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text("${cat.emoji} ${if (isEnglish) cat.labelEn else cat.labelNp}") },
                                onClick = {
                                    selectedCategory = cat
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(if (isEnglish) "Amount in NPR (रु.)" else "रकम (रु.)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_tx_amount")
                )

                // Payment mode dropdown
                ExposedDropdownMenuBox(
                    expanded = paymentDropdownExpanded,
                    onExpandedChange = { paymentDropdownExpanded = !paymentDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = if (isEnglish) selectedPaymentMode.labelEn else selectedPaymentMode.labelNp,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isEnglish) "Payment Method" else "भुक्तानी माध्यम") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentDropdownExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = paymentDropdownExpanded,
                        onDismissRequest = { paymentDropdownExpanded = false }
                    ) {
                        TransactionPaymentMode.values().forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(if (isEnglish) mode.labelEn else mode.labelNp) },
                                onClick = {
                                    selectedPaymentMode = mode
                                    paymentDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = partyName,
                    onValueChange = { partyName = it },
                    label = { Text(if (isEnglish) "Customer / Vendor Party" else "ग्राहक वा आपूर्तिकर्ताको नाम") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = refNo,
                    onValueChange = { refNo = it },
                    label = { Text(if (isEnglish) "Invoice / Voucher Reference #" else "भौचर वा बिल नम्बर") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(if (isEnglish) "Description" else "विवरण / कैफियत") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedAmt = amount.toDoubleOrNull() ?: 0.0
                    if (parsedAmt > 0) {
                        onSubmit(
                            selectedType,
                            selectedCategory,
                            parsedAmt,
                            selectedPaymentMode,
                            partyName.trim(),
                            refNo.trim(),
                            description.trim(),
                            date
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary),
                modifier = Modifier.testTag("btn_save_transaction")
            ) {
                Text(text = if (isEnglish) "Save Entry" else "सुरक्षित गर्नुहोस्")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = if (isEnglish) "Cancel" else "रद्द गर्नुहोस्")
            }
        }
    )
}

@Composable
fun EditBalanceSheetLedgerDialog(
    ledger: FarmBalanceSheetLedger,
    isEnglish: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (FarmBalanceSheetLedger) -> Unit
) {
    var cashInHand by remember { mutableStateOf(ledger.cashInHand.toString()) }
    var bankBalance by remember { mutableStateOf(ledger.bankBalanceMuktinath.toString()) }
    var esewaBalance by remember { mutableStateOf(ledger.esewaBalance.toString()) }
    var receivables by remember { mutableStateOf(ledger.accountsReceivable.toString()) }
    var equipment by remember { mutableStateOf(ledger.fixedAssetsEquipment.toString()) }
    var landShed by remember { mutableStateOf(ledger.fixedAssetsLandShed.toString()) }
    var livestock by remember { mutableStateOf(ledger.livestockValuation.toString()) }
    var payables by remember { mutableStateOf(ledger.accountsPayable.toString()) }
    var salariesDue by remember { mutableStateOf(ledger.accruedSalaries.toString()) }
    var loans by remember { mutableStateOf(ledger.shortTermLoans.toString()) }
    var capital by remember { mutableStateOf(ledger.ownerCapital.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEnglish) "Update Balance Sheet Valuations" else "वासलात सम्पत्ति तथा दायित्व मूल्यांकन",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Text(text = "चालु सम्पत्ति (Current Assets)", fontWeight = FontWeight.Bold, color = AgroGreenPrimary)
                }
                item {
                    OutlinedTextField(
                        value = cashInHand,
                        onValueChange = { cashInHand = it },
                        label = { Text("नगद मौज्दात (Cash in Hand)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = bankBalance,
                        onValueChange = { bankBalance = it },
                        label = { Text("मुक्तिनाथ विकास बैंक (Bank Balance)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = esewaBalance,
                        onValueChange = { esewaBalance = it },
                        label = { Text("ईसेवा वालेट मौज्दात (eSewa)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = receivables,
                        onValueChange = { receivables = it },
                        label = { Text("उधारो लिन बाँकी (Accounts Receivable)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Text(text = "स्थिर तथा जैविक सम्पत्ति (Fixed & Livestock)", fontWeight = FontWeight.Bold, color = AgroGreenPrimary)
                }
                item {
                    OutlinedTextField(
                        value = equipment,
                        onValueChange = { equipment = it },
                        label = { Text("उपकरण तथा चिलिङ भ्याट (Equipment)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = landShed,
                        onValueChange = { landShed = it },
                        label = { Text("गोठ, टनेल तथा संरचना (Land & Barns)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = livestock,
                        onValueChange = { livestock = it },
                        label = { Text("पशुधन मूल्यांकन (Livestock Herd Valuation)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Text(text = "दायित्व तथा पुँजी (Liabilities & Capital)", fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                }
                item {
                    OutlinedTextField(
                        value = payables,
                        onValueChange = { payables = it },
                        label = { Text("साहुलाई तिर्न बाँकी (Accounts Payable)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = salariesDue,
                        onValueChange = { salariesDue = it },
                        label = { Text("कर्मचारी तलब भुक्तानी बाँकी (Accrued Wages)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = loans,
                        onValueChange = { loans = it },
                        label = { Text("ऋण तथा सापटी (Loans & Credit)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = capital,
                        onValueChange = { capital = it },
                        label = { Text("सञ्चालक लगानी पुँजी (Owner Capital)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = ledger.copy(
                        cashInHand = cashInHand.toDoubleOrNull() ?: ledger.cashInHand,
                        bankBalanceMuktinath = bankBalance.toDoubleOrNull() ?: ledger.bankBalanceMuktinath,
                        esewaBalance = esewaBalance.toDoubleOrNull() ?: ledger.esewaBalance,
                        accountsReceivable = receivables.toDoubleOrNull() ?: ledger.accountsReceivable,
                        fixedAssetsEquipment = equipment.toDoubleOrNull() ?: ledger.fixedAssetsEquipment,
                        fixedAssetsLandShed = landShed.toDoubleOrNull() ?: ledger.fixedAssetsLandShed,
                        livestockValuation = livestock.toDoubleOrNull() ?: ledger.livestockValuation,
                        accountsPayable = payables.toDoubleOrNull() ?: ledger.accountsPayable,
                        accruedSalaries = salariesDue.toDoubleOrNull() ?: ledger.accruedSalaries,
                        shortTermLoans = loans.toDoubleOrNull() ?: ledger.shortTermLoans,
                        ownerCapital = capital.toDoubleOrNull() ?: ledger.ownerCapital
                    )
                    onSubmit(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary),
                modifier = Modifier.testTag("btn_save_ledger")
            ) {
                Text(text = if (isEnglish) "Save Valuations" else "मूल्यांकन सुरक्षित गर्नुहोस्")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = if (isEnglish) "Cancel" else "रद्द गर्नुहोस्")
            }
        }
    )
}
