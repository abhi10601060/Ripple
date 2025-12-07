package com.app.ripple.data.nearby.model

import com.app.ripple.data.local.realm.model.TextMessageRealm

data class TextMessage(
    var id: Long = System.currentTimeMillis(),
    val content: String,
    val senderId: String,
    val receiverId: String,
    val endpointId: String = "null",
    val timestamp: Long = System.currentTimeMillis(),
    val deliveryStatus: DeliveryStatus = DeliveryStatus.PENDING
)

fun TextMessage.toTextMessageRealm() : TextMessageRealm{
    return TextMessageRealm(
        id = this.id,
        content = this.content,
        senderId = this.senderId,
        receiverId = this.receiverId,
        _deliveryStatus = this.deliveryStatus.name
    )
}