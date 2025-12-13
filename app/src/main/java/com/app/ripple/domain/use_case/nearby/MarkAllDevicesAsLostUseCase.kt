package com.app.ripple.domain.use_case.nearby

import com.app.ripple.data.nearby.NearbyShareManager
import com.app.ripple.domain.repo.NearbyDeviceRepo
import javax.inject.Inject

class MarkAllDevicesAsLostUseCase @Inject constructor(
    val nearbyDeviceRepo: NearbyDeviceRepo,
    val nearbyShareManager: NearbyShareManager
) {
    suspend operator fun invoke(){
        nearbyShareManager.connectedDevices.collect {
            it.forEach { device ->
                nearbyShareManager.disconnectFromDevice(device.endpointId)
            }
        }
        nearbyDeviceRepo.markAllDevicesAsLost()
    }
}