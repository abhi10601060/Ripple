package com.app.ripple.presentation.screen.splash

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import com.app.ripple.data.local.sharedpreferences.SharedprefConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : ViewModel(){

    fun getUserName() : String?{
        return sharedPreferences.getString(SharedprefConstants.USER_NAME.name, null)
    }

    fun saveUserName(userName: String) {
        sharedPreferences.edit(commit = true) {
            putString(SharedprefConstants.USER_NAME.name, userName)
        }
    }
}