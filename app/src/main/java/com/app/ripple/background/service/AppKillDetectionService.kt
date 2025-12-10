package com.app.ripple.background.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.app.ripple.background.worker.OnAppCloseWorker
import com.app.ripple.domain.use_case.nearby.MarkAllDevicesAsLostUseCase
import com.app.ripple.domain.use_case.nearby.StopAdvertisingUseCase
import com.app.ripple.domain.use_case.nearby.StopDiscoveryUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class AppKillDetectionService: Service() {
    private val TAG = "AppKillDetectionService"

    @Inject lateinit var markAllDevicesAsLostUseCase: MarkAllDevicesAsLostUseCase
    @Inject lateinit var stopAdvertisingUseCase: StopAdvertisingUseCase
    @Inject lateinit var stopDiscoveryUseCase: StopDiscoveryUseCase

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d(TAG, "onTaskRemoved: started")

        val job1 = CoroutineScope(Dispatchers.Default).launch {
            stopAdvertisingUseCase.invoke().collect {
                Log.d(TAG, "onDestroy: stopAdvertisingUseCase : $it")
            }
        }

        val job2 = CoroutineScope(Dispatchers.Default).launch {
            stopDiscoveryUseCase.invoke().collect {
                Log.d(TAG, "onDestroy: stopDiscoveryUseCase : $it")
            }
        }

        val job3 = CoroutineScope(Dispatchers.Default).launch {
            markAllDevicesAsLostUseCase.invoke()
        }

        runBlocking {
            joinAll(job1, job2, job3)
        }

        Log.d(TAG, "onTaskRemoved: ended")
        super.onTaskRemoved(rootIntent)
    }
}