# Release & Signing Guide

This guide details the step-by-step procedure to build, sign, and submit **PDF & Image Converter** to the **Amazon Appstore** and **Google Play Console**.

---

## 1. Configured AdMob Monetization Units

- **Publisher ID:** `pub-3133971041724338`
- **AdMob App ID:** `ca-app-pub-3133971041724338~3095353960`
- **Banner Ad ID:** `ca-app-pub-3133971041724338/7363444026`
- **Interstitial Ad ID:** `ca-app-pub-3133971041724338/7664707251`
- **Rewarded Ad ID:** `ca-app-pub-3133971041724338/7960667830`
- **Native Advanced Ad ID:** `ca-app-pub-3133971041724338/9979879405`
- **App Open Ad ID:** `ca-app-pub-3133971041724338/5038543915`

Debug builds automatically use Google test IDs (`ca-app-pub-3940256099942544/...`) to prevent policy strikes. Release builds use your production IDs.

---

## 2. Secure Keystore Generation

Never commit your keystore passwords or keys to git. Run:
```bash
keytool -genkeypair -v -keystore pdfconverter-release.jks -alias pdfconverter -keyalg RSA -keysize 2048 -validity 10000 -storetype JKS
```

---

## 3. Assembling the Release Variant

Set your keystore environment variables:
```bash
export KEYSTORE_PATH="/path/to/pdfconverter-release.jks"
export KEYSTORE_PASSWORD="YourPassword"
export KEY_ALIAS="pdfconverter"
export KEY_PASSWORD="YourPassword"

./gradlew assembleRelease
```

Upload the generated APK to the Amazon Developer Portal under "Appstore Submissions" > "Upload APK".
