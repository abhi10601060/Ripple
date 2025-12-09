package com.app.ripple.domain.model

import com.app.ripple.data.local.realm.model.NearbyDeviceRealm
import com.app.ripple.data.local.realm.model.TextMessageRealm
import com.app.ripple.data.nearby.model.ConnectionState
import com.app.ripple.data.nearby.model.DeviceVisibility
import com.app.ripple.data.nearby.model.NearbyDevice
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
){
    companion object{
        val mock = NearbyDeviceDomain(
            id = "123",
            endpointId = "abc",
            deviceName = "abcdefghijk",
            connectionState = ConnectionState.CONNECTED,
            lastSeen = System.currentTimeMillis(),
            signalStrength = 100,
            visibility = DeviceVisibility.ONLINE,
            recentMessage= TextMessageDomain.mock,
            allMessages = listOf(TextMessageDomain.mock, TextMessageDomain.mock)
        )
    }
}


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

fun NearbyDevice.toNearbyDeviceDomain(): NearbyDeviceDomain{
    return NearbyDeviceDomain(
        id = this.id,
        endpointId = this.endpointId,
        deviceName = this.deviceName,
        connectionState = this.connectionState,
        lastSeen = this.lastSeen,
        signalStrength =  this.signalStrength
    )
}