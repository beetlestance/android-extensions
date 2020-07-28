package com.beetlestance.androidextensions.navigation.data

import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.fragment.FragmentNavigator

data class NavigateOnceDirectionRequest(
    // directions for navigation
    val directions: NavDirections,
    // NavOptions stores special options for navigate actions
    val navOptions: NavOptions? = null,
    // Extras that can be passed to FragmentNavigator to enable Fragment specific behavior
    // such as shared transitions for destination
    val navigatorExtras: FragmentNavigator.Extras? = null,
    // Defines the behavior of multiple continuous instances to be allowed or not for a destination.
    // true: allows continuous instances of a same fragment.
    // false: do not allow continuous instances of same fragment.
    val allowMultipleInstance: Boolean = false,
    //  if set to true fragment will be popped from backStack and navigate
    // else set to false backStackEntry will be used with old arguments if any
    val updateArguments: Boolean = true
)
