package com.app.ripple.data.nearby.model

import kotlinx.serialization.Serializable

@Serializable
enum class ConnectionState {
    DISCONNECTED,
    DISCOVERED,
    CONNECTING,
    CONNECTED,
    SENDING,
    RECEIVING,
    ERROR
}