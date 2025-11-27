package com.app.ripple.presentation.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.size.Size
import com.app.ripple.R

@Composable
fun RippleLogo(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp
) {
    Image(
        painter = painterResource(R.drawable.whiteripple),
        contentDescription = "Ripple logo",
        modifier = Modifier.size(size)
    )

}

@Preview
@Composable
private fun RippleLogoPrev() {
    RippleLogo()
}