package com.app.ripple.data.nearby.model

data class TextMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val content: String,
    val senderId: String,
    val receiverId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val deliveryStatus: DeliveryStatus = DeliveryStatus.PENDING
)

