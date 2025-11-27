package com.app.ripple.presentation.navigation.garph

import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.app.ripple.presentation.screen.home.HomeScreen
import com.app.ripple.presentation.screen.splash.SplashScreen
import kotlinx.serialization.Serializable


fun NavGraphBuilder.mainGraph(navController: NavController){

    navigation<MainNavGraphRoute>(
        startDestination = SplashScreenRoute
    ){
        composable<SplashScreenRoute> {
            SplashScreen(navController = navController)
        }

        composable<HomeScreenRoute> {
            HomeScreen(navController = navController)
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