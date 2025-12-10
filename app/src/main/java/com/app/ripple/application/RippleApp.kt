package com.app.ripple.application

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.app.ripple.background.service.AppKillDetectionService
import com.app.ripple.background.worker.CustomWorkerFactory
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class RippleApp : Application(), Configuration.Provider {
    private val TAG = "RippleApp"

    @Inject lateinit var workerFactory: CustomWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        startAppKillDetectionService()
    }

    private fun startAppKillDetectionService() {
        Log.d(TAG, "startAppKillDetectionService: called")
        startService(Intent(this, AppKillDetectionService::class.java))
    }
}