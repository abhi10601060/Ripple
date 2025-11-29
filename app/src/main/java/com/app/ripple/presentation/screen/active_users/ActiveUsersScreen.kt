package com.app.ripple.presentation.screen.active_users

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.ripple.presentation.ui.theme.DarkBG

@Composable
fun ActiveUsersScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier.fillMaxSize()
            .background(color = DarkBG)
    ){
        LazyColumn {
            items(count = 20) {
                ActiveUserItem()

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
private fun ActiveUsersScreenPreview() {
    ActiveUsersScreen()
}