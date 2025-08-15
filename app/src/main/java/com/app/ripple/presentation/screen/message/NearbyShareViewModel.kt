package com.app.ripple.presentation.screen.message

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.ripple.data.nearby.model.ClusterInfo
import com.app.ripple.data.nearby.model.NearbyDevice
import com.app.ripple.data.nearby.model.TextMessage
import com.app.ripple.domain.repo.NearbyShareRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NearbyShareViewModel @Inject constructor(
    private val repository: NearbyShareRepo
) : ViewModel() {

    private val _uiState = MutableLiveData<NearbyShareUiState>()
    val uiState: LiveData<NearbyShareUiState> = _uiState

    private val _discoveredDevices = MutableLiveData<List<NearbyDevice>>()
    val discoveredDevices: LiveData<List<NearbyDevice>> = _discoveredDevices

    private val _connectedDevices = MutableLiveData<List<NearbyDevice>>()
    val connectedDevices: LiveData<List<NearbyDevice>> = _connectedDevices

    private val _messages = MutableLiveData<List<TextMessage>>()
    val messages: LiveData<List<TextMessage>> = _messages

    private val _clusterInfo = MutableLiveData<ClusterInfo>()
    val clusterInfo: LiveData<ClusterInfo> = _clusterInfo

    init {
        observeDevices()
        observeMessages()
        observeCluster()
    }

    private fun observeDevices() {
        viewModelScope.launch {
            repository.getDiscoveredDevices().collect { devices ->
                _discoveredDevices.postValue(devices)
            }
        }

        viewModelScope.launch {
            repository.getConnectedDevices().collect { devices ->
                _connectedDevices.postValue(devices)
            }
        }
    }

    private fun observeMessages() {
        viewModelScope.launch {
            combine(
                repository.getReceivedMessages(),
                repository.getSentMessages()
            ) { received, sent ->
                (received + sent).sortedBy { it.timestamp }
            }.collect { allMessages ->
                _messages.postValue(allMessages)
            }
        }
    }

    private fun observeCluster() {
        viewModelScope.launch {
            repository.getClusterInfo().collect { cluster ->
                _clusterInfo.postValue(cluster)
            }
        }
    }

    fun startAdvertising() {
        viewModelScope.launch {
            _uiState.postValue(NearbyShareUiState.Loading)
            repository.startAdvertising()
                .catch { exception ->
                    _uiState.postValue(NearbyShareUiState.Error("Failed to start advertising: ${exception.message}"))
                }
                .collect { success ->
                    if (success) {
                        _uiState.postValue(NearbyShareUiState.Advertising)
                    } else {
                        _uiState.postValue(NearbyShareUiState.Error("Failed to start advertising"))
                    }
                }
        }
    }

    fun stopAdvertising() {
        viewModelScope.launch {
            repository.stopAdvertising().collect { success ->
                if (success) {
                    _uiState.postValue(NearbyShareUiState.Idle)
                }
            }
        }
    }

    fun startDiscovery() {
        viewModelScope.launch {
            _uiState.postValue(NearbyShareUiState.Loading)
            repository.startDiscovery()
                .catch { exception ->
                    Log.d("ViewModel", "startDiscovery: catched : " + exception.message)
                    _uiState.postValue(NearbyShareUiState.Error("Failed to start discovery: ${exception.message}"))
                }
                .collect { success ->
                    if (success) {
                        _uiState.postValue(NearbyShareUiState.Discovering)
                    } else {
                        _uiState.postValue(NearbyShareUiState.Error("Failed to start discovery"))
                    }
                }
        }
    }

    fun stopDiscovery() {
        viewModelScope.launch {
            repository.stopDiscovery().collect { success ->
                if (success) {
                    _uiState.postValue(NearbyShareUiState.Idle)
                }
            }
        }
    }

    fun connectToDevice(deviceId: String) {
        viewModelScope.launch {
            _uiState.postValue(NearbyShareUiState.Connecting)
            repository.connectToDevice(deviceId)
                .catch { exception ->
                    _uiState.postValue(NearbyShareUiState.Error("Failed to connect: ${exception.message}"))
                }
                .collect { success ->
                    if (success) {
                        _uiState.postValue(NearbyShareUiState.Connected)
                    } else {
                        _uiState.postValue(NearbyShareUiState.Error("Failed to connect to device"))
                    }
                }
        }
    }

    fun disconnectFromDevice(deviceId: String) {
        viewModelScope.launch {
            repository.disconnectFromDevice(deviceId).collect { success ->
                if (success) {
                    _uiState.postValue(NearbyShareUiState.Idle)
                }
            }
        }
    }

    fun sendMessage(content: String, receiverId: String) {
        viewModelScope.launch {
            val message = TextMessage(
                content = content,
                senderId = android.os.Build.MODEL,
                receiverId = receiverId
            )

            repository.sendTextMessage(message)
                .catch { exception ->
                    _uiState.postValue(NearbyShareUiState.Error("Failed to send message: ${exception.message}"))
                }
                .collect { success ->
                    if (success) {
                        _uiState.postValue(NearbyShareUiState.MessageSent)
                    } else {
                        _uiState.postValue(NearbyShareUiState.Error("Failed to send message"))
                    }
                }
        }
    }

    fun createCluster() {
        viewModelScope.launch {
            repository.createCluster()
                .catch { exception ->
                    _uiState.postValue(NearbyShareUiState.Error("Failed to create cluster: ${exception.message}"))
                }
                .collect { clusterId ->
                    _uiState.postValue(NearbyShareUiState.ClusterCreated(clusterId))
                }
        }
    }

    fun joinCluster(clusterId: String) {
        viewModelScope.launch {
            repository.joinCluster(clusterId)
                .catch { exception ->
                    _uiState.postValue(NearbyShareUiState.Error("Failed to join cluster: ${exception.message}"))
                }
                .collect { success ->
                    if (success) {
                        _uiState.postValue(NearbyShareUiState.ClusterJoined)
                    } else {
                        _uiState.postValue(NearbyShareUiState.Error("Failed to join cluster"))
                    }
                }
        }
    }

    fun leaveCluster() {
        viewModelScope.launch {
            repository.leaveCluster().collect { success ->
                if (success) {
                    _uiState.postValue(NearbyShareUiState.Idle)
                }
            }
        }
    }
}