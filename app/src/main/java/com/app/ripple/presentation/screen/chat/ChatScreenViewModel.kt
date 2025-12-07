package com.app.ripple.presentation.screen.chat

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.ripple.data.nearby.model.NearbyDevice
import com.app.ripple.data.nearby.model.TextMessage
import com.app.ripple.domain.use_case.chat.GetReceivedMessageUseCase
import com.app.ripple.domain.use_case.chat.GetSentMessageUseCase
import com.app.ripple.domain.use_case.chat.SendTextMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatScreenViewModel @Inject constructor(
    private val sendTextMessageUseCase: SendTextMessageUseCase,
    private val getSentMessageUseCase: GetSentMessageUseCase,
    private val getReceivedMessageUseCase: GetReceivedMessageUseCase
) : ViewModel() {

    private val TAG = "ChatScreenViewModel"

    private lateinit var receiverDevice: NearbyDevice
    private lateinit var androidId: String

    private val _sentMessages = mutableStateOf(listOf<TextMessage>())
    val sentMessages: State<List<TextMessage>> = _sentMessages

    private val _receivedMessages = mutableStateOf(listOf<TextMessage>())
    val receivedMessages: State<List<TextMessage>> = _receivedMessages

    @SuppressLint("HardwareIds")
    fun init(receiverDevice: NearbyDevice, context: Context) {
        this.receiverDevice = receiverDevice
        androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun sendTextMessage(payload: String){
        if (payload.isEmpty()) return

        val textMessage = TextMessage(
            senderId = "${android.os.Build.MODEL}:${androidId}",
            receiverId = receiverDevice.endpointId,
            content = payload
        )
        viewModelScope.launch(Dispatchers.IO) {
            sendTextMessageUseCase.invoke(textMessage = textMessage).collect { sentSuccessfully ->
                Log.d(TAG, "sendTextMessage: message sent successfully : $sentSuccessfully")
            }
        }
    }

    fun observeSentMessage(){
        viewModelScope.launch(Dispatchers.IO) {
            getSentMessageUseCase.invoke().collect { textMessages ->
                _sentMessages.value = textMessages
            }
        }
    }

    fun observeReceivedMessage(){
        viewModelScope.launch(Dispatchers.IO) {
            getReceivedMessageUseCase.invoke().collect { textMessages ->
                _receivedMessages.value = textMessages
            }
        }
    }

}