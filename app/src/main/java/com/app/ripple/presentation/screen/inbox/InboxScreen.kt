package com.app.ripple.presentation.screen.inbox

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.app.ripple.data.local.realm.model.toNearbyDevice
import com.app.ripple.data.nearby.model.toNearbyDevice
import com.app.ripple.presentation.navigation.garph.ChatScreenRoute
import com.app.ripple.presentation.screen.home.HomeScreenViewModel
import com.app.ripple.presentation.shared.Ripple
import com.app.ripple.presentation.shared.RippleTextField

@Composable
fun InboxScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeScreenViewModel,
    navController: NavController? = null
) {
    val connectedDevices by remember { viewModel.nearbyConnectedDevices }

    var searchText by remember {
        mutableStateOf("")
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ){
        LazyColumn {

            stickyHeader {
                SearchBox(
                    modifier = Modifier.padding(bottom = 10.dp),
                    onSearchTextChange = {
                        searchText = it
                    }
                )
            }

            items(items = connectedDevices.filter { it.deviceName.contains(searchText, ignoreCase = true) }) { connectedDevice ->
                InboxItem(
                    modifier = Modifier
                        .clickable{
                            navController?.navigate(ChatScreenRoute(receiverDevice = connectedDevice.toNearbyDevice()))
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

@Composable
fun SearchBox(
    modifier: Modifier = Modifier,
    onSearchTextChange: (String) -> Unit = {}
){
    var searchText by remember {
        mutableStateOf("")
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Ripple(
            modifier = Modifier
                .padding(end = 6.dp),
            size= 50.dp
        ) {
            Icon(
                modifier = Modifier
                    .size(14.dp),
                imageVector = Icons.Rounded.Search,
                contentDescription = "Send Button",
                tint = Color.Black
            )
        }

        RippleTextField(
            value = searchText,
            onValueChange = {
                searchText = it
                onSearchTextChange(it)
            },
            placeholder = "Search here..."
        )
    }
}

@Preview
@Composable
private fun InboxScreenPreview() {
//    InboxScreen()
}