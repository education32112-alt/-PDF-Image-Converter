package com.educationwithhassan.pdfimageconverter

import com.educationwithhassan.pdfimageconverter.admob.AdMobManager
import com.educationwithhassan.pdfimageconverter.admob.InterstitialAdManager
import org.junit.Assert.*
import org.junit.Test

class AdMobPolicyTest {

    @Test
    fun testOfficialGoogleTestIdsConfigured() {
        assertEquals("ca-app-pub-3940256099942544/6300978111", AdMobManager.TEST_BANNER_ID)
        assertEquals("ca-app-pub-3940256099942544/1033173712", AdMobManager.TEST_INTERSTITIAL_ID)
        assertEquals("ca-app-pub-3940256099942544/5224354917", AdMobManager.TEST_REWARDED_ID)
        assertEquals("ca-app-pub-3940256099942544/2247696110", AdMobManager.TEST_NATIVE_ID)
        assertEquals("ca-app-pub-3940256099942544/9257395921", AdMobManager.TEST_APP_OPEN_ID)
    }

    @Test
    fun testProductionAdUnitsRegistered() {
        assertEquals("pub-3133971041724338", AdMobManager.PUBLISHER_ID)
        assertEquals("ca-app-pub-3133971041724338/7363444026", AdMobManager.PROD_BANNER_ID)
        assertEquals("ca-app-pub-3133971041724338/7664707251", AdMobManager.PROD_INTERSTITIAL_ID)
        assertEquals("ca-app-pub-3133971041724338/7960667830", AdMobManager.PROD_REWARDED_ID)
        assertEquals("ca-app-pub-3133971041724338/9979879405", AdMobManager.PROD_NATIVE_ID)
        assertEquals("ca-app-pub-3133971041724338/5038543915", AdMobManager.PROD_APP_OPEN_ID)
    }

    @Test
    fun testConversionActiveSafetyLock() {
        AdMobManager.isConversionActive = true
        assertTrue(AdMobManager.isConversionActive)
        // Reset state
        AdMobManager.isConversionActive = false
        assertFalse(AdMobManager.isConversionActive)
    }

    @Test
    fun testInterstitialNavigationRules() {
        InterstitialAdManager.resetForTesting()
        assertEquals(3, InterstitialAdManager.ACTIONS_BETWEEN_ADS)
        assertEquals(90_000L, InterstitialAdManager.MIN_INTERVAL_BETWEEN_ADS_MS)

        // Actions 1 and 2 should not trigger ads
        var executed = false
        InterstitialAdManager.onNavigationAction(null) { executed = true }
        assertTrue(executed)
        assertEquals(1, InterstitialAdManager.getNavigationActionCount())

        executed = false
        InterstitialAdManager.onNavigationAction(null) { executed = true }
        assertTrue(executed)
        assertEquals(2, InterstitialAdManager.getNavigationActionCount())

        executed = false
        InterstitialAdManager.onNavigationAction(null) { executed = true }
        assertTrue(executed)
        assertEquals(3, InterstitialAdManager.getNavigationActionCount())
    }

    @Test
    fun testConversionActiveBlocksInterstitial() {
        InterstitialAdManager.resetForTesting()
        InterstitialAdManager.setNavigationActionCount(3)
        AdMobManager.isConversionActive = true

        var navigated = false
        // Even with count at 3, if conversion is active, ad is blocked and navigation resumes immediately
        InterstitialAdManager.onNavigationAction(null) { navigated = true }
        assertTrue(navigated)

        AdMobManager.isConversionActive = false
    }
}
