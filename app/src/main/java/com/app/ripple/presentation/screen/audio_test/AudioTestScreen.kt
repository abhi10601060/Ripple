package com.app.ripple.presentation.screen.audio_test

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.ripple.data.audio_recording.model.AudioRecording
import com.app.ripple.data.audio_recording.model.PlaybackState
import com.app.ripple.data.audio_recording.model.RecordingState
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Main Compose Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioRecorderScreen(
    modifier: Modifier = Modifier,
    viewModel: AudioRecorderViewModel = hiltViewModel()
) {
    val TAG = "AudioRecorderScreen"
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { res ->
        Log.d(TAG, "AudioRecorderScreen: granted audio permission : $res")
    }

    val recordingState by viewModel.recordingState
    val playbackState by viewModel.playbackState
    val recordings by viewModel.recordings
    val currentRecordingTime by viewModel.currentRecordingTime
    val currentPlayingFile by viewModel.currentPlayingFile
    val errorMessage by viewModel.errorMessage

    // Show error messages
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            // In a real app, you might want to show a Snackbar here
            println("Error: $it")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Audio Recorder",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Recording Section
        RecordingControlSection(
            recordingState = recordingState,
            currentRecordingTime = currentRecordingTime,
            onStartRecording = {
                if(ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){
                    viewModel.startRecording()
                }
                else{
                    permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                }
            },
            onStopRecording = viewModel::stopRecording,
            onPauseRecording = viewModel::pauseRecording,
            onResumeRecording = viewModel::resumeRecording
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Recordings List
        Text(
            text = "Recordings (${recordings.size})",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (recordings.isEmpty()) {
            EmptyRecordingsState()
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(recordings) { _,recording ->
                    RecordingItem(
                        recording = recording,
                        isPlaying = currentPlayingFile == recording.filePath && playbackState == PlaybackState.PLAYING,
                        isPaused = currentPlayingFile == recording.filePath && playbackState == PlaybackState.PAUSED,
                        onPlay = { viewModel.playRecording(recording) },
                        onPause = viewModel::pausePlayback,
                        onResume = viewModel::resumePlayback,
                        onStop = viewModel::stopPlayback,
                        onDelete = { viewModel.deleteRecording(recording) }
                    )
                }
            }
        }
    }

    // Clear error message after showing
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            delay(3000)
            viewModel.clearError()
        }
    }
}

@Composable
fun RecordingControlSection(
    recordingState: RecordingState,
    currentRecordingTime: Long,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onPauseRecording: () -> Unit,
    onResumeRecording: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (recordingState) {
                RecordingState.RECORDING -> Color(0xFFFFE0E0)
                RecordingState.PAUSED -> Color(0xFFFFF3E0)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Recording Status
            Text(
                text = when (recordingState) {
                    RecordingState.IDLE -> "Ready to Record"
                    RecordingState.RECORDING -> "Recording..."
                    RecordingState.PAUSED -> "Recording Paused"
                    RecordingState.STOPPED -> "Recording Stopped"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = when (recordingState) {
                    RecordingState.RECORDING -> Color(0xFFD32F2F)
                    RecordingState.PAUSED -> Color(0xFFFF9800)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Recording Time
            if (recordingState != RecordingState.IDLE) {
                Text(
                    text = formatTime(currentRecordingTime),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (recordingState == RecordingState.RECORDING) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Control Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (recordingState) {
                    RecordingState.IDLE -> {
                        RecordButton(
                            onClick = onStartRecording,
                            enabled = true
                        )
                    }
                    RecordingState.RECORDING -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            PauseButton(onClick = onPauseRecording)
                        }
                        StopButton(onClick = onStopRecording)
                    }
                    RecordingState.PAUSED -> {
                        ResumeButton(onClick = onResumeRecording)
                        StopButton(onClick = onStopRecording)
                    }
                    RecordingState.STOPPED -> {
                        RecordButton(
                            onClick = onStartRecording,
                            enabled = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecordButton(
    onClick: () -> Unit,
    enabled: Boolean
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier.size(64.dp),
        containerColor = Color(0xFFD32F2F),
        contentColor = Color.White
    ) {
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = "Start Recording",
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun PauseButton(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        containerColor = Color(0xFFFF9800),
        contentColor = Color.White
    ) {
        Icon(
            imageVector = Icons.Default.Pause,
            contentDescription = "Pause Recording",
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun ResumeButton(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        containerColor = Color(0xFF4CAF50),
        contentColor = Color.White
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Resume Recording",
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun StopButton(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        containerColor = Color(0xFF757575),
        contentColor = Color.White
    ) {
        Icon(
            imageVector = Icons.Default.Stop,
            contentDescription = "Stop Recording",
            modifier = Modifier.size(24.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingItem(
    recording: AudioRecording,
    isPlaying: Boolean,
    isPaused: Boolean,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) Color(0xFFE8F5E8) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Recording Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AudioFile,
                    contentDescription = null,
                    tint = if (isPlaying) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.run { width(12.dp) })

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recording.fileName.substringBeforeLast("."),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formatDate(recording.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${formatFileSize(recording.size)} • ${formatDuration(recording.duration)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status indicator
                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50))
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Control Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when {
                    isPlaying -> {
                        IconButton(onClick = onPause) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = "Pause",
                                tint = Color(0xFF4CAF50)
                            )
                        }
                        IconButton(onClick = onStop) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop",
                                tint = Color(0xFF757575)
                            )
                        }
                    }
                    isPaused -> {
                        IconButton(onClick = onResume) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Resume",
                                tint = Color(0xFF4CAF50)
                            )
                        }
                        IconButton(onClick = onStop) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop",
                                tint = Color(0xFF757575)
                            )
                        }
                    }
                    else -> {
                        IconButton(onClick = onPlay) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color(0xFF4CAF50)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFD32F2F)
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Recording") },
            text = { Text("Are you sure you want to delete this recording? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFD32F2F))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun EmptyRecordingsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.MicNone,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No recordings yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "Tap the record button to create your first recording",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

// Utility Functions
@SuppressLint("DefaultLocale")
private fun formatTime(timeMs: Long): String {
    val totalSeconds = timeMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${bytes / (1024 * 1024)} MB"
    }
}

private fun formatDuration(durationMs: Long): String {
    return formatTime(durationMs)
}

@Preview
@Composable
private fun AudioScreenPrev() {
    AudioRecorderScreen()
}