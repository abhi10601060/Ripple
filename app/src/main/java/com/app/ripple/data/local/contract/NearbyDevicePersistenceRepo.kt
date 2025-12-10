package com.app.ripple.data.local.contract

import com.app.ripple.data.local.realm.model.NearbyDeviceRealm
import com.app.ripple.data.nearby.model.ConnectionState
import com.app.ripple.data.nearby.model.NearbyDevice
import kotlinx.coroutines.flow.Flow

interface NearbyDevicePersistenceRepo {

    suspend fun upsertDiscoveredNearbyDevice(nearbyDevice: NearbyDevice)

    fun getAllNearbyDevices(): Flow<List<NearbyDeviceRealm>>

    fun getAllDiscoveredNearbyDevices(): Flow<List<NearbyDeviceRealm>>

    suspend fun updateConnectionState(endpointId: String, connectionState: ConnectionState)

    suspend fun getNearbyDeviceById(deviceId: String) : Flow<NearbyDeviceRealm?>

    suspend fun markAllDevicesAsLost()
}