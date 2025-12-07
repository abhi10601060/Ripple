package com.app.ripple.domain.repo

import com.app.ripple.data.local.realm.model.NearbyDeviceRealm
import kotlinx.coroutines.flow.Flow

interface NearbyDeviceRepo {
    fun getAllNearbyDevices(): Flow<List<NearbyDeviceRealm>>
}