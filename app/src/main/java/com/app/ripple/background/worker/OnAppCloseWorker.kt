package com.app.ripple.background.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.app.ripple.domain.use_case.nearby.MarkAllDevicesAsLostUseCase
import com.app.ripple.domain.use_case.nearby.StopAdvertisingUseCase
import com.app.ripple.domain.use_case.nearby.StopDiscoveryUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltWorker
class OnAppCloseWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    val stopDiscoveryUseCase: StopDiscoveryUseCase,
    val stopAdvertisingUseCase: StopAdvertisingUseCase,
    val markAllDevicesAsLostUseCase: MarkAllDevicesAsLostUseCase
) : Worker(context, params) {

    private val TAG = "OnAppCloseWorker"

//    @Inject lateinit var markAllDevicesAsLostUseCase: MarkAllDevicesAsLostUseCase
//    @Inject lateinit var stopAdvertisingUseCase: StopAdvertisingUseCase
//    @Inject lateinit var stopDiscoveryUseCase: StopAdvertisingUseCase

//    override suspend fun doWork(): Result {
//
//        Log.d(TAG, "doWork: called")
//
//        val job1 = CoroutineScope(Dispatchers.Default).launch {
//            stopAdvertisingUseCase.invoke().collect {
//                Log.d(TAG, "onDestroy: stopAdvertisingUseCase : $it")
//            }
//        }
//
//        val job2 = CoroutineScope(Dispatchers.Default).launch {
//            stopDiscoveryUseCase.invoke().collect {
//                Log.d(TAG, "onDestroy: stopDiscoveryUseCase : $it")
//            }
//        }
//
//        markAllDevicesAsLostUseCase.invoke()
//
//        joinAll(job1, job2)
//
//        Log.d("OnAppCloseWorker", "doWork: work done")
//        return Result.success()
//    }

    override fun doWork(): Result {
        Log.d(TAG, "doWork: called")

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

        Log.d("OnAppCloseWorker", "doWork: work done")
        return Result.success()
    }
}