package com.app.ripple.presentation.screen.home

import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.app.ripple.data.nearby.model.NearbyDevice
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

) : ViewModel() {

    private val TAG = "HomeScreenViewModel"

    private val _nearbyDiscoveredDevices = mutableStateOf(listOf<NearbyDevice>())
    val nearbyDiscoveredDevices: State<List<NearbyDevice>> = _nearbyDiscoveredDevices

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

}