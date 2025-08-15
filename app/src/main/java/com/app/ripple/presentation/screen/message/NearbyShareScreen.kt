package com.app.ripple.presentation.screen.message

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.ripple.data.nearby.model.ClusterInfo
import com.app.ripple.data.nearby.model.ConnectionState
import com.app.ripple.data.nearby.model.DeliveryStatus
import com.app.ripple.data.nearby.model.NearbyDevice
import com.app.ripple.data.nearby.model.TextMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyShareScreen(
    modifier: Modifier,
    viewModel: NearbyShareViewModel = viewModel()
) {
    val uiState by viewModel.uiState.observeAsState(NearbyShareUiState.Idle)
    val discoveredDevices by viewModel.discoveredDevices.observeAsState(emptyList())
    val connectedDevices by viewModel.connectedDevices.observeAsState(emptyList())
    val messages by viewModel.messages.observeAsState(emptyList())
    val clusterInfo by viewModel.clusterInfo.observeAsState()

    var showMessageDialog by remember { mutableStateOf(false) }
    var selectedDeviceId by remember { mutableStateOf("") }
    var messageText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with status
        StatusCard(uiState = uiState)

        Spacer(modifier = Modifier.height(16.dp))

        // Control buttons
        ControlButtonsSection(
            uiState = uiState,
            onStartAdvertising = { viewModel.startAdvertising(); viewModel.startDiscovery() },
            onStopAdvertising = { viewModel.stopAdvertising() },
            onStartDiscovery = {
//                viewModel.startDiscovery()
                },
            onStopDiscovery = {
//                viewModel.stopDiscovery()
                },
            onCreateCluster = { viewModel.createCluster() },
            onLeaveCluster = {
//                viewModel.leaveCluster()
                viewModel.stopAdvertising(); viewModel.stopDiscovery()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Cluster info
        clusterInfo?.let { it ->
            ClusterInfoCard(cluster = it)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Tabs for different sections
        var selectedTab by remember { mutableStateOf(0) }
        val tabs = listOf("Discovered", "Connected", "Messages")

        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = selectedTab == index,
                    onClick = { selectedTab = index }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Content based on selected tab
        when (selectedTab) {
            0 -> DiscoveredDevicesSection(
                devices = discoveredDevices,
                onConnectDevice = { deviceId ->
                    viewModel.connectToDevice(deviceId)
                }
            )
            1 -> ConnectedDevicesSection(
                devices = connectedDevices,
                onDisconnectDevice = { deviceId ->
                    viewModel.disconnectFromDevice(deviceId)
                },
                onSendMessage = { deviceId ->
                    selectedDeviceId = deviceId
                    showMessageDialog = true
                }
            )
            2 -> MessagesSection(messages = messages)
        }
    }

    // Message dialog
    if (showMessageDialog) {
        MessageDialog(
            messageText = messageText,
            onMessageTextChange = { messageText = it },
            onSendMessage = {
                if (messageText.isNotBlank()) {
                    viewModel.sendMessage(messageText, selectedDeviceId)
                    messageText = ""
                    showMessageDialog = false
                }
            },
            onDismiss = {
                showMessageDialog = false
                messageText = ""
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusCard(uiState: NearbyShareUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (uiState) {
                is NearbyShareUiState.Error -> MaterialTheme.colorScheme.errorContainer
                is NearbyShareUiState.Connected -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                is NearbyShareUiState.Advertising -> Color(0xFF2196F3).copy(alpha = 0.1f)
                is NearbyShareUiState.Discovering -> Color(0xFFFF9800).copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (uiState) {
                    is NearbyShareUiState.Error -> Icons.Default.Error
                    is NearbyShareUiState.Connected -> Icons.Default.CheckCircle
                    is NearbyShareUiState.Advertising -> Icons.Default.Wifi
                    is NearbyShareUiState.Discovering -> Icons.Default.Search
                    is NearbyShareUiState.Loading -> Icons.Default.Refresh
                    else -> Icons.Default.Info
                },
                contentDescription = null,
                tint = when (uiState) {
                    is NearbyShareUiState.Error -> MaterialTheme.colorScheme.error
                    is NearbyShareUiState.Connected -> Color(0xFF4CAF50)
                    is NearbyShareUiState.Advertising -> Color(0xFF2196F3)
                    is NearbyShareUiState.Discovering -> Color(0xFFFF9800)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = when (uiState) {
                    is NearbyShareUiState.Idle -> "Ready"
                    is NearbyShareUiState.Loading -> "Loading..."
                    is NearbyShareUiState.Advertising -> "Advertising to nearby devices"
                    is NearbyShareUiState.Discovering -> "Discovering nearby devices"
                    is NearbyShareUiState.Connecting -> "Connecting to device..."
                    is NearbyShareUiState.Connected -> "Device connected"
                    is NearbyShareUiState.MessageSent -> "Message sent successfully"
                    is NearbyShareUiState.ClusterCreated -> "Cluster created: ${uiState.clusterId}"
                    is NearbyShareUiState.ClusterJoined -> "Joined cluster successfully"
                    is NearbyShareUiState.Error -> uiState.message
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ControlButtonsSection(
    uiState: NearbyShareUiState,
    onStartAdvertising: () -> Unit,
    onStopAdvertising: () -> Unit,
    onStartDiscovery: () -> Unit,
    onStopDiscovery: () -> Unit,
    onCreateCluster: () -> Unit,
    onLeaveCluster: () -> Unit
) {
    val isAdvertising = uiState is NearbyShareUiState.Advertising
    val isDiscovering = uiState is NearbyShareUiState.Discovering
    val isLoading = uiState is NearbyShareUiState.Loading

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height(200.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionButton(
                    text = if (isAdvertising) "Stop Advertising" else "Start Advertising",
                    icon = if (isAdvertising) Icons.Default.Stop else Icons.Default.Wifi,
                    onClick = if (isAdvertising) onStopAdvertising else onStartAdvertising,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f),
                    color = if (isAdvertising) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )

                ActionButton(
                    text = if (isDiscovering) "Stop Discovery" else "Start Discovery",
                    icon = if (isDiscovering) Icons.Default.Stop else Icons.Default.Search,
                    onClick = if (isDiscovering) onStopDiscovery else onStartDiscovery,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f),
                    color = if (isDiscovering) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionButton(
                    text = "Create Cluster",
                    icon = Icons.Default.Add,
                    onClick = onCreateCluster,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f)
                )

                ActionButton(
                    text = "Leave Cluster",
                    icon = Icons.Default.ExitToApp,
                    onClick = onLeaveCluster,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClusterInfoCard(cluster: ClusterInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (cluster.isActive)
                Color(0xFF4CAF50).copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    tint = if (cluster.isActive) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cluster: ${cluster.clusterId.take(8)}...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = if (cluster.isActive) "Active" else "Inactive",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (cluster.isActive) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${cluster.devices.size} devices in cluster",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DiscoveredDevicesSection(
    devices: List<NearbyDevice>,
    onConnectDevice: (String) -> Unit
) {
    if (devices.isEmpty()) {
        EmptyStateMessage(
            icon = Icons.Default.Search,
            message = "No devices discovered.\nStart discovery to find nearby devices."
        )
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(devices) { device ->
                DeviceCard(
                    device = device,
                    actionText = "Connect",
                    actionIcon = Icons.Default.Link,
                    onAction = { onConnectDevice(device.deviceId) },
                    actionEnabled = device.connectionState != ConnectionState.CONNECTING
                )
            }
        }
    }
}

@Composable
fun ConnectedDevicesSection(
    devices: List<NearbyDevice>,
    onDisconnectDevice: (String) -> Unit,
    onSendMessage: (String) -> Unit
) {
    if (devices.isEmpty()) {
        EmptyStateMessage(
            icon = Icons.Default.DeviceHub,
            message = "No connected devices.\nConnect to devices first."
        )
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(devices) { device ->
                ConnectedDeviceCard(
                    device = device,
                    onDisconnect = { onDisconnectDevice(device.deviceId) },
                    onSendMessage = { onSendMessage(device.deviceId) }
                )
            }
        }
    }
}

@Composable
fun MessagesSection(messages: List<TextMessage>) {
    if (messages.isEmpty()) {
        EmptyStateMessage(
            icon = Icons.Default.Message,
            message = "No messages yet.\nSend messages to connected devices."
        )
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                MessageCard(message = message)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceCard(
    device: NearbyDevice,
    actionText: String,
    actionIcon: ImageVector,
    onAction: () -> Unit,
    actionEnabled: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.deviceName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = device.connectionState.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            FilledTonalButton(
                onClick = onAction,
                enabled = actionEnabled,
                modifier = Modifier.height(36.dp)
            ) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(actionText, fontSize = 12.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectedDeviceCard(
    device: NearbyDevice,
    onDisconnect: () -> Unit,
    onSendMessage: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF50).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.deviceName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Connected",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4CAF50)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onSendMessage,
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }

                OutlinedButton(
                    onClick = onDisconnect,
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LinkOff,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageCard(message: TextMessage) {
    val isOutgoing = message.senderId == android.os.Build.MODEL

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isOutgoing)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isOutgoing) Icons.Default.Send else Icons.Default.CallReceived,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (isOutgoing)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSecondaryContainer
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = if (isOutgoing) "To: ${message.receiverId}" else "From: ${message.senderId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isOutgoing)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSecondaryContainer
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = message.deliveryStatus.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = when (message.deliveryStatus) {
                        DeliveryStatus.DELIVERED -> Color(0xFF4CAF50)
                        DeliveryStatus.FAILED -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun EmptyStateMessage(
    icon: ImageVector,
    message: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageDialog(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Send Message")
        },
        text = {
            OutlinedTextField(
                value = messageText,
                onValueChange = onMessageTextChange,
                label = { Text("Message") },
                placeholder = { Text("Enter your message...") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = onSendMessage,
                enabled = messageText.isNotBlank()
            ) {
                Text("Send")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun NearbyShareScreenPreview() {
    MaterialTheme {
        // Preview would need mock ViewModel
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
//            NearbyShareScreen()
        }
    }
}