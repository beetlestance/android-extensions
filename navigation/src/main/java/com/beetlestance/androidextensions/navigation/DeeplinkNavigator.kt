package com.beetlestance.androidextensions.navigation

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.beetlestance.androidextensions.navigation.data.NavigateOnceDeeplinkRequest
import com.beetlestance.androidextensions.navigation.extensions.navigateDeeplink
import com.beetlestance.androidextensions.navigation.extensions.navigateOnce
import com.beetlestance.androidextensions.navigation.util.toSingleEvent
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 *
 */
open class DeeplinkNavigator {

    internal var primaryFragmentId: Int? = null

    internal var isBottomNavigationAttachedToActivity: Boolean = false

    private var hasSetGraphHandledDeeplink: Boolean = false
        get() {
            val mHasSetGraphHandledDeeplink = field
            return mHasSetGraphHandledDeeplink.also {
                hasSetGraphHandledDeeplink = false
            }
        }

    private val navigatorDeeplink: MutableLiveData<NavigateOnceDeeplinkRequest> = MutableLiveData()
    internal val navigateRequest = navigatorDeeplink.toSingleEvent()

    private val clearBackStack: MutableLiveData<Boolean> = MutableLiveData(false)
    val resetStackBeforeNavigation = clearBackStack.toSingleEvent()

    /**
     * check if current fragment is DashboardFragment
     */
    fun Fragment.isDashboardFragment(): Boolean {
        val navController = findNavController()
        return navController.currentDestination?.id == primaryFragmentId
    }

    fun navigateToTopLevelDestination(request: NavigateOnceDeeplinkRequest) {
        navigatorDeeplink.postValue(request)
    }

    fun clearBackStack(shouldClear: Boolean) {
        clearBackStack.postValue(shouldClear)
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
