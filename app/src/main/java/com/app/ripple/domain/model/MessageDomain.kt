package com.app.ripple.domain.model

import com.app.ripple.data.local.realm.model.MessageRealm
import com.app.ripple.data.nearby.model.DeliveryStatus
import com.app.ripple.data.nearby.model.MessageType

data class MessageDomain(
    val id : Long = System.currentTimeMillis(),
    val content: String,
    val senderId: String,
    val receiverId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val deliveryStatus: DeliveryStatus = DeliveryStatus.FAILED,
    val messageType: MessageType
){
    companion object{
        val mock = MessageDomain(
            content = "Hello How are you?",
            senderId = "123",
            receiverId = "abc",
            timestamp = System.currentTimeMillis(),
            messageType = MessageType.TEXT
        )
    }
}

fun MessageRealm.toTextMessageDomain(): MessageDomain{
    return MessageDomain(
        id = this.id,
        content = this.content,
        senderId = this.senderId,
        receiverId = this.receiverId,
        timestamp = this.timestamp,
        deliveryStatus = this.deliveryStatus,
        messageType = this.messageType
    )
}