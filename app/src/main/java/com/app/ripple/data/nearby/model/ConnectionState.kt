package com.app.ripple.data.nearby.model

enum class ConnectionState {
    DISCONNECTED,
    DISCOVERED,
    CONNECTING,
    CONNECTED,
    SENDING,
    RECEIVING,
    ERROR
}