package com.app.ripple.data.local.realm

import com.app.ripple.data.local.contract.TextMessagePersistenceRepo
import com.app.ripple.data.local.realm.model.NearbyDeviceRealm
import com.app.ripple.data.local.realm.model.MessageRealm
import com.app.ripple.data.nearby.model.DeliveryStatus
import io.realm.kotlin.Realm

class TextMessageRealmRepo(private val realm: Realm): TextMessagePersistenceRepo {
    override suspend fun insertSentMessage(message: MessageRealm) {
        realm.write {
            val receiverNearbyDevice = query(NearbyDeviceRealm::class, "id == $0", message.receiverId).first().find()
            receiverNearbyDevice?.apply {
                this.recentMessage = message
                this.allMessages.add(message)
            }
        }
    }

    override suspend fun insertReceivedMessage(message: MessageRealm) {
        realm.write {
            val senderDevice = query(NearbyDeviceRealm::class, "id == $0", message.senderId).first().find()
            senderDevice?.apply {
                this.recentMessage = message
                this.allMessages.add(message)
            }
        }
    }

    override suspend fun updateDeliveryStatus(
        id: Long,
        status: DeliveryStatus
    ) {

        realm.write {
            val savedMessage = query(MessageRealm::class, "id == $0", id).first().find()

            savedMessage?.apply {
                this._deliveryStatus = status.name
            }
        }
    }

    override suspend fun updateMessageProgress(id: Long, progress: Float) {
        realm.write {
            val savedMessage = query(MessageRealm::class, "id == $0", id).first().find()

            savedMessage?.apply {
                this.progress = progress
            }
        }
    }
}