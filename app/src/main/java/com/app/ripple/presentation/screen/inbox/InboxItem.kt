package com.app.ripple.presentation.screen.inbox

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.ripple.presentation.shared.CircularImage
import com.app.ripple.presentation.ui.theme.CourierPrimeFamily
import com.app.ripple.presentation.ui.theme.DarkBG
import com.app.ripple.presentation.ui.theme.TertiaryDarkBG

@Composable
fun InboxItem(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBG)
            .padding(start = 5.dp)
    ) {
        ProfileImageSection()

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp, vertical = 5.dp)
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Abhishek Velekar",
                color = Color.White,
                fontSize = 18.sp,
                lineHeight = 20.sp,
                fontFamily = CourierPrimeFamily,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "This is Latest message from abhi to shubham which is saying about something I dont know for real",
                color = TertiaryDarkBG,
                fontSize = 15.sp,
                fontFamily = CourierPrimeFamily,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                lineHeight = 16.sp,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(
                text =  "Yesterday",
                color = Color.Gray,
                fontSize = 12.sp,
                fontFamily = CourierPrimeFamily,
                fontWeight = FontWeight.Thin,
                modifier = Modifier.padding(top = 5.dp)
            )

            Icon(
                imageVector = Icons.Rounded.KeyboardArrowRight,
                contentDescription = "arrow right",
                tint = Color.Gray,
                modifier = Modifier
                    .padding(top = 5.dp)
                    .size(16.dp)

            )
        }

    }
}

@Composable
fun ProfileImageSection(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularImage(size = 50.dp)

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Cancel,
                contentDescription = "disconnected",
                tint = Color.White,
                modifier = Modifier
                    .padding(bottom = 2.dp)
                    .size(10.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "Offline",
                color = Color.White,
                fontSize = 10.sp,
                fontFamily = CourierPrimeFamily
            )
        }
    }
}

@Preview
@Composable
private fun InboxItemPreview() {
    InboxItem()
}