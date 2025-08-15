package com.app.ripple.domain.repo

import com.app.ripple.data.nearby.model.ClusterInfo
import com.app.ripple.data.nearby.model.NearbyDevice
import com.app.ripple.data.nearby.model.TextMessage
import kotlinx.coroutines.flow.Flow

interface NearbyShareRepo {
    fun startAdvertising(): Flow<Boolean>
    fun stopAdvertising(): Flow<Boolean>
    fun startDiscovery(): Flow<Boolean>
    fun stopDiscovery(): Flow<Boolean>
    fun getDiscoveredDevices(): Flow<List<NearbyDevice>>
    fun getConnectedDevices(): Flow<List<NearbyDevice>>
    fun connectToDevice(deviceId: String): Flow<Boolean>
    fun disconnectFromDevice(deviceId: String): Flow<Boolean>
    fun sendTextMessage(message: TextMessage): Flow<Boolean>
    fun getReceivedMessages(): Flow<List<TextMessage>>
    fun getSentMessages(): Flow<List<TextMessage>>
    fun getClusterInfo(): Flow<ClusterInfo>
    fun createCluster(): Flow<String>
    fun joinCluster(clusterId: String): Flow<Boolean>
    fun leaveCluster(): Flow<Boolean>
}