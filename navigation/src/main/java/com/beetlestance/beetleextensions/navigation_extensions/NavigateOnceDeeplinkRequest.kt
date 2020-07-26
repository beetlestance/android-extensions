package com.beetlestance.beetleextensions.navigation_extensions

import android.net.Uri

data class NavigateOnceDeeplinkRequest(
    // deeplink for navigation
    val deeplink: Uri,
    // If set to false and currentDestination has deeplink, backStackEntry will be used
    // else set to true each time it will create new instance
    val allowMultipleInstance: Boolean = false,
    //  if set to true fragment will be popped from backStack and navigate
    // else set to false backStackEntry will be used with old arguments if any
    val updateArguments: Boolean = true
)
