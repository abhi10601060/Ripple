package com.app.ripple.data.nearby.dto

import com.app.ripple.data.nearby.model.TextMessage

data class TextMessageDto(
    val content: String,
    val senderId: String,
    val receiverId: String,
)

fun TextMessage.toTextMessageDto(): TextMessageDto{
    return TextMessageDto(
        content = this.content,
        senderId = this.senderId,
        receiverId = this.receiverId
    )
}