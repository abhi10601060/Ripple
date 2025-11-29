package com.app.ripple.presentation.screen.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.app.ripple.presentation.shared.CircularImage
import com.app.ripple.presentation.shared.Ripple
import com.app.ripple.presentation.shared.RippleTextField
import com.app.ripple.presentation.ui.theme.CourierPrimeFamily
import com.app.ripple.presentation.ui.theme.DarkBG
import com.app.ripple.presentation.ui.theme.MontserratFamily

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    navController: NavController? = null
) {

    var typedMessage by remember {
        mutableStateOf("")
    }

    Box(
        modifier = modifier.fillMaxSize()
            .background(color = DarkBG)
    ){
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            ChatScreenHeader(
                onBackClick = {
                    navController?.popBackStack()
                }
            )

            LazyColumn {
                items(count = 10) { i ->
                    MessageItem(isFromCurrentUser = i % 2 == 0)
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
            RippleTextField(
                value = "",
                onValueChange = {
                    typedMessage = it
                },
                placeholder = "Type a message..."
            )

            Ripple(
                modifier = Modifier
                    .padding(end = 6.dp)
                    .align(Alignment.CenterEnd),
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

@Composable
fun ChatScreenHeader(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
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
            text = "Abhishek Velekar",
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
    ChatScreen()
}