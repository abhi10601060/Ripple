package com.app.ripple.presentation.screen.chat

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.app.ripple.data.local.realm.model.toTextMessage
import com.app.ripple.data.nearby.model.ConnectionState
import com.app.ripple.data.nearby.model.DeviceVisibility
import com.app.ripple.data.nearby.model.NearbyDevice
import com.app.ripple.presentation.shared.CircularImage
import com.app.ripple.presentation.shared.Ripple
import com.app.ripple.presentation.shared.RippleTextField
import com.app.ripple.presentation.ui.theme.CourierPrimeFamily
import com.app.ripple.presentation.ui.theme.DarkBG
import com.app.ripple.presentation.ui.theme.MontserratFamily
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    receiverDevice: NearbyDevice,
    viewModel: ChatScreenViewModel
) {
    val context = LocalContext.current

    val receiverDeviceDomain by remember {
        viewModel.receiverDeviceDomain
    }

    val chatScrollState = rememberLazyListState()

    val allMessages by  remember {
        derivedStateOf {
            receiverDeviceDomain?.allMessages ?: listOf()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> { viewModel.setChatScreenVisibleFor( receiverDevice.id ) }
                Lifecycle.Event.ON_PAUSE -> { viewModel.removeChatScreeIsVisibleFor() }
                Lifecycle.Event.ON_CREATE -> {}
                Lifecycle.Event.ON_START -> {}
                Lifecycle.Event.ON_STOP -> {}
                Lifecycle.Event.ON_DESTROY -> {}
                Lifecycle.Event.ON_ANY -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(key1 = allMessages.size) {
        if (allMessages.isNotEmpty()){
            chatScrollState.animateScrollToItem(allMessages.size - 1)
        }

        launch {
            viewModel.observeReceiverDevice()
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.init(receiverDevice = receiverDevice, context = context)
        viewModel.observeSentMessage()
        viewModel.observeReceivedMessage()
    }

    var typedMessage by remember {
        mutableStateOf("")
    }

    var showAttachmentOptions by remember {
        mutableStateOf(false)
    }

    val sheetState = rememberModalBottomSheetState()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            // TODO: viewModel.sendFile(it)
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // TODO: viewModel.sendFile(it)
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
            .background(color = DarkBG)
    ){
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(bottom = 75.dp)
        ) {
            ChatScreenHeader(
                receiverDevice = receiverDevice,
                onBackClick = {
                    navController?.popBackStack()
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            LazyColumn(
                state = chatScrollState
            ) {
                items(items = allMessages){ message ->
                    TextMessageItem(textMessage = message, isFromCurrentUser = message.senderId != receiverDeviceDomain?.id)
                }
            }
        }

        // MARK: Bottom Floating Message Box
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(10.dp)
                .fillMaxWidth()
        ){
            if(receiverDeviceDomain?.visibility == DeviceVisibility.OFFLINE){
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ){
                    Text(text = "Device offline. Cannot connect...", color = Color.Gray, fontFamily = CourierPrimeFamily)
                }
            }
            else if(receiverDeviceDomain?.connectionState == ConnectionState.CONNECTED){
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        modifier = Modifier
                            .padding(end = 3.dp)
                            .size(30.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = false),
                                onClick = {
                            showAttachmentOptions = true
                        }
                    ),
                imageVector = Icons.Rounded.AttachFile,
                contentDescription = "Send Files",
                tint = Color.Gray
            )

                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ){
                        RippleTextField(
                            value = typedMessage,
                            onValueChange = {
                                typedMessage = it
                            },
                            placeholder = "Type a message..."
                        )

                        Ripple(
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .align(Alignment.CenterEnd)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(),
                                    onClick = {
                                        viewModel.sendTextMessage(typedMessage)
                                        typedMessage = ""
                                    }
                                ),
                            size= 40.dp
                        ) {
                            Icon(
                                modifier = Modifier
                                    .size(10.dp),
                                imageVector = Icons.Rounded.Send,
                                contentDescription = "Send Button",
                                tint = Color.Black
                            )
                        }
                    }
                }

            }
            else{
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .clickable{
                            receiverDeviceDomain?.let {
                                viewModel.connectToDevice(it)
                            }
                        }
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ){
                    Text(text = "Tap to connect...", color = Color.Gray, fontFamily = CourierPrimeFamily)
                }
            }

        }

        if (showAttachmentOptions) {
            ModalBottomSheet(
                onDismissRequest = { showAttachmentOptions = false },
                sheetState = sheetState,
                containerColor = DarkBG
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp, start = 16.dp, end = 16.dp, top = 8.dp)
                ) {
                    Text(
                        text = "Attach",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontFamily = MontserratFamily,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                                showAttachmentOptions = false
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PhotoLibrary,
                            contentDescription = "Gallery",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Gallery (Images & Videos)",
                            color = Color.White,
                            fontFamily = CourierPrimeFamily
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                filePickerLauncher.launch("*/*")
                                showAttachmentOptions = false
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.InsertDriveFile,
                            contentDescription = "Files",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Files & Documents",
                            color = Color.White,
                            fontFamily = CourierPrimeFamily
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatScreenHeader(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    receiverDevice: NearbyDevice
) {
    Row(
        modifier = modifier.fillMaxWidth()
            .padding(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Ripple(size= 36.dp) {
            Icon(
                modifier = Modifier
                    .padding(end = 2.dp)
                    .size(9.dp)
                    .clickable{
                       onBackClick()
                    },
                imageVector = Icons.Rounded.ArrowBackIosNew,
                contentDescription = "Back Button",
                tint = Color.Black
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        CircularImage(size = 40.dp)

        Text(
            text = receiverDevice.deviceName,
            color = Color.White,
            modifier = Modifier.padding(start = 10.dp),
            fontFamily = MontserratFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp
        )
    }
}

@Preview
@Composable
private fun ChatScreenPrev() {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            modifier = Modifier.padding(end = 3.dp).size(30.dp),
            imageVector = Icons.Rounded.AttachFile,
            contentDescription = "Send Files",
            tint = Color.Gray
        )


        Box(
            modifier = Modifier.fillMaxWidth().weight(1f)
        ){
            RippleTextField(
                value = "enter the message here...",
                onValueChange = {},
                placeholder = "Type a message..."
            )

            Ripple(
                modifier = Modifier
                    .padding(end = 6.dp)
                    .align(Alignment.CenterEnd)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(),
                        onClick = {

                        }
                    ),
                size= 40.dp
            ) {
                Icon(
                    modifier = Modifier
                        .size(10.dp),
                    imageVector = Icons.Rounded.Send,
                    contentDescription = "Send Button",
                    tint = Color.Black
                )
            }
        }
    }
}