package com.crepass.kotlin_chatting

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)


        val name = "채팅 알림"
        val descriptionText = "채팅알림입니다"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel(
            getString(R.string.default_notification_channel_id), name, importance
        )
        mChannel.description=descriptionText
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)

        val body=message.notification?.body?:""
       var notificationBuilder= NotificationCompat.Builder(applicationContext,getString(R.string.default_notification_channel_id))
            .setSmallIcon(R.drawable.baseline_cruelty_free_24)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(body)

        notificationManager.notify(0,notificationBuilder.build())
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

}