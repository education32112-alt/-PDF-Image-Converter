package com.educationwithhassan.pdfimageconverter.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.educationwithhassan.pdfimageconverter.ui.theme.RoyalBlue

@Composable
fun OnboardingScreen(navController: NavController) {
    var step by remember { mutableStateOf(0) }

    val titles = listOf(
        "Images to PDF Converter",
        "Merge & Split Documents",
        "Compress & Resize Images"
    )
    val descriptions = listOf(
        "Combine multiple photos, camera receipts, and scans into a single organized PDF with customizable page sizes.",
        "Consolidate multiple PDF files into one continuous document, or extract precise page ranges easily.",
        "Optimize file sizes with quality sliders and custom pixel dimensions while keeping high clarity."
    )

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STEP ${step + 1} OF 3",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoyalBlue
                )
                TextButton(onClick = { navController.navigate("home") }) {
                    Text(text = "Skip", fontSize = 12.sp)
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Surface(
                    modifier = Modifier.size(96.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = RoyalBlue.copy(alpha = 0.12f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val icon = when (step) {
                            0 -> Icons.Default.Collections
                            1 -> Icons.Default.CallMerge
                            else -> Icons.Default.Compress
                        }
                        Icon(imageVector = icon, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(48.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = titles[step],
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = descriptions[step],
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }

            Button(
                onClick = {
                    if (step < 2) step++ else navController.navigate("home")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
            ) {
                Text(text = if (step == 2) "Get Started" else "Next", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
