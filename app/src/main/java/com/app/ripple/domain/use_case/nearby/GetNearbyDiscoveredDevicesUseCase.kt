package com.app.ripple.domain.use_case.nearby

import com.app.ripple.data.nearby.model.NearbyDevice
import com.app.ripple.domain.model.NearbyDeviceDomain
import com.app.ripple.domain.model.toNearbyDeviceDomain
import com.app.ripple.domain.repo.NearbyDeviceRepo
import com.app.ripple.domain.repo.NearbyShareRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetNearbyDiscoveredDevicesUseCase @Inject constructor(
    val nearbyDeviceRepo: NearbyDeviceRepo
){

    operator fun invoke() : Flow<List<NearbyDeviceDomain>> {
        val allDiscoveredDevices = nearbyDeviceRepo.getAllDiscoveredDevices()
            .map {
                it.map { nearbyDevice ->
                    nearbyDevice.toNearbyDeviceDomain()
                }
            }

        return allDiscoveredDevices
    }
}