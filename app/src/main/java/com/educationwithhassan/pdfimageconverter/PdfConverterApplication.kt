package com.educationwithhassan.pdfimageconverter

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class PdfConverterApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize PDFBox Android resources for offline document rendering
        try {
            PDFBoxResourceLoader.init(applicationContext)
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        // Initialize Google Mobile Ads safely (graceful fallback on Amazon Fire without Play Services)
        try {
            MobileAds.initialize(this) {}
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}
