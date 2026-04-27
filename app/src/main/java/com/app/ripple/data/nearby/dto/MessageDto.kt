package com.app.ripple.data.nearby.dto

import com.app.ripple.data.nearby.model.Message
import com.app.ripple.data.nearby.model.MessageType

data class MessageDto(
    val content: String,
    val senderId: String,
    val receiverId: String,
    val _messageType: String
){
    val messageType : MessageType
        get() = MessageType.valueOf(_messageType)
}

fun Message.toTextMessageDto(): MessageDto{
    return MessageDto(
        content = this.content,
        senderId = this.senderId,
        receiverId = this.receiverId,
        _messageType = this.messageType.name
    )
}