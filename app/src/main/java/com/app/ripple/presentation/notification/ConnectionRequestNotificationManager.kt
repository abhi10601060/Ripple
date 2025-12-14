package com.app.ripple.presentation.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.app.ripple.R
import com.app.ripple.presentation.activity.MainActivity
import com.app.ripple.presentation.notification.receivers.ConnectionRequestActionReceiver

class ConnectionRequestNotificationManager(
    private val context: Context,
) {
    // Constants
    val CONNECTION_REQUEST_CHANNEL_ID = "connection_request"
    val CONNECTION_REQUEST_NOTIFICATION_ID = 1001

    private lateinit var notificationManagerCompat: NotificationManagerCompat

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Connection requests"
            val description = "Notifications for new connection requests"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(
                CONNECTION_REQUEST_CHANNEL_ID,
                name,
                importance
            ).apply { this.description = description }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager
            manager.createNotificationChannel(channel)        // [web:16]
        }

        notificationManagerCompat = NotificationManagerCompat.from(context)
    }

    fun removeNotification(notificationId: Int){
        notificationManagerCompat.cancel(notificationId)
    }

    fun showConnectionRequestNotification(deviceName: String, endpointId: String) {
        // Reject action
        val rejectIntent = Intent(context, ConnectionRequestActionReceiver::class.java).apply {
            action = "ACTION_REJECT"
            putExtra("notification_id", CONNECTION_REQUEST_NOTIFICATION_ID)
            putExtra("endpoint_id", endpointId)
        }
        val rejectPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            rejectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Accept action
        val acceptIntent = Intent(context, ConnectionRequestActionReceiver::class.java).apply {
            action = "ACTION_ACCEPT"
            putExtra("notification_id", CONNECTION_REQUEST_NOTIFICATION_ID)
            putExtra("endpoint_id", endpointId)
        }
        val acceptPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            acceptIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            context,
            CONNECTION_REQUEST_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.whiteripple)
            .setContentTitle("New connection request")
            .setContentText("${deviceName} wants to connect with you")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)                         // dismiss on tap [web:13]
            .setTimeoutAfter(30_000L)                    // auto-remove after 30s [web:13][web:21]
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Reject",
                rejectPendingIntent
            )                                            // [web:13][web:20]
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Accept",
                acceptPendingIntent
            )
            .build()

        if(context.applicationContext.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED){
            notificationManagerCompat
                .notify(CONNECTION_REQUEST_NOTIFICATION_ID, notification)
        }

    }

    fun showConnectionRejectedNotification(deviceName: String){
        val notification = NotificationCompat.Builder(
            context,
            CONNECTION_REQUEST_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.whiteripple)
            .setContentTitle("Connection Request Rejected")
            .setContentText("$deviceName Rejected your connection request")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)                         // dismiss on tap [web:13]
            .setTimeoutAfter(15_000L)                    // auto-remove after 15s [web:13][web:21]
            .build()

        if(context.applicationContext.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED){
            notificationManagerCompat
                .notify(1002, notification)
        }
    }

    fun showConnectionAcceptedNotification(deviceName : String){
        val notification = NotificationCompat.Builder(
            context,
            CONNECTION_REQUEST_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.whiteripple)
            .setContentTitle("Connection Request Accepted")
            .setContentText("$deviceName connected with you")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)                         // dismiss on tap [web:13]
            .setTimeoutAfter(15_000L)                    // auto-remove after 15s [web:13][web:21]
            .build()

        if(context.applicationContext.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED){
            notificationManagerCompat
                .notify(1003, notification)
        }
    }

    fun showDeviceDisconnectedNotification(deviceName : String){
        val notification = NotificationCompat.Builder(
            context,
            CONNECTION_REQUEST_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.whiteripple)
            .setContentTitle("Device Disconnected")
            .setContentText("$deviceName disconnected from you")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)                         // dismiss on tap [web:13]
            .setTimeoutAfter(15_000L)                    // auto-remove after 15s [web:13][web:21]
            .build()

        if(context.applicationContext.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED){
            notificationManagerCompat
                .notify(1004, notification)
        }
    }

}