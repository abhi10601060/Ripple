package com.app.ripple.data.nearby

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import com.app.ripple.data.local.contract.NearbyDevicePersistenceRepo
import com.app.ripple.data.local.contract.TextMessagePersistenceRepo
import com.app.ripple.data.nearby.dto.TextMessageDto
import com.app.ripple.data.nearby.dto.toTextMessageDto
import com.app.ripple.data.nearby.model.ClusterInfo
import com.app.ripple.data.nearby.model.ConnectionState
import com.app.ripple.data.nearby.model.DeliveryStatus
import com.app.ripple.data.nearby.model.NearbyDevice
import com.app.ripple.data.nearby.model.TextMessage
import com.app.ripple.data.nearby.model.toTextMessageRealm
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
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resumeWithException

class NearbyShareManager private constructor(
    private val context: Context,
    private val nearbyDevicePersistenceRepo: NearbyDevicePersistenceRepo,
    private val textMessagePersistenceRepo: TextMessagePersistenceRepo,
    private val connectionRequestNotificationManager: ConnectionRequestNotificationManager
) {

    private val TAG = "NearbyShareManager"
    private val connectionPool = mutableStateMapOf<String, String>()
    private var iRejected = false

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: NearbyShareManager? = null

        fun getInstance(context: Context,
                        nearbyDevicePersistenceRepo: NearbyDevicePersistenceRepo,
                        textMessagePersistenceRepo: TextMessagePersistenceRepo,
                        connectionRequestNotificationManager: ConnectionRequestNotificationManager
        ): NearbyShareManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NearbyShareManager(
                    context.applicationContext,
                    nearbyDevicePersistenceRepo,
                    textMessagePersistenceRepo,
                    connectionRequestNotificationManager
                ).also { INSTANCE = it }
            }
        }
    }

    private val _discoveredDevices = MutableStateFlow<List<NearbyDevice>>(emptyList())
    private val _connectedDevices = MutableStateFlow<List<NearbyDevice>>(emptyList())
    private val _receivedMessages = MutableStateFlow<List<TextMessage>>(emptyList())
    private val _sentMessages = MutableStateFlow<List<TextMessage>>(emptyList())
    private val _clusterInfo = MutableStateFlow<ClusterInfo?>(null)
    private val _advertisingState = MutableStateFlow(false)
    private val _discoveryState = MutableStateFlow(false)

    // State accessors
    val discoveredDevices: StateFlow<List<NearbyDevice>> = _discoveredDevices.asStateFlow()
    val connectedDevices: StateFlow<List<NearbyDevice>> = _connectedDevices.asStateFlow()
    val receivedMessages: StateFlow<List<TextMessage>> = _receivedMessages.asStateFlow()
    val sentMessages: StateFlow<List<TextMessage>> = _sentMessages.asStateFlow()
    val clusterInfo: StateFlow<ClusterInfo?> = _clusterInfo.asStateFlow()
    val isAdvertising: StateFlow<Boolean> = _advertisingState.asStateFlow()
    val isDiscovering: StateFlow<Boolean> = _discoveryState.asStateFlow()

    // Nearby Connections API client
    private val connectionsClient: ConnectionsClient = Nearby.getConnectionsClient(context)

    @SuppressLint("HardwareIds")
    private val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    private val deviceName = "${android.os.Build.MODEL}:${androidId}"
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
                id = info.endpointName.split(":")[1],
                endpointId = endpointId,
                deviceName = info.endpointName.split(":")[0],
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
                val receivedMessage = Gson().fromJson(serialisedMessage, TextMessageDto::class.java)
                val message = TextMessage(
                    content = receivedMessage.content,
                    senderId = receivedMessage.senderId,
                    receiverId = receivedMessage.receiverId,
                    deliveryStatus = DeliveryStatus.DELIVERED
                )

                GlobalScope.launch(Dispatchers.IO) {
                    textMessagePersistenceRepo.insertReceivedMessage(message.toTextMessageRealm())
                }

                addReceivedMessage(message)
                Log.d("NearbyShare", "Message received: ${message}")
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
                }
                PayloadTransferUpdate.Status.FAILURE -> {
                    Log.d("NearbyShare", "Payload transfer failed")

                    GlobalScope.launch(Dispatchers.IO) {
                        textMessagePersistenceRepo.updateDeliveryStatus(update.payloadId, DeliveryStatus.DELIVERED)
                    }

                    updateMessageDeliveryStatus(endpointId, DeliveryStatus.FAILED)
                }
                PayloadTransferUpdate.Status.IN_PROGRESS -> {
                    Log.d("NearbyShare", "Payload transfer in progress")
                }
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

    // Public API Methods
    fun startAdvertising(): Flow<Boolean> = flow {
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
                deviceName,
                deviceId,
                connectionLifecycleCallback
            ).await()
            Log.d(TAG, "connectToDevice: Success")
            emit(true)
        }
        catch (e : Exception){
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

    fun sendTextMessage(message: TextMessage): Flow<Boolean> = flow {
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
            addSentMessage(message.copy(deliveryStatus = DeliveryStatus.FAILED))
            Log.d(TAG, "sendTextMessage: Error : ${e.message}")
            emit(false)
        }
    }

    fun createCluster(): Flow<String> = flow {
        val clusterId = java.util.UUID.randomUUID().toString()
        val cluster = ClusterInfo(
            clusterId = clusterId,
            devices = listOf(NearbyDevice("123",deviceName, deviceName, ConnectionState.CONNECTED)),
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

    private fun updateDeviceConnectionState(deviceId: String, state: ConnectionState) {
        // Update in Realm
        GlobalScope.launch(Dispatchers.IO) {
            nearbyDevicePersistenceRepo.updateConnectionState(deviceId, state)
        }

        // Update in discovered devices
        _discoveredDevices.value = _discoveredDevices.value.map { device ->
            if (device.endpointId == deviceId) device.copy(connectionState = state) else device
        }

        // Update connected devices list
        when (state) {
            ConnectionState.CONNECTED -> {
                val device = _discoveredDevices.value.find { it.endpointId == deviceId }
                device?.let {
                    val connectedList = _connectedDevices.value.toMutableList()
                    if (!connectedList.any { it.endpointId == deviceId }) {
                        connectedList.add(it.copy(connectionState = state))
                        _connectedDevices.value = connectedList
                    }
                }
            }
            ConnectionState.DISCONNECTED -> {
                _connectedDevices.value = _connectedDevices.value.filter { it.endpointId != deviceId }
            }
            else -> {
                _connectedDevices.value = _connectedDevices.value.map { device ->
                    if (device.endpointId == deviceId) device.copy(connectionState = state) else device
                }
            }
        }
    }

    private fun addReceivedMessage(message: TextMessage) {
//        GlobalScope.launch {
//            val returnMessage = TextMessage(
//                content = "Message received",
//                senderId = deviceName,
//                receiverId = message.senderId,
//                deliveryStatus = DeliveryStatus.DELIVERED
//            )
//            sendTextMessage(returnMessage).collect {
//                if (it) Log.d("NearbyShare", "Message sent successfully")
//            }
//        }
        _receivedMessages.value = _receivedMessages.value + message
    }

    private fun addSentMessage(message: TextMessage) {
        _sentMessages.value = _sentMessages.value + message
    }

    private fun updateMessageDeliveryStatus(endpointId: String, status: DeliveryStatus) {
        _sentMessages.value = _sentMessages.value.map { message ->
            if (message.receiverId == endpointId) message.copy(deliveryStatus = status) else message
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
