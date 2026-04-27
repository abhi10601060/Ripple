package com.app.ripple.data.nearby.model

import com.app.ripple.data.local.realm.model.MessageRealm

data class Message(
    var id: Long = System.currentTimeMillis(),
    val content: String,
    val senderId: String,
    val receiverId: String,
    val endpointId: String = "null",
    val timestamp: Long = System.currentTimeMillis(),
    val deliveryStatus: DeliveryStatus = DeliveryStatus.PENDING,
    val messageType: MessageType
)

fun Message.toTextMessageRealm() : MessageRealm{
    return MessageRealm(
        id = this.id,
        content = this.content,
        senderId = this.senderId,
        receiverId = this.receiverId,
        _deliveryStatus = this.deliveryStatus.name,
        _messageType =  this.messageType.name
    )
}