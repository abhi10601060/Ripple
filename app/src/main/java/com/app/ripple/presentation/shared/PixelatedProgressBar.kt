package com.app.ripple.presentation.shared

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.ripple.presentation.ui.theme.CourierPrimeFamily

@Composable
fun PixelatedProgressBar(
    modifier: Modifier = Modifier,
    progress: Float, // 0.0f to 1.0f
    color: Color = Color.White
) {
    val totalSegments = 10
    val filledSegments = (progress * totalSegments).toInt()

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Progress Capsule
        Box(
            modifier = Modifier
                .height(20.dp)
                .weight(1f)
                .border(1.dp, color, RoundedCornerShape(5.dp))
                .padding(horizontal = 3.dp, vertical = 3.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                repeat(totalSegments) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                color = if (index < filledSegments) color else Color.Transparent
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Percentage Text
        Text(
            text = "${(progress * 100).toInt()}%",
            color = color,
            fontFamily = CourierPrimeFamily,
            fontSize = 12.sp,
            modifier = Modifier.widthIn(min = 35.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun PixelatedProgressBarPreview() {

    val percentage by rememberInfiniteTransition().animateFloat(0f, 1f, animationSpec = InfiniteRepeatableSpec(animation = tween(5000)))

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PixelatedProgressBar(progress = percentage)
        PixelatedProgressBar(progress = 0.3f)
        PixelatedProgressBar(progress = 0.7f)
        PixelatedProgressBar(progress = 1.0f)
    }
}
