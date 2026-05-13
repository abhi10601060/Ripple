package com.app.ripple.data.local.contract

import com.app.ripple.data.local.realm.model.MessageRealm
import com.app.ripple.data.nearby.model.DeliveryStatus

interface TextMessagePersistenceRepo {
    suspend fun insertSentMessage(message: MessageRealm)

    suspend fun insertReceivedMessage(message: MessageRealm)

    suspend fun updateDeliveryStatus(id: Long, status: DeliveryStatus)

    suspend fun updateMessageProgress(id: Long, progress: Float)
}