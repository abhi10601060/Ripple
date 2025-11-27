package com.app.ripple.presentation.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.ripple.presentation.ui.theme.CourierPrimeFamily

@Composable
fun BottomTitledIcon(
    modifier: Modifier = Modifier,
    title: String = "",
    imageVector: ImageVector = Icons.Default.BrokenImage,
    color: Color = Color.White,
    iconSize: Dp = 40.dp,
    fontSize: TextUnit = 16.sp
) {
    Column(
        modifier = modifier.wrapContentSize()
            .background(color = Color.Transparent),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
       Icon(
           modifier = Modifier.size(iconSize),
           imageVector = imageVector,
           contentDescription = "",
           tint = color
       )

        Text(text = title, color = color, fontSize = fontSize, fontFamily = CourierPrimeFamily)
    }
}

@Preview
@Composable
private fun BottomTitledIconPreview() {
    BottomTitledIcon(title = "This is Image")
}