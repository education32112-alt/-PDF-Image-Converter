package com.educationwithhassan.pdfimageconverter.ui.components

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.educationwithhassan.pdfimageconverter.admob.AdMobManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 50.dp, max = 60.dp),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = AdMobManager.bannerAdUnitId
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        Log.d("AdMobBanner", "Banner ad loaded successfully. Unit: ${AdMobManager.bannerAdUnitId}")
                    }
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        Log.w("AdMobBanner", "Banner ad failed: ${error.message} (code: ${error.code})")
                    }
                }
                try {
                    loadAd(AdRequest.Builder().build())
                } catch (e: Throwable) {
                    Log.e("AdMobBanner", "Error calling loadAd: ${e.message}")
                }
            }
        }
    )
}
