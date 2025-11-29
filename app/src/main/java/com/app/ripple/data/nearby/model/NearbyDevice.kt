package com.app.ripple.data.nearby.model

import kotlinx.serialization.Serializable

@Serializable
data class NearbyDevice(
    val deviceId: String,
    val deviceName: String,
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val lastSeen: Long = System.currentTimeMillis(),
    val signalStrength: Int = 0
){
    companion object{
        val mock = NearbyDevice(
            deviceId = "abc",
            deviceName = "abcdefghijk",
            connectionState = ConnectionState.CONNECTED,
            lastSeen = System.currentTimeMillis(),
            signalStrength = 100
        )
    }
}
