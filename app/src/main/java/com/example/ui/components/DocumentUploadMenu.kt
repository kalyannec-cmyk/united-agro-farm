package com.example.ui.components

import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AgroGreenPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class AttachedDocumentItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: String, // "COMPANY_LOGO", "OFFICIAL_PERMIT", "PAYMENT_RECEIPT", "INVOICE"
    val uriString: String,
    val fileName: String,
    val fileSizeFormatted: String,
    val isImage: Boolean,
    val uploadedAt: String = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
)

/**
 * Reusable Upload Menu and manager for:
 * 1. Company images / logos
 * 2. Official documents / certificates (PDF, Docs)
 * 3. Payment receipts / bank vouchers
 *
 * Uses Android Photo Picker and GetContent contracts (zero runtime permissions required).
 */
@Composable
fun DocumentUploadMenuDialog(
    isOpen: Boolean,
    isEnglish: Boolean = false,
    defaultCategory: String = "OFFICIAL_PERMIT",
    onDismiss: () -> Unit,
    onDocumentAttached: (AttachedDocumentItem) -> Unit
) {
    if (!isOpen) return

    val context = LocalContext.current
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var detectedFileName by remember { mutableStateOf("") }
    var detectedFileSize by remember { mutableStateOf("") }
    var isImageFile by remember { mutableStateOf(false) }

    var customTitle by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(defaultCategory) }

    // Zero-permission Android Photo Picker for Images/Photos
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            isImageFile = true
            val name = queryFileName(context, uri) ?: "company_image_${System.currentTimeMillis()}.jpg"
            detectedFileName = name
            detectedFileSize = queryFileSize(context, uri) ?: "Image file"
            if (customTitle.isBlank()) {
                customTitle = if (selectedCategory == "COMPANY_LOGO") {
                    if (isEnglish) "United Agro Company Logo" else "युनाइटेड एग्रो आधिकारिक लोगो"
                } else {
                    name.substringBeforeLast(".")
                }
            }
        }
    }

    // Zero-permission document picker for PDFs and any document type
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            val name = queryFileName(context, uri) ?: "attached_document_${System.currentTimeMillis()}.pdf"
            detectedFileName = name
            detectedFileSize = queryFileSize(context, uri) ?: "Document file"
            isImageFile = name.endsWith(".jpg", true) || name.endsWith(".jpeg", true) || name.endsWith(".png", true)
            if (customTitle.isBlank()) {
                customTitle = name.substringBeforeLast(".")
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(AgroGreenPrimary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.UploadFile,
                        contentDescription = null,
                        tint = AgroGreenPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = if (isEnglish) "Upload Company Image / Document" else "कम्पनी फोटो तथा कागजात अपलोड",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isEnglish) "Attach documents, logo, or payment receipt" else "लोगो, दर्ता प्रमाणपत्र वा रसिद संलग्न गर्नुहोस्",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Category Selector
                Text(
                    text = if (isEnglish) "Select Document Type:" else "कागजातको प्रकार चयन गर्नुहोस्:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CategoryChip(
                        label = if (isEnglish) "Logo / Photo" else "कम्पनी लोगो",
                        isSelected = selectedCategory == "COMPANY_LOGO",
                        onClick = { selectedCategory = "COMPANY_LOGO" },
                        modifier = Modifier.weight(1f)
                    )
                    CategoryChip(
                        label = if (isEnglish) "Permit/PDF" else "कागजात/दर्ता",
                        isSelected = selectedCategory == "OFFICIAL_PERMIT",
                        onClick = { selectedCategory = "OFFICIAL_PERMIT" },
                        modifier = Modifier.weight(1f)
                    )
                    CategoryChip(
                        label = if (isEnglish) "Receipt" else "बैंक भौचर",
                        isSelected = selectedCategory == "PAYMENT_RECEIPT",
                        onClick = { selectedCategory = "PAYMENT_RECEIPT" },
                        modifier = Modifier.weight(1f)
                    )
                }

                HorizontalDivider()

                // Upload Trigger Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("upload_image_picker_button")
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isEnglish) "Pick Photo" else "फोटो छान्नुहोस्", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            documentPickerLauncher.launch("*/*")
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("upload_document_picker_button")
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isEnglish) "Pick Document" else "कागजात छान्नुहोस्", fontSize = 12.sp)
                    }
                }

                // File selected display preview card
                if (selectedUri != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AgroGreenPrimary.copy(alpha = 0.08f)),
                        border = BorderStroke(1.dp, AgroGreenPrimary.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isImageFile) Icons.Default.Image else Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = AgroGreenPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = detectedFileName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = detectedFileSize,
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                            IconButton(onClick = {
                                selectedUri = null
                                detectedFileName = ""
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.Gray)
                            }
                        }
                    }

                    // Optional Title input
                    OutlinedTextField(
                        value = customTitle,
                        onValueChange = { customTitle = it },
                        label = { Text(if (isEnglish) "Document / Image Title" else "कागजातको शीर्षक") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("upload_document_title_input")
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isEnglish) "No file selected yet. Tap above to select." else "कुनै फाइल छानिएको छैन। माथि ट्याप गर्नुहोस्।",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedUri != null) {
                        val item = AttachedDocumentItem(
                            title = customTitle.ifBlank { detectedFileName },
                            category = selectedCategory,
                            uriString = selectedUri.toString(),
                            fileName = detectedFileName,
                            fileSizeFormatted = detectedFileSize,
                            isImage = isImageFile
                        )
                        onDocumentAttached(item)
                        Toast.makeText(
                            context,
                            if (isEnglish) "Attached successfully!" else "सफलतापूर्वक संलग्न गरियो!",
                            Toast.LENGTH_SHORT
                        ).show()
                        onDismiss()
                    }
                },
                enabled = selectedUri != null,
                colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("upload_confirm_save_button")
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isEnglish) "Save Attachment" else "संलग्न सुरक्षित गर्नुहोस्")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isEnglish) "Cancel" else "रद्द गर्नुहोस्")
            }
        }
    )
}

@Composable
fun AttachedDocumentsRow(
    documents: List<AttachedDocumentItem>,
    isEnglish: Boolean = false,
    onRemoveDocument: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (documents.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = if (isEnglish) "Attached Documents & Images (${documents.size})" else "संलग्न फोटो तथा कागजातहरू (${documents.size})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(documents, key = { it.id }) { doc ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                    border = BorderStroke(1.dp, AgroGreenPrimary.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .width(220.dp)
                        .testTag("attached_doc_card_${doc.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (doc.isImage) Icons.Default.Image else Icons.Default.Description,
                            contentDescription = null,
                            tint = AgroGreenPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = doc.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${doc.category} • ${doc.uploadedAt}",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                        IconButton(
                            onClick = { onRemoveDocument(doc.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = if (isSelected) AgroGreenPrimary else Color(0xFFEEEEEE),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.height(34.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else Color(0xFF424242)
            )
        }
    }
}

private fun queryFileName(context: android.content.Context, uri: Uri): String? {
    var name: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) name = it.getString(index)
            }
        }
    }
    return name ?: uri.lastPathSegment
}

private fun queryFileSize(context: android.content.Context, uri: Uri): String? {
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex >= 0) {
                    val bytes = it.getLong(sizeIndex)
                    return when {
                        bytes >= 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0))
                        bytes >= 1024 -> String.format(Locale.getDefault(), "%.1f KB", bytes / 1024.0)
                        bytes > 0 -> "$bytes B"
                        else -> null
                    }
                }
            }
        }
    }
    return null
}
