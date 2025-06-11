package com.narxoz.social.notification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.narxoz.social.R

class FCMService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCMService", "New token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCMService", "Message: ${'$'}{message.data}")
        showNotification(message)
    }

    private fun showNotification(message: RemoteMessage) {
        val channelId = "default"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Default", NotificationManager.IMPORTANCE_DEFAULT)
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }

        val notif = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(message.notification?.title ?: getString(R.string.app_name))
            .setContentText(message.notification?.body ?: message.data["body"])
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(this)) {
            notify(System.currentTimeMillis().toInt(), notif)
        }
    }
}