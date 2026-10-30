package com.educationwithhassan.pdfimageconverter.ui.screens

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.educationwithhassan.pdfimageconverter.engine.StorageManager
import com.educationwithhassan.pdfimageconverter.ui.components.AdMobBanner
import com.educationwithhassan.pdfimageconverter.ui.theme.RoyalBlue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedPdfUri by remember { mutableStateOf<Uri?>(null) }
    var pdfFileName by remember { mutableStateOf("") }
    var pageCount by remember { mutableIntStateOf(0) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var cachedFile by remember { mutableStateOf<File?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    fun renderPdfPreview(uri: Uri) {
        isLoading = true
        coroutineScope.launch {
            try {
                val tempFile = StorageManager.copyUriToTempFile(context, uri, "view_pdf", ".pdf")
                if (tempFile != null && tempFile.exists()) {
                    cachedFile = tempFile
                    pdfFileName = StorageManager.getFileName(context, uri)

                    withContext(Dispatchers.IO) {
                        val pfd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
                        val renderer = PdfRenderer(pfd)
                        pageCount = renderer.pageCount
                        if (renderer.pageCount > 0) {
                            val page = renderer.openPage(0)
                            val bmp = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                            page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            page.close()
                            renderer.close()
                            pfd.close()
                            withContext(Dispatchers.Main) {
                                previewBitmap = bmp
                            }
                        }
                    }
                }
            } catch (e: Throwable) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Could not render PDF preview: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                isLoading = false
            }
        }
    }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            selectedPdfUri = uri
            renderPdfPreview(uri)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("PDF Viewer & Inspector", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    try {
                        pickerLauncher.launch(arrayOf("application/pdf"))
                    } catch (e: Throwable) {
                        Toast.makeText(context, "Picker error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
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
                Text(text = if (selectedPdfUri != null) "Choose Different PDF" else "Select PDF Document")
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(24.dp))
            } else if (previewBitmap != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = pdfFileName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = "Total Pages: $pageCount · Offline Preview", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            cachedFile?.let { file ->
                                IconButton(onClick = { StorageManager.shareFile(context, file) }) {
                                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = RoyalBlue)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 280.dp, max = 420.dp)
                                .background(Color.White, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = previewBitmap!!.asImageBitmap(),
                                contentDescription = "PDF Page 1",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(56.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No PDF file opened", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Tap 'Select PDF Document' to inspect pages.", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}
