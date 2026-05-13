package com.app.ripple.domain.repo

import com.app.ripple.data.nearby.model.ClusterInfo
import com.app.ripple.data.nearby.model.NearbyDevice
import com.app.ripple.data.nearby.model.Message
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
    fun sendTextMessage(message: Message): Flow<Boolean>
    fun sendFile(metadataMessage: Message): Flow<Boolean>
    fun getReceivedMessages(): Flow<List<Message>>
    fun getSentMessages(): Flow<List<Message>>
    fun getClusterInfo(): Flow<ClusterInfo>
    fun createCluster(): Flow<String>
    fun joinCluster(clusterId: String): Flow<Boolean>
    fun leaveCluster(): Flow<Boolean>
    fun acceptConnection(endpointId: String)
    fun rejectConnection(endpointId: String)
}