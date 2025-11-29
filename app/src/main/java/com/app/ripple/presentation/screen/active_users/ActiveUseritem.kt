package com.app.ripple.presentation.screen.active_users

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.ripple.presentation.shared.CircularImage
import com.app.ripple.presentation.ui.theme.CourierPrimeFamily
import com.app.ripple.presentation.ui.theme.DarkBG

@Composable
fun ActiveUserItem(modifier: Modifier = Modifier) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = DarkBG)
    ) {
        CircularImage(size = 50.dp)

        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier.padding(vertical = 5.dp)
        ) {
            Text(
                text = "Abhishek Velekar",
                color = Color.White,
                fontFamily = CourierPrimeFamily,
                fontSize = 18.sp,
            )

            Text(
                text = "~abhi_velekar",
                color = Color.Gray,
                fontFamily = CourierPrimeFamily,
                fontSize = 15.sp
            )
        }
    }
}

@Preview
@Composable
private fun ActiveUserItemPreview() {
    ActiveUserItem()
}