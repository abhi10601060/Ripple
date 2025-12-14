package com.app.ripple.presentation.notification.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.app.ripple.domain.use_case.nearby.AcceptConnectionUseCase
import com.app.ripple.domain.use_case.nearby.RejectConnectionUseCae
import com.app.ripple.presentation.notification.ConnectionRequestNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ConnectionRequestActionReceiver : BroadcastReceiver() {
    val TAG = "ConnectionRequestActionReceiver"

    @Inject lateinit var acceptConnectionUseCase: AcceptConnectionUseCase
    @Inject lateinit var rejectConnectionUseCae: RejectConnectionUseCae

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive: ${intent?.action}")
        val id = intent?.getIntExtra("notification_id", -1)
        val endpointId = intent?.getStringExtra("endpoint_id") ?: ""
        val connectionRequestNotificationManager = ConnectionRequestNotificationManager(context!!)
        when (intent?.action) {
            "ACTION_REJECT" -> {
                id?.let {  connectionRequestNotificationManager.removeNotification(it) }
                rejectConnectionUseCae.invoke(endpointId = endpointId)
            }
            "ACTION_ACCEPT" -> {
                id?.let {  connectionRequestNotificationManager.removeNotification(it) }
                acceptConnectionUseCase.invoke(endpointId = endpointId)
                connectionRequestNotificationManager.showConnectionAcceptedNotification(endpointId)
            }
        }
    }
}