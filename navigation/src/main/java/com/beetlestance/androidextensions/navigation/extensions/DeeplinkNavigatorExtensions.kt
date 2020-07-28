package com.beetlestance.androidextensions.navigation.extensions

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.beetlestance.androidextensions.navigation.DeeplinkNavigator
import com.beetlestance.androidextensions.navigation.data.NavigateOnceDeeplinkRequest
import com.google.android.material.bottomnavigation.BottomNavigationView

fun Fragment.handleDeeplink(
    bottomNavigationView: BottomNavigationView,
    request: NavigateOnceDeeplinkRequest.() -> NavigateOnceDeeplinkRequest = { this }
) {
    val navigator = DeeplinkNavigator.getTopLevelNavigator()
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
    val navigator = DeeplinkNavigator.getTopLevelNavigator()
    navigator.navigateRequest.observe(this) {
        navigator.handleDeeplink(
            navController = findNavController(navHostFragmentId),
            bottomNavigationView = bottomNavigationView,
            fragmentManager = supportFragmentManager,
            request = it.request()
        )
    }
}

fun AppCompatActivity.handleMultiFragmentBackStack(
    navHostFragmentId: Int,
    primaryFragmentId: Int,
    avoidNavigationForFragmentIds: List<Int> = emptyList()
) {
    lifecycleScope.launchWhenStarted {
        DeeplinkNavigator.getTopLevelNavigator().popToPrimaryFragment.observe(this@handleMultiFragmentBackStack) {
            findNavController(navHostFragmentId).popBackStack(primaryFragmentId, false)
        }
    }
}

fun AppCompatActivity.handleDeeplinkIntent(
    navHostFragmentId: Int,
    validateDeeplinkRequest: NavigateOnceDeeplinkRequest? = null,
    handleIntent: (intent: Intent?) -> Unit = {}
) {
    lifecycleScope.launchWhenStarted {
        DeeplinkNavigator.getTopLevelNavigator().handleDeeplinkIntent(
            intent = intent,
            intentUpdated = false,
            validateDeeplinkRequest = validateDeeplinkRequest,
            handleIntent = handleIntent,
            navController = findNavController(navHostFragmentId)
        )
    }
}

fun AppCompatActivity.handleOnNewDeeplinkIntent(
    intent: Intent?,
    navHostFragmentId: Int,
    validateDeeplinkRequest: NavigateOnceDeeplinkRequest? = null,
    handleIntent: (intent: Intent?) -> Unit = {}
) {
    DeeplinkNavigator.getTopLevelNavigator().handleDeeplinkIntent(
        intent = intent,
        intentUpdated = true,
        validateDeeplinkRequest = validateDeeplinkRequest,
        handleIntent = handleIntent,
        navController = findNavController(navHostFragmentId)
    )
}
