package com.app.ripple.presentation.screen.inbox

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.app.ripple.presentation.navigation.garph.ChatScreenRoute
import com.app.ripple.presentation.screen.home.HomeScreenViewModel

@Composable
fun InboxScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeScreenViewModel,
    navController: NavController? = null
) {
    val connectedDevices by remember { viewModel.nearbyConnectedDevices }

    Box(
        modifier = Modifier.fillMaxSize()
    ){
        LazyColumn {
            items(items = connectedDevices) { connectedDevice ->
                InboxItem(
                    modifier = Modifier
                        .clickable{
                            navController?.navigate(ChatScreenRoute(receiverDevice = connectedDevice))
                        },
                    nearbyDevice = connectedDevice
                )
                Spacer(modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(color = Color.Gray))
            }
        }
    }
}

@Preview
@Composable
private fun InboxScreenPreview() {
//    InboxScreen()
}