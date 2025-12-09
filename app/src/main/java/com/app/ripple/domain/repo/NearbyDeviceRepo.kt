package com.app.ripple.domain.repo

import com.app.ripple.data.local.realm.model.NearbyDeviceRealm
import com.app.ripple.data.nearby.model.NearbyDevice
import kotlinx.coroutines.flow.Flow

interface NearbyDeviceRepo {
    fun getAllNearbyDevices(): Flow<List<NearbyDeviceRealm>>

    fun getAllDiscoveredDevices(): Flow<List<NearbyDeviceRealm>>

    suspend fun getNearbyDeviceById(deviceId: String): Flow<NearbyDeviceRealm?>
}