package com.app.ripple.data.local.realm.model

import com.app.ripple.data.nearby.model.DeliveryStatus
import com.app.ripple.data.nearby.model.Message
import com.app.ripple.data.nearby.model.MessageType
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey


class MessageRealm(
    @PrimaryKey
    var id : Long = System.currentTimeMillis(),
    var content: String,
    var senderId: String,
    var receiverId: String,
    var timestamp: Long = System.currentTimeMillis(),
    var _deliveryStatus: String = DeliveryStatus.FAILED.name,
    var _messageType: String,
    var payloadSize: Float = 0f,
    var fileName: String = "",
    var mimeType: String = "",
    var progress: Float = 0f
): RealmObject {

    val deliveryStatus: DeliveryStatus
        get() = DeliveryStatus.valueOf(_deliveryStatus)

    val messageType: MessageType
        get() = MessageType.valueOf(_messageType)

    constructor() : this(content = "", senderId = "", receiverId = "", _messageType="")
}

fun MessageRealm.toTextMessage(): Message{
    return Message(
        id = this.id,
        content = this.content,
        senderId = this.senderId,
        receiverId = this.receiverId,
        timestamp = this.timestamp,
        deliveryStatus = this.deliveryStatus,
        messageType =  this.messageType,
        payloadSize = this.payloadSize,
        fileName = this.fileName,
        mimeType = this.mimeType,
        progress =  this.progress
    )
}