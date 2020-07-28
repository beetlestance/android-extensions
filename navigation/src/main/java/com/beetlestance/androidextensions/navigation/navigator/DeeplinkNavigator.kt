package com.beetlestance.androidextensions.navigation.navigator

import android.content.Intent
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import com.beetlestance.androidextensions.navigation.NavigateOnceDeeplinkRequest
import com.beetlestance.androidextensions.navigation.navigateDeeplink
import com.beetlestance.androidextensions.navigation.navigateOnce
import com.google.android.material.bottomnavigation.BottomNavigationView

open class DeeplinkNavigator {




    private val navigatorDeeplink: MutableLiveData<NavigateOnceDeeplinkRequest> = MutableLiveData()
    val observerForTopLevelNavigation = navigatorDeeplink.toSingleEvent()

    private val clearBackStack: MutableLiveData<Boolean> = MutableLiveData(false)
    val observeForClearBackStack = clearBackStack.toSingleEvent()

    fun navigateToTopLevelDestination(request: NavigateOnceDeeplinkRequest) {
        navigatorDeeplink.postValue(request)
    }

    fun clearBackStack(shouldClear: Boolean) {
        clearBackStack.postValue(shouldClear)
    }

    fun handleDeeplink(
        topLevelNavController: NavController,
        bottomNavigationView: BottomNavigationView,
        fragmentManager: FragmentManager,
        request: NavigateOnceDeeplinkRequest
    ) {
        val isTopLevelDestination = topLevelNavController.graph.hasDeepLink(request.deeplink)
        if (isTopLevelDestination) {
            topLevelNavController.navigateOnce(request)
        } else {
            bottomNavigationView.navigateDeeplink(
                request = request,
                fragmentManager = fragmentManager
            )
        }
    }

    fun handleDeeplinkIntent(
        intent: Intent?,
        validateDeeplinkRequest: NavigateOnceDeeplinkRequest? = null,
        handleIntent: (intent: Intent?) -> Unit = {}
    ) {
        setNavigatorWithDeeplinkIntent(intent, validateDeeplinkRequest)
        handleIntent(intent)
        intent?.data = null
    }

    private fun setNavigatorWithDeeplinkIntent(
        intent: Intent?,
        validateDeeplinkRequest: NavigateOnceDeeplinkRequest? = null
    ) {
        val deeplinkRequest = when {
            validateDeeplinkRequest != null -> validateDeeplinkRequest
            intent?.data != null -> NavigateOnceDeeplinkRequest(deeplink = intent.data!!)
            else -> null
        }

        deeplinkRequest?.let { navigatorDeeplink.postValue(it) }
    }

    companion object {
        private var deeplinkNavigatorInstance: DeeplinkNavigator? = null

        // Provide single instance for [TopLevelNavigator]
        fun getTopLevelNavigator() =
            deeplinkNavigatorInstance ?: DeeplinkNavigator().also { navigator ->
                deeplinkNavigatorInstance = navigator
            }
    }
}