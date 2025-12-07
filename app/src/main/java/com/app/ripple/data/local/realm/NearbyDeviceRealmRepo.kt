package com.app.ripple.data.local.realm

import android.util.Log
import com.app.ripple.data.local.contract.NearbyDevicePersistenceRepo
import com.app.ripple.data.local.realm.model.NearbyDeviceRealm
import com.app.ripple.data.nearby.model.DeviceVisibility
import com.app.ripple.data.nearby.model.NearbyDevice
import com.app.ripple.data.nearby.model.toNearbyDeviceRealm
import io.realm.kotlin.Realm
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

}