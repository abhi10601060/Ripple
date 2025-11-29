package com.app.ripple.domain.use_case.nearby

import com.app.ripple.domain.repo.NearbyShareRepo
import javax.inject.Inject

class DisconnectDeviceUseCase @Inject constructor(val nearbyShareRepo: NearbyShareRepo) {
    operator fun invoke(deviceId: String) = nearbyShareRepo.disconnectFromDevice(deviceId = deviceId)
}