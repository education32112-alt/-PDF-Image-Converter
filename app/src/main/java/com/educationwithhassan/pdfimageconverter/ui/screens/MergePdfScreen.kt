package com.educationwithhassan.pdfimageconverter.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MergePdfScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isProcessing by remember { mutableStateOf(false) }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (!uris.isNullOrEmpty()) {
            val valid = uris.filterNotNull()
            selectedUris = (selectedUris + valid).distinct()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Merge PDF Documents", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (selectedUris.isNotEmpty()) {
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Button(
                onClick = {
                    try {
                        pickerLauncher.launch(arrayOf("application/pdf"))
                    } catch (e: Throwable) {
                        Toast.makeText(context, "Error opening picker: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select PDF Files (${selectedUris.size} selected)")
            }

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
                            Icon(Icons.Default.CallMerge, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("No PDFs selected", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Add at least 2 PDF files to merge into a single document.", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(selectedUris) { index, uri ->
                        val fileName = remember(uri) { StorageManager.getFileName(context, uri) }
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
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
                                    Text(text = fileName, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                                }
                                IconButton(onClick = { selectedUris = selectedUris.filterIndexed { i, _ -> i != index } }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }

            if (isProcessing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Button(
                onClick = {
                    if (selectedUris.size < 2) {
                        Toast.makeText(context, "Please select at least 2 PDF files to merge", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isProcessing = true
                    AdMobManager.isConversionActive = true
                    coroutineScope.launch {
                        val tempFiles = mutableListOf<File>()
                        var copyFailed = false
                        selectedUris.forEachIndexed { idx, uri ->
                            val tmp = StorageManager.copyUriToTempFile(context, uri, "merge_$idx", ".pdf")
                            if (tmp != null && tmp.exists() && tmp.length() > 0) {
                                tempFiles.add(tmp)
                            } else {
                                copyFailed = true
                            }
                        }

                        if (copyFailed || tempFiles.size < 2) {
                            isProcessing = false
                            AdMobManager.isConversionActive = false
                            Toast.makeText(context, "Failed to read one or more selected PDF files.", Toast.LENGTH_LONG).show()
                            tempFiles.forEach { try { it.delete() } catch (_: Throwable) {} }
                            return@launch
                        }

                        val outFile = StorageManager.generateUniqueFile(context, "MergedDoc", "pdf")
                        val result = PdfEngine.mergePdfs(tempFiles, outFile) { _, _ -> }
                        isProcessing = false
                        AdMobManager.isConversionActive = false
                        tempFiles.forEach { try { it.delete() } catch (_: Throwable) {} }

                        result.onSuccess {
                            Toast.makeText(context, "Merged successfully: ${outFile.name}", Toast.LENGTH_LONG).show()
                            StorageManager.shareFile(context, outFile)
                        }.onFailure { err ->
                            Toast.makeText(context, "Merge error: ${err.localizedMessage ?: err.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                enabled = !isProcessing && selectedUris.size >= 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
            ) {
                Text(text = if (isProcessing) "Merging..." else "Merge Documents", fontWeight = FontWeight.Bold)
            }
        }
    }
}
