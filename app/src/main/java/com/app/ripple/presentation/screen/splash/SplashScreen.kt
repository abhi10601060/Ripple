package com.app.ripple.presentation.screen.splash

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.app.ripple.data.local.sharedpreferences.SharedprefConstants
import com.app.ripple.presentation.navigation.garph.HomeScreenRoute
import com.app.ripple.presentation.shared.RippleLogo
import com.app.ripple.presentation.shared.RippleTextButton
import com.app.ripple.presentation.shared.RippleTextField
import com.app.ripple.presentation.ui.theme.CourierPrimeFamily
import com.app.ripple.presentation.ui.theme.DarkBG
import com.app.ripple.presentation.ui.theme.MontserratFamily
import com.app.ripple.presentation.ui.theme.TertiaryDarkBG
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    splashViewModel: SplashViewModel
) {

    var userNameValue by remember {
        mutableStateOf("")
    }

    var showUserNameInput by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(key1 = true) {
        val savedUserName = splashViewModel.getUserName()
        if (savedUserName.isNullOrEmpty()){
            showUserNameInput = true
        }
        else{
            delay(1400)
            navController?.navigate(HomeScreenRoute)
        }
    }

    Box(
        modifier = modifier
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
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.clickable{
//
                }
            )

            AnimatedVisibility(
                modifier = Modifier
                    .padding(top = 100.dp)
                    .fillMaxWidth()
                    .padding(20.dp),
                visible = showUserNameInput,
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
                        modifier = Modifier.alpha(if(userNameValue.isEmpty())0.3f else 1f),
                        title = "Continue",
                        onClick = {
                            if(userNameValue.isNotEmpty()) {
                                splashViewModel.saveUserName(userName = userNameValue)
                                navController?.navigate(HomeScreenRoute)
                            }
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
//    SplashScreen()
}