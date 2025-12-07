package com.app.ripple.data.local.realm

import com.app.ripple.data.local.contract.TextMessagePersistenceRepo
import com.app.ripple.data.local.realm.model.NearbyDeviceRealm
import com.app.ripple.data.local.realm.model.TextMessageRealm
import com.app.ripple.data.nearby.model.DeliveryStatus
import io.realm.kotlin.Realm

class TextMessageRealmRepo(private val realm: Realm): TextMessagePersistenceRepo {
    override suspend fun insertSentMessage(message: TextMessageRealm) {
        realm.write {
            val receiverNearbyDevice = query(NearbyDeviceRealm::class, "id == $0", message.receiverId).first().find()
            receiverNearbyDevice?.apply {
                this.recentMessage = message
                this.allMessages.add(message)
            }
        }
    }

    override suspend fun insertReceivedMessage(message: TextMessageRealm) {
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
            val savedMessage = query(TextMessageRealm::class, "id == $0", id).first().find()

            savedMessage?.apply {
                this._deliveryStatus = status.name
            }
        }
    }
}