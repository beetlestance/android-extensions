package com.beetlestance.androidextensions.sample.fcm

import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class NotificationPayload(

    @SerializedName("action")
    val action: String,

    @SerializedName("body")
    val body: String?,

    @SerializedName("en")
    val title: String?,

    @SerializedName("silent")
    val silent: Boolean,

    @SerializedName("imageURL")
    val imageURL: String?
)

fun RemoteMessage.toNotificationPayload(): NotificationPayload {
    val remoteMessageTree: JsonElement = Gson().toJsonTree(data)
    return Gson().fromJson(remoteMessageTree, NotificationPayload::class.java)
}
