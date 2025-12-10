package com.app.ripple.background.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.app.ripple.domain.use_case.nearby.MarkAllDevicesAsLostUseCase
import com.app.ripple.domain.use_case.nearby.StopAdvertisingUseCase
import com.app.ripple.domain.use_case.nearby.StopDiscoveryUseCase
import javax.inject.Inject

class CustomWorkerFactory @Inject constructor(
    private val stopDiscoveryUseCase: StopDiscoveryUseCase,
    private val stopAdvertisingUseCase: StopAdvertisingUseCase,
    private val markAllDevicesAsLostUseCase: MarkAllDevicesAsLostUseCase
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return if (workerClassName == OnAppCloseWorker::class.java.name) {
            OnAppCloseWorker(
                appContext,
                workerParameters,
                stopDiscoveryUseCase,
                stopAdvertisingUseCase,
                markAllDevicesAsLostUseCase
            )
        } else {
            null
        }
    }
}