package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.DocumentCategory
import com.example.model.DocumentStatus
import com.example.model.FarmOfficialInfo
import com.example.model.OfficialDocument
import com.example.ui.theme.AgroGoldTertiary
import com.example.ui.theme.AgroGreenPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(
    documents: List<OfficialDocument>,
    searchQuery: String,
    selectedCategory: DocumentCategory?,
    selectedStatus: DocumentStatus?,
    isEnglish: Boolean = false,
    onSearchChange: (String) -> Unit,
    onCategoryFilterChange: (DocumentCategory?) -> Unit,
    onStatusFilterChange: (DocumentStatus?) -> Unit,
    onAddDocument: (
        titleNp: String,
        titleEn: String,
        documentNumber: String,
        category: DocumentCategory,
        issuingAuthorityNp: String,
        issuingAuthorityEn: String,
        issueDate: String,
        expiryDate: String,
        status: DocumentStatus,
        summaryPointsNp: List<String>,
        summaryPointsEn: List<String>,
        contactOfficer: String,
        notes: String
    ) -> Unit,
    onUpdateDocument: (OfficialDocument) -> Unit,
    onDeleteDocument: (String) -> Unit,
    onRenewDocument: (docId: String, newExpiry: String, notes: String) -> Unit,
    onOpenUploadMenu: (() -> Unit)? = null,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    var showAddDialog by remember { mutableStateOf(false) }
    var viewingCertificate by remember { mutableStateOf<OfficialDocument?>(null) }
    var editingDocument by remember { mutableStateOf<OfficialDocument?>(null) }
    var deletingDocId by remember { mutableStateOf<String?>(null) }

    val totalDocs = documents.size
    val activeDocs = documents.count { it.status == DocumentStatus.ACTIVE }
    val permanentDocs = documents.count { it.status == DocumentStatus.PERMANENT }
    val renewalDueDocs = documents.count { it.status == DocumentStatus.RENEWAL_DUE }

    Scaffold(
        modifier = Modifier.testTag("documents_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isEnglish) "Official Documents & Permits" else "आधिकारिक कागजात तथा अनुमतिपत्र",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (isEnglish) "Lalitpur United Agro Farm Compliance Database" else "ललितपुर युनाइटेड एग्रो फार्म दर्ता तथा लाइसेन्स अभिलेख",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFC8E6C9),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_back_documents")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (onOpenUploadMenu != null) {
                        IconButton(
                            onClick = onOpenUploadMenu,
                            modifier = Modifier.testTag("btn_top_upload_attachment")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = "Upload Document or Image",
                                tint = Color.White
                            )
                        }
                    }

                    IconButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.testTag("btn_top_add_document")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Document",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AgroGreenPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = AgroGreenPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_document")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEnglish) "Add Document" else "कागजात दर्ता",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Summary KPI metrics
            item {
                DocumentSummaryHeader(
                    total = totalDocs,
                    active = activeDocs,
                    permanent = permanentDocs,
                    renewalDue = renewalDueDocs,
                    isEnglish = isEnglish
                )
            }

            // Search Bar & Filter Controls
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_document_search"),
                    placeholder = {
                        Text(if (isEnglish) "Search document name, reg no, authority..." else "कागजातको नाम, दर्ता/प्यान नं., जारी गर्ने निकाय खोज्नुहोस्...")
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = AgroGreenPrimary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Category Filter Chips Row
            item {
                Column {
                    Text(
                        text = if (isEnglish) "Filter by Category" else "क्षेत्रगत वर्गीकरण अनुसार हेर्नुहोस्",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { onCategoryFilterChange(null) },
                            label = { Text(if (isEnglish) "All (${documents.size})" else "सबै (${documents.size})") },
                            modifier = Modifier.testTag("chip_category_all"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AgroGreenPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                        DocumentCategory.values().forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { onCategoryFilterChange(if (selectedCategory == cat) null else cat) },
                                label = { Text(if (isEnglish) cat.labelEn else cat.labelNp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = getCategoryIcon(cat),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AgroGreenPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Status Filter Chips Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedStatus == null,
                        onClick = { onStatusFilterChange(null) },
                        label = { Text(if (isEnglish) "All Status" else "सबै स्थिति") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    DocumentStatus.values().forEach { st ->
                        val count = documents.count { it.status == st }
                        FilterChip(
                            selected = selectedStatus == st,
                            onClick = { onStatusFilterChange(if (selectedStatus == st) null else st) },
                            label = { Text("${if (isEnglish) st.labelEn else st.labelNp} ($count)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(st.colorHex),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Results count badge
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEnglish) "${documents.size} Official Documents Found" else "कुल ${documents.size} आधिकारिक कागजातहरू उपलब्ध",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Empty state
            if (documents.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (isEnglish) "No matching official documents found" else "कुनै मिल्दो कागजात वा इजाजत फेला परेन",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isEnglish) "Try clearing search or filters, or add a new document." else "खोज वा फिल्टर परिवर्तन गर्नुहोस् वा नयाँ कागजात दर्ता गर्नुहोस्।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // List of Documents
            items(documents, key = { it.id }) { doc ->
                DocumentCard(
                    document = doc,
                    isEnglish = isEnglish,
                    onViewCertificate = { viewingCertificate = doc },
                    onEdit = { editingDocument = doc },
                    onDelete = { deletingDocId = doc.id },
                    onCopyNumber = { num ->
                        clipboard.setText(AnnotatedString(num))
                        Toast.makeText(context, "$num कपी गरियो!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Bottom space for FAB
            item {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }

    // Add Document Dialog
    if (showAddDialog) {
        AddDocumentDialog(
            isEnglish = isEnglish,
            onDismiss = { showAddDialog = false },
            onConfirm = { tNp, tEn, docNum, cat, authNp, authEn, issDate, expDate, st, ptsNp, ptsEn, officer, nts ->
                onAddDocument(tNp, tEn, docNum, cat, authNp, authEn, issDate, expDate, st, ptsNp, ptsEn, officer, nts)
                showAddDialog = false
            }
        )
    }

    // Edit Document Dialog
    editingDocument?.let { doc ->
        EditDocumentDialog(
            document = doc,
            isEnglish = isEnglish,
            onDismiss = { editingDocument = null },
            onConfirm = { updated ->
                onUpdateDocument(updated)
                editingDocument = null
            },
            onRenew = { expDate, notes ->
                onRenewDocument(doc.id, expDate, notes)
                editingDocument = null
            }
        )
    }

    // Delete Confirmation Dialog
    deletingDocId?.let { docId ->
        AlertDialog(
            onDismissRequest = { deletingDocId = null },
            title = {
                Text(if (isEnglish) "Delete Official Record?" else "कागजात रेकर्ड मेटाउने?")
            },
            text = {
                Text(if (isEnglish) "Are you sure you want to remove this official document record from the database? This action cannot be undone." else "के तपाईं यो आधिकारिक कागजात रेकर्ड डाटाबेसबाट हटाउन निश्चित हुनुहुन्छ?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteDocument(docId)
                        deletingDocId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isEnglish) "Delete" else "मेटाउनुहोस्")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingDocId = null }) {
                    Text(if (isEnglish) "Cancel" else "रद्द गर्नुहोस्")
                }
            }
        )
    }

    // Full Digital Certificate Viewer Dialog
    viewingCertificate?.let { doc ->
        OfficialCertificateViewerDialog(
            document = doc,
            isEnglish = isEnglish,
            onDismiss = { viewingCertificate = null }
        )
    }
}

@Composable
private fun DocumentSummaryHeader(
    total: Int,
    active: Int,
    permanent: Int,
    renewalDue: Int,
    isEnglish: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("documents_summary_header"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = AgroGreenPrimary.copy(alpha = 0.12f),
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = AgroGreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = if (isEnglish) "Legal Compliance & Licenses" else "कानुनी मान्यता तथा अनुमति डाटाबेस",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${FarmOfficialInfo.FARM_NAME_NP} • PAN: ${FarmOfficialInfo.PAN_VAT_NO}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KpiPill(
                    title = if (isEnglish) "Total" else "जम्मा",
                    value = "$total",
                    color = AgroGreenPrimary,
                    modifier = Modifier.weight(1f)
                )
                KpiPill(
                    title = if (isEnglish) "Active" else "सक्रिय",
                    value = "$active",
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )
                KpiPill(
                    title = if (isEnglish) "Permanent" else "आजीवन",
                    value = "$permanent",
                    color = Color(0xFF1565C0),
                    modifier = Modifier.weight(1f)
                )
                KpiPill(
                    title = if (isEnglish) "Due" else "नवीकरण",
                    value = "$renewalDue",
                    color = if (renewalDue > 0) Color(0xFFE65100) else Color.Gray,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun KpiPill(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DocumentCard(
    document: OfficialDocument,
    isEnglish: Boolean,
    onViewCertificate: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCopyNumber: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("doc_card_${document.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Category Badge & Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AgroGreenPrimary.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(document.category),
                            contentDescription = null,
                            tint = AgroGreenPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isEnglish) document.category.labelEn else document.category.labelNp,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AgroGreenPrimary
                        )
                    }
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(document.status.colorHex).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(document.status.colorHex).copy(alpha = 0.4f))
                ) {
                    Text(
                        text = if (isEnglish) document.status.labelEn else document.status.labelNp,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(document.status.colorHex),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Document Title
            Text(
                text = if (isEnglish && document.titleEn.isNotBlank()) document.titleEn else document.titleNp,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (isEnglish && document.titleNp.isNotBlank()) {
                Text(
                    text = document.titleNp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Document Number Row with Copy Button
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (isEnglish) "Document / Reg / License No." else "दर्ता / इजाजत / प्यान नं.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = document.documentNumber,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = { onCopyNumber(document.documentNumber) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Number",
                            modifier = Modifier.size(16.dp),
                            tint = AgroGreenPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Issuing Authority
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Apartment,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (isEnglish && document.issuingAuthorityEn.isNotBlank()) document.issuingAuthorityEn else document.issuingAuthorityNp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Dates Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${if (isEnglish) "Issued: " else "जारी: "}${document.issueDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "${if (isEnglish) "Valid: " else "म्याद: "}${document.expiryDate}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (document.status == DocumentStatus.RENEWAL_DUE) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Expandable Key Summary Points
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    val points = if (isEnglish && document.summaryPointsEn.isNotEmpty()) document.summaryPointsEn else document.summaryPointsNp
                    if (points.isNotEmpty()) {
                        Text(
                            text = if (isEnglish) "Key Terms & Legal Scope:" else "मुख्य सर्त तथा कानुनी अधिकार:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AgroGreenPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        points.forEach { point ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("•", color = AgroGreenPrimary, fontWeight = FontWeight.Bold)
                                Text(
                                    text = point,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    if (document.contactOfficer.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${if (isEnglish) "Signatory / Officer: " else "प्रमाणीकरण अधिकृत: "}${document.contactOfficer}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (document.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${if (isEnglish) "Notes: " else "कैफियत: "}${document.notes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // View Certificate Button
                Button(
                    onClick = onViewCertificate,
                    colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_view_cert_${document.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isEnglish) "View Certificate" else "प्रमाणपत्र हेर्नुहोस्",
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                // Edit / Renew Button
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_edit_doc_${document.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Toggle Details Button
                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Show Less" else "Show Details"
                    )
                }

                // Delete Button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddDocumentDialog(
    isEnglish: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (
        titleNp: String,
        titleEn: String,
        documentNumber: String,
        category: DocumentCategory,
        issuingAuthorityNp: String,
        issuingAuthorityEn: String,
        issueDate: String,
        expiryDate: String,
        status: DocumentStatus,
        summaryPointsNp: List<String>,
        summaryPointsEn: List<String>,
        contactOfficer: String,
        notes: String
    ) -> Unit
) {
    var titleNp by remember { mutableStateOf("") }
    var titleEn by remember { mutableStateOf("") }
    var docNumber by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(DocumentCategory.REGISTRATION) }
    var issuingAuthNp by remember { mutableStateOf("") }
    var issuingAuthEn by remember { mutableStateOf("") }
    var issueDate by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var expiryDate by remember { mutableStateOf("स्थायी / आजीवन") }
    var selectedStatus by remember { mutableStateOf(DocumentStatus.ACTIVE) }
    var pointsText by remember { mutableStateOf("") }
    var contactOfficer by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEnglish) "Add Official Document / Permit" else "नयाँ आधिकारिक कागजात / इजाजत दर्ता",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AgroGreenPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Clear, contentDescription = "Close")
                    }
                }

                HorizontalDivider()

                // Title (Nepali)
                OutlinedTextField(
                    value = titleNp,
                    onValueChange = { titleNp = it },
                    label = { Text(if (isEnglish) "Document Title (Nepali) *" else "कागजातको नाम (नेपालीमा) *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Title (English)
                OutlinedTextField(
                    value = titleEn,
                    onValueChange = { titleEn = it },
                    label = { Text(if (isEnglish) "Document Title (English)" else "कागजातको नाम (अंग्रेजीमा)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Document Number
                OutlinedTextField(
                    value = docNumber,
                    onValueChange = { docNumber = it },
                    label = { Text(if (isEnglish) "Reg / License / Permit No. *" else "दर्ता / इजाजत / प्यान / सम्झौता नं. *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = if (isEnglish) selectedCategory.labelEn else selectedCategory.labelNp,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isEnglish) "Category" else "क्षेत्रगत वर्ग") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        DocumentCategory.values().forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(if (isEnglish) cat.labelEn else cat.labelNp) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Issuing Authority (Nepali)
                OutlinedTextField(
                    value = issuingAuthNp,
                    onValueChange = { issuingAuthNp = it },
                    label = { Text(if (isEnglish) "Issuing Authority (Nepali) *" else "जारी गर्ने निकाय (मन्त्रालय/विभाग/कार्यालय) *") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Dates Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = issueDate,
                        onValueChange = { issueDate = it },
                        label = { Text(if (isEnglish) "Issue Date" else "जारी मिति") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = { expiryDate = it },
                        label = { Text(if (isEnglish) "Expiry Date" else "म्याद सकिने मिति") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Status Dropdown
                ExposedDropdownMenuBox(
                    expanded = statusDropdownExpanded,
                    onExpandedChange = { statusDropdownExpanded = !statusDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = if (isEnglish) selectedStatus.labelEn else selectedStatus.labelNp,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isEnglish) "Status" else "स्थिति") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = statusDropdownExpanded,
                        onDismissRequest = { statusDropdownExpanded = false }
                    ) {
                        DocumentStatus.values().forEach { st ->
                            DropdownMenuItem(
                                text = { Text(if (isEnglish) st.labelEn else st.labelNp) },
                                onClick = {
                                    selectedStatus = st
                                    statusDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Officer
                OutlinedTextField(
                    value = contactOfficer,
                    onValueChange = { contactOfficer = it },
                    label = { Text(if (isEnglish) "Authorized Official / Contact" else "प्रमाणीकरण गर्ने अधिकृत") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Key terms / points (one per line)
                OutlinedTextField(
                    value = pointsText,
                    onValueChange = { pointsText = it },
                    label = { Text(if (isEnglish) "Key Terms / Points (one per line)" else "मुख्य सर्त तथा बुँदाहरू (प्रति हरफ एक)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (isEnglish) "Additional Notes" else "अतिरिक्त कैफियत") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(if (isEnglish) "Cancel" else "रद्द गर्नुहोस्")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (titleNp.isNotBlank() && docNumber.isNotBlank() && issuingAuthNp.isNotBlank()) {
                                val pts = pointsText.split("\n").map { it.trim() }.filter { it.isNotBlank() }
                                onConfirm(
                                    titleNp,
                                    titleEn,
                                    docNumber,
                                    selectedCategory,
                                    issuingAuthNp,
                                    issuingAuthEn,
                                    issueDate,
                                    expiryDate,
                                    selectedStatus,
                                    pts,
                                    pts,
                                    contactOfficer,
                                    notes
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary),
                        enabled = titleNp.isNotBlank() && docNumber.isNotBlank() && issuingAuthNp.isNotBlank()
                    ) {
                        Text(if (isEnglish) "Save Record" else "रेकर्ड सुरक्षित गर्नुहोस्")
                    }
                }
            }
        }
    }
}

@Composable
private fun EditDocumentDialog(
    document: OfficialDocument,
    isEnglish: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (OfficialDocument) -> Unit,
    onRenew: (String, String) -> Unit
) {
    var docNumber by remember { mutableStateOf(document.documentNumber) }
    var expiryDate by remember { mutableStateOf(document.expiryDate) }
    var selectedStatus by remember { mutableStateOf(document.status) }
    var notes by remember { mutableStateOf(document.notes) }
    var contactOfficer by remember { mutableStateOf(document.contactOfficer) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEnglish) "Edit / Renew Document" else "कागजात सम्पादन तथा नवीकरण",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AgroGreenPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Clear, contentDescription = "Close")
                    }
                }

                Text(
                    text = document.titleNp,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                HorizontalDivider()

                OutlinedTextField(
                    value = docNumber,
                    onValueChange = { docNumber = it },
                    label = { Text(if (isEnglish) "Document Number" else "दर्ता/इजाजत नम्बर") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = expiryDate,
                    onValueChange = { expiryDate = it },
                    label = { Text(if (isEnglish) "Expiry / Renewal Date" else "म्याद सकिने / नवीकरण मिति") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick Status Buttons
                Text(
                    text = if (isEnglish) "Quick Status Update:" else "स्थिति अद्यावधिक:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DocumentStatus.values().forEach { st ->
                        FilterChip(
                            selected = selectedStatus == st,
                            onClick = { selectedStatus = st },
                            label = { Text(if (isEnglish) st.labelEn else st.labelNp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(st.colorHex),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = contactOfficer,
                    onValueChange = { contactOfficer = it },
                    label = { Text(if (isEnglish) "Signatory Official" else "प्रमाणीकरण गर्ने अधिकृत") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (isEnglish) "Notes / Remarks" else "कैफियत / टिप्पणी") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(if (isEnglish) "Cancel" else "रद्द गर्नुहोस्")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val updated = document.copy(
                                documentNumber = docNumber,
                                expiryDate = expiryDate,
                                status = selectedStatus,
                                contactOfficer = contactOfficer,
                                notes = notes,
                                updatedAt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                            )
                            onConfirm(updated)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary)
                    ) {
                        Text(if (isEnglish) "Update Record" else "अद्यावधिक गर्नुहोस्")
                    }
                }
            }
        }
    }
}

@Composable
private fun OfficialCertificateViewerDialog(
    document: OfficialDocument,
    isEnglish: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .clip(RoundedCornerShape(16.dp)),
            color = Color(0xFFFFFDF7) // Parchment paper tone
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Dismiss Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEnglish) "Official Digital Certificate" else "आधिकारिक डिजिटल प्रमाणपत्र",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF1B5E20),
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Clear, contentDescription = "Close", tint = Color.Black)
                    }
                }

                // Official Certificate Border Container
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = BorderStroke(2.dp, Color(0xFF2E7D32)),
                    shadowElevation = 3.dp
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Emblem & Government / Official Crest
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE8F5E9),
                            border = BorderStroke(1.5.dp, Color(0xFF2E7D32)),
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = Color(0xFF1B5E20),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = document.issuingAuthorityNp,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20),
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = document.issuingAuthorityEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF388E3C),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color(0xFF2E7D32), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Certificate Title Banner
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF2E7D32)
                        ) {
                            Text(
                                text = document.titleNp,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = document.titleEn,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2E7D32),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Registered Organization Details Box
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF1F8E9),
                            border = BorderStroke(1.dp, Color(0xFFC5E1A5))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                CertificateMetaRow(
                                    labelNp = "संस्थाको नाम:",
                                    labelEn = "Entity Name:",
                                    value = FarmOfficialInfo.FARM_NAME_NP,
                                    isEnglish = isEnglish
                                )
                                CertificateMetaRow(
                                    labelNp = "ठेगाना:",
                                    labelEn = "Location:",
                                    value = FarmOfficialInfo.LOCATION_NP,
                                    isEnglish = isEnglish
                                )
                                CertificateMetaRow(
                                    labelNp = "दर्ता / इजाजत नम्बर:",
                                    labelEn = "Document No:",
                                    value = document.documentNumber,
                                    isBold = true,
                                    isEnglish = isEnglish
                                )
                                CertificateMetaRow(
                                    labelNp = "क्षेत्रगत वर्ग:",
                                    labelEn = "Category:",
                                    value = if (isEnglish) document.category.labelEn else document.category.labelNp,
                                    isEnglish = isEnglish
                                )
                                CertificateMetaRow(
                                    labelNp = "जारी मिति:",
                                    labelEn = "Issue Date:",
                                    value = document.issueDate,
                                    isEnglish = isEnglish
                                )
                                CertificateMetaRow(
                                    labelNp = "मान्यता अवधि:",
                                    labelEn = "Validity / Expiry:",
                                    value = document.expiryDate,
                                    isBold = true,
                                    isEnglish = isEnglish
                                )
                                CertificateMetaRow(
                                    labelNp = "प्रमाणीकरण कोड:",
                                    labelEn = "Digital Ref Code:",
                                    value = document.digitalRefCode,
                                    isEnglish = isEnglish
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Summary Points Section
                        val points = if (isEnglish && document.summaryPointsEn.isNotEmpty()) document.summaryPointsEn else document.summaryPointsNp
                        if (points.isNotEmpty()) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = if (isEnglish) "Legal Scope & Authorization Provisions:" else "कानुनी अधिकार तथा प्रमाणीकरण सर्तहरू:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B5E20)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                points.forEach { pt ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 3.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("✓", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                        Text(
                                            text = pt,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Official Stamp & QR Code Bottom Section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // QR & Barcode seal
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    border = BorderStroke(1.dp, Color.Black),
                                    modifier = Modifier.size(68.dp),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.QrCode2,
                                            contentDescription = "QR Verification",
                                            modifier = Modifier.size(56.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = document.digitalRefCode,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp
                                )
                            }

                            // Official Government Stamp & Signatory
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFFFEBEE),
                                    border = BorderStroke(1.5.dp, Color(0xFFC62828)),
                                    modifier = Modifier.size(68.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(4.dp)
                                    ) {
                                        Text(
                                            text = "प्रमाणित",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFC62828)
                                        )
                                        Text(
                                            text = "VERIFIED",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFC62828)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = document.contactOfficer.ifBlank { "अधिकृत हस्ताक्षरकर्ता" },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    android.content.Intent.EXTRA_SUBJECT,
                                    "${document.titleNp} - Lalitpur United Agro Farm"
                                )
                                putExtra(
                                    android.content.Intent.EXTRA_TEXT,
                                    """
                                    ललितपुर युनाइटेड एग्रो फार्म प्रा. लि. (Lalitpur United Agro Farm Pvt. Ltd.)
                                    ------------------------------------------------------------------------
                                    कागजात: ${document.titleNp} (${document.titleEn})
                                    दर्ता/इजाजत नम्बर: ${document.documentNumber}
                                    क्षेत्रगत वर्ग: ${document.category.labelNp}
                                    जारी गर्ने निकाय: ${document.issuingAuthorityNp}
                                    जारी मिति: ${document.issueDate} | म्याद: ${document.expiryDate}
                                    प्रमाणीकरण कोड: ${document.digitalRefCode}
                                    स्थिति: ${document.status.labelNp}
                                    """.trimIndent()
                                )
                            }
                            context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Certificate Details"))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isEnglish) "Share Record" else "विवरण साझा गर्नुहोस्")
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isEnglish) "Close" else "बन्द गर्नुहोस्")
                    }
                }
            }
        }
    }
}

@Composable
private fun CertificateMetaRow(
    labelNp: String,
    labelEn: String,
    value: String,
    isBold: Boolean = false,
    isEnglish: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = if (isEnglish) labelEn else labelNp,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF424242)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = Color.Black,
            textAlign = TextAlign.End
        )
    }
}

private fun getCategoryIcon(category: DocumentCategory): ImageVector {
    return when (category) {
        DocumentCategory.REGISTRATION -> Icons.Default.Business
        DocumentCategory.TAX_VAT -> Icons.Default.ReceiptLong
        DocumentCategory.MUNICIPAL_PERMIT -> Icons.Default.Apartment
        DocumentCategory.AGRICULTURE_LIVESTOCK -> Icons.Default.Agriculture
        DocumentCategory.FOOD_SAFETY_DFTQC -> Icons.Default.Verified
        DocumentCategory.BANKING_FINANCE -> Icons.Default.AccountBalance
        DocumentCategory.ENVIRONMENTAL -> Icons.Default.Eco
        DocumentCategory.LAND_LEASE -> Icons.Default.Landscape
    }
}
