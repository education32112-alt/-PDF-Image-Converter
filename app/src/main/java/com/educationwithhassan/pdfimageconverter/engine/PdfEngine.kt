package com.educationwithhassan.pdfimageconverter.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.multipdf.PDFMergerUtility
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.io.MemoryUsageSetting

object PdfEngine {

    /**
     * Converts a list of image URIs to a single multi-page PDF document.
     * Prevents OutOfMemoryError and crashes by:
     * 1. Safely resolving content:// URIs into local cache or reading via ContentResolver
     * 2. Calculating inSampleSize to bound memory consumption
     * 3. Reading and applying EXIF orientation
     * 4. Catching Throwable to handle OOM and stream errors gracefully
     */
    suspend fun convertImagesToPdf(
        context: Context,
        imageUris: List<Uri>,
        outputFile: File,
        pageSize: String = "A4", // A4, Letter, Fit
        marginDp: Int = 18,
        onProgress: (Int, String) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        if (imageUris.isEmpty()) {
            return@withContext Result.failure<File>(IllegalArgumentException("No images provided for PDF conversion."))
        }

        val pdfDocument = PdfDocument()
        val tempFilesToClean = mutableListOf<File>()

        try {
            val total = imageUris.size
            imageUris.forEachIndexed { index, uri ->
                onProgress((((index + 0.1f) / total) * 90).toInt(), "Processing image ${index + 1} of $total...")

                // Copy URI to a temporary cache file to ensure stable stream access and EXIF reading
                val tempFile = StorageManager.copyUriToTempFile(context, uri, "pdf_img_$index", ".tmp")
                if (tempFile == null || !tempFile.exists() || tempFile.length() == 0L) {
                    throw IllegalStateException("Failed to read image at index ${index + 1}. The file may be inaccessible or deleted.")
                }
                tempFilesToClean.add(tempFile)

                // 1. Measure dimensions without full decode to avoid memory spike
                val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(tempFile.absolutePath, boundsOptions)

                val origWidth = boundsOptions.outWidth
                val origHeight = boundsOptions.outHeight

                if (origWidth <= 0 || origHeight <= 0) {
                    throw IllegalArgumentException("Cannot decode image stream at index ${index + 1}: unsupported or corrupt format.")
                }

                // 2. Read EXIF orientation to correct camera photos
                var rotationAngle = 0f
                try {
                    val exif = ExifInterface(tempFile.absolutePath)
                    rotationAngle = when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                        else -> 0f
                    }
                } catch (_: Throwable) {
                    // Ignore EXIF failure, proceed with standard orientation
                }

                // 3. Compute sample size (target max dimension 2400px for crisp print quality without OOM)
                val maxTargetDim = 2400
                var sampleSize = 1
                while ((origWidth / sampleSize) > maxTargetDim || (origHeight / sampleSize) > maxTargetDim) {
                    sampleSize *= 2
                }

                val decodeOptions = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = Bitmap.Config.RGB_565 // 50% memory savings vs ARGB_8888
                }

                var rawBitmap: Bitmap? = null
                try {
                    rawBitmap = BitmapFactory.decodeFile(tempFile.absolutePath, decodeOptions)
                } catch (oom: OutOfMemoryError) {
                    // Retry with higher sample size if memory is critical
                    decodeOptions.inSampleSize = sampleSize * 2
                    rawBitmap = BitmapFactory.decodeFile(tempFile.absolutePath, decodeOptions)
                }

                val validRaw = rawBitmap ?: throw IllegalStateException("Failed to decode image bitmap at page ${index + 1}.")

                // Apply EXIF rotation if needed
                val bitmap: Bitmap = if (rotationAngle != 0f) {
                    try {
                        val matrix = Matrix().apply { postRotate(rotationAngle) }
                        val rotated = Bitmap.createBitmap(validRaw, 0, 0, validRaw.width, validRaw.height, matrix, true)
                        if (rotated != validRaw) {
                            validRaw.recycle()
                        }
                        rotated
                    } catch (_: Throwable) {
                        validRaw
                    }
                } else {
                    validRaw
                }

                try {
                    // Standard print dimensions in points (72 points = 1 inch)
                    val (pageWidth, pageHeight) = when (pageSize.uppercase()) {
                        "LETTER" -> 612 to 792
                        "FIT" -> bitmap.width to bitmap.height
                        else -> 595 to 842 // Standard A4
                    }

                    val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
                    val page = pdfDocument.startPage(pageInfo)
                    val canvas: Canvas = page.canvas

                    canvas.drawColor(Color.WHITE)

                    val availableWidth = (pageWidth - marginDp * 2).toFloat().coerceAtLeast(1f)
                    val availableHeight = (pageHeight - marginDp * 2).toFloat().coerceAtLeast(1f)
                    val scale = Math.min(availableWidth / bitmap.width.toFloat(), availableHeight / bitmap.height.toFloat())

                    val scaledWidth = bitmap.width * scale
                    val scaledHeight = bitmap.height * scale
                    val left = marginDp + (availableWidth - scaledWidth) / 2
                    val top = marginDp + (availableHeight - scaledHeight) / 2

                    val destRect = RectF(left, top, left + scaledWidth, top + scaledHeight)
                    canvas.drawBitmap(bitmap, null, destRect, null)

                    pdfDocument.finishPage(page)
                } finally {
                    bitmap.recycle()
                }
            }

