package com.app.ripple.data.nearby.dto

import com.app.ripple.data.nearby.model.Message
import com.app.ripple.data.nearby.model.MessageType

data class MessageDto(
    val id: Long,
    val content: String,
    val senderId: String,
    val receiverId: String,
    val _messageType: String,
    val fileName: String = "",
    val mimeType: String = "",
    val payloadSize: Float = 0f,
){
    val messageType : MessageType
        get() = MessageType.valueOf(_messageType)
}

fun Message.toTextMessageDto(): MessageDto{
    return MessageDto(
        id = this.id,
        content = this.content,
        senderId = this.senderId,
        receiverId = this.receiverId,
        _messageType = this.messageType.name,
        payloadSize= this.payloadSize,
        fileName = this.fileName,
        mimeType = this.mimeType,
    )
}