package com.app.ripple.data.nearby.model

import com.app.ripple.data.local.realm.model.NearbyDeviceRealm
import com.app.ripple.domain.model.NearbyDeviceDomain
import kotlinx.serialization.Serializable

@Serializable
data class NearbyDevice(
    val id: String,
    val endpointId: String,         // this will be the android id
    val deviceName: String,        // broadcasters device name
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val lastSeen: Long = System.currentTimeMillis(),
    val signalStrength: Int = 0
){
    companion object{
        val mock = NearbyDevice(
            id = "123",
            endpointId = "abc",
            deviceName = "abcdefghijk",
            connectionState = ConnectionState.CONNECTED,
            lastSeen = System.currentTimeMillis(),
            signalStrength = 100
        )
    }
}

fun NearbyDevice.toNearbyDeviceRealm() : NearbyDeviceRealm{
    return NearbyDeviceRealm(
        id = this.id,
        endpointId = this.endpointId,
        deviceName = this.deviceName,
    )
}

fun NearbyDeviceDomain.toNearbyDevice(): NearbyDevice{
    return NearbyDevice(
        id = this.id,
        endpointId = this.endpointId,
        deviceName = this.deviceName,
        connectionState= this.connectionState
    )
}