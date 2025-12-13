package com.app.ripple.background

import android.util.Log
import com.app.ripple.data.local.contract.NearbyDevicePersistenceRepo
import com.app.ripple.data.nearby.NearbyShareManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


import javax.inject.Inject


class ComprehensiveCleanupManager @Inject constructor(
    private val nearbyDevicePersistenceRepo: NearbyDevicePersistenceRepo,
    private val nearbyShareManager: NearbyShareManager
) {
    private val TAG = "ComprehensiveCleanupManager"

    suspend fun performCleanup(): Boolean{
        val job = coroutineScope {
            launch(Dispatchers.IO) {
                nearbyDevicePersistenceRepo.markAllDevicesAsLost()
            }

            nearbyShareManager.stopAllEndpoints()

            launch(Dispatchers.Default) {
                nearbyShareManager.stopDiscovery().collect {
                    Log.d(TAG, "performCleanup: stopDiscovery : $it")
                }
            }

            launch(Dispatchers.Default) {
                nearbyShareManager.stopAdvertising().collect {
                    Log.d(TAG, "performCleanup: stopAdvertising : $it")
                }
            }
        }

        job.join()

        return true
    }
}