            onProgress(95, "Finalizing PDF file...")
            val parentDir = outputFile.parentFile
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs()
            }

            FileOutputStream(outputFile).use { outStream ->
                pdfDocument.writeTo(outStream)
                outStream.flush()
            }

            onProgress(100, "Conversion complete!")
            Result.success(outputFile)
        } catch (e: Throwable) {
            Result.failure<File>(e)
        } finally {
            try {
                pdfDocument.close()
            } catch (_: Throwable) {}
            // Clean up temporary cache files
            tempFilesToClean.forEach {
                try { it.delete() } catch (_: Throwable) {}
            }
        }
    }

    /**
     * Merges multiple PDF files into one output file using PDFBox-Android
     */
    suspend fun mergePdfs(
        pdfFiles: List<File>,
        outputFile: File,
        onProgress: (Int, String) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        if (pdfFiles.size < 2) {
            return@withContext Result.failure<File>(IllegalArgumentException("At least 2 PDF documents are required to merge."))
        }

        try {
            onProgress(20, "Initializing PDF merger...")
            val merger = PDFMergerUtility()

            pdfFiles.forEachIndexed { idx, file ->
                if (!file.exists() || file.length() == 0L) {
                    throw IllegalArgumentException("Source file does not exist or is empty: ${file.name}")
                }
                onProgress(30 + ((idx / pdfFiles.size.toFloat()) * 50).toInt(), "Adding ${file.name}...")
                merger.addSource(file)
            }

            onProgress(85, "Merging documents...")
            if (!outputFile.parentFile?.exists()!!) {
                outputFile.parentFile?.mkdirs()
            }
            merger.setDestinationFileName(outputFile.absolutePath)
            merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly())

            onProgress(100, "Merge complete!")
            Result.success(outputFile)
        } catch (e: Throwable) {
            Result.failure<File>(e)
        }
    }

    /**
     * Splits a PDF and extracts selected pages into a new document using PDFBox-Android
     */
    suspend fun splitPdf(
        sourceFile: File,
        pagesToExtract: List<Int>, // 1-based indices
        outputFile: File,
        onProgress: (Int, String) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        if (!sourceFile.exists() || sourceFile.length() == 0L) {
            return@withContext Result.failure<File>(IllegalArgumentException("Source PDF file does not exist or is empty."))
        }

        try {
            onProgress(20, "Loading source document...")
            val result: Result<File> = PDDocument.load(sourceFile).use { donorDoc ->
                val totalPages = donorDoc.numberOfPages
                val validPages = pagesToExtract.filter { it in 1..totalPages }.distinct().sorted()

                if (validPages.isEmpty()) {
                    Result.failure<File>(IllegalArgumentException("No valid pages selected within document range (1..$totalPages)."))
                } else {
                    onProgress(50, "Extracting ${validPages.size} pages...")
                    PDDocument().use { newDoc ->
                        validPages.forEach { pageNum ->
                            newDoc.importPage(donorDoc.getPage(pageNum - 1))
                        }

                        onProgress(85, "Saving output PDF...")
                        if (!outputFile.parentFile?.exists()!!) {
                            outputFile.parentFile?.mkdirs()
                        }
                        newDoc.save(outputFile)
                    }

                    onProgress(100, "Extraction complete!")
                    Result.success(outputFile)
                }
            }
            result
        } catch (e: Throwable) {
            Result.failure<File>(e)
        }
    }
}
