package com.app.ripple.presentation.screen.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.ripple.data.nearby.model.TextMessage
import com.app.ripple.domain.model.TextMessageDomain
import com.app.ripple.presentation.shared.CircularImage
import com.app.ripple.presentation.ui.theme.CourierPrimeFamily
import com.app.ripple.presentation.ui.theme.DarkBG
import com.app.ripple.presentation.ui.theme.SecondaryDarkBG

@Composable
fun TextMessageItem(
    modifier: Modifier = Modifier,
    textMessage: TextMessageDomain,
    isFromCurrentUser: Boolean = false,
) {
    Row(
        modifier = modifier.fillMaxWidth()
            .background(color = DarkBG)
            .padding(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularImage(
            modifier = Modifier.alpha(if(isFromCurrentUser) 0f else 1f),
            size = 22.dp
        )

        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if( isFromCurrentUser) Arrangement.End  else Arrangement.Start
        ){
            Box(
                modifier = Modifier.fillMaxWidth(0.8f)
            ){
                Text(
                    text = textMessage.content,
                    modifier = Modifier
                        .align(if(isFromCurrentUser) Alignment.CenterEnd else Alignment.CenterStart)
                        .padding(start = 5.dp, end = 5.dp)
                        .clip(shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = if(isFromCurrentUser) 8.dp else 0.dp, bottomEnd = if(isFromCurrentUser) 0.dp else 8.dp))
                        .background(color = if (isFromCurrentUser) Color.White else SecondaryDarkBG)
                        .padding(5.dp),
                    fontFamily = CourierPrimeFamily,
                    color = if (isFromCurrentUser) Color.Black else Color.White
                )
            }

        }

    }
}

@Preview
@Composable
private fun TextMessageItemPreview() {
//    TextMessageItem()
}