package com.app.ripple.presentation.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.app.ripple.R
import com.app.ripple.presentation.activity.MainActivity

class ConnectionRequestNotificationManager(
    private val context: Context
) {
    // Constants
    val CONNECTION_REQUEST_CHANNEL_ID = "connection_request"
    val CONNECTION_REQUEST_NOTIFICATION_ID = 1001

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
    }

    fun showConnectionRequestNotification() {
        // Reject action
        val rejectIntent = Intent(context, MainActivity::class.java).apply {
            action = "ACTION_REJECT"
            putExtra("notification_id", CONNECTION_REQUEST_NOTIFICATION_ID)
        }
        val rejectPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            rejectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Cancel action
        val cancelIntent = Intent(context, MainActivity::class.java).apply {
            action = "ACTION_CANCEL"
            putExtra("notification_id", CONNECTION_REQUEST_NOTIFICATION_ID)
        }
        val cancelPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            context,
            CONNECTION_REQUEST_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.whiteripple)
            .setContentTitle("New connection request")
            .setContentText("User X wants to connect with you")
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
                "Cancel",
                cancelPendingIntent
            )
            .build()

        NotificationManagerCompat.from(context)
            .notify(CONNECTION_REQUEST_NOTIFICATION_ID, notification)
    }


}