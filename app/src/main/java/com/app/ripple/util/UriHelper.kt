package com.app.ripple.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.annotation.RequiresApi
import java.io.File
import java.io.IOException

class UriHelper(val context: Context){

    fun getUriFromFilePath(path: String): Uri {
        val file = File(path)
        return Uri.fromFile(file)
    }

    fun getFileInfoFromUri(uri: Uri): FileInfo {
        var name = "unknown"
        var size = 0L
        var mimeType = "application/octet-stream"

        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)

                if (nameIndex >= 0) name = cursor.getString(nameIndex) ?: "unknown"
                if (sizeIndex >= 0) size = cursor.getLong(sizeIndex)
            }
        }

        mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"

        return FileInfo(name, size, mimeType)
    }

    fun getFileFromUri(uri: Uri): File? {
        if (uri.scheme == "file") {
            return uri.path?.let { File(it) }
        }

        val fileInfo = getFileInfoFromUri(uri)
        val tempFile = File(context.cacheDir, fileInfo.name)

        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                tempFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun uriFileExists(uri: Uri): Boolean {
        return try {
            when (uri.scheme) {
                "content" -> {
                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        cursor.count > 0
                    } ?: false
                }
                "file" -> {
                    uri.path?.let { File(it).exists() } ?: false
                }
                else -> {
                    context.contentResolver.openInputStream(uri)?.use { true } ?: false
                }
            }
        } catch (e: Exception) {
            false
        }
    }
}
data class FileInfo(val name: String, val size: Long, val mimeType: String)

