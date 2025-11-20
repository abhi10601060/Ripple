package com.app.ripple.data.audio_recording.model

data class AudioRecording(
    val fileName: String,
    val filePath: String,
    val duration: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val size: Long = 0L
)

enum class RecordingState {
    IDLE,
    RECORDING,
    PAUSED,
    STOPPED
}

enum class PlaybackState {
    IDLE,
    PLAYING,
    PAUSED,
    STOPPED
}
