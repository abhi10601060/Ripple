package com.app.ripple.data.nearby.model

data class NearbyDevice(
    val deviceId: String,
    val deviceName: String,
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val lastSeen: Long = System.currentTimeMillis(),
    val signalStrength: Int = 0
)