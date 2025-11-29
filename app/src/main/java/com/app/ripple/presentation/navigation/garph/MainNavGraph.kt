package com.app.ripple.presentation.navigation.garph

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.app.ripple.data.nearby.model.ConnectionState
import com.app.ripple.data.nearby.model.NearbyDevice
import com.app.ripple.presentation.navigation.nav_type.NearbyDeviceNavType
import com.app.ripple.presentation.screen.chat.ChatScreen
import com.app.ripple.presentation.screen.home.HomeScreen
import com.app.ripple.presentation.screen.splash.SplashScreen
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf


fun NavGraphBuilder.mainGraph(navController: NavController){

    navigation<MainNavGraphRoute>(
        startDestination = SplashScreenRoute
    ){
        composable<SplashScreenRoute> {
            SplashScreen(navController = navController)
        }

        composable<HomeScreenRoute> {
            HomeScreen(navController = navController, viewModel = hiltViewModel())
        }

        composable<ChatScreenRoute>(
            typeMap = mapOf(
                typeOf<NearbyDevice>() to NearbyDeviceNavType,
                typeOf<ConnectionState>() to NavType.EnumType(ConnectionState::class.java)
            )
        ){
            val chatScreenRoute = it.toRoute<ChatScreenRoute>()
            ChatScreen(navController = navController, viewModel = hiltViewModel(), receiverDevice = chatScreenRoute.receiverDevice)
        }
    }
}


// Routes

@Serializable
object MainNavGraphRoute

@Serializable
object SplashScreenRoute{}

@Serializable
object HomeScreenRoute{}

@Serializable
data class ChatScreenRoute(
    val receiverDevice: NearbyDevice
)