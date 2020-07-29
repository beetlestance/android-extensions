package com.beetlestance.androidextensions.navigation.extensions

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.beetlestance.androidextensions.navigation.Navigator
import com.beetlestance.androidextensions.navigation.data.NavigateOnceDeeplinkRequest
import com.google.android.material.bottomnavigation.BottomNavigationView

fun Fragment.handleDeeplink(
    bottomNavigationView: BottomNavigationView,
    request: NavigateOnceDeeplinkRequest.() -> NavigateOnceDeeplinkRequest = { this }
) {
    val navigator = Navigator.getInstance()
    navigator.navigateRequest.observe(viewLifecycleOwner) {
        navigator.handleDeeplink(
            navController = findNavController(),
            bottomNavigationView = bottomNavigationView,
            fragmentManager = childFragmentManager,
            request = it.request()
        )
    }
}

fun AppCompatActivity.handleDeeplink(
    navHostFragmentId: Int,
    bottomNavigationView: BottomNavigationView,
    request: NavigateOnceDeeplinkRequest.() -> NavigateOnceDeeplinkRequest = { this }
) {
    val navigator = Navigator.getInstance()
    navigator.navigateRequest.observe(this) {
        navigator.handleDeeplink(
            navController = findNavController(navHostFragmentId),
            bottomNavigationView = bottomNavigationView,
            fragmentManager = supportFragmentManager,
            request = it.request()
        )
    }
}

fun AppCompatActivity.setBackStackPopBehavior(
    primaryFragmentId: Int,
    avoidNavigationForFragmentIds: List<Int> = emptyList()
) {
    val navigator = Navigator.getInstance()
    lifecycleScope.launchWhenStarted {
        navigator.popToPrimaryFragment.observe(
            this@setBackStackPopBehavior,
            navigator.popBackStackObserver(primaryFragmentId)
        )
    }
}

fun AppCompatActivity.handleIntentForDeeplink(
    isIntentUpdated: Boolean,
    validateDeeplinkRequest: NavigateOnceDeeplinkRequest? = null,
    handleIntent: (intent: Intent?) -> Unit = {}
) {
    lifecycleScope.launchWhenStarted {
        Navigator.getInstance().handleDeeplinkIntent(
            intent = intent,
            intentUpdated = isIntentUpdated,
            validateDeeplinkRequest = validateDeeplinkRequest,
            handleIntent = handleIntent
        )
    }
}


fun AppCompatActivity.setUpNavHostFragmentId(viewId: Int) {
    lifecycleScope.launchWhenStarted {
        val controller = findNavController(viewId)
        Navigator.getInstance().setActivityNavController(controller)
    }
}
