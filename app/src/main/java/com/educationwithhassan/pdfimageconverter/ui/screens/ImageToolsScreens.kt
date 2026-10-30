package com.educationwithhassan.pdfimageconverter.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.educationwithhassan.pdfimageconverter.admob.AdMobManager
import com.educationwithhassan.pdfimageconverter.engine.ImageEngine
import com.educationwithhassan.pdfimageconverter.engine.StorageManager
import com.educationwithhassan.pdfimageconverter.ui.components.AdMobBanner
import com.educationwithhassan.pdfimageconverter.ui.theme.RoyalBlue
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageConverterScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("") }
    var targetFormat by remember { mutableStateOf("PNG") }
    var isProcessing by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedUri = uri
            fileName = StorageManager.getFileName(context, uri)
        }
    }
    val safPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            selectedUri = uri
            fileName = StorageManager.getFileName(context, uri)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Image Format Converter", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
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
                        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    } catch (_: Throwable) {
                        try {
                            safPicker.launch(arrayOf("image/*"))
                        } catch (e: Throwable) {
                            Toast.makeText(context, "Picker error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
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
                Text(text = if (selectedUri != null) "Selected: $fileName" else "Select Image")
            }

            Text("Select Target Output Format:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("PNG", "JPEG", "WEBP").forEach { fmt ->
                    FilterChip(
                        selected = targetFormat == fmt,
                        onClick = { targetFormat = fmt },
                        label = { Text(fmt) }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f, fill = false))

            if (isProcessing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Button(
                onClick = {
                    val uri = selectedUri ?: return@Button
                    isProcessing = true
                    AdMobManager.isConversionActive = true
                    coroutineScope.launch {
                        val compressFmt = when (targetFormat) {
                            "JPEG" -> Bitmap.CompressFormat.JPEG
                            "WEBP" -> Bitmap.CompressFormat.WEBP
                            else -> Bitmap.CompressFormat.PNG
                        }
                        val ext = if (targetFormat == "JPEG") "jpg" else targetFormat.lowercase()
                        val outFile = StorageManager.generateUniqueFile(context, "Converted", ext)
                        val res = ImageEngine.processImage(context, uri, outFile, compressFmt, 90)
                        isProcessing = false
                        AdMobManager.isConversionActive = false
                        res.onSuccess {
                            Toast.makeText(context, "Saved to: ${outFile.name}", Toast.LENGTH_SHORT).show()
                            StorageManager.shareFile(context, outFile, "image/$ext")
                        }.onFailure { err ->
                            Toast.makeText(context, "Error: ${err.localizedMessage ?: err.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = selectedUri != null && !isProcessing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
            ) {
                Text(if (isProcessing) "Converting..." else "Convert Image", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageResizerScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("") }
    var widthInput by remember { mutableStateOf("1080") }
    var heightInput by remember { mutableStateOf("1080") }
    var isProcessing by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedUri = uri
            fileName = StorageManager.getFileName(context, uri)
        }
    }
    val safPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            selectedUri = uri
            fileName = StorageManager.getFileName(context, uri)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Image Resizer", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
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
                        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    } catch (_: Throwable) {
                        try {
                            safPicker.launch(arrayOf("image/*"))
                        } catch (e: Throwable) {
                            Toast.makeText(context, "Picker error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
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
                Text(if (selectedUri != null) "Selected: $fileName" else "Select Image to Resize")
            }

            OutlinedTextField(
                value = widthInput,
                onValueChange = { widthInput = it },
                label = { Text("Target Width (px)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = heightInput,
                onValueChange = { heightInput = it },
                label = { Text("Target Height (px)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            if (isProcessing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Button(
                onClick = {
                    val uri = selectedUri ?: return@Button
                    val w = widthInput.toIntOrNull() ?: 1080
                    val h = heightInput.toIntOrNull() ?: 1080
                    isProcessing = true
                    AdMobManager.isConversionActive = true
                    coroutineScope.launch {
                        val outFile = StorageManager.generateUniqueFile(context, "Resized", "jpg")
                        val res = ImageEngine.processImage(context, uri, outFile, Bitmap.CompressFormat.JPEG, 90, w, h)
                        isProcessing = false
                        AdMobManager.isConversionActive = false
                        res.onSuccess {
                            Toast.makeText(context, "Resized to ${w}x${h} successfully!", Toast.LENGTH_SHORT).show()
                            StorageManager.shareFile(context, outFile, "image/jpeg")
                        }.onFailure { err ->
                            Toast.makeText(context, "Resize error: ${err.localizedMessage ?: err.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = selectedUri != null && !isProcessing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
            ) {
                Text(if (isProcessing) "Resizing..." else "Resize and Save", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageCompressorScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("") }
    var quality by remember { mutableFloatStateOf(70f) }
    var isProcessing by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedUri = uri
            fileName = StorageManager.getFileName(context, uri)
        }
    }
    val safPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            selectedUri = uri
            fileName = StorageManager.getFileName(context, uri)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Image Compressor", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
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
                        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    } catch (_: Throwable) {
                        try {
                            safPicker.launch(arrayOf("image/*"))
                        } catch (e: Throwable) {
                            Toast.makeText(context, "Picker error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
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
                Text(if (selectedUri != null) "Selected: $fileName" else "Select Image to Compress")
            }

            Text("Compression Quality: ${quality.toInt()}%", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Slider(
                value = quality,
                onValueChange = { quality = it },
                valueRange = 10f..95f,
                steps = 17
            )

            if (isProcessing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Button(
                onClick = {
                    val uri = selectedUri ?: return@Button
                    isProcessing = true
                    AdMobManager.isConversionActive = true
                    coroutineScope.launch {
                        val outFile = StorageManager.generateUniqueFile(context, "Compressed", "jpg")
                        val res = ImageEngine.processImage(context, uri, outFile, Bitmap.CompressFormat.JPEG, quality.toInt())
                        isProcessing = false
                        AdMobManager.isConversionActive = false
                        res.onSuccess {
                            Toast.makeText(context, "Compressed successfully! Saved to ${outFile.name}", Toast.LENGTH_SHORT).show()
                            StorageManager.shareFile(context, outFile, "image/jpeg")
                        }.onFailure { err ->
                            Toast.makeText(context, "Compress error: ${err.localizedMessage ?: err.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = selectedUri != null && !isProcessing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
            ) {
                Text(if (isProcessing) "Compressing..." else "Compress Image", fontWeight = FontWeight.Bold)
            }
        }
    }
}
