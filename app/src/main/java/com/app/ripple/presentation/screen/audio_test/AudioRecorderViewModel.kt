package com.app.ripple.presentation.screen.audio_test

import android.content.Context
import android.os.Build
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.ripple.data.audio_recording.AudioRecordingManager
import com.app.ripple.data.audio_recording.model.AudioRecording
import com.app.ripple.data.audio_recording.model.PlaybackState
import com.app.ripple.data.audio_recording.model.RecordingState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.State
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class AudioRecorderViewModel @Inject constructor(val audioManager: AudioRecordingManager) : ViewModel() {

    private val _recordingState = mutableStateOf(RecordingState.IDLE)
    val recordingState: State<RecordingState> = _recordingState

    private val _playbackState = mutableStateOf(PlaybackState.IDLE)
    val playbackState: State<PlaybackState> = _playbackState

    private val _recordings = mutableStateOf<List<AudioRecording>>(emptyList())
    val recordings: State<List<AudioRecording>> = _recordings

    private val _currentRecordingTime = mutableStateOf(0L)
    val currentRecordingTime: State<Long> = _currentRecordingTime

    private val _currentPlayingFile = mutableStateOf<String?>(null)
    val currentPlayingFile: State<String?> = _currentPlayingFile

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    init {
        loadRecordings()
    }

    fun startRecording() {
        viewModelScope.launch {
            audioManager.startRecording()
                .onSuccess {
                    _recordingState.value = RecordingState.RECORDING
                    startRecordingTimer()
                }
                .onFailure { error ->
                    _errorMessage.value = "Failed to start recording: ${error.message}"
                }
        }
    }

    fun stopRecording() {
        viewModelScope.launch {
            audioManager.stopRecording()
                .onSuccess { recording ->
                    _recordingState.value = RecordingState.STOPPED
                    _currentRecordingTime.value = 0L
                    loadRecordings()
                }
                .onFailure { error ->
                    _errorMessage.value = "Failed to stop recording: ${error.message}"
                }
        }
    }

    fun pauseRecording() {
        audioManager.pauseRecording()
            .onSuccess {
                _recordingState.value = RecordingState.PAUSED
            }
            .onFailure { error ->
                _errorMessage.value = "Failed to pause recording: ${error.message}"
            }
    }

    fun resumeRecording() {
        audioManager.resumeRecording()
            .onSuccess {
                _recordingState.value = RecordingState.RECORDING
            }
            .onFailure { error ->
                _errorMessage.value = "Failed to resume recording: ${error.message}"
            }
    }

    fun playRecording(recording: AudioRecording) {
        viewModelScope.launch {
            // Stop any current playback
            if (_playbackState.value == PlaybackState.PLAYING) {
                stopPlayback()
            }

            audioManager.startPlayback(recording.filePath)
                .onSuccess {
                    _playbackState.value = PlaybackState.PLAYING
                    _currentPlayingFile.value = recording.filePath
                    monitorPlayback(recording.filePath)
                }
                .onFailure { error ->
                    _errorMessage.value = "Failed to play recording: ${error.message}"
                }
        }
    }

    fun pausePlayback() {
        audioManager.pausePlayback()
            .onSuccess {
                _playbackState.value = PlaybackState.PAUSED
            }
            .onFailure { error ->
                _errorMessage.value = "Failed to pause playback: ${error.message}"
            }
    }

    fun resumePlayback() {
        audioManager.resumePlayback()
            .onSuccess {
                _playbackState.value = PlaybackState.PLAYING
            }
            .onFailure { error ->
                _errorMessage.value = "Failed to resume playback: ${error.message}"
            }
    }

    fun stopPlayback() {
        audioManager.stopPlayback()
            .onSuccess {
                _playbackState.value = PlaybackState.STOPPED
                _currentPlayingFile.value = null
            }
            .onFailure { error ->
                _errorMessage.value = "Failed to stop playback: ${error.message}"
            }
    }

    fun deleteRecording(recording: AudioRecording) {
        viewModelScope.launch {
            audioManager.deleteRecording(recording.filePath)
                .onSuccess {
                    loadRecordings()
                }
                .onFailure { error ->
                    _errorMessage.value = "Failed to delete recording: ${error.message}"
                }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun loadRecordings() {
        _recordings.value = audioManager.getAllRecordings()
    }

    private fun startRecordingTimer() {
        viewModelScope.launch {
            while (_recordingState.value == RecordingState.RECORDING) {
                delay(1000)
                _currentRecordingTime.value += 1000
            }
        }
    }

    private fun monitorPlayback(filePath: String) {
        viewModelScope.launch {
            while (_playbackState.value == PlaybackState.PLAYING && _currentPlayingFile.value == filePath) {
                if (!audioManager.isPlaying()) {
                    _playbackState.value = PlaybackState.STOPPED
                    _currentPlayingFile.value = null
                    break
                }
                delay(100)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioManager.release()
    }
}