package com.educationwithhassan.pdfimageconverter.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.educationwithhassan.pdfimageconverter.admob.AdMobManager
import com.educationwithhassan.pdfimageconverter.engine.PdfEngine
import com.educationwithhassan.pdfimageconverter.engine.StorageManager
import com.educationwithhassan.pdfimageconverter.ui.components.AdMobBanner
import com.educationwithhassan.pdfimageconverter.ui.theme.RoyalBlue
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagesToPdfScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var pageSize by remember { mutableStateOf("A4") }
    var marginDp by remember { mutableIntStateOf(18) }
    var isProcessing by remember { mutableStateOf(false) }
    var progressStatus by remember { mutableStateOf("") }
    var progressPercent by remember { mutableIntStateOf(0) }

    // Crash-proof Photo Picker (Android 13+ / Google Play Services)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 50)
    ) { uris ->
        if (!uris.isNullOrEmpty()) {
            val validUris = uris.filterNotNull()
            selectedUris = (selectedUris + validUris).distinct()
        }
    }

    // Storage Access Framework Fallback for devices without Photo Picker (e.g. Amazon Fire OS)
    val safPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (!uris.isNullOrEmpty()) {
            val validUris = uris.filterNotNull()
            validUris.forEach { uri ->
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Throwable) {}
            }
            selectedUris = (selectedUris + validUris).distinct()
        }
    }

    fun launchSafePicker() {
        try {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } catch (e1: Throwable) {
            try {
                safPickerLauncher.launch(arrayOf("image/*"))
            } catch (e2: Throwable) {
                Toast.makeText(context, "Cannot open image picker: ${e2.localizedMessage ?: "Unknown error"}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Images to PDF", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (selectedUris.isNotEmpty() && !isProcessing) {
                        TextButton(onClick = { selectedUris = emptyList() }) {
                            Text("Clear", color = Color.Red, fontSize = 12.sp)
                        }
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                AdMobBanner(modifier = Modifier.fillMaxWidth())
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Upload Button
            Button(
                onClick = { launchSafePicker() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
            ) {
                Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (selectedUris.isEmpty()) "Select Images" else "Add More Images (${selectedUris.size} selected)")
            }

            // Options: Page Size & Margins
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("A4", "Letter", "Fit").forEach { size ->
                    FilterChip(
                        selected = pageSize == size,
                        onClick = { pageSize = size },
                        label = { Text(size, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Selected Images List
            if (selectedUris.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Collections, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("No images selected yet", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Tap 'Select Images' to convert photos into PDF.", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(selectedUris) { index, uri ->
                        val fileName = remember(uri) { StorageManager.getFileName(context, uri) }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = RoyalBlue.copy(alpha = 0.15f),
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("${index + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = fileName,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                IconButton(onClick = {
                                    selectedUris = selectedUris.filterIndexed { i, _ -> i != index }
                                }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }

            if (isProcessing) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), progress = { progressPercent / 100f })
                    Text(text = progressStatus, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
            }

            // Convert Action Button
            Button(
                onClick = {
                    if (selectedUris.isEmpty()) {
                        Toast.makeText(context, "Select at least 1 image", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isProcessing = true
                    AdMobManager.isConversionActive = true
                    coroutineScope.launch {
                        val outFile = StorageManager.generateUniqueFile(context, "ConvertedDoc", "pdf")
                        val result = PdfEngine.convertImagesToPdf(
                            context = context,
                            imageUris = selectedUris,
                            outputFile = outFile,
                            pageSize = pageSize,
                            marginDp = marginDp,
                            onProgress = { pct, status ->
                                progressPercent = pct
                                progressStatus = status
                            }
                        )
                        isProcessing = false
                        AdMobManager.isConversionActive = false
                        result.onSuccess {
                            Toast.makeText(context, "Saved: ${outFile.name}", Toast.LENGTH_LONG).show()
                            StorageManager.shareFile(context, outFile)
                        }.onFailure { err ->
                            Toast.makeText(context, "Conversion failed: ${err.localizedMessage ?: err.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                enabled = !isProcessing && selectedUris.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (isProcessing) "Converting..." else "Convert to PDF", fontWeight = FontWeight.Bold)
            }
        }
    }
}
