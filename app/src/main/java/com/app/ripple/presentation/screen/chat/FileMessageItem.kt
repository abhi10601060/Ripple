package com.app.ripple.presentation.screen.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.ripple.data.nearby.model.Message
import com.app.ripple.domain.model.MessageDomain
import com.app.ripple.presentation.shared.CircularImage
import com.app.ripple.presentation.shared.PixelatedProgressBar
import com.app.ripple.presentation.ui.theme.CourierPrimeFamily
import com.app.ripple.presentation.ui.theme.DarkBG
import com.app.ripple.presentation.ui.theme.SecondaryDarkBG
import com.app.ripple.util.millisToDateTime

@Composable
fun FileMessageItem(
    modifier: Modifier = Modifier,
    metadataMessage: MessageDomain,
    isFromCurrentUser: Boolean,
    onClick: (metadataMessage: MessageDomain) -> Unit
) {

    var color by remember { mutableStateOf(if(isFromCurrentUser) Color.Black else Color.White) }

    Row(
        modifier = modifier.fillMaxWidth()
            .background(color = DarkBG)
            .padding(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ){

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
                Row(modifier = Modifier
                    .wrapContentSize()
                    .align(if(isFromCurrentUser) Alignment.CenterEnd else Alignment.CenterStart)
                    .padding(start = 5.dp, end = 5.dp)
                    .clip(shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = if(isFromCurrentUser) 8.dp else 0.dp, bottomEnd = if(isFromCurrentUser) 0.dp else 8.dp))
                    .background(color = if (isFromCurrentUser) Color.White else SecondaryDarkBG)
                    .padding(5.dp)
                    .align(Alignment.Center)
                    .clickable{
                        if (metadataMessage.progress == 1f) onClick(metadataMessage)
                    }
                ) {
                    Icon(
                        modifier = Modifier.border(1.dp, color = color, shape = CircleShape).padding(8.dp).size(40.dp),
                        imageVector = Icons.Rounded.FileOpen,
                        tint = color,
                        contentDescription = "file"
                    )

                    Spacer(modifier = Modifier.width(5.dp).height(1.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                    ){
                        Text(
                            text = metadataMessage.fileName,
                            color = if (isFromCurrentUser) Color.Black else Color.White,
                            fontFamily = CourierPrimeFamily,
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        if (metadataMessage.progress == 1f){
                            Text(
                                text = "${if(isFromCurrentUser) "Sent" else "Received"} at ${millisToDateTime(metadataMessage.timestamp)}",
                                color = if (isFromCurrentUser) Color.Black else Color.White,
                                fontFamily = CourierPrimeFamily,
                                fontWeight = FontWeight.Thin,
                                fontSize = 12.sp
                            )
                        }
                        else{
                            PixelatedProgressBar(
                                progress = metadataMessage.progress,
                                color = if (isFromCurrentUser) Color.Black else Color.White
                            )
                        }
                    }

                }
            }
        }

    }
}


@Preview
@Composable
private fun FileMessageItem() {
    FileMessageItem(modifier = Modifier.fillMaxWidth(), metadataMessage = MessageDomain.mockFileMetadataMessage, isFromCurrentUser = false, onClick = {})
}