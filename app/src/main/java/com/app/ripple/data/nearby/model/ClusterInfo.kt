package com.app.ripple.data.nearby.model

data class ClusterInfo(
    val clusterId: String,
    val devices: List<NearbyDevice> = emptyList(),
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)