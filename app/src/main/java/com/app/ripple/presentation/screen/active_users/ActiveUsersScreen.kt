package com.app.ripple.presentation.screen.active_users

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ActiveUsersScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier.fillMaxSize()
            .background(color = Color.Green)
    )
}

@Preview
@Composable
private fun ActiveUsersScreenPreview() {
    ActiveUsersScreen()
}