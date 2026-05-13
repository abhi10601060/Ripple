package com.app.ripple.util

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.annotation.RequiresApi
import java.io.File

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
}
data class FileInfo(val name: String, val size: Long, val mimeType: String)

