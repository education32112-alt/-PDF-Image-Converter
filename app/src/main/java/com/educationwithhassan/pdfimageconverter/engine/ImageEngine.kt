package com.educationwithhassan.pdfimageconverter.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ImageEngine {

    suspend fun processImage(
        context: Context,
        inputUri: Uri,
        outputFile: File,
        format: Bitmap.CompressFormat, // JPEG, PNG, WEBP
        quality: Int, // 10 to 100
        targetWidth: Int? = null,
        targetHeight: Int? = null
    ): Result<File> = withContext(Dispatchers.IO) {
        var tempSourceFile: File? = null
        var decodedBitmap: Bitmap? = null
        var finalBitmap: Bitmap? = null
        try {
            // Safely copy URI to temp file to read bounds and EXIF reliably
            tempSourceFile = StorageManager.copyUriToTempFile(context, inputUri, "img_proc", ".tmp")
            if (tempSourceFile == null || !tempSourceFile.exists()) {
                return@withContext Result.failure(IllegalStateException("Could not access selected image file."))
            }

            // 1. Measure dimensions first without memory allocation
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(tempSourceFile.absolutePath, boundsOptions)

            val origWidth = boundsOptions.outWidth
            val origHeight = boundsOptions.outHeight
            if (origWidth <= 0 || origHeight <= 0) {
                return@withContext Result.failure(IllegalArgumentException("Unsupported image format or corrupt file."))
            }

            // 2. Read EXIF orientation
            var rotationAngle = 0f
            try {
                val exif = ExifInterface(tempSourceFile.absolutePath)
                rotationAngle = when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } catch (_: Throwable) {}

            // 3. Memory-Safe Downsampling: Cap standard dimensions to 4096px to prevent OOM
            val MAX_SAFE_DIM = 4096
            val effectiveReqW = targetWidth ?: origWidth.coerceAtMost(MAX_SAFE_DIM)
            val effectiveReqH = targetHeight ?: origHeight.coerceAtMost(MAX_SAFE_DIM)

            var sampleSize = 1
            while ((origWidth / sampleSize) > (effectiveReqW * 1.2) || (origHeight / sampleSize) > (effectiveReqH * 1.2)) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            try {
                decodedBitmap = BitmapFactory.decodeFile(tempSourceFile.absolutePath, decodeOptions)
            } catch (oom: OutOfMemoryError) {
                // Secondary recovery: halve memory usage by switching to RGB_565 and increasing sample size
                try {
                    decodeOptions.inSampleSize = sampleSize * 2
                    decodeOptions.inPreferredConfig = Bitmap.Config.RGB_565
                    decodedBitmap = BitmapFactory.decodeFile(tempSourceFile.absolutePath, decodeOptions)
                } catch (fatalOom: OutOfMemoryError) {
                    return@withContext Result.failure(OutOfMemoryError("Image is too large to process on this device. Please select a smaller image."))
                }
            }

            val validDecoded = decodedBitmap ?: return@withContext Result.failure(IllegalStateException("Failed to decode image data."))

            // Rotate if EXIF dictates
            val orientedBitmap: Bitmap = if (rotationAngle != 0f) {
                try {
                    val matrix = Matrix().apply { postRotate(rotationAngle) }
                    val rotated = Bitmap.createBitmap(validDecoded, 0, 0, validDecoded.width, validDecoded.height, matrix, true)
                    if (rotated != validDecoded) {
                        validDecoded.recycle()
                    }
                    rotated
                } catch (_: Throwable) {
                    validDecoded
                }
            } else {
                validDecoded
            }
            decodedBitmap = orientedBitmap

            // 4. Precise scale if target requested
            val scaledBitmap: Bitmap = if (targetWidth != null && targetHeight != null &&
                (orientedBitmap.width != targetWidth || orientedBitmap.height != targetHeight)) {
                Bitmap.createScaledBitmap(orientedBitmap, targetWidth, targetHeight, true)
            } else {
                orientedBitmap
            }
            finalBitmap = scaledBitmap

            val parentDir = outputFile.parentFile
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs()
            }

            FileOutputStream(outputFile).use { outStream ->
                scaledBitmap.compress(format, quality.coerceIn(10, 100), outStream)
                outStream.flush()
            }

            Result.success(outputFile)
        } catch (e: Throwable) {
            Result.failure(e)
        } finally {
            try {
                val toRecycleFinal = finalBitmap
                val toRecycleDecoded = decodedBitmap
                if (toRecycleFinal != null && toRecycleFinal != toRecycleDecoded && !toRecycleFinal.isRecycled) {
                    toRecycleFinal.recycle()
                }
            } catch (_: Throwable) {}
            try {
                val toRecycleDecoded = decodedBitmap
                if (toRecycleDecoded != null && !toRecycleDecoded.isRecycled) {
                    toRecycleDecoded.recycle()
                }
            } catch (_: Throwable) {}
            try { tempSourceFile?.delete() } catch (_: Throwable) {}
        }
    }
}
