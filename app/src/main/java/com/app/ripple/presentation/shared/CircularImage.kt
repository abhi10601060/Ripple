package com.app.ripple.presentation.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.request.colorSpace

@Composable
fun CircularImage(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    Box(
        modifier = modifier.size(size)
            .clip(shape = CircleShape)
    ){
        Image(
            imageVector = Icons.Outlined.AccountCircle,
            contentDescription = "Profile Image",
            modifier = Modifier.size(size),
            colorFilter = ColorFilter.tint(Color.Gray)
        )
    }
}

@Preview
@Composable
private fun CircularImagePreview() {
    CircularImage()
}