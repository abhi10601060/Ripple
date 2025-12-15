package com.app.ripple.presentation.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.app.ripple.R
import com.app.ripple.data.local.sharedpreferences.SharedprefConstants

class ChatNotificationManager(
    private val context: Context,
    private val sharedPreferences: SharedPreferences
) {

    private val TAG = "ChatNotificationManager"

    data class TextMessageChatModel(
        val userId: String,
        val userName: String,
        val messageText: String,
        val timeStamp: Long = System.currentTimeMillis()
    )

    // Constants
    val CHAT_MESSGAE_CHANNEL_ID = "chat_message"

    private lateinit var notificationManagerCompat: NotificationManagerCompat

    private val allUnseenMessages = mutableListOf<TextMessageChatModel>()

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Chat notifications"
            val description = "Notifications for new chat message"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(
                CHAT_MESSGAE_CHANNEL_ID,
                name,
                importance
            ).apply { this.description = description }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager
            manager.createNotificationChannel(channel)        // [web:16]
        }

        notificationManagerCompat = NotificationManagerCompat.from(context)
    }

    fun showChatMessage(
        userId: String,
        userName: String,
        messageText: String,
        notificationId: Int = userId.hashCode()
    ) {
        val openedUserChatScreen = sharedPreferences.getString(SharedprefConstants.VISIBLE_CHAT_SCREEN_USER.name, null )
        if ( openedUserChatScreen != null && openedUserChatScreen == userId){
            return
        }

        Log.d(TAG, "showChatMessage: called for $messageText")
        val message = TextMessageChatModel(userId = userId, userName = userName, messageText = messageText)
        allUnseenMessages.add(message)

        // Create/update messaging style for this user
        val messagingStyle = NotificationCompat.MessagingStyle("Me") // Current user
            .setConversationTitle(userName)
            .setGroupConversation(false)

        notificationManagerCompat.cancel(userId.hashCode())

        // Add new message
        allUnseenMessages.filter { it.userId == userId }.forEach {
            messagingStyle.addMessage(
                it.messageText,
                it.timeStamp,
                it.userName // Sender name
            )
        }

        val notification = NotificationCompat.Builder(context, CHAT_MESSGAE_CHANNEL_ID)
            .setSmallIcon(R.drawable.whiteripple)
            .setStyle(messagingStyle)
            .setGroup(userId) // Group by user
            .setAutoCancel(true)
            .build()


        if(context.applicationContext.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManagerCompat.notify(notificationId, notification)
        }
    }

    fun removeChatNotification(userId: String){
        notificationManagerCompat.cancel(userId.hashCode())
    }

}