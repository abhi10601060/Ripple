package com.app.ripple.data.local.realm.model

import com.app.ripple.data.nearby.model.DeliveryStatus
import com.app.ripple.data.nearby.model.TextMessage
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey


class TextMessageRealm(
    @PrimaryKey
    var id : Long = System.currentTimeMillis(),
    var content: String,
    var senderId: String,
    var receiverId: String,
    var timestamp: Long = System.currentTimeMillis(),
    var _deliveryStatus: String = DeliveryStatus.FAILED.name
): RealmObject {

    val deliveryStatus: DeliveryStatus
        get() = DeliveryStatus.valueOf(_deliveryStatus)

    constructor() : this(content = "", senderId = "", receiverId = "")
}

fun TextMessageRealm.toTextMessage(): TextMessage{
    return TextMessage(
        id = this.id,
        content = this.content,
        senderId = this.senderId,
        receiverId = this.receiverId,
        timestamp = this.timestamp,
        deliveryStatus = this.deliveryStatus
    )
}