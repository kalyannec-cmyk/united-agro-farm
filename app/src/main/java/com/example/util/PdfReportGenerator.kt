package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.model.BalanceSheetSummary
import com.example.model.DailyTrendPoint
import com.example.model.FarmBalanceSheetLedger
import com.example.model.FarmOfficialInfo
import com.example.model.FarmTransaction
import com.example.model.MonthlyDashboardSummary
import com.example.model.TransactionType
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PdfReportItem(
    val file: File,
    val name: String,
    val reportType: String,
    val dateCreated: String,
    val sizeBytes: Long
) {
    val sizeFormatted: String
        get() = "${(sizeBytes / 1024).coerceAtLeast(1)} KB"
}

object PdfReportGenerator {

    private const val PAGE_WIDTH = 595 // Standard A4 points (8.27 x 11.69 inches)
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 36f
    private const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2)

    private val numberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }

    private fun formatCurrency(amount: Double): String {
        return "रु. ${numberFormat.format(amount)}"
    }

    private fun getReportsDir(context: Context): File {
        val dir = File(context.filesDir, "reports")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Retrieves all saved PDF reports for record-keeping history
     */
    fun getSavedReports(context: Context): List<PdfReportItem> {
        val dir = getReportsDir(context)
        val files = dir.listFiles { f -> f.extension.equals("pdf", ignoreCase = true) } ?: return emptyList()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        return files.sortedByDescending { it.lastModified() }.map { file ->
            val type = when {
                file.name.contains("BalanceSheet", ignoreCase = true) -> "Balance Sheet"
                file.name.contains("FinancialSummary", ignoreCase = true) -> "Financial Summary"
                file.name.contains("AuditReport", ignoreCase = true) -> "Complete Audit Pack"
                else -> "Financial Report"
            }
            PdfReportItem(
                file = file,
                name = file.name,
                reportType = type,
                dateCreated = sdf.format(Date(file.lastModified())),
                sizeBytes = file.length()
            )
        }
    }

    /**
     * Delete an existing generated PDF
     */
    fun deleteReport(file: File): Boolean {
        return try {
            if (file.exists()) file.delete() else false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Opens PDF in default viewer application
     */
    fun openPdf(context: Context, file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("PdfReportGenerator", "Failed to open PDF", e)
            Toast.makeText(context, "PDF खोल्न उपयुक्त एप भेटिएन (No PDF viewer app found)", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Shares PDF via WhatsApp, Email, Drive, etc.
     */
    fun sharePdf(context: Context, file: File, subject: String = "United Agro Farm Financial Report") {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, "कृपया ललितपुर युनाइटेड एग्रो फार्मको आधिकारिक प्रतिवेदन फेला पार्नुहोस्। (${file.name})")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "प्रतिवेदन साझा गर्नुहोस् (Share Report via)")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e("PdfReportGenerator", "Failed to share PDF", e)
            Toast.makeText(context, "प्रतिवेदन साझा गर्न असफल: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // =========================================================================
    // REPORT 1: MONTHLY BALANCE SHEET PDF
    // =========================================================================

    fun generateBalanceSheetPdf(
        context: Context,
        balanceSheet: BalanceSheetSummary,
        ledger: FarmBalanceSheetLedger,
        isEnglish: Boolean = false
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        var y = drawDocumentHeader(
            canvas = canvas,
            title = if (isEnglish) "OFFICIAL BALANCE SHEET STATEMENT" else "कम्पनीको आधिकारिक वासलात विवरण (BALANCE SHEET)",
            subTitle = if (isEnglish) "Statement of Financial Position | As of: ${balanceSheet.asOfDate}" else "वित्तीय स्थिति विवरण | मिति: ${balanceSheet.asOfDate} सम्म",
            reportBadge = "AUDITED STATEMENT"
        )

        // Equation Check Banner
        val isBalanced = balanceSheet.isBalanced
        val bannerPaint = Paint().apply {
            color = if (isBalanced) Color.rgb(232, 245, 233) else Color.rgb(255, 243, 224)
            style = Paint.Style.FILL
        }
        val bannerBorder = Paint().apply {
            color = if (isBalanced) Color.rgb(46, 125, 50) else Color.rgb(230, 81, 0)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val bannerRect = RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 26f)
        canvas.drawRoundRect(bannerRect, 4f, 4f, bannerPaint)
        canvas.drawRoundRect(bannerRect, 4f, 4f, bannerBorder)

        val bannerTextPaint = Paint().apply {
            color = if (isBalanced) Color.rgb(27, 94, 32) else Color.rgb(191, 54, 12)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val statusMsg = if (isBalanced) {
            "✓ ACCOUNTING EQUATION BALANCED: Total Assets (${formatCurrency(balanceSheet.totalAssets)}) = Liabilities + Equity (${formatCurrency(balanceSheet.totalLiabilities + balanceSheet.totalEquity)})"
        } else {
            "⚠ VARIANCE NOTED: Total Assets (${formatCurrency(balanceSheet.totalAssets)}) ≠ Liabilities + Equity"
        }
        canvas.drawText(statusMsg, MARGIN + 10f, y + 17f, bannerTextPaint)
        y += 34f

        // 1. ASSETS SECTION
        y = drawSectionHeader(
            canvas = canvas,
            y = y,
            sectionNum = "1",
            title = if (isEnglish) "ASSETS (सम्पत्तिहरू)" else "१. कुल सम्पत्ति (ASSETS)",
            totalAmount = balanceSheet.totalAssets,
            accentColor = Color.rgb(27, 94, 32)
        )

        val textPaint = Paint().apply {
            textSize = 9f
            isAntiAlias = true
            color = Color.rgb(30, 30, 30)
        }
        val boldPaint = Paint().apply {
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            color = Color.rgb(20, 20, 20)
        }

        // Subhead: Current Assets
        y = drawSubhead(canvas, y, if (isEnglish) "Current Assets (चालु सम्पत्ति)" else "चालु सम्पत्ति (Current Assets)")
        y = drawTableRow(canvas, y, "  • Cash in Hand (नगद मौज्दात)", formatCurrency(balanceSheet.cashInHand), textPaint)
        y = drawTableRow(canvas, y, "  • Muktinath Bikas Bank Ltd (A/C: ${FarmOfficialInfo.BANK_ACCOUNT_NO.take(12)}...)", formatCurrency(balanceSheet.bankBalanceMuktinath), textPaint)
        y = drawTableRow(canvas, y, "  • eSewa Digital Wallet Balance (ID: ${FarmOfficialInfo.ESEWA_ID})", formatCurrency(balanceSheet.esewaBalance), textPaint)
        y = drawTableRow(canvas, y, "  • Accounts Receivable (उधारो असुल हुन बाँकी)", formatCurrency(balanceSheet.accountsReceivable), textPaint)
        y = drawTableRow(canvas, y, "  • Finished Goods & Live Inventory Stock Valuation", formatCurrency(balanceSheet.inventoryStockValue), textPaint)
        y = drawSubtotalRow(canvas, y, "Total Current Assets (कुल चालु सम्पत्ति)", formatCurrency(balanceSheet.totalCurrentAssets))

        // Subhead: Fixed & Biological Assets
        y += 4f
        y = drawSubhead(canvas, y, if (isEnglish) "Fixed & Biological Assets (स्थिर तथा पशुधन सम्पत्ति)" else "स्थिर तथा जैविक सम्पत्ति (Fixed & Biological Assets)")
        y = drawTableRow(canvas, y, "  • Dairy Equipment, Chiller Vat & Milking Systems", formatCurrency(balanceSheet.fixedAssetsEquipment), textPaint)
        y = drawTableRow(canvas, y, "  • Land, Sheds, Polyhouses & Farm Infrastructure", formatCurrency(balanceSheet.fixedAssetsLandShed), textPaint)
        y = drawTableRow(canvas, y, "  • Livestock Valuation (Dairy Cows, Goats, Poultry Birds)", formatCurrency(balanceSheet.livestockValuation), textPaint)
        y = drawSubtotalRow(canvas, y, "Total Fixed & Livestock Assets (कुल स्थिर सम्पत्ति)", formatCurrency(balanceSheet.totalFixedAssets))

        y = drawGrandTotalRow(canvas, y, "TOTAL ASSETS (कुल सम्पत्ति)", formatCurrency(balanceSheet.totalAssets), Color.rgb(27, 94, 32))

        y += 12f

        // 2. LIABILITIES SECTION
        y = drawSectionHeader(
            canvas = canvas,
            y = y,
            sectionNum = "2",
            title = if (isEnglish) "LIABILITIES (दायित्वहरू)" else "२. कुल दायित्व (LIABILITIES)",
            totalAmount = balanceSheet.totalLiabilities,
            accentColor = Color.rgb(198, 40, 40)
        )
        y = drawTableRow(canvas, y, "  • Accounts Payable (दाना, बिउ तथा सामाग्री साहु बाँकी)", formatCurrency(balanceSheet.accountsPayable), textPaint)
        y = drawTableRow(canvas, y, "  • Accrued Staff Wages & Salaries (कर्मचारी तलब बाँकी)", formatCurrency(balanceSheet.accruedSalaries), textPaint)
        y = drawTableRow(canvas, y, "  • Short-Term Agricultural Cooperative & Bank Loans", formatCurrency(balanceSheet.shortTermLoans), textPaint)
        y = drawGrandTotalRow(canvas, y, "TOTAL LIABILITIES (कुल दायित्व)", formatCurrency(balanceSheet.totalLiabilities), Color.rgb(198, 40, 40))

        y += 12f

        // 3. OWNER'S EQUITY SECTION
        y = drawSectionHeader(
            canvas = canvas,
            y = y,
            sectionNum = "3",
            title = if (isEnglish) "OWNER'S EQUITY (पुँजी तथा खुद बचत)" else "३. शेयरधनी पुँजी तथा खुद नाफा (EQUITY)",
            totalAmount = balanceSheet.totalEquity,
            accentColor = Color.rgb(13, 71, 161)
        )
        y = drawTableRow(canvas, y, "  • Paid-Up Shareholder Capital (सञ्चालकहरूको लगानी पुँजी)", formatCurrency(balanceSheet.ownerCapital), textPaint)
        y = drawTableRow(canvas, y, "  • Retained Farm Earnings / Net Profit (संचित खुद बचत/नाफा)", formatCurrency(balanceSheet.netFarmProfit), boldPaint)
        y = drawGrandTotalRow(canvas, y, "TOTAL OWNER'S EQUITY (कुल पुँजी तथा खुद बचत)", formatCurrency(balanceSheet.totalEquity), Color.rgb(13, 71, 161))

        y += 8f
        // Summary Equation Check
        y = drawGrandTotalRow(
            canvas,
            y,
            "TOTAL LIABILITIES & EQUITY (कुल दायित्व तथा पुँजी)",
            formatCurrency(balanceSheet.totalLiabilities + balanceSheet.totalEquity),
            Color.rgb(30, 40, 50)
        )

        // Sign-off / Verification signatures block at bottom
        drawSignOffBlock(canvas, PAGE_HEIGHT - 90f, balanceSheet.asOfDate)

        document.finishPage(page)

        val timeTag = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val outFile = File(getReportsDir(context), "BalanceSheet_${balanceSheet.asOfDate}_$timeTag.pdf")
        val outStream = FileOutputStream(outFile)
        document.writeTo(outStream)
        outStream.flush()
        outStream.close()
        document.close()

        return outFile
    }

    // =========================================================================
    // REPORT 2: MONTHLY FINANCIAL SUMMARY & P&L REPORT
    // =========================================================================

    fun generateFinancialSummaryPdf(
        context: Context,
        summary: MonthlyDashboardSummary,
        transactions: List<FarmTransaction>,
        isEnglish: Boolean = false
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        var y = drawDocumentHeader(
            canvas = canvas,
            title = if (isEnglish) "MONTHLY FINANCIAL SUMMARY & P&L STATEMENT" else "मासिक वित्तीय समीक्षा तथा नाफा-नोक्सान प्रतिवेदन",
            subTitle = if (isEnglish) "Month: ${summary.monthLabelEn} | Total Activity Recorded" else "महिना: ${summary.monthLabelNp} (${summary.monthYear})",
            reportBadge = "MONTHLY AUDIT"
        )

        // Executive KPI Cards Bar
        y = drawKpiCardsRow(
            canvas = canvas,
            y = y,
            income = summary.totalIncome,
            expense = summary.totalExpense,
            net = summary.netCashFlow,
            inventory = summary.currentInventoryValue
        )

        y += 14f

        // Revenue Breakdown Table
        val monthTxs = transactions.filter { it.monthYear == summary.monthYear || it.date.startsWith(summary.monthYear) }
        val incomeCategories = monthTxs.filter { it.type == TransactionType.INCOME }
            .groupBy { it.category }
            .mapValues { it.value.sumOf { tx -> tx.amount } }
            .toList()
            .sortedByDescending { it.second }

        val expenseCategories = monthTxs.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { it.value.sumOf { tx -> tx.amount } }
            .toList()
            .sortedByDescending { it.second }

        val textPaint = Paint().apply {
            textSize = 9f
            isAntiAlias = true
            color = Color.rgb(30, 30, 30)
        }

        // Two Column Section: Income Categories vs Expense Categories
        y = drawSectionHeader(
            canvas = canvas,
            y = y,
            sectionNum = "A",
            title = if (isEnglish) "FARM REVENUE BY SOURCE (आम्दानी स्रोत)" else "क. कृषि उत्पादन आम्दानी स्रोत (REVENUE)",
            totalAmount = summary.totalIncome,
            accentColor = Color.rgb(46, 125, 50)
        )

        if (incomeCategories.isEmpty()) {
            y = drawTableRow(canvas, y, "  • Regular Farm Sales / Customer Payments", formatCurrency(summary.totalIncome), textPaint)
        } else {
            for ((cat, amount) in incomeCategories.take(5)) {
                val share = if (summary.totalIncome > 0) (amount / summary.totalIncome) * 100 else 0.0
                val label = "  • ${cat.labelNp} (${cat.labelEn}) [${String.format(Locale.US, "%.1f", share)}%]"
                y = drawTableRow(canvas, y, label, formatCurrency(amount), textPaint)
            }
        }
        y = drawGrandTotalRow(canvas, y, "TOTAL OPERATING REVENUE (कुल आम्दानी)", formatCurrency(summary.totalIncome), Color.rgb(46, 125, 50))

        y += 12f

        // Operating Expenses Section
        y = drawSectionHeader(
            canvas = canvas,
            y = y,
            sectionNum = "B",
            title = if (isEnglish) "OPERATING EXPENSES (सञ्चालन खर्च)" else "ख. फार्म सञ्चालन खर्चको विवरण (EXPENSES)",
            totalAmount = summary.totalExpense,
            accentColor = Color.rgb(198, 40, 40)
        )

        if (expenseCategories.isEmpty()) {
            y = drawTableRow(canvas, y, "  • Regular Feed, Veterinary & Wages", formatCurrency(summary.totalExpense), textPaint)
        } else {
            for ((cat, amount) in expenseCategories.take(5)) {
                val share = if (summary.totalExpense > 0) (amount / summary.totalExpense) * 100 else 0.0
                val label = "  • ${cat.labelNp} (${cat.labelEn}) [${String.format(Locale.US, "%.1f", share)}%]"
                y = drawTableRow(canvas, y, label, formatCurrency(amount), textPaint)
            }
        }
        y = drawGrandTotalRow(canvas, y, "TOTAL OPERATING EXPENSES (कुल खर्च)", formatCurrency(summary.totalExpense), Color.rgb(198, 40, 40))

        y += 12f

        // Net Operating Income Banner
        val netColor = if (summary.netCashFlow >= 0) Color.rgb(27, 94, 32) else Color.rgb(198, 40, 40)
        y = drawGrandTotalRow(
            canvas = canvas,
            y = y,
            label = "NET MONTHLY OPERATING PROFIT / SURPLUS (खुद मासिक नाफा/बचत)",
            value = formatCurrency(summary.netCashFlow),
            bgColor = netColor
        )

        y += 14f

        // Daily Trend Breakdown Table
        y = drawSubhead(canvas, y, if (isEnglish) "Daily Cashflow & Inventory Valuation Trend" else "दैनिक आय-व्यय तथा मौज्दात स्टकको प्रगति विवरण")
        y = drawDailyTrendTableHeader(canvas, y)

        for (pt in summary.dailyTrends.take(8)) {
            y = drawDailyTrendTableRow(canvas, y, pt)
        }

        drawSignOffBlock(canvas, PAGE_HEIGHT - 90f, summary.monthYear)

        document.finishPage(page)

        val timeTag = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val outFile = File(getReportsDir(context), "FinancialSummary_${summary.monthYear}_$timeTag.pdf")
        val outStream = FileOutputStream(outFile)
        document.writeTo(outStream)
        outStream.flush()
        outStream.close()
        document.close()

        return outFile
    }

    // =========================================================================
    // REPORT 3: COMPLETE EXECUTIVE AUDIT PACK (2 PAGES)
    // =========================================================================

    fun generateCompleteAuditReportPdf(
        context: Context,
        balanceSheet: BalanceSheetSummary,
        ledger: FarmBalanceSheetLedger,
        summary: MonthlyDashboardSummary,
        transactions: List<FarmTransaction>,
        isEnglish: Boolean = false
    ): File {
        val document = PdfDocument()

        // Page 1: Balance Sheet
        val page1Info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page1 = document.startPage(page1Info)
        val canvas1 = page1.canvas

        var y1 = drawDocumentHeader(
            canvas = canvas1,
            title = if (isEnglish) "COMPREHENSIVE FINANCIAL AUDIT REPORT" else "समग्र वित्तीय प्रतिवेदन तथा कम्पनी वासलात (MASTER AUDIT)",
            subTitle = "Page 1: Statement of Financial Position (वासलात) | As of: ${balanceSheet.asOfDate}",
            reportBadge = "PAGE 1 OF 2"
        )

        // Draw full balance sheet on page 1
        val textPaint = Paint().apply {
            textSize = 9f
            isAntiAlias = true
            color = Color.rgb(30, 30, 30)
        }
        val boldPaint = Paint().apply {
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            color = Color.rgb(20, 20, 20)
        }

        y1 = drawSectionHeader(canvas1, y1, "1", "१. कुल सम्पत्ति (ASSETS)", balanceSheet.totalAssets, Color.rgb(27, 94, 32))
        y1 = drawSubhead(canvas1, y1, "Current Assets (चालु सम्पत्ति)")
        y1 = drawTableRow(canvas1, y1, "  • Cash in Hand (नगद मौज्दात)", formatCurrency(balanceSheet.cashInHand), textPaint)
        y1 = drawTableRow(canvas1, y1, "  • Muktinath Bikas Bank (A/C: ${FarmOfficialInfo.BANK_ACCOUNT_NO})", formatCurrency(balanceSheet.bankBalanceMuktinath), textPaint)
        y1 = drawTableRow(canvas1, y1, "  • eSewa Wallet (ID: ${FarmOfficialInfo.ESEWA_ID})", formatCurrency(balanceSheet.esewaBalance), textPaint)
        y1 = drawTableRow(canvas1, y1, "  • Accounts Receivable (उधारो असुल हुन बाँकी)", formatCurrency(balanceSheet.accountsReceivable), textPaint)
        y1 = drawTableRow(canvas1, y1, "  • Inventory Stock Valuation (उत्पादन स्टक)", formatCurrency(balanceSheet.inventoryStockValue), textPaint)
        y1 = drawSubtotalRow(canvas1, y1, "Total Current Assets", formatCurrency(balanceSheet.totalCurrentAssets))

        y1 += 4f
        y1 = drawSubhead(canvas1, y1, "Fixed & Biological Assets (स्थिर तथा जैविक सम्पत्ति)")
        y1 = drawTableRow(canvas1, y1, "  • Dairy Equipment & Machinery", formatCurrency(balanceSheet.fixedAssetsEquipment), textPaint)
        y1 = drawTableRow(canvas1, y1, "  • Land, Polyhouse & Shed Structure", formatCurrency(balanceSheet.fixedAssetsLandShed), textPaint)
        y1 = drawTableRow(canvas1, y1, "  • Livestock Valuation (Cows, Goats, Poultry)", formatCurrency(balanceSheet.livestockValuation), textPaint)
        y1 = drawSubtotalRow(canvas1, y1, "Total Fixed Assets", formatCurrency(balanceSheet.totalFixedAssets))
        y1 = drawGrandTotalRow(canvas1, y1, "TOTAL ASSETS (कुल सम्पत्ति)", formatCurrency(balanceSheet.totalAssets), Color.rgb(27, 94, 32))

        y1 += 12f
        y1 = drawSectionHeader(canvas1, y1, "2", "२. कुल दायित्व (LIABILITIES)", balanceSheet.totalLiabilities, Color.rgb(198, 40, 40))
        y1 = drawTableRow(canvas1, y1, "  • Accounts Payable (साहु भुक्तानी बाँकी)", formatCurrency(balanceSheet.accountsPayable), textPaint)
        y1 = drawTableRow(canvas1, y1, "  • Accrued Salaries (कर्मचारी तलब बाँकी)", formatCurrency(balanceSheet.accruedSalaries), textPaint)
        y1 = drawTableRow(canvas1, y1, "  • Agricultural Bank/Cooperative Loans", formatCurrency(balanceSheet.shortTermLoans), textPaint)
        y1 = drawGrandTotalRow(canvas1, y1, "TOTAL LIABILITIES (कुल दायित्व)", formatCurrency(balanceSheet.totalLiabilities), Color.rgb(198, 40, 40))

        y1 += 12f
        y1 = drawSectionHeader(canvas1, y1, "3", "३. शेयरधनी पुँजी तथा खुद नाफा (EQUITY)", balanceSheet.totalEquity, Color.rgb(13, 71, 161))
        y1 = drawTableRow(canvas1, y1, "  • Owner Paid-Up Capital", formatCurrency(balanceSheet.ownerCapital), textPaint)
        y1 = drawTableRow(canvas1, y1, "  • Retained Net Profit", formatCurrency(balanceSheet.netFarmProfit), boldPaint)
        y1 = drawGrandTotalRow(canvas1, y1, "TOTAL OWNER'S EQUITY (कुल पुँजी)", formatCurrency(balanceSheet.totalEquity), Color.rgb(13, 71, 161))

        y1 += 6f
        y1 = drawGrandTotalRow(canvas1, y1, "TOTAL LIABILITIES & EQUITY", formatCurrency(balanceSheet.totalLiabilities + balanceSheet.totalEquity), Color.rgb(30, 40, 50))

        drawSignOffBlock(canvas1, PAGE_HEIGHT - 90f, balanceSheet.asOfDate)
        document.finishPage(page1)

        // Page 2: Monthly Financial Summary & Daily Breakdown
        val page2Info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 2).create()
        val page2 = document.startPage(page2Info)
        val canvas2 = page2.canvas

        var y2 = drawDocumentHeader(
            canvas = canvas2,
            title = if (isEnglish) "MONTHLY INCOME, EXPENSE & INVENTORY AUDIT" else "मासिक आय-व्यय तथा मौज्दात स्टक अडिट प्रतिवेदन",
            subTitle = "Page 2: Performance Breakdown for ${summary.monthLabelNp} (${summary.monthYear})",
            reportBadge = "PAGE 2 OF 2"
        )

        y2 = drawKpiCardsRow(
            canvas = canvas2,
            y = y2,
            income = summary.totalIncome,
            expense = summary.totalExpense,
            net = summary.netCashFlow,
            inventory = summary.currentInventoryValue
        )

        y2 += 16f
        y2 = drawSubhead(canvas2, y2, "दैनिक आय-व्यय तथा मौज्दात स्टकको पूर्ण विवरण (Daily Breakdown Table)")
        y2 = drawDailyTrendTableHeader(canvas2, y2)

        for (pt in summary.dailyTrends) {
            y2 = drawDailyTrendTableRow(canvas2, y2, pt)
        }

        y2 += 16f
        // Key Highlights Box
        val boxPaint = Paint().apply {
            color = Color.rgb(245, 245, 245)
            style = Paint.Style.FILL
        }
        val boxBorder = Paint().apply {
            color = Color.rgb(200, 200, 200)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val noteRect = RectF(MARGIN, y2, PAGE_WIDTH - MARGIN, y2 + 65f)
        canvas2.drawRoundRect(noteRect, 6f, 6f, boxPaint)
        canvas2.drawRoundRect(noteRect, 6f, 6f, boxBorder)

        val noteTitlePaint = Paint().apply {
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.rgb(27, 94, 32)
            isAntiAlias = true
        }
        canvas2.drawText("EXECUTIVE AUDIT SUMMARY NOTES / अडिट निष्कर्ष:", MARGIN + 10f, y2 + 18f, noteTitlePaint)

        val noteBodyPaint = Paint().apply {
            textSize = 8.5f
            color = Color.rgb(60, 60, 60)
            isAntiAlias = true
        }
        canvas2.drawText("• Top Revenue Driver: ${summary.topIncomeCategory} | Highest Sales Day: ${summary.highestIncomeDay}", MARGIN + 10f, y2 + 34f, noteBodyPaint)
        canvas2.drawText("• Major Cost Driver: ${summary.topExpenseCategory} | Current Active Inventory Units: ${summary.totalStockUnits.toInt()} units", MARGIN + 10f, y2 + 48f, noteBodyPaint)

        drawSignOffBlock(canvas2, PAGE_HEIGHT - 90f, summary.monthYear)
        document.finishPage(page2)

        val timeTag = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val outFile = File(getReportsDir(context), "CompleteAuditReport_${summary.monthYear}_$timeTag.pdf")
        val outStream = FileOutputStream(outFile)
        document.writeTo(outStream)
        outStream.flush()
        outStream.close()
        document.close()

        return outFile
    }

    // =========================================================================
    // DRAWING HELPER FUNCTIONS
    // =========================================================================

    private fun drawDocumentHeader(
        canvas: Canvas,
        title: String,
        subTitle: String,
        reportBadge: String
    ): Float {
        // Top Green Bar
        val topBarPaint = Paint().apply {
            color = Color.rgb(27, 94, 32) // AgroGreenPrimary
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 10f, topBarPaint)

        var y = MARGIN + 15f

        // Farm Name Heading
        val farmTitlePaint = Paint().apply {
            color = Color.rgb(27, 94, 32)
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(FarmOfficialInfo.FARM_NAME_NP, MARGIN, y, farmTitlePaint)
        y += 14f

        val farmEnPaint = Paint().apply {
            color = Color.rgb(80, 80, 80)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("${FarmOfficialInfo.FARM_NAME_EN} | ${FarmOfficialInfo.LOCATION_NP}", MARGIN, y, farmEnPaint)

        // Right side badge
        val badgeBgPaint = Paint().apply {
            color = Color.rgb(255, 248, 225)
            style = Paint.Style.FILL
        }
        val badgeBorderPaint = Paint().apply {
            color = Color.rgb(255, 179, 0)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val badgeTextPaint = Paint().apply {
            color = Color.rgb(191, 54, 12)
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val badgeRect = RectF(PAGE_WIDTH - MARGIN - 110f, MARGIN - 2f, PAGE_WIDTH - MARGIN, MARGIN + 18f)
        canvas.drawRoundRect(badgeRect, 3f, 3f, badgeBgPaint)
        canvas.drawRoundRect(badgeRect, 3f, 3f, badgeBorderPaint)
        canvas.drawText(reportBadge, PAGE_WIDTH - MARGIN - 100f, MARGIN + 11f, badgeTextPaint)

        y += 12f
        val regPaint = Paint().apply {
            color = Color.rgb(110, 110, 110)
            textSize = 8f
            isAntiAlias = true
        }
        canvas.drawText("PAN/VAT: ${FarmOfficialInfo.PAN_VAT_NO} | Reg No: ${FarmOfficialInfo.REGISTRATION_NO} | Email: ${FarmOfficialInfo.EMAIL}", MARGIN, y, regPaint)

        y += 8f
        // Divider line
        val linePaint = Paint().apply {
            color = Color.rgb(210, 210, 210)
            strokeWidth = 1f
        }
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)

        y += 16f
        // Document Report Title
        val docTitlePaint = Paint().apply {
            color = Color.rgb(20, 40, 20)
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(title, MARGIN, y, docTitlePaint)

        y += 12f
        val docSubPaint = Paint().apply {
            color = Color.rgb(90, 90, 90)
            textSize = 8.5f
            isAntiAlias = true
        }
        canvas.drawText(subTitle, MARGIN, y, docSubPaint)

        y += 12f
        return y
    }

    private fun drawSectionHeader(
        canvas: Canvas,
        y: Float,
        sectionNum: String,
        title: String,
        totalAmount: Double,
        accentColor: Int
    ): Float {
        val bgPaint = Paint().apply {
            color = Color.rgb(245, 247, 245)
            style = Paint.Style.FILL
        }
        val rect = RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 20f)
        canvas.drawRoundRect(rect, 3f, 3f, bgPaint)

        // Left accent strip
        val stripPaint = Paint().apply {
            color = accentColor
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(MARGIN, y, MARGIN + 4f, y + 20f), 2f, 2f, stripPaint)

        val titlePaint = Paint().apply {
            color = accentColor
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(title, MARGIN + 12f, y + 14f, titlePaint)

        val amountPaint = Paint().apply {
            color = accentColor
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText(formatCurrency(totalAmount), PAGE_WIDTH - MARGIN - 10f, y + 14f, amountPaint)

        return y + 26f
    }

    private fun drawSubhead(canvas: Canvas, y: Float, label: String): Float {
        val paint = Paint().apply {
            color = Color.rgb(70, 70, 70)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(label, MARGIN + 4f, y + 10f, paint)
        return y + 16f
    }

    private fun drawTableRow(
        canvas: Canvas,
        y: Float,
        label: String,
        amount: String,
        textPaint: Paint
    ): Float {
        canvas.drawText(label, MARGIN + 6f, y + 10f, textPaint)

        val rightPaint = Paint(textPaint).apply {
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(amount, PAGE_WIDTH - MARGIN - 10f, y + 10f, rightPaint)

        // Dotted light separator
        val linePaint = Paint().apply {
            color = Color.rgb(238, 238, 238)
            strokeWidth = 0.5f
        }
        canvas.drawLine(MARGIN + 6f, y + 14f, PAGE_WIDTH - MARGIN - 6f, y + 14f, linePaint)

        return y + 16f
    }

    private fun drawSubtotalRow(
        canvas: Canvas,
        y: Float,
        label: String,
        amount: String
    ): Float {
        val boldPaint = Paint().apply {
            color = Color.rgb(40, 40, 40)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(label, MARGIN + 10f, y + 10f, boldPaint)

        val rightPaint = Paint(boldPaint).apply {
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(amount, PAGE_WIDTH - MARGIN - 10f, y + 10f, rightPaint)

        val linePaint = Paint().apply {
            color = Color.rgb(200, 200, 200)
            strokeWidth = 0.8f
        }
        canvas.drawLine(MARGIN + 6f, y + 14f, PAGE_WIDTH - MARGIN - 6f, y + 14f, linePaint)

        return y + 18f
    }

    private fun drawGrandTotalRow(
        canvas: Canvas,
        y: Float,
        label: String,
        value: String,
        bgColor: Int
    ): Float {
        val bgPaint = Paint().apply {
            color = bgColor
            style = Paint.Style.FILL
        }
        val rect = RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 20f)
        canvas.drawRoundRect(rect, 4f, 4f, bgPaint)

        val labelPaint = Paint().apply {
            color = Color.WHITE
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(label, MARGIN + 10f, y + 14f, labelPaint)

        val valuePaint = Paint().apply {
            color = Color.WHITE
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText(value, PAGE_WIDTH - MARGIN - 10f, y + 14f, valuePaint)

        return y + 26f
    }

    private fun drawKpiCardsRow(
        canvas: Canvas,
        y: Float,
        income: Double,
        expense: Double,
        net: Double,
        inventory: Double
    ): Float {
        val cardWidth = (CONTENT_WIDTH - 18f) / 4f
        val cardHeight = 44f

        drawMiniKpiCard(canvas, MARGIN, y, cardWidth, cardHeight, "कुल आम्दानी (Revenue)", formatCurrency(income), Color.rgb(232, 245, 233), Color.rgb(27, 94, 32))
        drawMiniKpiCard(canvas, MARGIN + cardWidth + 6f, y, cardWidth, cardHeight, "कुल खर्च (Expenses)", formatCurrency(expense), Color.rgb(255, 235, 238), Color.rgb(198, 40, 40))
        drawMiniKpiCard(canvas, MARGIN + (cardWidth + 6f) * 2, y, cardWidth, cardHeight, "खुद नाफा (Net Profit)", formatCurrency(net), if (net >= 0) Color.rgb(232, 245, 233) else Color.rgb(255, 235, 238), if (net >= 0) Color.rgb(27, 94, 32) else Color.rgb(198, 40, 40))
        drawMiniKpiCard(canvas, MARGIN + (cardWidth + 6f) * 3, y, cardWidth, cardHeight, "स्टक मूल्य (Inventory)", formatCurrency(inventory), Color.rgb(255, 248, 225), Color.rgb(183, 110, 0))

        return y + cardHeight + 8f
    }

    private fun drawMiniKpiCard(
        canvas: Canvas,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        title: String,
        value: String,
        bgColor: Int,
        textColor: Int
    ) {
        val bgPaint = Paint().apply {
            color = bgColor
            style = Paint.Style.FILL
        }
        val borderPaint = Paint().apply {
            color = textColor
            style = Paint.Style.STROKE
            strokeWidth = 0.5f
        }
        val rect = RectF(x, y, x + w, y + h)
        canvas.drawRoundRect(rect, 4f, 4f, bgPaint)
        canvas.drawRoundRect(rect, 4f, 4f, borderPaint)

        val titlePaint = Paint().apply {
            color = Color.rgb(70, 70, 70)
            textSize = 7f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        canvas.drawText(title, x + 6f, y + 14f, titlePaint)

        val valPaint = Paint().apply {
            color = textColor
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(value, x + 6f, y + 32f, valPaint)
    }

    private fun drawDailyTrendTableHeader(canvas: Canvas, y: Float): Float {
        val bgPaint = Paint().apply {
            color = Color.rgb(27, 94, 32)
            style = Paint.Style.FILL
        }
        val rect = RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 18f)
        canvas.drawRoundRect(rect, 3f, 3f, bgPaint)

        val thPaint = Paint().apply {
            color = Color.WHITE
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        canvas.drawText("Date (मिति)", MARGIN + 8f, y + 12f, thPaint)
        canvas.drawText("आम्दानी (Income)", MARGIN + 110f, y + 12f, thPaint)
        canvas.drawText("खर्च (Expense)", MARGIN + 210f, y + 12f, thPaint)
        canvas.drawText("खुद बचत (Net Cashflow)", MARGIN + 310f, y + 12f, thPaint)
        canvas.drawText("स्टक मूल्य (Inventory Val.)", MARGIN + 415f, y + 12f, thPaint)

        return y + 22f
    }

    private fun drawDailyTrendTableRow(canvas: Canvas, y: Float, pt: DailyTrendPoint): Float {
        val rowPaint = Paint().apply {
            textSize = 8f
            color = Color.rgb(40, 40, 40)
            isAntiAlias = true
        }

        canvas.drawText(pt.dateStr, MARGIN + 8f, y + 10f, rowPaint)
        canvas.drawText(formatCurrency(pt.income), MARGIN + 110f, y + 10f, rowPaint)
        canvas.drawText(formatCurrency(pt.expense), MARGIN + 210f, y + 10f, rowPaint)

        val netPaint = Paint(rowPaint).apply {
            color = if (pt.net >= 0) Color.rgb(27, 94, 32) else Color.rgb(198, 40, 40)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(formatCurrency(pt.net), MARGIN + 310f, y + 10f, netPaint)
        canvas.drawText(formatCurrency(pt.inventoryValue), MARGIN + 415f, y + 10f, rowPaint)

        val linePaint = Paint().apply {
            color = Color.rgb(230, 230, 230)
            strokeWidth = 0.5f
        }
        canvas.drawLine(MARGIN + 4f, y + 14f, PAGE_WIDTH - MARGIN - 4f, y + 14f, linePaint)

        return y + 16f
    }

    private fun drawSignOffBlock(canvas: Canvas, y: Float, dateStr: String) {
        val linePaint = Paint().apply {
            color = Color.rgb(160, 160, 160)
            strokeWidth = 0.8f
        }

        val colW = (CONTENT_WIDTH - 40f) / 3f

        // 3 Signature Lines
        val x1 = MARGIN
        val x2 = MARGIN + colW + 20f
        val x3 = MARGIN + (colW + 20f) * 2

        canvas.drawLine(x1, y + 25f, x1 + colW, y + 25f, linePaint)
        canvas.drawLine(x2, y + 25f, x2 + colW, y + 25f, linePaint)
        canvas.drawLine(x3, y + 25f, x3 + colW, y + 25f, linePaint)

        val labelPaint = Paint().apply {
            color = Color.rgb(60, 60, 60)
            textSize = 7.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("तयार गर्ने (Prepared By)", x1 + 10f, y + 36f, labelPaint)
        canvas.drawText("लेखा अधिकृत (Accountant)", x1 + 10f, y + 46f, Paint(labelPaint).apply { typeface = Typeface.DEFAULT; color = Color.GRAY })

        canvas.drawText("प्रमाणीकरण (Verified By)", x2 + 10f, y + 36f, labelPaint)
        canvas.drawText("आन्तरिक लेखापरीक्षक (Auditor)", x2 + 10f, y + 46f, Paint(labelPaint).apply { typeface = Typeface.DEFAULT; color = Color.GRAY })

        canvas.drawText("सञ्चालक (Managing Director)", x3 + 10f, y + 36f, labelPaint)
        canvas.drawText("आधिकारिक छाप र हस्ताक्षर (Seal)", x3 + 10f, y + 46f, Paint(labelPaint).apply { typeface = Typeface.DEFAULT; color = Color.GRAY })

        // Footer Legal & Verification Code
        val footerPaint = Paint().apply {
            color = Color.rgb(130, 130, 130)
            textSize = 7f
            isAntiAlias = true
        }
        val genTimestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val verifyCode = "DOC-UAF-${dateStr.replace("-", "")}-${System.currentTimeMillis().toString().takeLast(6)}"
        canvas.drawText("Generated on: $genTimestamp | Doc ID: $verifyCode | United Agro Farm Pvt. Ltd.", MARGIN, y + 68f, footerPaint)

        val confPaint = Paint(footerPaint).apply {
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("CONFIDENTIAL & OFFICIAL RECORD", PAGE_WIDTH - MARGIN, y + 68f, confPaint)
    }
}
