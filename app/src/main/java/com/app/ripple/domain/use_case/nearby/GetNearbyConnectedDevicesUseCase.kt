package com.app.ripple.domain.use_case.nearby

import com.app.ripple.data.local.realm.model.NearbyDeviceRealm
import com.app.ripple.data.nearby.model.NearbyDevice
import com.app.ripple.domain.repo.NearbyDeviceRepo
import com.app.ripple.domain.repo.NearbyShareRepo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNearbyConnectedDevicesUseCase @Inject constructor(val nearbyDeviceRepo: NearbyDeviceRepo) {
    operator fun invoke() : Flow<List<NearbyDeviceRealm>> = nearbyDeviceRepo.getAllNearbyDevices()
}