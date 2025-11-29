package com.app.ripple.presentation.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.size.Size
import com.app.ripple.presentation.ui.theme.DarkBG
import com.app.ripple.presentation.ui.theme.SecondaryDarkBG
import com.app.ripple.presentation.ui.theme.TertiaryDarkBG

@Composable
fun Ripple(
    modifier: Modifier = Modifier,
    size: Dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .size(size)
            .background(shape = CircleShape, color = SecondaryDarkBG)
            .padding((size.value / 7).dp)
            .background(shape = CircleShape, color = TertiaryDarkBG)
            .padding((size.value / 7).dp)
            .background(shape = CircleShape, color = Color.White),
        contentAlignment = Alignment.Center
    ){
        content()
    }
}

@Preview
@Composable
private fun RipplePrev() {
    Ripple(size = 50.dp){
        Icon(
            modifier = Modifier
                .padding(end = 2.dp)
                .size(15.dp),
            imageVector = Icons.Rounded.ArrowBackIosNew,
            contentDescription = "arrow",
            tint = Color.Black
        )
    }
}