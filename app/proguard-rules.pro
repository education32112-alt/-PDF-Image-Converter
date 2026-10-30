# ProGuard Rules for PDF & Image Converter
# Package: com.educationwithhassan.pdfimageconverter

# Keep ViewModel & AndroidViewModel
-keepclassmembers class * extends androidx.lifecycle.ViewModel { *; }
-keepclassmembers class * extends androidx.lifecycle.AndroidViewModel { *; }

# PDFBox Android rules
-keep class com.tom_roush.pdfbox.** { *; }
-dontwarn com.tom_roush.pdfbox.**
-dontwarn org.bouncycastle.**

# AdMob & Google Play Services rules
-keep public class com.google.android.gms.ads.** { public *; }
-keep public class com.google.ads.** { public *; }
-keep class com.google.android.gms.ads.mediation.** { *; }
-keep class com.google.ads.mediation.** { *; }
-dontwarn com.google.android.gms.ads.**
-dontwarn com.google.ads.**

# AdMob FullScreenContentCallback & Ad Load Callbacks
-keep class com.google.android.gms.ads.FullScreenContentCallback { *; }
-keep class com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback { *; }
-keep class com.google.android.gms.ads.rewarded.RewardedAdLoadCallback { *; }
-keep class com.google.android.gms.ads.AdLoadCallback { *; }
-keep class com.google.android.gms.ads.AdListener { *; }

# Google UMP (User Messaging Platform) Consent
-keep class com.google.android.ump.** { *; }
-dontwarn com.google.android.ump.**

# Keep FileProvider
-keep class androidx.core.content.FileProvider { *; }
