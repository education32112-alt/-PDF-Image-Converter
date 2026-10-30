package com.educationwithhassan.pdfimageconverter.admob

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.util.concurrent.atomic.AtomicInteger

/**
 * Centralized, automatic Google AdMob Interstitial Ads management system.
 * 
 * Strict Policy & UX Adherence:
 * 1. Background Preloading: Preloads the next interstitial immediately and re-requests upon dismissal.
 * 2. Central Navigation Event Counter: Considers showing an interstitial after every 3 successful navigation actions.
 * 3. Minimum Interval: Enforces a strict minimum 90-second cooldown between interstitial displays.
 * 4. Non-Blocking Navigation: If an ad is not ready, navigation executes immediately without delay.
 * 5. Lifecycle & OOM Safety: Prevents memory leaks and checks Activity validity (!isFinishing && !isDestroyed).
 * 6. Protection of Critical Tasks: Never displays during file picking, PDF conversion, merging, splitting, saving or viewing.
 * 7. Duplicate Prevention: Locks presentation state and clears cached reference before invoking show().
 */
object InterstitialAdManager {

    private const val TAG = "InterstitialAdManager"

    // Configuration rules per brief
    const val ACTIONS_BETWEEN_ADS = 3
    const val MIN_INTERVAL_BETWEEN_ADS_MS = 90_000L // 90 seconds minimum cooldown

    @Volatile
    private var interstitialAd: InterstitialAd? = null

    @Volatile
    private var isLoading = false

    @Volatile
    private var isShowingAd = false

    @Volatile
    private var lastAdShowTimeMs: Long = 0L

    private val navigationActionCount = AtomicInteger(0)
    private var appContext: Context? = null

    val isAdAvailable: Boolean
        get() = interstitialAd != null && !isShowingAd

    /**
     * Initializes the manager with Application Context.
     * Starts the 90-second quiet period timer from launch to prevent ads upon startup.
     */
    fun initialize(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
            // Start quiet cooldown timer at app launch
            lastAdShowTimeMs = SystemClock.elapsedRealtime()
            loadAd()
        }
    }

    /**
     * Preloads the next interstitial ad in the background.
     */
    @Synchronized
    fun loadAd() {
        val context = appContext ?: return
        if (isLoading || interstitialAd != null) {
            return
        }

        isLoading = true
        Log.d(TAG, "Preloading interstitial ad in background (Unit: ${AdMobManager.interstitialAdUnitId})...")

        try {
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context,
                AdMobManager.interstitialAdUnitId,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        Log.d(TAG, "Interstitial ad loaded and cached successfully.")
                        interstitialAd = ad
                        isLoading = false
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.w(TAG, "Interstitial ad failed to load: ${loadAdError.message} (code: ${loadAdError.code})")
                        interstitialAd = null
                        isLoading = false
                    }
                }
            )
        } catch (e: Throwable) {
            Log.e(TAG, "Exception during interstitial preload: ${e.message}")
            interstitialAd = null
            isLoading = false
        }
    }

    /**
     * Automatic Navigation Hook:
     * Increments the navigation counter thread-safely.
     * Evaluates policy criteria:
     * - Count >= 3
     * - Elapsed time >= 90 seconds
     * - No active file conversion/processing (AdMobManager.isConversionActive == false)
     * - Ad loaded and available
     * - Activity is valid and alive (!isFinishing && !isDestroyed)
     * If eligible, presents the interstitial and executes onContinue() upon dismissal.
     * If ineligible or ad is not ready, executes onContinue() immediately with ZERO delay.
     */
    fun onNavigationAction(activity: Activity?, onContinue: () -> Unit) {
        val count = navigationActionCount.incrementAndGet()
        val now = SystemClock.elapsedRealtime()
        val timeSinceLastAd = now - lastAdShowTimeMs

        Log.d(TAG, "Navigation action #$count recorded (elapsed since last ad: ${timeSinceLastAd / 1000}s)")

        val isThresholdReached = count >= ACTIONS_BETWEEN_ADS
        val isIntervalSatisfied = timeSinceLastAd >= MIN_INTERVAL_BETWEEN_ADS_MS
        val isOperationSafe = !AdMobManager.isConversionActive
        val isActivityValid = activity != null && !activity.isFinishing && !activity.isDestroyed

        val canShowAd = isThresholdReached &&
                isIntervalSatisfied &&
                isOperationSafe &&
                !isShowingAd &&
                interstitialAd != null &&
                isActivityValid

        if (!canShowAd) {
            // Non-blocking: continue navigation instantly without waiting
            onContinue()
            if (interstitialAd == null && !isLoading) {
                loadAd()
            }
            return
        }

        val adToShow: InterstitialAd
        synchronized(this) {
            val ad = interstitialAd
            if (ad == null || isShowingAd) {
                onContinue()
                loadAd()
                return
            }
            isShowingAd = true
            adToShow = ad
            interstitialAd = null // Invalidate cached ad immediately to prevent duplicate display
        }

        adToShow.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Interstitial dismissed. Resuming navigation.")
                isShowingAd = false
                lastAdShowTimeMs = SystemClock.elapsedRealtime()
                navigationActionCount.set(0)

                // Resume navigation smoothly
                onContinue()

                // Preload next ad in background
                loadAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.w(TAG, "Interstitial failed to display: ${adError.message}. Continuing navigation immediately.")
                isShowingAd = false

                // Do not block navigation on show failure
                onContinue()

                loadAd()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Interstitial ad showed on screen.")
            }
        }

        try {
            adToShow.show(activity!!)
        } catch (e: Throwable) {
            Log.e(TAG, "Error displaying interstitial: ${e.message}")
            isShowingAd = false
            onContinue()
            loadAd()
        }
    }

    /**
     * Shows ad at natural completion of an event if available
     */
    fun showInterstitialAtNaturalBreak(activity: Activity?, onAdClosed: () -> Unit) {
        if (AdMobManager.isConversionActive || isShowingAd || activity == null || activity.isFinishing || activity.isDestroyed) {
            onAdClosed()
            return
        }

        val adToShow: InterstitialAd
        synchronized(this) {
            val ad = interstitialAd ?: run {
                onAdClosed()
                return
            }
            isShowingAd = true
            adToShow = ad
            interstitialAd = null
        }

        adToShow.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                isShowingAd = false
                lastAdShowTimeMs = SystemClock.elapsedRealtime()
                navigationActionCount.set(0)
                loadAd()
                onAdClosed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                isShowingAd = false
                loadAd()
                onAdClosed()
            }
        }
        try {
            adToShow.show(activity)
        } catch (_: Throwable) {
            isShowingAd = false
            onAdClosed()
        }
    }

    // Inspection & testing methods
    fun getNavigationActionCount(): Int = navigationActionCount.get()
    fun getLastAdShowTimeMs(): Long = lastAdShowTimeMs
    fun setNavigationActionCount(count: Int) { navigationActionCount.set(count) }
    fun setLastAdShowTimeMs(time: Long) { lastAdShowTimeMs = time }
    fun resetForTesting() {
        navigationActionCount.set(0)
        lastAdShowTimeMs = 0L
        isShowingAd = false
        isLoading = false
        interstitialAd = null
    }

    fun setAdAvailableForTesting(ad: InterstitialAd?) {
        interstitialAd = ad
    }
}
