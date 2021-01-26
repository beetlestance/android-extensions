package com.beetlestance.androidextensions.sample.navigation.fcm

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.beetlestance.androidextensions.sample.navigation.NavigationActivityWithPrimaryFragment
import com.beetlestance.androidextensions.sample.navigation.constants.FEED_DEEPLINK
import com.beetlestance.androidextensions.sample.navigation.constants.NOTIFICATION_DEEPLINK

//  Notify to pull new feed
const val ACTION_FEED_UPDATED: String = "FEED UPDATED"

//  Notify to pull new notifications
const val ACTION_NOTIFICATION_UPDATED: String = "NOTIFICATION UPDATED"

// Map of actions to deeplink
private val actionsDestination: HashMap<String, String> = hashMapOf<String, String>().apply {
    put(ACTION_FEED_UPDATED, FEED_DEEPLINK)
    put(ACTION_NOTIFICATION_UPDATED, NOTIFICATION_DEEPLINK)
}

/**
 * @param context to create pending intent
 * @param notificationPayload to check for action and other attributes send via server in additional
 * data
 * @return PendingIntent
 * */
fun pendingIntentForAction(
    context: Context,
    notificationPayload: NotificationPayload
): PendingIntent? {
    val deepLink = notificationDeeplink(notificationPayload.action)
    val notifyIntent = context.createNotifyIntent(deepLink)

    return PendingIntent.getActivity(context, 0, notifyIntent, FLAG_UPDATE_CURRENT)
}

/*
* provide valid Deeplink for given action
* */
private fun notificationDeeplink(action: String): Uri? {
    val deeplink: String = actionsDestination[action] ?: return null
    val args = deeplinkArgs(action)
    return Uri.parse(deeplink + args)
}

/*
* Provide arguments for given action
* */
private fun deeplinkArgs(action: String): String {
    return when (action) {
        ACTION_NOTIFICATION_UPDATED -> "?input=from notification"
        ACTION_FEED_UPDATED -> "?input=from notification"
        else -> null
    } ?: ""
}

/*
* create PendingIntent for given deeplink
* */
private fun Context.createNotifyIntent(deepLink: Uri?): Intent {
    return Intent(this, NavigationActivityWithPrimaryFragment::class.java).apply { data = deepLink }
}
