package com.educationwithhassan.pdfimageconverter.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.UploadFile
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
import com.tom_roush.pdfbox.pdmodel.PDDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitPdfScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("") }
    var totalPages by remember { mutableIntStateOf(0) }
    var pageInput by remember { mutableStateOf("1") }
    var isProcessing by remember { mutableStateOf(false) }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri
            fileName = StorageManager.getFileName(context, uri)
            coroutineScope.launch {
                val tmp = StorageManager.copyUriToTempFile(context, uri, "split_check", ".pdf")
                if (tmp != null) {
                    try {
                        withContext(Dispatchers.IO) {
                            PDDocument.load(tmp).use { doc ->
                                totalPages = doc.numberOfPages
                            }
                        }
                    } catch (_: Throwable) {} finally {
                        try { tmp.delete() } catch (_: Throwable) {}
                    }
                }
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Split PDF Document", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    try {
                        pickerLauncher.launch(arrayOf("application/pdf"))
                    } catch (e: Throwable) {
                        Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
            ) {
                Icon(imageVector = Icons.Default.UploadFile, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (selectedUri != null) "Change Selected File" else "Select PDF to Split")
            }

            if (selectedUri != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = fileName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = if (totalPages > 0) "Total pages in document: $totalPages" else "Analyzing pages...",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                OutlinedTextField(
                    value = pageInput,
                    onValueChange = { pageInput = it },
                    label = { Text("Pages to extract (e.g. 1, 2, 3)") },
                    supportingText = { Text(if (totalPages > 0) "Valid range: 1 to $totalPages" else "Enter comma-separated page numbers") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CallSplit, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("No PDF file selected", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Select a multi-page PDF to extract pages.", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }

            if (isProcessing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Button(
                onClick = {
                    val uri = selectedUri ?: return@Button
                    val pages = pageInput.split(",")
                        .mapNotNull { it.trim().toIntOrNull() }
                    if (pages.isEmpty()) {
                        Toast.makeText(context, "Enter at least 1 valid page number", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isProcessing = true
                    AdMobManager.isConversionActive = true
                    coroutineScope.launch {
                        val tmp = StorageManager.copyUriToTempFile(context, uri, "split_source", ".pdf")
                        if (tmp == null || !tmp.exists()) {
                            isProcessing = false
                            AdMobManager.isConversionActive = false
                            Toast.makeText(context, "Failed to read source PDF.", Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        val outFile = StorageManager.generateUniqueFile(context, "SplitDoc", "pdf")
                        val result = PdfEngine.splitPdf(tmp, pages, outFile) { _, _ -> }
                        isProcessing = false
                        AdMobManager.isConversionActive = false
                        try { tmp.delete() } catch (_: Throwable) {}

                        result.onSuccess {
                            Toast.makeText(context, "Split PDF Created: ${outFile.name}", Toast.LENGTH_SHORT).show()
                            StorageManager.shareFile(context, outFile)
                        }.onFailure { err ->
                            Toast.makeText(context, "Error: ${err.localizedMessage ?: err.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                enabled = !isProcessing && selectedUri != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
            ) {
                Text(text = if (isProcessing) "Extracting Pages..." else "Extract Selected Pages", fontWeight = FontWeight.Bold)
            }
        }
    }
}
