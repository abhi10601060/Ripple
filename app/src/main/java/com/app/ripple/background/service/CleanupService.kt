package com.app.ripple.background.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.app.ripple.background.ComprehensiveCleanupManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class CleanupService: Service() {
    private val TAG = "AppKillDetectionService"

    @Inject lateinit var cleanupManager: ComprehensiveCleanupManager

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d(TAG, "onTaskRemoved: started")

        runBlocking {
            val ans = cleanupManager.performCleanup()
            Log.d(TAG, "onTaskRemoved: performed cleanup $ans")
            stopSelf()
        }

        Log.d(TAG, "onTaskRemoved: ended")
        super.onTaskRemoved(rootIntent)
    }
}