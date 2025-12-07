package com.app.ripple.data.local.contract

import com.app.ripple.data.local.realm.model.TextMessageRealm

interface TextMessagePersistenceRepo {
    suspend fun insertSentMessage(message: TextMessageRealm)

    suspend fun insertReceivedMessage(message: TextMessageRealm)
}