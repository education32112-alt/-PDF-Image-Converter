# PDF & Image Converter - Native Android Application

**Publisher:** Rana Hassan  
**Package Name:** `com.educationwithhassan.pdfimageconverter`  
**Target:** Android Phones, Tablets, and Amazon Fire OS Devices  
**Min SDK:** 24 (Android 7.0) · **Compile SDK:** 35 (Android 15)  

---

## 🚀 Features

- **Images to PDF**: Select multiple JPG/PNG images and convert them into a structured PDF with custom page size (A4/Letter/Fit), margins, and orientation. OOM-protected bitmap downsampling.
- **PDF Viewer & Tools**: Fast client-side PDF previewing, page count inspection, and Android printing.
- **Merge PDFs**: Combine multiple PDF documents using PDFBox-Android.
- **Split PDF**: Extract selected page numbers or ranges into a standalone PDF.
- **Image Converter**: Convert between PNG, JPG, and WebP with transparency handling.
- **Image Resizer**: Custom dimension scaling with aspect ratio support.
- **Image Compressor**: Quality compression slider with live file size savings comparison.
- **100% On-Device Processing**: No files are uploaded to external servers.
- **AdMob Policy Compliant**: Debug builds use official Google test IDs; release builds use verified production IDs.

---

## 🛠 Build Instructions

### Prerequisites
- Android Studio Ladybug (2024.2.1+) or newer
- JDK 17
- Android SDK 35

### Compile Debug Build
```bash
./gradlew assembleDebug
# Windows: gradlew.bat assembleDebug
```

### Run Unit Tests
```bash
./gradlew test
# Windows: gradlew.bat test
```

### Build Signed Release APK
```bash
export KEYSTORE_PATH="keystore/release.jks"
export KEYSTORE_PASSWORD="your-secure-password"
export KEY_ALIAS="pdfconverter"
export KEY_PASSWORD="your-secure-password"

./gradlew assembleRelease
# Windows: gradlew.bat assembleRelease
```

The release APK will be generated at:
`app/build/outputs/apk/release/app-release.apk`
