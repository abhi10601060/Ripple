package com.app.ripple.data.local.contract

import com.app.ripple.data.local.realm.model.TextMessageRealm
import com.app.ripple.data.nearby.model.DeliveryStatus

interface TextMessagePersistenceRepo {
    suspend fun insertSentMessage(message: TextMessageRealm)

    suspend fun insertReceivedMessage(message: TextMessageRealm)

    suspend fun updateDeliveryStatus(id: Long, status: DeliveryStatus)
}