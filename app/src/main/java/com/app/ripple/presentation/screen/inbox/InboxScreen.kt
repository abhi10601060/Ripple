package com.app.ripple.presentation.screen.inbox

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.app.ripple.presentation.navigation.garph.ChatScreenRoute

@Composable
fun InboxScreen(
    modifier: Modifier = Modifier,
    navController: NavController? = null
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ){
        LazyColumn {
            items(count = 20) {
                InboxItem(
                    modifier = Modifier
                        .clickable{
                            navController?.navigate(ChatScreenRoute)
                        }
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
    InboxScreen()
}