package com.app.ripple.data.local.realm

import android.util.Log
import com.app.ripple.data.local.contract.NearbyDevicePersistenceRepo
import com.app.ripple.data.local.realm.model.NearbyDeviceRealm
import com.app.ripple.data.nearby.model.ConnectionState
import com.app.ripple.data.nearby.model.DeviceVisibility
import com.app.ripple.data.nearby.model.NearbyDevice
import com.app.ripple.data.nearby.model.toNearbyDeviceRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NearbyDeviceRealmRepo(private val realm: Realm): NearbyDevicePersistenceRepo {

    private val TAG = "NearbyDeviceRealmRepo"

    override suspend fun upsertDiscoveredNearbyDevice(nearbyDevice: NearbyDevice) {
        realm.write {

            val existingDevice = query(NearbyDeviceRealm::class, "id == $0", nearbyDevice.id).first().find()

            existingDevice?.apply {
                endpointId = nearbyDevice.endpointId
                deviceName = nearbyDevice.deviceName
                _connectionState = nearbyDevice.connectionState.name
                _visibility = DeviceVisibility.ONLINE.name
            }
                ?: copyToRealm(nearbyDevice.toNearbyDeviceRealm())

            Log.d(TAG, "upsertDiscoveredNearbyDevice: device added successfully")
        }
    }

    override fun getAllNearbyDevices(): Flow<List<NearbyDeviceRealm>> {
        val allDevices = realm.query<NearbyDeviceRealm>()
            .asFlow()
            .map { result ->
                result.list
                    .filter {
                        it.connectionState == ConnectionState.CONNECTED || it.recentMessage != null
                    }
                    .map { it }
            }

        return allDevices
    }

    override suspend fun updateConnectionState(endpointId: String, connectionState: ConnectionState) {
        realm.write {
            val discoveredDevice = query(NearbyDeviceRealm::class, "endpointId == $0", endpointId).first().find()
            discoveredDevice?.apply {
                this._connectionState = connectionState.name
            }
        }
    }

}