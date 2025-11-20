package com.app.ripple.data.audio_recording

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import com.app.ripple.data.audio_recording.model.AudioRecording
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Audio Manager Class
class AudioRecordingManager private constructor(private val context: Context){

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: AudioRecordingManager? = null

        fun getInstance(context: Context): AudioRecordingManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AudioRecordingManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentRecordingFile: String? = null
    private var recordingStartTime: Long = 0L

    // Get app's internal storage directory
    private val recordingsDirectory: File
        get() {
            val dir = File(context.getExternalFilesDir(null), "recordings")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return dir
        }

    fun startRecording(): Result<String> {
        return try {
            val fileName = generateFileName()
            val filePath = File(recordingsDirectory, fileName).absolutePath
            currentRecordingFile = filePath
            recordingStartTime = System.currentTimeMillis()

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setOutputFile(filePath)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

                prepare()
                start()
            }

            Result.success(fileName)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun stopRecording(): Result<AudioRecording> {
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null

            val recordingFile = File(currentRecordingFile!!)
            val duration = System.currentTimeMillis() - recordingStartTime

            val recording = AudioRecording(
                fileName = recordingFile.name,
                filePath = recordingFile.absolutePath,
                duration = duration,
                size = recordingFile.length()
            )

            currentRecordingFile = null
            Result.success(recording)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun pauseRecording(): Result<Unit> {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder?.pause()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun resumeRecording(): Result<Unit> {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder?.resume()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun startPlayback(filePath: String): Result<Unit> {
        return try {
            stopPlayback() // Stop any existing playback

            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                start()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun pausePlayback(): Result<Unit> {
        return try {
            mediaPlayer?.pause()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun resumePlayback(): Result<Unit> {
        return try {
            mediaPlayer?.start()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun stopPlayback(): Result<Unit> {
        return try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
            mediaPlayer = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAllRecordings(): List<AudioRecording> {
        return recordingsDirectory.listFiles()?.filter { file ->
            file.isFile && file.extension in listOf("3gp", "mp3", "m4a", "aac")
        }?.map { file ->
            AudioRecording(
                fileName = file.name,
                filePath = file.absolutePath,
                createdAt = file.lastModified(),
                size = file.length()
            )
        }?.sortedByDescending { it.createdAt } ?: emptyList()
    }

    fun deleteRecording(filePath: String): Result<Unit> {
        return try {
            val file = File(filePath)
            if (file.exists() && file.delete()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete file"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true

    fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0

    fun getDuration(filePath: String): Int {
        return try {
            val mp = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
            }
            val duration = mp.duration
            mp.release()
            duration
        } catch (e: Exception) {
            0
        }
    }

    private fun generateFileName(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "recording_$timestamp.mp3"
    }

    fun release() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaPlayer?.release()
        } catch (e: Exception) {
            // Ignore exceptions during cleanup
        }
        mediaRecorder = null
        mediaPlayer = null
    }
}