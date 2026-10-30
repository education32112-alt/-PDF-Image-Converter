package com.educationwithhassan.pdfimageconverter.engine

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object StorageManager {

    fun getOutputDirectory(context: Context): File {
        val dir = File(context.filesDir, "converted_docs")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun generateUniqueFile(context: Context, prefix: String, extension: String): File {
        val dir = getOutputDirectory(context)
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return File(dir, "${prefix}_$timestamp.$extension")
    }

    fun shareFile(context: Context, file: File, mimeType: String = "application/pdf") {
        if (!file.exists()) return
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Share Document"))
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    /**
     * Safely copies content:// URI to a local temporary cache file.
     * Prevents stream expiry, repeated URI access failures, and permission errors.
     */
    fun copyUriToTempFile(context: Context, uri: Uri, prefix: String = "temp_file", extension: String = ".tmp"): File? {
        return try {
            val tempFile = File.createTempFile(prefix, extension, context.cacheDir)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                    outputStream.flush()
                }
            }
            if (tempFile.exists() && tempFile.length() > 0) tempFile else null
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Resolves human-readable file name from URI using OpenableColumns
     */
    fun getFileName(context: Context, uri: Uri): String {
        var name: String? = null
        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (index >= 0) {
                            name = cursor.getString(index)
                        }
                    }
                }
            } catch (_: Throwable) {}
        }
        if (name.isNullOrBlank()) {
            name = uri.lastPathSegment
        }
        return name ?: "Document"
    }

    /**
     * Resolves file size from URI in bytes
     */
    fun getFileSize(context: Context, uri: Uri): Long {
        var size: Long = 0L
        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val index = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (index >= 0) {
                            size = cursor.getLong(index)
                        }
                    }
                }
            } catch (_: Throwable) {}
        }
        return size
    }
}
