package com.beetlestance.sample.utils

import android.content.Context
import android.net.Uri
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeeplinkValidator @Inject constructor() {

    fun validateDeeplink(uri: Uri, context: Context): Uri {
//        val user = User()
//        val deeplink = uri.toString()
//        val noArgsDeeplink = deeplink.split("?").getOrElse(0) { uri.toString() }
//        val validDeepLink = when {
//            noArgsDeeplink.contains(DASHBOARD_DEEPLINK) -> {
//                if (user.loggedIn)  deeplink
//                else LOGIN_REGISTRATION_DEEPLINK
//            }
//        noArgsDeeplink.contains(IN_APP_BROWSER_DEEPLINK) -> {
//                if (context.isWebViewAvailable()) {
//                    deeplink
//                } else {
//                    val url = deeplink.split("=").getOrNull(1)
//                    REMOVE_DEEPLINK.also { url?.let { context.launchNativeBrowser(it) } }
//                }
//            }
//            else -> deeplink
//        }
        return uri // validDeeplink.toUri()
    }

    companion object {
        private const val REMOVE_DEEPLINK = ""
    }
}
