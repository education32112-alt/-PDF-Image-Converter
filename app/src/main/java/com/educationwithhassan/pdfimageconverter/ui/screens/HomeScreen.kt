package com.educationwithhassan.pdfimageconverter.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.educationwithhassan.pdfimageconverter.admob.InterstitialAdManager
import com.educationwithhassan.pdfimageconverter.ui.components.AdMobBanner
import com.educationwithhassan.pdfimageconverter.ui.theme.CyanAccent
import com.educationwithhassan.pdfimageconverter.ui.theme.Navy
import com.educationwithhassan.pdfimageconverter.ui.theme.RoyalBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scrollState = rememberScrollState()

    fun navigateToTool(route: String) {
        InterstitialAdManager.onNavigationAction(activity) {
            navController.navigate(route)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                windowInsets = TopAppBarDefaults.windowInsets,
                title = {
                    Column {
                        Text(
                            text = "PDF & Image Converter",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "100% On-Device Processing · Private",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navigateToTool("settings") }) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // AdMob Banner safely placed above bottom navigation
                    AdMobBanner(modifier = Modifier.fillMaxWidth())

                    // Material 3 Fixed Bottom Navigation with zero extra insets to prevent double padding
                    NavigationBar(
                        windowInsets = WindowInsets(0, 0, 0, 0),
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp,
                        modifier = Modifier.height(64.dp)
                    ) {
                        NavigationBarItem(
                            selected = true,
                            onClick = { /* Current screen */ },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                        NavigationBarItem(
                            selected = false,
                            onClick = { navigateToTool("history") },
                            icon = { Icon(Icons.Default.History, contentDescription = "History") },
                            label = { Text("History", fontSize = 11.sp) }
                        )
                        NavigationBarItem(
                            selected = false,
                            onClick = { navigateToTool("faq") },
                            icon = { Icon(Icons.Default.HelpOutline, contentDescription = "Help") },
                            label = { Text("Help", fontSize = 11.sp) }
                        )
                        NavigationBarItem(
                            selected = false,
                            onClick = { navigateToTool("privacy_policy") },
                            icon = { Icon(Icons.Default.Shield, contentDescription = "Privacy") },
                            label = { Text("Privacy", fontSize = 11.sp) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Primary Card: Images to PDF
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navigateToTool("images_to_pdf") },
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Navy),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Navy, RoyalBlue, CyanAccent)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "PRIMARY TOOL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "Images to PDF", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(text = "Select multiple photos and combine them into a single PDF document.", fontSize = 12.sp, color = Color.White.copy(alpha = 0.9f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Convert Images →", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                    }
                }
            }

            // Document Tools Grid
            Text(text = "DOCUMENT TOOLS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ToolCard(Modifier.weight(1f), "PDF Viewer", "Inspect documents", Icons.Default.PictureAsPdf, Color(0xFF2563EB)) {
                    navigateToTool("pdf_viewer")
                }
                ToolCard(Modifier.weight(1f), "Merge PDF", "Combine documents", Icons.Default.CallMerge, Color(0xFF4F46E5)) {
                    navigateToTool("merge_pdf")
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ToolCard(Modifier.weight(1f), "Split PDF", "Extract pages", Icons.Default.CallSplit, Color(0xFF06B6D4)) {
                    navigateToTool("split_pdf")
                }
                ToolCard(Modifier.weight(1f), "Format Convert", "JPG, PNG, WebP", Icons.Default.SwapHoriz, Color(0xFF10B981)) {
                    navigateToTool("image_converter")
                }
            }

            // Image Tools Grid
            Text(text = "IMAGE TOOLS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ToolCard(Modifier.weight(1f), "Resize Image", "Custom dimensions", Icons.Default.AspectRatio, Color(0xFFF59E0B)) {
                    navigateToTool("image_resizer")
                }
                ToolCard(Modifier.weight(1f), "Compress Image", "Reduce file size", Icons.Default.Compress, Color(0xFFEF4444)) {
                    navigateToTool("image_compressor")
                }
            }

            // Privacy & Trust Badge Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("100% Private & Offline", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(
                            "All document and image operations occur exclusively on this device. No files are uploaded to external servers.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun ToolCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = iconTint.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}
