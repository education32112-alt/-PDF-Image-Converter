package com.educationwithhassan.pdfimageconverter

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.educationwithhassan.pdfimageconverter.admob.AdMobManager
import com.educationwithhassan.pdfimageconverter.admob.InterstitialAdManager
import com.educationwithhassan.pdfimageconverter.ui.screens.*
import com.educationwithhassan.pdfimageconverter.ui.theme.PDFImageConverterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize AdMob, UMP Consent, and InterstitialAdManager safely
        try {
            InterstitialAdManager.initialize(this)
            AdMobManager.requestConsentAndInit(this) {
                InterstitialAdManager.loadAd()
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        setContent {
            PDFImageConverterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // Centralized NavHost listener to keep background ad preloaded and monitor navigation
                    DisposableEffect(navController) {
                        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
                            val route = destination.route
                            if (route != null && route != "splash" && route != "onboarding") {
                                InterstitialAdManager.loadAd()
                            }
                        }
                        navController.addOnDestinationChangedListener(listener)
                        onDispose {
                            navController.removeOnDestinationChangedListener(listener)
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = "splash"
                    ) {
                        composable("splash") { SplashScreen(navController) }
                        composable("onboarding") { OnboardingScreen(navController) }
                        composable("home") { HomeScreen(navController) }
                        composable("images_to_pdf") { ImagesToPdfScreen(navController) }
                        composable("pdf_viewer") { PdfViewerScreen(navController) }
                        composable("merge_pdf") { MergePdfScreen(navController) }
                        composable("split_pdf") { SplitPdfScreen(navController) }
                        composable("image_converter") { ImageConverterScreen(navController) }
                        composable("image_resizer") { ImageResizerScreen(navController) }
                        composable("image_compressor") { ImageCompressorScreen(navController) }
                        composable("history") { HistoryScreen(navController) }
                        composable("settings") { SettingsScreen(navController) }
                        composable("privacy_policy") { PrivacyPolicyScreen(navController) }
                        composable("terms_of_use") { TermsOfUseScreen(navController) }
                        composable("about") { AboutScreen(navController) }
                        composable("contact_us") { ContactUsScreen(navController) }
                        composable("licenses") { LicensesScreen(navController) }
                        composable("faq") { FaqScreen(navController) }
                    }
                }
            }
        }
    }
}
