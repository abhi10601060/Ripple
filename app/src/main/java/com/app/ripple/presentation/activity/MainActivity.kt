package com.app.ripple.presentation.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.navigation.NavHost
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.app.ripple.background.worker.OnAppCloseWorker
import com.app.ripple.di.Test
import com.app.ripple.domain.use_case.nearby.StartAdvertisingUseCase
import com.app.ripple.domain.use_case.nearby.StopAdvertisingUseCase
import com.app.ripple.presentation.navigation.garph.MainNavGraphRoute
import com.app.ripple.presentation.navigation.garph.mainGraph
import com.app.ripple.presentation.screen.audio_test.AudioRecorderScreen
import com.app.ripple.presentation.screen.message.NearbyShareScreen
import com.app.ripple.presentation.screen.splash.SplashScreen
import com.app.ripple.presentation.ui.theme.RippleTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val PERMISSION_LIST = getPerMissionList()
    private val TAG = "MainActivity"

    @Inject lateinit var testData : Test

    @Inject lateinit var startAdvertisingUseCase: StartAdvertisingUseCase
    @Inject lateinit var startDiscoveryUseCase: StartAdvertisingUseCase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RippleTheme {
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions(),
                ) { res ->
                    res.forEach {
                        Log.d("MainActivity", "onCreate: " + it.key + " " + it.value)
                    }
                }
                LaunchedEffect(true) {
                    if (!checkPermissions()) permissionLauncher.launch(PERMISSION_LIST)
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    App(modifier = Modifier.padding(innerPadding))
//                    NearbyShareScreen(modifier = Modifier.padding(innerPadding))
//                    AudioRecorderScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }

        startAdvertisingAndDiscovery()
    }

    fun startAdvertisingAndDiscovery(){
        CoroutineScope(Dispatchers.Default).launch {
            startAdvertisingUseCase.invoke().collect {
                Log.d(TAG, "onStart: startAdvertisingUseCase : $it")
            }
        }

        CoroutineScope(Dispatchers.Default).launch {
            startDiscoveryUseCase.invoke().collect {
                Log.d(TAG, "onStart: startDiscoveryUseCase : $it")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("MainActivity", "onStart: ")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "onDestroy: ")
    }

    @Composable
    fun App(modifier: Modifier){
        val navController = rememberNavController()
        NavHost(modifier = modifier, navController = navController, startDestination = MainNavGraphRoute){
            mainGraph(navController = navController)
        }
    }

    fun checkPermissions() : Boolean{
        PERMISSION_LIST.forEach {
            if (checkSelfPermission(it) == PackageManager.PERMISSION_DENIED) return false
        }
        return true
    }
}




fun getPerMissionList() : Array<String>{
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf<String>(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.NEARBY_WIFI_DEVICES,
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf<String>(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf<String>(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        } else {
            arrayOf<String>(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RippleTheme {
        Greeting("Android")
    }
}