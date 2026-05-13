package com.app.ripple.presentation.screen.media_preview

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.app.ripple.util.UriHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MediaPreviewViewModel @Inject constructor(
    private val uriHelper: UriHelper
) : ViewModel() {

    fun getFileFromUri(uri: Uri): File?{
        return uriHelper.getFileFromUri(uri)
    }

    fun uriFileExists(uri: Uri): Boolean{
        return uriHelper.uriFileExists(uri)
    }
}