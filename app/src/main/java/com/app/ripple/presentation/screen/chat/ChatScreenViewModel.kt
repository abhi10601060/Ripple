package com.app.ripple.presentation.screen.chat

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toLong
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.ripple.data.local.sharedpreferences.SharedprefConstants
import com.app.ripple.data.nearby.model.NearbyDevice
import com.app.ripple.data.nearby.model.Message
import com.app.ripple.domain.model.NearbyDeviceDomain
import com.app.ripple.domain.use_case.chat.GetReceivedMessageUseCase
import com.app.ripple.domain.use_case.chat.GetSentMessageUseCase
import com.app.ripple.domain.use_case.chat.SendTextMessageUseCase
import com.app.ripple.domain.use_case.nearby.ConnectDeviceUseCase
import com.app.ripple.domain.use_case.nearby.GetNearbyDeviceByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.core.content.edit
import com.app.ripple.data.nearby.model.DeliveryStatus
import com.app.ripple.data.nearby.model.MessageType
import com.app.ripple.domain.use_case.chat.SendFileUseCase
import com.app.ripple.presentation.notification.ChatNotificationManager
import com.app.ripple.util.FileInfo
import com.app.ripple.util.UriHelper
import kotlin.math.log

@HiltViewModel
class ChatScreenViewModel @Inject constructor(
    private val sendTextMessageUseCase: SendTextMessageUseCase,
    private val sendFileUseCase: SendFileUseCase,
    private val getSentMessageUseCase: GetSentMessageUseCase,
    private val getReceivedMessageUseCase: GetReceivedMessageUseCase,
    private val getNearbyDeviceByIdUseCase: GetNearbyDeviceByIdUseCase,
    private val connectDeviceUseCase: ConnectDeviceUseCase,
    private val sharedPreferences: SharedPreferences,
    private val chatNotificationManager: ChatNotificationManager,
    private val uriHelper: UriHelper
) : ViewModel() {

    private val TAG = "ChatScreenViewModel"

    private lateinit var receiverDevice: NearbyDevice
    private lateinit var androidId: String

    private val _receiverDeviceDomain = mutableStateOf<NearbyDeviceDomain?>(null)
    val receiverDeviceDomain: State<NearbyDeviceDomain?> = _receiverDeviceDomain

    @SuppressLint("HardwareIds")
    fun init(receiverDevice: NearbyDevice, context: Context) {
        this.receiverDevice = receiverDevice
        androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun sendTextMessage(payload: String){
        if (payload.isEmpty()) return

        val textMessage = Message(
            endpointId = receiverDevice.endpointId,
            senderId = androidId,
            receiverId = receiverDeviceDomain.value?.id.toString(),
            content = payload,
            messageType = MessageType.TEXT
        )
        viewModelScope.launch(Dispatchers.IO) {
            sendTextMessageUseCase.invoke(textMessage = textMessage).collect { sentSuccessfully ->
                Log.d(TAG, "sendTextMessage: message sent successfully : $sentSuccessfully")
            }
        }
    }

    fun sendFileMetadataMessage(uri: Uri?){
        val path = uri?.toString() ?: return

        val fileInfo = uriHelper.getFileInfoFromUri(uri = uri)

        Log.d(TAG, "sendFileMetadataMessage: $path")
        Log.d(TAG, "sendFileMetadataMessage: filename : ${fileInfo.name}, size: ${fileInfo.size}, type: ${fileInfo.mimeType}")
        val metadataMessage = Message(
            endpointId = receiverDevice.endpointId,
            senderId = androidId,
            receiverId = receiverDeviceDomain.value?.id.toString(),
            content = path,
            messageType = MessageType.METADATA,
            deliveryStatus = DeliveryStatus.PENDING,
            fileName = fileInfo.name,
            payloadSize = fileInfo.size.toFloat(),
            mimeType = fileInfo.mimeType
        )

        viewModelScope.launch(Dispatchers.IO) {
            sendFileUseCase.invoke(metadataMessage = metadataMessage).collect { sentSuccessfully ->
                Log.d(TAG, "sendFileMetadataMessage: message sent successfully : $sentSuccessfully")
            }
        }
    }

    fun observeReceiverDevice(){
        viewModelScope.launch(Dispatchers.IO) {
            getNearbyDeviceByIdUseCase.invoke(id = receiverDevice.id).collect {
                _receiverDeviceDomain.value = it
            }
        }
    }

    fun connectToDevice(device: NearbyDeviceDomain){
        viewModelScope.launch {
            connectDeviceUseCase.invoke(deviceId = device.endpointId).collect { isConnected ->
                Log.d(TAG, "connected to Device: ${device.deviceName} : $isConnected")
            }
        }
    }

    fun setChatScreenVisibleFor(userId: String){
        sharedPreferences.edit {
            putString(SharedprefConstants.VISIBLE_CHAT_SCREEN_USER.name, userId)
        }
        chatNotificationManager.removeChatNotification(userId = userId)
    }

    fun removeChatScreeIsVisibleFor(){
        sharedPreferences.edit { remove(SharedprefConstants.VISIBLE_CHAT_SCREEN_USER.name) }
    }

}