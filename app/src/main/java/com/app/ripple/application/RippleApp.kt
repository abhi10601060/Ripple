package com.app.ripple.application

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RippleApp : Application() {
    private val TAG = "RippleApp"

    override fun onCreate() {
        super.onCreate()
    }
}