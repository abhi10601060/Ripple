package com.app.ripple.data.local.realm.model

import com.app.ripple.data.nearby.model.ConnectionState
import com.app.ripple.data.nearby.model.DeviceVisibility
import com.app.ripple.data.nearby.model.TextMessage
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class NearbyDeviceRealm(
    @PrimaryKey
    var id: String,                                                             // Android Id
    var endpointId: String,                                                     // Endpoint Id for communication
    var deviceName: String,                                                     // Users broadcasting Name
    var savedDeviceName: String? = null,                                        // Saved name by receiver
    var _connectionState: String = ConnectionState.DISCOVERED.name,
    var _visibility: String = DeviceVisibility.ONLINE.name,
    var lastSeen: Long = System.currentTimeMillis(),
    var signalStength: Int = 100,
    var recentMessage: TextMessageRealm? = null,
    var allMessages : RealmList<TextMessageRealm> = realmListOf()
    ): RealmObject {

         val connectionState : ConnectionState
            get() = ConnectionState.valueOf(_connectionState)

         val visibility : DeviceVisibility
            get() = DeviceVisibility.valueOf(_visibility)

    constructor() : this(id = "", endpointId = "", deviceName = "")
}