package com.app.ripple.domain.use_case.nearby

import com.app.ripple.data.nearby.model.NearbyDevice
import com.app.ripple.domain.repo.NearbyShareRepo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNearbyConnectedDevicesUseCase @Inject constructor(val nearbyShareRepo: NearbyShareRepo) {
    operator fun invoke() : Flow<List<NearbyDevice>> = nearbyShareRepo.getConnectedDevices()
}