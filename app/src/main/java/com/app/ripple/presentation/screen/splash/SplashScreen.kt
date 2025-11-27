package com.app.ripple.presentation.screen.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.app.ripple.R
import com.app.ripple.presentation.navigation.garph.HomeScreenRoute
import com.app.ripple.presentation.shared.RippleLogo
import com.app.ripple.presentation.shared.RippleTextButton
import com.app.ripple.presentation.shared.RippleTextField
import com.app.ripple.presentation.ui.theme.CourierPrimeFamily
import com.app.ripple.presentation.ui.theme.DarkBG
import com.app.ripple.presentation.ui.theme.MontserratFamily
import com.app.ripple.presentation.ui.theme.TertiaryDarkBG

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    navController: NavController? = null
) {

    var userNameValue by remember {
        mutableStateOf("")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = DarkBG)
    ) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .align(alignment = Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RippleLogo(size = 100.dp)

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "RIPPLE",
                fontSize = 40.sp,
                color = Color.White,
                fontFamily = MontserratFamily,
                fontWeight = FontWeight.ExtraBold
            )

            AnimatedVisibility(
                modifier = Modifier
                    .padding(top = 100.dp)
                    .fillMaxWidth()
                    .padding(20.dp),
                visible = true,
                enter = fadeIn(animationSpec = tween(durationMillis = 1000))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RippleTextField(
                        value = userNameValue,
                        onValueChange = {
                            userNameValue = it
                        },
                        placeholder = "Enter User name..."
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Note: User name is bound to your device to identify specific devices.",
                        color = TertiaryDarkBG,
                        fontFamily = CourierPrimeFamily
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    RippleTextButton(
                        title = "Continue",
                        onClick = {
                            navController?.navigate(HomeScreenRoute)
                        }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun SplashScreenPreview() {
    SplashScreen()
}