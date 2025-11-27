package com.app.ripple.presentation.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.ripple.presentation.ui.theme.CourierPrimeFamily

@Composable
fun RippleTextButton(
    modifier: Modifier = Modifier,
    title: String,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier
            .padding(vertical = 8.dp),
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonColors(
            containerColor = Color.White,
            contentColor = Color.Black,
            disabledContainerColor = Color.Black,
            disabledContentColor = Color.Black
        )
    ) {
        Text(
            text = title,
            color = Color.Black,
            fontFamily = CourierPrimeFamily,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Preview
@Composable
private fun RippleButtonPreview() {
    RippleTextButton(
        title = "Button",
        onClick = {}
    )
}