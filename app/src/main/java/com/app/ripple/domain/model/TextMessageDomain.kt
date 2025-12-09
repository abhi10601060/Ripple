package com.app.ripple.domain.model

import com.app.ripple.data.local.realm.model.NearbyDeviceRealm
import com.app.ripple.data.local.realm.model.TextMessageRealm
import com.app.ripple.data.nearby.model.DeliveryStatus

data class TextMessageDomain(
    val id : Long = System.currentTimeMillis(),
    val content: String,
    val senderId: String,
    val receiverId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val deliveryStatus: DeliveryStatus = DeliveryStatus.FAILED
){
    companion object{
        val mock = TextMessageDomain(
            content = "Hello How are you?",
            senderId = "123",
            receiverId = "abc",
            timestamp = System.currentTimeMillis()
        )
    }
}

fun TextMessageRealm.toTextMessageDomain(): TextMessageDomain{
    return TextMessageDomain(
        id = this.id,
        content = this.content,
        senderId = this.senderId,
        receiverId = this.receiverId,
        timestamp = this.timestamp,
        deliveryStatus = this.deliveryStatus
    )
}