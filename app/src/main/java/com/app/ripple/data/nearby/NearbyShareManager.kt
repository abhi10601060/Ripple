package com.app.ripple.data.nearby

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import android.util.Log
import com.app.ripple.data.local.contract.NearbyDevicePersistenceRepo
import com.app.ripple.data.local.contract.TextMessagePersistenceRepo
import com.app.ripple.data.local.sharedpreferences.SharedprefConstants
import com.app.ripple.data.nearby.dto.MessageDto
import com.app.ripple.data.nearby.dto.toTextMessageDto
import com.app.ripple.data.nearby.model.ClusterInfo
import com.app.ripple.data.nearby.model.ConnectionState
import com.app.ripple.data.nearby.model.DeliveryStatus
import com.app.ripple.data.nearby.model.NearbyDevice
import com.app.ripple.data.nearby.model.Message
import com.app.ripple.data.nearby.model.MessageType
import com.app.ripple.data.nearby.model.toTextMessageRealm
import com.app.ripple.data.nearby.util.UserMessageQueue
import com.app.ripple.presentation.notification.ChatNotificationManager
import com.app.ripple.presentation.notification.ConnectionRequestNotificationManager
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resumeWithException
import androidx.core.net.toUri
import com.app.ripple.util.getRippleFormattedFileName

