package com.app.ripple.presentation.screen.chat

import androidx.lifecycle.ViewModel
import com.app.ripple.data.nearby.model.NearbyDevice
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatScreenViewModel @Inject constructor() : ViewModel() {
    private lateinit var receiverDevice: NearbyDevice

    fun init(receiverDevice: NearbyDevice) {
        this.receiverDevice = receiverDevice
    }

}