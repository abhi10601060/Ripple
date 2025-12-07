package com.app.ripple.domain.model

import com.app.ripple.data.local.realm.model.NearbyDeviceRealm
import com.app.ripple.data.local.realm.model.TextMessageRealm
import com.app.ripple.data.nearby.model.ConnectionState
import com.app.ripple.data.nearby.model.DeviceVisibility
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList

data class NearbyDeviceDomain(
    val id: String,                                                             // Android Id
    val endpointId: String,                                                     // Endpoint Id for communication
    val deviceName: String,                                                     // Users broadcasting Name
    val savedDeviceName: String? = null,                                        // Saved name by receiver
    val connectionState: ConnectionState = ConnectionState.DISCOVERED,
    val visibility: DeviceVisibility = DeviceVisibility.ONLINE,
    val lastSeen: Long = System.currentTimeMillis(),
    val signalStrength: Int = 100,
    val recentMessage: TextMessageDomain? = null,
    val allMessages : List<TextMessageDomain> = listOf()
)


fun NearbyDeviceRealm.toNearbyDeviceDomain() : NearbyDeviceDomain{
    return NearbyDeviceDomain(
        id = this.id,
        endpointId = this.endpointId,
        deviceName = this.deviceName,
        savedDeviceName = this.deviceName,
        connectionState = this.connectionState,
        visibility = this.visibility,
        lastSeen = this.lastSeen,
        signalStrength = this.signalStength,
        recentMessage = this.recentMessage?.toTextMessageDomain(),
        allMessages = this.allMessages.toList().map { it.toTextMessageDomain() }
    )
}