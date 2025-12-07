package com.app.ripple.data.local.contract

import com.app.ripple.data.nearby.model.NearbyDevice

interface NearbyDevicePersistenceRepo {

    suspend fun upsertDiscoveredNearbyDevice(nearbyDevice: NearbyDevice)
}