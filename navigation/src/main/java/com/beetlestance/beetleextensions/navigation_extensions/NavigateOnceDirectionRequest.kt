package com.beetlestance.beetleextensions.navigation_extensions

import androidx.navigation.NavDirections
import androidx.navigation.fragment.FragmentNavigator

data class NavigateOnceDirectionRequest(
    // directions for navigation
    val directions: NavDirections,
    // Extras that can be passed to FragmentNavigator to enable Fragment specific behavior
    // such as shared transitions for destination
    val navigatorExtras: FragmentNavigator.Extras? = null,
    // If set to false and currentDestination has deeplink, backStackEntry will be used
    // else set to true each time it will create new instance
    val allowMultipleInstance: Boolean = false,
    //  if set to true fragment will be popped from backStack and navigate
    // else set to false backStackEntry will be used with old arguments if any
    val updateArguments: Boolean = true
)
