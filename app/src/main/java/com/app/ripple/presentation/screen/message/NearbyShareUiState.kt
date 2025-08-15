package com.app.ripple.presentation.screen.message

sealed class NearbyShareUiState {
    object Idle : NearbyShareUiState()
    object Loading : NearbyShareUiState()
    object Advertising : NearbyShareUiState()
    object Discovering : NearbyShareUiState()
    object Connecting : NearbyShareUiState()
    object Connected : NearbyShareUiState()
    object MessageSent : NearbyShareUiState()
    data class ClusterCreated(val clusterId: String) : NearbyShareUiState()
    object ClusterJoined : NearbyShareUiState()
    data class Error(val message: String) : NearbyShareUiState()
}