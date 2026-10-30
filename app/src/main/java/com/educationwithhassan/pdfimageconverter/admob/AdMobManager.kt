package com.educationwithhassan.pdfimageconverter.admob

import android.app.Activity
import android.content.Context
import android.util.Log
import com.educationwithhassan.pdfimageconverter.BuildConfig
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.ump.*

/**
 * AdMob Integration Architecture
 * 
 * Strict Policy Adherence:
 * - In Debug builds, official Google Test IDs are ALWAYS loaded to safeguard account standing.
 * - In Release builds, verified Production IDs are utilized.
 * - Ads are never displayed while user is selecting files or actively executing a conversion.
 */
object AdMobManager {

    private const val TAG = "AdMobManager"

    // Publisher Account Identifier
    const val PUBLISHER_ID = "pub-3133971041724338"
    const val PROD_APP_ID = "ca-app-pub-3133971041724338~3095353960"

    // Production Ad Unit IDs
    const val PROD_BANNER_ID = "ca-app-pub-3133971041724338/7363444026"
    const val PROD_INTERSTITIAL_ID = "ca-app-pub-3133971041724338/7664707251"
    const val PROD_REWARDED_ID = "ca-app-pub-3133971041724338/7960667830"
    const val PROD_NATIVE_ID = "ca-app-pub-3133971041724338/9979879405"
    const val PROD_APP_OPEN_ID = "ca-app-pub-3133971041724338/5038543915"

    // Official Google Test IDs (Used in debug builds per AdMob policy)
    const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"
    const val TEST_NATIVE_ID = "ca-app-pub-3940256099942544/2247696110"
    const val TEST_APP_OPEN_ID = "ca-app-pub-3940256099942544/9257395921"

    // Dynamic Selectors based on BuildConfig.DEBUG
    val bannerAdUnitId: String
        get() = if (BuildConfig.DEBUG) TEST_BANNER_ID else PROD_BANNER_ID

    val interstitialAdUnitId: String
        get() = if (BuildConfig.DEBUG) TEST_INTERSTITIAL_ID else PROD_INTERSTITIAL_ID

    val rewardedAdUnitId: String
        get() = if (BuildConfig.DEBUG) TEST_REWARDED_ID else PROD_REWARDED_ID

    val nativeAdUnitId: String
        get() = if (BuildConfig.DEBUG) TEST_NATIVE_ID else PROD_NATIVE_ID

    // Safety lock: never present interstitial or disruptive ad while conversion is active
    var isConversionActive: Boolean = false

    fun requestConsentAndInit(activity: Activity, onDone: () -> Unit = {}) {
        try {
            val params = ConsentRequestParameters.Builder().setTagForUnderAgeOfConsent(false).build()
            val consentInfo = UserMessagingPlatform.getConsentInformation(activity)

            consentInfo.requestConsentInfoUpdate(
                activity,
                params,
                {
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { error ->
                        if (error != null) {
                            Log.w(TAG, "Consent form error: ${error.message}")
                        }
                        if (consentInfo.canRequestAds()) {
                            initializeMobileAds(activity)
                        }
                        onDone()
                    }
                },
                { requestError ->
                    Log.w(TAG, "Consent info update failed: ${requestError.message}")
                    initializeMobileAds(activity)
                    onDone()
                }
            )
        } catch (e: Throwable) {
            Log.e(TAG, "Error during consent request: ${e.message}")
            initializeMobileAds(activity)
            onDone()
        }
    }

    private fun initializeMobileAds(context: Context) {
        try {
            MobileAds.initialize(context) { status ->
                Log.d(TAG, "Google Mobile Ads initialized successfully: ${status.adapterStatusMap.keys}")
                InterstitialAdManager.initialize(context)
            }
        } catch (e: Throwable) {
            Log.e(TAG, "MobileAds initialization error: ${e.message}")
        }
    }

    fun loadInterstitial(context: Context) {
        InterstitialAdManager.initialize(context)
        InterstitialAdManager.loadAd()
    }

    fun showInterstitialAtNaturalBreak(activity: Activity, onAdClosed: () -> Unit) {
        InterstitialAdManager.showInterstitialAtNaturalBreak(activity, onAdClosed)
    }
}
