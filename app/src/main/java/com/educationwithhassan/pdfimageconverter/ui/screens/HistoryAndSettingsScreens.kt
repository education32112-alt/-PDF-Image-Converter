package com.educationwithhassan.pdfimageconverter.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.educationwithhassan.pdfimageconverter.engine.StorageManager
import com.educationwithhassan.pdfimageconverter.ui.components.AdMobBanner
import com.educationwithhassan.pdfimageconverter.ui.theme.RoyalBlue
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController) {
    val context = LocalContext.current
    var files by remember { mutableStateOf<List<File>>(emptyList()) }

    fun refreshFiles() {
        val dir = StorageManager.getOutputDirectory(context)
        files = dir.listFiles()?.filter { it.isFile && it.length() > 0 }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    LaunchedEffect(Unit) {
        refreshFiles()
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Recent Conversions", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { refreshFiles() }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
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
        if (files.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No converted files found yet", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Your converted PDFs and images will be listed here.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(files) { file ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = RoyalBlue)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = file.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                                Text(text = "${file.length() / 1024} KB", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            IconButton(onClick = { StorageManager.shareFile(context, file) }) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = RoyalBlue)
                            }
                            IconButton(onClick = {
                                try {
                                    file.delete()
                                    refreshFiles()
                                } catch (_: Throwable) {}
                            }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Settings & Information", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("General & Legal", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = RoyalBlue)

            ListItem(
                headlineContent = { Text("Privacy Policy") },
                supportingContent = { Text("Offline document processing policy & AdMob disclosure") },
                leadingContent = { Icon(Icons.Default.Security, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                trailingContent = {
                    TextButton(onClick = { navController.navigate("privacy_policy") }) {
                        Text("View")
                    }
                }
            )

            ListItem(
                headlineContent = { Text("Terms of Use") },
                supportingContent = { Text("App store license & acceptable use terms") },
                leadingContent = { Icon(Icons.Default.Gavel, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                trailingContent = {
                    TextButton(onClick = { navController.navigate("terms_of_use") }) {
                        Text("View")
                    }
                }
            )

            ListItem(
                headlineContent = { Text("Frequently Asked Questions") },
                supportingContent = { Text("Troubleshooting, features, and file tips") },
                leadingContent = { Icon(Icons.Default.HelpOutline, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                trailingContent = {
                    TextButton(onClick = { navController.navigate("faq") }) {
                        Text("View")
                    }
                }
            )

            ListItem(
                headlineContent = { Text("Open Source Licenses") },
                supportingContent = { Text("Apache 2.0, PDFBox-Android, Google Mobile Ads") },
                leadingContent = { Icon(Icons.Default.Article, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                trailingContent = {
                    TextButton(onClick = { navController.navigate("licenses") }) {
                        Text("View")
                    }
                }
            )

            ListItem(
                headlineContent = { Text("About & Contact Us") },
                supportingContent = { Text("Developer: Rana Hassan · Version 1.0.0") },
                leadingContent = { Icon(Icons.Default.Info, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                trailingContent = {
                    TextButton(onClick = { navController.navigate("about") }) {
                        Text("View")
                    }
                }
            )
        }
    }
}
