package com.app.ripple.data.repo

import com.app.ripple.data.local.contract.NearbyDevicePersistenceRepo
import com.app.ripple.data.local.realm.model.NearbyDeviceRealm
import com.app.ripple.domain.repo.NearbyDeviceRepo
import kotlinx.coroutines.flow.Flow

class NearbyDeviceRepoImpl(private val nearbyDevicePersistenceRepo: NearbyDevicePersistenceRepo): NearbyDeviceRepo {

    override  fun getAllNearbyDevices(): Flow<List<NearbyDeviceRealm>> {
        return nearbyDevicePersistenceRepo.getAllNearbyDevices()
    }
}