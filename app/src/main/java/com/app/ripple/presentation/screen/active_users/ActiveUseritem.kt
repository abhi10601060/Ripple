package com.app.ripple.presentation.screen.active_users

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.ripple.data.nearby.model.ConnectionState
import com.app.ripple.data.nearby.model.NearbyDevice
import com.app.ripple.domain.model.NearbyDeviceDomain
import com.app.ripple.presentation.shared.CircularImage
import com.app.ripple.presentation.ui.theme.CourierPrimeFamily
import com.app.ripple.presentation.ui.theme.DarkBG
import com.app.ripple.presentation.ui.theme.SecondaryDarkBG

@Composable
fun ActiveUserItem(
    modifier: Modifier = Modifier,
    nearbyDevice: NearbyDeviceDomain,
    onConnectClick: () -> Unit = {},
    onDisconnectClick: () -> Unit = {}
) {

    fun handleActionClick() {
        if (nearbyDevice.connectionState == ConnectionState.CONNECTED){
            onDisconnectClick()
        }
        else{
            onConnectClick()
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color = DarkBG)
    ) {
        CircularImage(size = 50.dp)

        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier.padding(vertical = 5.dp)
        ) {
            Text(
                text = nearbyDevice.deviceName,
                color = Color.White,
                fontFamily = CourierPrimeFamily,
                fontSize = 18.sp,
            )

            Text(
                text = "~${nearbyDevice.endpointId}",
                color = Color.Gray,
                fontFamily = CourierPrimeFamily,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .padding(horizontal = 5.dp)
                .height(50.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if(nearbyDevice.connectionState == ConnectionState.CONNECTED) "Disconnect" else "Connect",
                color = Color.White,
                fontFamily = CourierPrimeFamily,
                fontSize = 12.sp,
                modifier = Modifier
                    .background(shape = RoundedCornerShape(15.dp), color = SecondaryDarkBG)
                    .padding(vertical = 5.dp, horizontal = 10.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(),
                        onClick = {
                            handleActionClick()
                        }
                    )
            )
        }
    }
}

@Preview
@Composable
private fun ActiveUserItemPreview() {
    ActiveUserItem(nearbyDevice = NearbyDeviceDomain.mock)
}