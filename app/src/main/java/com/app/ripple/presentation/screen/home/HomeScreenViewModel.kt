package com.app.ripple.presentation.screen.home

import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.app.ripple.data.local.realm.model.NearbyDeviceRealm
import com.app.ripple.data.nearby.model.NearbyDevice
import com.app.ripple.domain.use_case.nearby.ConnectDeviceUseCase
import com.app.ripple.domain.use_case.nearby.DisconnectDeviceUseCase
import com.app.ripple.domain.use_case.nearby.GetNearbyConnectedDevicesUseCase
import com.app.ripple.domain.use_case.nearby.GetNearbyDiscoveredDevicesUseCase
import com.app.ripple.domain.use_case.nearby.StartAdvertisingUseCase
import com.app.ripple.domain.use_case.nearby.StartDiscoveryUseCase
import com.app.ripple.domain.use_case.nearby.StopAdvertisingUseCase
import com.app.ripple.domain.use_case.nearby.StopDiscoveryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val nearbyDiscoveredDevicesUseCase: GetNearbyDiscoveredDevicesUseCase,
    private val startAdvertisingUseCase: StartAdvertisingUseCase,
    private val startDiscoveryUseCase: StartDiscoveryUseCase,
    private val stopDiscoveryUseCase: StopDiscoveryUseCase,
    private val stopAdvertisingUseCase: StopAdvertisingUseCase,
    private val connectDeviceUseCase: ConnectDeviceUseCase,
    private val disconnectDeviceUseCase: DisconnectDeviceUseCase,
    private val getNearbyConnectedDevices: GetNearbyConnectedDevicesUseCase
) : ViewModel() {

    private val TAG = "HomeScreenViewModel"

    private val _nearbyDiscoveredDevices = mutableStateOf(listOf<NearbyDevice>())
    val nearbyDiscoveredDevices: State<List<NearbyDevice>> = _nearbyDiscoveredDevices

    private val _nearbyConnectedDevices = mutableStateOf(listOf<NearbyDeviceRealm>())
    val nearbyConnectedDevices: State<List<NearbyDeviceRealm>> = _nearbyConnectedDevices

    suspend fun getNearbyDiscoveredDevices(){
        nearbyDiscoveredDevicesUseCase.invoke().collect { devices ->
            _nearbyDiscoveredDevices.value = devices
        }
    }

    suspend fun startAdvertisingAndDiscovery(){
        startAdvertisingUseCase.invoke().collect { isStarted ->
            Log.d(TAG, "startAdvertising started: $isStarted")
        }

        startDiscoveryUseCase.invoke().collect { isStarted ->
            Log.d(TAG, "startDiscovery started: $isStarted")
        }
    }

    suspend fun stopAdvertisingAndDiscovery(){
        stopAdvertisingUseCase.invoke().collect { isStopped ->
            Log.d(TAG, "stopAdvertising stopped: $isStopped")
        }

        stopDiscoveryUseCase.invoke().collect { isStopped ->
            Log.d(TAG, "stopDiscovery stopped: $isStopped")
        }
    }

    suspend fun connectDevice(device: NearbyDevice){
        connectDeviceUseCase.invoke(device.endpointId).collect { isConnected ->
            Log.d(TAG, "connected to Device: ${device.deviceName} : $isConnected")
        }
    }

    suspend fun disconnectDevice(device: NearbyDevice){
        disconnectDeviceUseCase.invoke(device.endpointId).collect { isDisconnected ->
            Log.d(TAG, "disconnected from device: ${device.deviceName} : $isDisconnected")
        }
    }

    suspend fun getNearbyConnectedDevices(){
        getNearbyConnectedDevices.invoke().collect { connectedDevices ->
            Log.d(TAG, "getNearbyConnectedDevices: found connected devices : $connectedDevices")
            _nearbyConnectedDevices.value = connectedDevices
        }
    }
}