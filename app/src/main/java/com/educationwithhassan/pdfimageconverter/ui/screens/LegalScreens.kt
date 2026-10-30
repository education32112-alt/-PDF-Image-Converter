package com.educationwithhassan.pdfimageconverter.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.educationwithhassan.pdfimageconverter.ui.components.AdMobBanner
import com.educationwithhassan.pdfimageconverter.ui.theme.RoyalBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(navController: NavController) {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Privacy Policy for PDF & Image Converter", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RoyalBlue)
            Text("Effective Date: October 2026 | Developer: Rana Hassan")
            Text("1. On-Device File Processing", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("PDF & Image Converter processes all documents, pictures, camera scans, and files locally on your Android device. Your files are never uploaded to any remote server or third-party cloud service.")
            Text("2. Advertising & Analytics Disclosures", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("This app integrates the Google AdMob SDK. Google AdMob may collect device identifiers and usage telemetry strictly for advertising delivery and fraud detection in accordance with the Google Privacy Policy.")
            Text("3. Storage Access Permissions", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("The app uses standard Android Storage Access Framework and Photo Picker to read files selected by you. We do not access any files outside your explicit selection.")
            Text("4. Contact Information", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("For questions regarding this privacy policy, please contact Rana Hassan via the Contact Us screen.")
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsOfUseScreen(navController: NavController) {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Terms of Use", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Terms of Use", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RoyalBlue)
            Text("1. License Grant", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("PDF & Image Converter is licensed to you for personal and commercial productivity purposes on compatible Android and Amazon Fire devices.")
            Text("2. Limitation of Liability", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("The software is provided 'as is' without warranty of any kind. You are solely responsible for maintaining backup copies of your documents and images.")
            Text("3. Intellectual Property", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("All app branding, artwork, and code are owned by Rana Hassan. Third-party libraries remain the property of their respective copyright holders.")
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavController) {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("About PDF & Image Converter", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("PDF & Image Converter", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = RoyalBlue)
            Text("Version: 1.0.0 (Release Variant)")
            Text("Package: com.educationwithhassan.pdfimageconverter")
            Text("Developer & Publisher: Rana Hassan")
            Text("Designed for phones, tablets, and Amazon Fire devices. Engineered with Jetpack Compose, Material 3, and PDFBox-Android for fast, reliable, 100% on-device document operations.")
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactUsScreen(navController: NavController) {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Contact Us", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Support & Inquiries", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RoyalBlue)
            Text("Developer: Rana Hassan")
            Text("Email: education32112@gmail.com")
            Text("Response time: typically within 24-48 business hours for feature feedback, bug reports, and assistance.")
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(navController: NavController) {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Open Source Licenses", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Open Source Software Notice", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RoyalBlue)
            Text("1. PDFBox-Android (com.tom-roush:pdfbox-android)")
            Text("Licensed under the Apache License, Version 2.0.")
            Text("2. AndroidX & Jetpack Compose")
            Text("Licensed under the Apache License, Version 2.0.")
            Text("3. Google Play Services & AdMob SDK")
            Text("Subject to Google APIs Terms of Service.")
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaqScreen(navController: NavController) {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Frequently Asked Questions", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            FaqItem("Are my files uploaded to the internet?", "No. Every operation—including converting images, merging PDFs, extracting pages, and resizing images—runs 100% locally on your device's processor.")
            FaqItem("How many images can I convert at once?", "You can select up to 50 images in a single batch. The app automatically manages memory to prevent crashes on high-resolution camera photos.")
            FaqItem("Where are converted files stored?", "Converted documents are saved in your app's private documents directory, where you can easily share or open them with any PDF viewer or messaging app.")
            FaqItem("Does this app work without an internet connection?", "Yes! All conversion and manipulation tools operate completely offline.")
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FaqItem(question: String, answer: String) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(question, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = RoyalBlue)
            Spacer(modifier = Modifier.height(6.dp))
            Text(answer, fontSize = 12.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
        }
    }
}
