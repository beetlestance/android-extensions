package com.beetlestance.androidextensions.sample.fcm

import android.app.Notification
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        parseFcmPayload(remoteMessage)
    }

    private fun parseFcmPayload(remoteMessage: RemoteMessage) {
        try {
            val notificationPayload = remoteMessage.toNotificationPayload()

            if (notificationPayload.silent) {
                // parseNotificationSilently.parseNotificationPayload(notificationPayload)
            } else {
                parseNotification(notificationPayload)

            }
        } catch (e: Exception) {
            // Timber.tag(TAG).e(e)
        }
    }

    private fun parseNotification(notificationPayload: NotificationPayload) {
        val notificationTrayItems = NotificationTrayItems(
            title = requireNotNull(notificationPayload.title),
            msg = requireNotNull(notificationPayload.body),
            channel = "Sample Channel",
            imageUrl = notificationPayload.imageURL,
            group = "group",
            isHeadsUp = true,
            deeplink = pendingIntentForAction(applicationContext, notificationPayload),
            notifyId = 1
        )

        val notification = createNotification(notificationTrayItems)

        NotificationManagerCompat.from(applicationContext)
            .notify(notificationTrayItems.notifyId, notification)
    }

    private fun createNotification(notificationTrayItems: NotificationTrayItems): Notification {
        val notificationBuilder =
            NotificationCompat.Builder(applicationContext, notificationTrayItems.channel)
                .setContentTitle(notificationTrayItems.title)
                .setContentText(notificationTrayItems.msg)
                .setAutoCancel(true)
                .setGroup(notificationTrayItems.group)

        notificationTrayItems.deeplink?.let { notificationBuilder.setContentIntent(it) }

        if (notificationTrayItems.isHeadsUp) {
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
        } else {
            notificationBuilder.setPriority(NotificationCompat.DEFAULT_ALL)
                .setDefaults(NotificationCompat.PRIORITY_DEFAULT)
        }

        return notificationBuilder.build()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
}

data class NotificationTrayItems(
    val title: String,
    val msg: String,
    val imageUrl: String?,
    val channel: String,
    val group: String,
    val deeplink: PendingIntent?,
    val isHeadsUp: Boolean,
    val notifyId: Int
)
