package com.app.ripple.presentation.screen.chat

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
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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

    val receivedMessages by remember {
        viewModel.receivedMessages
    }

    val sentMessages by remember {
        viewModel.sentMessages
    }

    val chatScrollState = rememberLazyListState()

    val allMessages by  remember {
        derivedStateOf {
            receiverDeviceDomain?.allMessages ?: listOf()
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

//            Spacer(
//                modifier = Modifier.height(80.dp)
//            )
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
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ){
                    Text(text = "Device offline. Cannot connect...", color = Color.Gray, fontFamily = CourierPrimeFamily)
                }
            }
            else if(receiverDeviceDomain?.connectionState == ConnectionState.CONNECTED){
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
//    ChatScreen(
//        receiverDevice = NearbyDevice.mock
//    )
}