package com.app.ripple.data.nearby.util

import com.app.ripple.data.nearby.NearbyShareManager
import com.app.ripple.data.nearby.model.DeliveryStatus
import com.app.ripple.data.nearby.model.Message
import com.google.android.gms.nearby.connection.Payload
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentLinkedQueue

class UserMessageQueue(
    private val nearbyShareManager: NearbyShareManager
) {
    // 1. Thread-safe queue for messages
    private val queue = ConcurrentLinkedQueue<Pair<Message, Payload>>()

    fun addMessage(message: Message, payload: Payload) {
        message.deliveryStatus = DeliveryStatus.PENDING
        queue.add(Pair(message, payload))
        processNext()
    }

    private fun processNext() {
        val pair = queue.peek()  ?: return
        val nextMessage = pair.first
        val filePayload = pair.second

        when (nextMessage.deliveryStatus) {
            DeliveryStatus.PENDING -> {
                nearbyShareManager.sendFile(nextMessage, filePayload)
                nextMessage.deliveryStatus = DeliveryStatus.PROCESSING
            }
            DeliveryStatus.PROCESSING -> {
                return
            }
            else -> {
                // If it's already canceled or failed, remove it and try next
                queue.poll()
                processNext()
            }
        }
    }

    fun onFileTransferredSuccessfully(messageId: Long) {
        val current = queue.peek()
        if (current?.first?.id == messageId) {
            queue.poll() // Remove the completed message
        }
        processNext() // Trigger next in line
    }

    fun onTransferFailed(messageId: String) {
        processNext()
    }

    fun cancelFileTransfer(msg: Message) {
        // Mark as canceled in the queue
        queue.forEach { if (it.first.id == msg.id) it.first.deliveryStatus = DeliveryStatus.CANCELLED }

        val current = queue.peek()
        if (current?.first?.id == msg.id) {
//            nearbyShareManager.cancelTransfer(messageId)
            queue.poll()
//            _state.value = UserMessageQueueStatus.AVAILABLE
            processNext()
        }
    }

    fun getPayloadById(id: Long): Payload? {
        queue.forEach {
            if (it.second.id == id) return it.second
        }

        return null
    }

    fun getMetadataMessageById(id : Long) : Message? {
        queue.forEach {
            if (it.first.id == id) return it.first
        }

        return null
    }

    fun cancelAllFileTransfers(){
        queue.forEach {
            if (it.first.deliveryStatus == DeliveryStatus.PROCESSING){
                // cancel transfer
            }
        }
        queue.clear()
    }
}

enum class UserMessageQueueStatus {
    AVAILABLE, PROCESSING, IDLE
}