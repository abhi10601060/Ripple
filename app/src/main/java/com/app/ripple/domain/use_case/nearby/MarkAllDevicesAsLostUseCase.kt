package com.app.ripple.domain.use_case.nearby

import com.app.ripple.domain.repo.NearbyDeviceRepo
import javax.inject.Inject

class MarkAllDevicesAsLostUseCase @Inject constructor(
    val nearbyDeviceRepo: NearbyDeviceRepo
) {
    suspend operator fun invoke(){
        nearbyDeviceRepo.markAllDevicesAsLost()
    }
}