package com.app.ripple.presentation.screen.media_preview

import android.R
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil3.compose.AsyncImage
import com.app.ripple.domain.model.MessageDomain
import java.io.File
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.ripple.presentation.shared.Ripple
import com.app.ripple.presentation.ui.theme.DarkBG

@Composable
fun MediaPreViewScreen(
    modifier: Modifier = Modifier,
    message: MessageDomain,
    isSentFromCurrentUser: Boolean = false,
    onBackClick: () -> Unit = {},
    viewModel: MediaPreviewViewModel = hiltViewModel()
) {
    val rippleFolder = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        "Ripple"
    )

    val mediaUri = remember(message, isSentFromCurrentUser) {
        if (isSentFromCurrentUser && message.content.isNotEmpty()) {
            message.content.toUri()
        } else {
            Uri.fromFile(File(rippleFolder, message.fileName))
        }
    }

    val mediaFile by remember {
        derivedStateOf { viewModel.getFileFromUri(mediaUri) }
    }
    
    val mimeType = message.mimeType.lowercase()
    val fileName = message.fileName.lowercase()

    val isImage = mimeType.contains("image") || 
            fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || 
            fileName.endsWith(".png") || fileName.endsWith(".webp") || 
            fileName.endsWith(".gif")
            
    val isVideo = mimeType.contains("video") || 
            fileName.endsWith(".mp4") || fileName.endsWith(".mkv") || 
            fileName.endsWith(".webm") || fileName.endsWith(".3gp")
            
    val isAudio = mimeType.contains("audio") || 
            fileName.endsWith(".mp3") || fileName.endsWith(".wav") || 
            fileName.endsWith(".m4a") || fileName.endsWith(".aac")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = DarkBG)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { /* Consume clicks to prevent propagation to ChatScreen */ }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Ripple(size = 50.dp) {
                IconButton(onClick = {
                    onBackClick()
                }) {
                    Icon(
                        modifier = Modifier.padding(2.dp).size(15.dp),
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
            }

            Text(
                text = message.fileName,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isSentFromCurrentUser && viewModel.uriFileExists(mediaUri)) {
                FileNotFoundUI(message.fileName)
            } else {
                when {
                    isImage -> ImagePreview(mediaUri)
                    isVideo -> VideoPreview(mediaUri)
                    isAudio -> AudioPreview(mediaUri, message.fileName)
                    else -> UnsupportedFileUI()
                }
            }
        }
    }
}

@Composable
fun ImagePreview(uri: Uri) {
    AsyncImage(
        model = uri,
        contentDescription = "Image Preview",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Fit
    )
}

@Composable
fun VideoPreview(uri: Uri) {
    AndroidView(
        factory = { context ->
            VideoView(context).apply {
                setVideoURI(uri)
                val controller = MediaController(context)
                controller.setAnchorView(this)
                setMediaController(controller)
                start()
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun AudioPreview(uri: Uri, fileName: String) {
    val context = LocalContext.current
    val mediaPlayer = remember { MediaPlayer() }
    var isPlaying by remember { mutableStateOf(false) }

    DisposableEffect(uri) {
        try {
            mediaPlayer.setDataSource(context, uri)
            mediaPlayer.prepare()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        onDispose {
            mediaPlayer.release()
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Default.Audiotrack,
            contentDescription = "Audio File",
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = fileName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if (isPlaying) {
                mediaPlayer.pause()
            } else {
                mediaPlayer.start()
            }
            isPlaying = !isPlaying
        }) {
            Text(if (isPlaying) "Pause" else "Play")
        }
    }
}

@Composable
fun FileNotFoundUI(fileName: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = "File Not Found",
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "File not found",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
        Text(
            text = "Path: Download/Ripple/$fileName",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun UnsupportedFileUI() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = "Unsupported File",
            modifier = Modifier.size(100.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Unable to process file",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "This file type is not supported for preview",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MediaPreviewScreenPrev() {
    MediaPreViewScreen(message = MessageDomain.mockFileMetadataMessage)
}
