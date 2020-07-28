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

    private var intentUpdated: Boolean = false
        get() {
            val mIntentUpdated = field
            return mIntentUpdated.also {
                intentUpdated = false
            }
        }

    private val navigatorDeeplink: MutableLiveData<NavigateOnceDeeplinkRequest> = MutableLiveData()
    val observerForTopLevelNavigation = navigatorDeeplink.toSingleEvent()

    private val clearBackStack: MutableLiveData<Boolean> = MutableLiveData(false)
    val observeForClearBackStack = clearBackStack.toSingleEvent()

    /**
     * check if current fragment is DashboardFragment
     */
    private fun Fragment.isDashboardFragment(): Boolean {
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
        val isTopLevelDestination = navController.graph.hasDeepLink(request.deeplink)
        if (isTopLevelDestination && intentUpdated) {
            navController.navigateOnce(request)
        } else {
            bottomNavigationView.navigateDeeplink(
                request = request,
                fragmentManager = fragmentManager
            )
        }
    }

    fun handleDeeplinkIntent(
        intent: Intent?,
        intentUpdated: Boolean,
        shouldClearBackStack: Boolean = false,
        validateDeeplinkRequest: NavigateOnceDeeplinkRequest? = null,
        handleIntent: (intent: Intent?) -> Unit = {}
    ) {
        this.intentUpdated = intentUpdated

        if (intentUpdated) clearBackStack(true)

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
        fun getTopLevelNavigator(): DeeplinkNavigator {
            return deeplinkNavigatorInstance ?: DeeplinkNavigator().also { navigator ->
                deeplinkNavigatorInstance = navigator
            }
        }
    }
}
