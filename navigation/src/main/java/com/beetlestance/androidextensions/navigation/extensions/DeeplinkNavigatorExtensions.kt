package com.beetlestance.androidextensions.navigation.extensions

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.beetlestance.androidextensions.navigation.DeeplinkNavigator
import com.beetlestance.androidextensions.navigation.data.NavigateOnceDeeplinkRequest
import com.google.android.material.bottomnavigation.BottomNavigationView

fun Fragment.handleDeeplink(
    bottomNavigationView: BottomNavigationView,
    request: NavigateOnceDeeplinkRequest
) {
    DeeplinkNavigator.getTopLevelNavigator().handleDeeplink(
        navController = findNavController(),
        bottomNavigationView = bottomNavigationView,
        fragmentManager = childFragmentManager,
        request = request
    )
}

fun AppCompatActivity.handleDeeplink(
    activityNavGraphId: Int,
    bottomNavigationView: BottomNavigationView,
    request: NavigateOnceDeeplinkRequest
) {
    DeeplinkNavigator.getTopLevelNavigator().handleDeeplink(
        navController = findNavController(activityNavGraphId),
        bottomNavigationView = bottomNavigationView,
        fragmentManager = supportFragmentManager,
        request = request
    )
}
