package com.app.ripple.data.repo

import android.content.Context
import com.app.ripple.data.nearby.NearbyShareManager
import com.app.ripple.data.nearby.model.ClusterInfo
import com.app.ripple.data.nearby.model.NearbyDevice
import com.app.ripple.data.nearby.model.TextMessage
import com.app.ripple.domain.repo.NearbyShareRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NearbyShareRepoImpl(
    private val context: Context
) : NearbyShareRepo {

    private val nearbyShareManager = NearbyShareManager.getInstance(context)

    override fun startAdvertising(): Flow<Boolean> = nearbyShareManager.startAdvertising()

    override fun stopAdvertising(): Flow<Boolean> = nearbyShareManager.stopAdvertising()

    override fun startDiscovery(): Flow<Boolean> = nearbyShareManager.startDiscovery()

    override fun stopDiscovery(): Flow<Boolean> = nearbyShareManager.stopDiscovery()

    override fun getDiscoveredDevices(): Flow<List<NearbyDevice>> =
        nearbyShareManager.discoveredDevices

    override fun getConnectedDevices(): Flow<List<NearbyDevice>> =
        nearbyShareManager.connectedDevices

    override fun connectToDevice(deviceId: String): Flow<Boolean> =
        nearbyShareManager.connectToDevice(deviceId)

    override fun disconnectFromDevice(deviceId: String): Flow<Boolean> =
        nearbyShareManager.disconnectFromDevice(deviceId)

    override fun sendTextMessage(message: TextMessage): Flow<Boolean> =
        nearbyShareManager.sendTextMessage(message)

    override fun getReceivedMessages(): Flow<List<TextMessage>> =
        nearbyShareManager.receivedMessages

    override fun getSentMessages(): Flow<List<TextMessage>> =
        nearbyShareManager.sentMessages

    // For 2.0

    override fun getClusterInfo(): Flow<ClusterInfo> =
        nearbyShareManager.clusterInfo.map { it ?: ClusterInfo("", emptyList(), false) }

    override fun createCluster(): Flow<String> = nearbyShareManager.createCluster()

    override fun joinCluster(clusterId: String): Flow<Boolean> =
        nearbyShareManager.joinCluster(clusterId)

    override fun leaveCluster(): Flow<Boolean> = nearbyShareManager.leaveCluster()
}