class NearbyShareManager private constructor(
    private val context: Context,
    private val nearbyDevicePersistenceRepo: NearbyDevicePersistenceRepo,
    private val sharedPreferences: SharedPreferences,
    private val textMessagePersistenceRepo: TextMessagePersistenceRepo,
    private val connectionRequestNotificationManager: ConnectionRequestNotificationManager,
    private val chatNotificationManager: ChatNotificationManager
) {

    private val TAG = "NearbyShareManager"
    private var iRejected = false

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: NearbyShareManager? = null

        fun getInstance(context: Context,
                        nearbyDevicePersistenceRepo: NearbyDevicePersistenceRepo,
                        sharedPreferences: SharedPreferences,
                        textMessagePersistenceRepo: TextMessagePersistenceRepo,
                        connectionRequestNotificationManager: ConnectionRequestNotificationManager,
                        chatNotificationManager: ChatNotificationManager
        ): NearbyShareManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NearbyShareManager(
                    context.applicationContext,
                    nearbyDevicePersistenceRepo,
                    sharedPreferences,
                    textMessagePersistenceRepo,
                    connectionRequestNotificationManager,
                    chatNotificationManager,
                ).also { INSTANCE = it }
            }
        }
    }

    private val _discoveredDevices = MutableStateFlow<List<NearbyDevice>>(emptyList())
    private val _connectedDevices = MutableStateFlow<List<NearbyDevice>>(emptyList())
    private val _receivedMessages = MutableStateFlow<List<Message>>(emptyList())
    private val _sentMessages = MutableStateFlow<List<Message>>(emptyList())
    private val _clusterInfo = MutableStateFlow<ClusterInfo?>(null)
    private val _advertisingState = MutableStateFlow(false)
    private val _discoveryState = MutableStateFlow(false)

    // State accessors
    val discoveredDevices: StateFlow<List<NearbyDevice>> = _discoveredDevices.asStateFlow()
    val connectedDevices: StateFlow<List<NearbyDevice>> = _connectedDevices.asStateFlow()
    val receivedMessages: StateFlow<List<Message>> = _receivedMessages.asStateFlow()
    val sentMessages: StateFlow<List<Message>> = _sentMessages.asStateFlow()
    val clusterInfo: StateFlow<ClusterInfo?> = _clusterInfo.asStateFlow()
    val isAdvertising: StateFlow<Boolean> = _advertisingState.asStateFlow()
    val isDiscovering: StateFlow<Boolean> = _discoveryState.asStateFlow()

    // Nearby Connections API client
    private val connectionsClient: ConnectionsClient = Nearby.getConnectionsClient(context)

    // For File Sharing per nearby connection message queue managers
    private val fileMessageHandlers = mutableMapOf<String, UserMessageQueue>()
    private val incomingFiles = mutableMapOf<Long, Payload>()
    private val outgoingFiles = mutableMapOf<Long, Payload>()
    private val incomingFileMetadata = mutableMapOf<Long, Message>()
    private val outgoingFileMetadata = mutableMapOf<Long, Message>()

    @SuppressLint("HardwareIds")
    private val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    private val deviceModel = android.os.Build.MODEL
    private val serviceId = "com.app.ripple"

    // Connection lifecycle callbacks
    @OptIn(DelicateCoroutinesApi::class)
    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            Log.d("NearbyShare", "Connection initiated with: ${info.endpointName} : $endpointId")
            if(info.isIncomingConnection) connectionRequestNotificationManager.showConnectionRequestNotification(deviceName= info.endpointName, endpointId = endpointId)
            else connectionsClient.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    Log.d("NearbyShare", "Connected to: $endpointId")
                    discoveredDevices.value.forEach {
                        if (it.endpointId == endpointId){
                            connectionRequestNotificationManager.showConnectionAcceptedNotification(it.deviceName)
                            return@forEach
                        }
                    }
                    updateDeviceConnectionState(endpointId, ConnectionState.CONNECTED)
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    Log.d("NearbyShare", "Connection rejected: $endpointId")
                    result.status.status
                    if (!iRejected){
                        discoveredDevices.value.forEach {
                            if (it.endpointId == endpointId){
                                connectionRequestNotificationManager.showConnectionRejectedNotification(it.deviceName)
                                return@forEach
                            }
                        }
                    }

                    updateDeviceConnectionState(endpointId, ConnectionState.DISCONNECTED)
                }
                else -> {
                    Log.d("NearbyShare", "Connection failed: $endpointId")
                    updateDeviceConnectionState(endpointId, ConnectionState.ERROR)
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.d("NearbyShare", "Disconnected from: $endpointId")
            discoveredDevices.value.forEach {
                if (it.endpointId == endpointId){
                    connectionRequestNotificationManager.showDeviceDisconnectedNotification(it.deviceName)
                    return@forEach
                }
            }
            updateDeviceConnectionState(endpointId, ConnectionState.DISCONNECTED)
        }
    }

    // Endpoint discovery callbacks
    @OptIn(DelicateCoroutinesApi::class)
    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Log.d("NearbyShare", "Endpoint found: ${info.endpointName}")
            val device = NearbyDevice(
                id = info.endpointName.split(":")[2],
                endpointId = endpointId,
                deviceName = info.endpointName.split(":")[0],
                model = info.endpointName.split(":")[1],
                connectionState = ConnectionState.DISCOVERED
            )

            GlobalScope.launch(Dispatchers.IO) {
                nearbyDevicePersistenceRepo.upsertDiscoveredNearbyDevice(nearbyDevice = device)
            }

            addDiscoveredDevice(device)
        }

        override fun onEndpointLost(endpointId: String) {
            Log.d("NearbyShare", "Endpoint lost: $endpointId")
            updateDeviceConnectionState(endpointId, ConnectionState.LOST)
            removeDiscoveredDevice(endpointId)
        }
    }

    // Payload callbacks for handling messages
    @OptIn(DelicateCoroutinesApi::class)
    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type == Payload.Type.BYTES) {
                val serialisedMessage = String(payload.asBytes()!!, Charsets.UTF_8)
                Log.d(TAG, "onPayloadReceived: $serialisedMessage")
                val receivedMessage = Gson().fromJson(serialisedMessage, MessageDto::class.java)
                val message = Message(
                    id = receivedMessage.id,
                    content = receivedMessage.content,
                    senderId = receivedMessage.senderId,
                    receiverId = receivedMessage.receiverId,
                    deliveryStatus = DeliveryStatus.DELIVERED,
                    messageType = receivedMessage.messageType,
                    payloadSize = receivedMessage.payloadSize,
                    fileName = getRippleFormattedFileName(receivedMessage.fileName),
                    mimeType = receivedMessage.mimeType
                )

                if (message.messageType == MessageType.METADATA){
                    handleReceivingFileMetadata(message)
                }

                // Add tgo realm
                GlobalScope.launch(Dispatchers.IO) {
                    textMessagePersistenceRepo.insertReceivedMessage(message.toTextMessageRealm())
                }

                // Show notification
                GlobalScope.launch(Dispatchers.IO) {
                    nearbyDevicePersistenceRepo.getNearbyDeviceById(message.senderId).take(1).collect { nearbyDevice ->
                        launch(Dispatchers.Main) {
                            Log.d(TAG, "onPayloadReceived: get device in payload ${nearbyDevice?.deviceName}")
                            chatNotificationManager.showChatMessage(
                                userId = message.senderId,
                                userName = nearbyDevice?.deviceName.toString(),
                                messageText = message.content
                            )
                        }
                    }
                }

                addReceivedMessage(message)
                Log.d("NearbyShare", "Message received: ${message}")
            }
            else if(payload.type == Payload.Type.FILE){
                Log.d(TAG, "onPayloadReceived: receiving File Payload : ${payload.id}")
                incomingFiles[payload.id] = payload
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            when (update.status) {
                PayloadTransferUpdate.Status.SUCCESS -> {
                    Log.d("NearbyShare", "Payload transfer successful")

                    GlobalScope.launch(Dispatchers.IO) {
                        textMessagePersistenceRepo.updateDeliveryStatus(update.payloadId, DeliveryStatus.DELIVERED)
                    }

                    updateMessageDeliveryStatus(endpointId, DeliveryStatus.DELIVERED)
                    checkAndUpdateFileDeliveryStatus(endpointId, update)
                }
                PayloadTransferUpdate.Status.FAILURE -> {
                    Log.d("NearbyShare", "Payload transfer failed")

                    GlobalScope.launch(Dispatchers.IO) {
                        textMessagePersistenceRepo.updateDeliveryStatus(update.payloadId, DeliveryStatus.FAILED)
                    }

                    updateMessageDeliveryStatus(endpointId, DeliveryStatus.FAILED)
                }
                PayloadTransferUpdate.Status.IN_PROGRESS -> {
                    // Receiver Side
                    val incomingPayload = incomingFiles[update.payloadId]
                    Log.d("NearbyShare", "Payload transfer in progress for : ${incomingPayload?.type}")

                    if(incomingPayload != null && incomingPayload.type == Payload.Type.FILE){
                        CoroutineScope(Dispatchers.IO).launch{
                            updateFileTransferProgress( update = update)
                        }
                    }

                    // sender side
                    val outGoingPayload = fileMessageHandlers[endpointId]?.getPayloadById(update.payloadId)

                    if(outGoingPayload != null && outGoingPayload.type == Payload.Type.FILE){
                        CoroutineScope(Dispatchers.IO).launch{
                            updateFileTransferProgress(endpointId, update)
                        }
                    }
                }

                // TODO: Theres CANCELED option as well to use for file transfer cancel  
            }
        }
    }

    // Connection Actions
    fun acceptConnection(endpointId: String) {
        Log.d(TAG, "acceptConnection: $endpointId")
        connectionsClient.acceptConnection(endpointId, payloadCallback)
    }

    fun rejectConnection(endpointId: String){
        connectionsClient.rejectConnection(endpointId)
        this.iRejected = true
    }

    fun getDeviceName(): String {
        val savedUserName = sharedPreferences.getString(SharedprefConstants.USER_NAME.name, "")
        return "${savedUserName}:${android.os.Build.MODEL}:${androidId}"
    }

    // Public API Methods
    fun startAdvertising(): Flow<Boolean> = flow {
        val deviceName = getDeviceName()

        val options = AdvertisingOptions.Builder()
            .setStrategy(Strategy.P2P_CLUSTER)
            .build()

        try {
            val result = connectionsClient.startAdvertising(
                deviceName,
                serviceId,
                connectionLifecycleCallback,
                options
            ).await()
            Log.d(TAG, "startAdvertising: success : $result")
            _advertisingState.value = true
            emit(true)
        }
        catch (e : Exception){
            if (e is CancellationException) throw e
            Log.d(TAG, "startAdvertising: Error : ${e.message}")
            _advertisingState.value = false
            emit(false)
        }

    }

    fun stopAdvertising(): Flow<Boolean> = flow {
        connectionsClient.stopAdvertising()
        _advertisingState.value = false
        emit(true)
    }

    fun startDiscovery(): Flow<Boolean> = flow {
        val options = DiscoveryOptions.Builder()
            .setStrategy(Strategy.P2P_CLUSTER)
            .build()

        try {
            val result = connectionsClient.startDiscovery(
                serviceId,
                endpointDiscoveryCallback,
                options
            ).await()
            Log.d(TAG, "startDiscovery: Success")
            _discoveryState.value = true
            emit(true)
        }
        catch (e : Exception){
            if (e is CancellationException) throw e
            Log.d(TAG, "startDiscovery: Error : ${e.message}")
            _discoveryState.value = false
            emit(false)
        }
    }

    fun stopDiscovery(): Flow<Boolean> = flow {
        connectionsClient.stopDiscovery()
        _discoveryState.value = false
        emit(true)
    }

    fun connectToDevice(deviceId: String): Flow<Boolean> = flow {
        updateDeviceConnectionState(deviceId, ConnectionState.CONNECTING)
        try {
            val result = connectionsClient.requestConnection(
                getDeviceName(),
                deviceId,
                connectionLifecycleCallback
            ).await()
            Log.d(TAG, "connectToDevice: Success")
            emit(true)
        }
        catch (e : Exception){
            if (e is CancellationException) throw e
            Log.d(TAG, "connectToDevice: Error : ${e.message}")
            emit(false)
        }
    }

    fun disconnectFromDevice(deviceId: String): Flow<Boolean> = flow {
        connectionsClient.disconnectFromEndpoint(deviceId)
        updateDeviceConnectionState(deviceId, ConnectionState.DISCONNECTED)
        emit(true)
    }

    fun stopAllEndpoints(){
        connectionsClient.stopAllEndpoints()
    }

    fun sendMessage(message: Message): Flow<Boolean> = flow {
        val serialisedMessage = Gson().toJson(message.toTextMessageDto())
        Log.d(TAG, "sendTextMessage: $serialisedMessage")
        val payload = Payload.fromBytes(serialisedMessage.toByteArray(Charsets.UTF_8))
        message.id = payload.id
        try {
            val result = connectionsClient.sendPayload(message.endpointId, payload).await()

            GlobalScope.launch(Dispatchers.IO) {
                textMessagePersistenceRepo.insertSentMessage(message.toTextMessageRealm())
            }

            addSentMessage(message.copy(deliveryStatus = DeliveryStatus.SENT))
            Log.d(TAG, "sendTextMessage: Success")
            emit(true)
        }catch (e : Exception){
            if (e is CancellationException) throw e
            addSentMessage(message.copy(deliveryStatus = DeliveryStatus.FAILED))
            Log.d(TAG, "sendTextMessage: Error : ${e.message}")
            emit(false)
        }
    }

    fun createCluster(): Flow<String> = flow {
        val clusterId = java.util.UUID.randomUUID().toString()
        val cluster = ClusterInfo(
            clusterId = clusterId,
            devices = listOf(NearbyDevice("123",getDeviceName(), getDeviceName(),"xyzModel", ConnectionState.CONNECTED)),
            isActive = true
        )
        _clusterInfo.value = cluster
        emit(clusterId)
    }

    fun joinCluster(clusterId: String): Flow<Boolean> = flow {
        // In a real implementation, you'd need to discover and connect to cluster members
        val cluster = ClusterInfo(
            clusterId = clusterId,
            devices = _connectedDevices.value,
            isActive = true
        )
        _clusterInfo.value = cluster
        emit(true)
    }

    fun leaveCluster(): Flow<Boolean> = flow {
        _clusterInfo.value = _clusterInfo.value?.copy(isActive = false)
        // Disconnect from all devices in cluster
        _connectedDevices.value.forEach { device ->
            connectionsClient.disconnectFromEndpoint(device.endpointId)
        }
        _connectedDevices.value = emptyList()
        emit(true)
    }

    // Private helper methods
    private fun addDiscoveredDevice(device: NearbyDevice) {
        val currentDevices = _discoveredDevices.value.toMutableList()
        val existingIndex = currentDevices.indexOfFirst { it.endpointId == device.endpointId }
        if (existingIndex >= 0) {
            currentDevices[existingIndex] = device
        } else {
            currentDevices.add(device)
        }
        _discoveredDevices.value = currentDevices
    }

    private fun removeDiscoveredDevice(deviceId: String) {
        _discoveredDevices.value = _discoveredDevices.value.filter { it.endpointId != deviceId }
    }

    private fun updateDeviceConnectionState(endpointId: String, state: ConnectionState) {
        // Update in Realm
        GlobalScope.launch(Dispatchers.IO) {
            nearbyDevicePersistenceRepo.updateConnectionState(endpointId, state)
        }

        // manage file message queue handlers
        updateUserFileMessageQueueOnConnectionStateChange(endpointId, state)

        // Update in discovered devices
        _discoveredDevices.value = _discoveredDevices.value.map { device ->
            if (device.endpointId == endpointId) device.copy(connectionState = state) else device
        }

        // Update connected devices list
        when (state) {
            ConnectionState.CONNECTED -> {
                val device = _discoveredDevices.value.find { it.endpointId == endpointId }
                device?.let {
                    val connectedList = _connectedDevices.value.toMutableList()
                    if (!connectedList.any { it.endpointId == endpointId }) {
                        connectedList.add(it.copy(connectionState = state))
                        _connectedDevices.value = connectedList
                    }
                }
            }
            ConnectionState.DISCONNECTED -> {
                _connectedDevices.value = _connectedDevices.value.filter { it.endpointId != endpointId }
            }
            else -> {
                _connectedDevices.value = _connectedDevices.value.map { device ->
                    if (device.endpointId == endpointId) device.copy(connectionState = state) else device
                }
            }
        }
    }

    private fun addReceivedMessage(message: Message) {
        _receivedMessages.value = _receivedMessages.value + message
    }

    private fun addSentMessage(message: Message) {
        _sentMessages.value = _sentMessages.value + message
    }

    private fun updateMessageDeliveryStatus(endpointId: String, status: DeliveryStatus) {
        _sentMessages.value = _sentMessages.value.map { message ->
            if (message.receiverId == endpointId) message.copy(deliveryStatus = status) else message
        }
    }

    // Mark : File Message Queue Handlers

    private fun updateUserFileMessageQueueOnConnectionStateChange(endpointId: String, connectionState: ConnectionState){
        if(connectionState == ConnectionState.LOST || connectionState == ConnectionState.DISCONNECTED || connectionState == ConnectionState.ERROR){
            for ((connEndPointId, queueHandler) in fileMessageHandlers){
                if (endpointId == connEndPointId){
                    queueHandler.cancelAllFileTransfers()
                }
            }
        }
        else if(connectionState == ConnectionState.CONNECTED){
            fileMessageHandlers[endpointId] = UserMessageQueue(this)
        }
    }

    fun queueFileForShare(message: Message): Flow<Boolean> = flow {
        try {
            val fileMessageHandler = fileMessageHandlers[message.endpointId]
            val uri = message.content.toUri()

            // Create FILE payload
            Log.d(TAG, "queueFileForShare: ${uri.toString()}")

            val pfd = context.contentResolver.openFileDescriptor(uri, "r")
            val filePayload = Payload.fromFile(pfd!!)

            message.id = filePayload.id
            fileMessageHandler?.addMessage(message, filePayload)

            // Adding to realm so sender can see metadata message in chat screen
            CoroutineScope(Dispatchers.IO).launch(Dispatchers.IO) {
                textMessagePersistenceRepo.insertSentMessage(message.toTextMessageRealm())
            }
            emit(true)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.d(TAG, "queueFileForShare: Error : ${e.message}")
            emit(false)
        }
    }

    private fun sendFileMetaData(message: Message): Flow<Boolean>  = flow {
        val serialisedMessage = Gson().toJson(message.toTextMessageDto())
        Log.d(TAG, "sendTextMessage: $serialisedMessage")
        val payload = Payload.fromBytes(serialisedMessage.toByteArray(Charsets.UTF_8))

        try {
            val result = connectionsClient.sendPayload(message.endpointId, payload).await()

            outgoingFileMetadata[message.id] = message

            addSentMessage(message.copy(deliveryStatus = DeliveryStatus.SENT))
            Log.d(TAG, "sendTextMessage: Success")
            emit(true)
        }catch (e : Exception){
            if (e is CancellationException) throw e
            addSentMessage(message.copy(deliveryStatus = DeliveryStatus.FAILED))
            Log.d(TAG, "sendTextMessage: Error : ${e.message}")
            emit(false)
        }
    }

    fun sendFile(message: Message, filePayload: Payload) {
        Log.d(TAG, "sendFile: sending file Metadata for : ${message.fileName}")
        try{
            CoroutineScope(Dispatchers.IO).launch {
                val isSuccessful = sendFileMetaData(message).first()

                if (isSuccessful){
                    // Create FILE payload
                    Log.d(TAG, "sendFile: sending ")
                    outgoingFiles[filePayload.id] = filePayload
                    connectionsClient.sendPayload(message.endpointId, filePayload)
                }
            }
        }
        catch (e : Exception){
            // TODO: On transfer failed or interrupted 
        }
    }

    // For Receiver Side File Handeling

    private fun handleReceivingFileMetadata(message: Message){
        incomingFileMetadata[message.id] = message
        // TODO: may be show different notification for incoming file
    }

    private fun updateFileTransferProgress(receiverEndpointId : String = "", update: PayloadTransferUpdate){
        if (update.totalBytes > 0){
            val percent = ((update.bytesTransferred * 100) / update.totalBytes).toInt()

            var metadataMessage : Message?

            if (receiverEndpointId.isEmpty()){
                metadataMessage = incomingFileMetadata[update.payloadId]
            }
            else{
                metadataMessage = fileMessageHandlers[receiverEndpointId]?.getMetadataMessageById(update.payloadId)
            }

            metadataMessage?.let {
                if( percent == 100 || metadataMessage.progress % 10 < percent % 10){
                    CoroutineScope(Dispatchers.IO).launch {
                        textMessagePersistenceRepo.updateMessageProgress(metadataMessage.id, percent / 100f)
                    }
                }
            }

        }
    }

    private fun checkAndUpdateFileDeliveryStatus(senderOrReceiverEndpointId: String, update: PayloadTransferUpdate){
        // For Sender side
        val fileMessageQueueHandler = fileMessageHandlers[senderOrReceiverEndpointId]
        fileMessageQueueHandler?.onFileTransferredSuccessfully(update.payloadId)
        CoroutineScope(Dispatchers.IO).launch {
            textMessagePersistenceRepo.updateMessageProgress(update.payloadId, 1f)
        }

        // For Receiver Side
        val payload = incomingFiles[update.payloadId]
        Log.d(TAG, "checkAndUpdateFileDeliveryStatus: ${payload?.type} : ${payload?.id}")
        if (payload != null && payload.type == Payload.Type.FILE){
            CoroutineScope(Dispatchers.IO).launch {
                saveFile(payload)
            }
        }
    }

    private fun saveFile(payload: Payload){
        Log.d(TAG, "saveFile: saving file : ${payload.id}")
        // getting metadata for file
        val metadata = incomingFileMetadata[payload.id] ?: return

        try {
            // Get the temporary file URI from Nearby Connections
            val tempUri: Uri = payload.asFile()?.asUri() ?: return
            Log.d(TAG, "saveFile: got temp uri to save file : ${tempUri.toString()}")
            // Create Ripple folder in Downloads if it doesn't exist
            val rippleFolder = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "Ripple"
            )

            if (!rippleFolder.exists()) {
                rippleFolder.mkdirs()
            }

            // Create final file with proper name
            val finalFile = File(rippleFolder,  metadata.fileName)

            // Copy from temporary location to final location
            context.contentResolver.openInputStream(tempUri)?.use { inputStream ->
                FileOutputStream(finalFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            // Delete the temporary file
            context.contentResolver.delete(tempUri, null, null)

            Log.d("FileReceiver", "File saved: ${finalFile.absolutePath}")

            CoroutineScope(Dispatchers.IO).launch {
                textMessagePersistenceRepo.updateMessageProgress(payload.id, 1f)
            }

        } catch (e: Exception) {
            Log.e("FileReceiver", "Failed to save file: ${metadata.content}", e)
        }
    }
}

suspend fun <T> Task<T>.await(): T {
    return suspendCancellableCoroutine { cont ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) {
                cont.resume(task.result) { cause, _, _ -> }
            } else {
                cont.resumeWithException(task.exception ?: Exception("Unknown error"))
            }
        }
    }
}
