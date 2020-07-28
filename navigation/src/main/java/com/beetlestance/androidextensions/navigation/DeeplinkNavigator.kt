package com.beetlestance.androidextensions.navigation

import android.content.Intent
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import com.beetlestance.androidextensions.navigation.data.NavigateOnceDeeplinkRequest
import com.beetlestance.androidextensions.navigation.extensions.navigateDeeplink
import com.beetlestance.androidextensions.navigation.extensions.navigateOnce
import com.beetlestance.androidextensions.navigation.util.toSingleEvent
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 *
 */
open class DeeplinkNavigator {

    private var isDestinationChangedListenerAttached: Boolean = false

    internal var isBottomNavigationAttachedToActivity: Boolean = false

    private var hasSetGraphHandledDeeplink: Boolean = false
        get() {
            val mHasSetGraphHandledDeeplink = field
            return mHasSetGraphHandledDeeplink.also {
                hasSetGraphHandledDeeplink = false
            }
        }

    private var resetStackBeforeNavigation: Boolean = false

    private val navigatorDeeplink: MutableLiveData<NavigateOnceDeeplinkRequest> = MutableLiveData()
    internal val navigateRequest = navigatorDeeplink.toSingleEvent()


    private val clearBackStack: MutableLiveData<Boolean> = MutableLiveData(false)
    internal val popToPrimaryFragment = clearBackStack.toSingleEvent()

    fun navigateToTopLevelDestination(request: NavigateOnceDeeplinkRequest) {
        clearBackStack.postValue(resetStackBeforeNavigation)
        navigatorDeeplink.postValue(request)
    }

    internal fun handleDeeplink(
        navController: NavController,
        bottomNavigationView: BottomNavigationView,
        fragmentManager: FragmentManager,
        request: NavigateOnceDeeplinkRequest
    ) {
        if (isBottomNavigationAttachedToActivity) {
            bottomNavigationView.navigateDeeplink(
                request = request,
                fragmentManager = fragmentManager
            )
        } else {
            val isParentWorthyEnough = navController.graph.hasDeepLink(request.deeplink)
            when {
                isParentWorthyEnough && hasSetGraphHandledDeeplink.not() -> {
                    navController.navigateOnce(request)
                }
                isParentWorthyEnough.not() -> {
                    bottomNavigationView.navigateDeeplink(
                        request = request,
                        fragmentManager = fragmentManager
                    )
                }
            }
        }
    }

    internal fun handleDeeplinkIntent(
        intent: Intent?,
        intentUpdated: Boolean,
        navController: NavController,
        validateDeeplinkRequest: NavigateOnceDeeplinkRequest? = null,
        handleIntent: (intent: Intent?) -> Unit = {}
    ) {
        val deeplinkRequest = when {
            validateDeeplinkRequest != null -> validateDeeplinkRequest
            intent?.data != null -> NavigateOnceDeeplinkRequest(deeplink = intent.data!!)
            else -> null
        }

        deeplinkRequest?.let {
            if (isBottomNavigationAttachedToActivity.not()
                && navController.graph.hasDeepLink(it.deeplink)
                && intentUpdated.not()
            ) {
                this.hasSetGraphHandledDeeplink = intentUpdated.not()
            } else {
                navigatorDeeplink.postValue(it)
            }
        }
        handleIntent(intent)
        intent?.data = null
    }

    @Synchronized
    internal fun onDestinationChangeListener(navController: NavController) {
        if (isDestinationChangedListenerAttached) return
        else isDestinationChangedListenerAttached = true

        val primaryFragmentId = requireNotNull(navController.currentDestination?.id)

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            resetStackBeforeNavigation = destination.id == primaryFragmentId
        }
    }

    companion object {
        private var deeplinkNavigatorInstance: DeeplinkNavigator? = null

        // Provide single instance for [TopLevelNavigator]
        fun getTopLevelNavigator(): DeeplinkNavigator {
            return deeplinkNavigatorInstance ?: DeeplinkNavigator().also { navigator ->
                deeplinkNavigatorInstance = navigator
            }
        }
    }
}
