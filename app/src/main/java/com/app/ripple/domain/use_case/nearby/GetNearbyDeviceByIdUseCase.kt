package com.app.ripple.domain.use_case.nearby

import com.app.ripple.data.local.contract.NearbyDevicePersistenceRepo
import com.app.ripple.data.nearby.model.NearbyDevice
import com.app.ripple.domain.model.NearbyDeviceDomain
import com.app.ripple.domain.model.toNearbyDeviceDomain
import com.app.ripple.domain.repo.NearbyDeviceRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetNearbyDeviceByIdUseCase @Inject constructor(val nearbyDeviceRepo: NearbyDeviceRepo){

    suspend operator fun invoke(id: String) : Flow<NearbyDeviceDomain?>{
        val device = nearbyDeviceRepo.getNearbyDeviceById(deviceId = id)
            .map { nearbyDevice ->
                nearbyDevice?.toNearbyDeviceDomain()
            }

        return device
    }
}