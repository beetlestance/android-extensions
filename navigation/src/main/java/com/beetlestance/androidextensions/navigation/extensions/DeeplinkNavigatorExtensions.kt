package com.beetlestance.androidextensions.navigation.extensions

import android.content.Intent
import androidx.annotation.IdRes
import androidx.annotation.NavigationRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.beetlestance.androidextensions.navigation.Navigator
import com.beetlestance.androidextensions.navigation.data.NavigateOnceDeeplinkRequest
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Fragment extension that handles all the deeplink request in the app.
 *
 * @param bottomNavigationView this is the bottomNavigationView that contains the fragment for
 * multiple back stacks
 * @param request is a lambda function that returns the current [NavigateOnceDeeplinkRequest] for
 * modification or any validation. it requires [NavigateOnceDeeplinkRequest] as its return type
 *
 * How does it work?
 * This function observe for navigate request[NavigateOnceDeeplinkRequest] that may be originated
 * from anywhere in the app through function `DeeplinkNavigator.navigate()` or handleIntentForDeeplink
 * in your launcher Activity.
 * @see handleIntentForDeeplink
 */
fun Fragment.handleDeeplink(
    bottomNavigationView: BottomNavigationView,
    request: NavigateOnceDeeplinkRequest.() -> NavigateOnceDeeplinkRequest = { this }
) {
    val navigator = Navigator.getInstance()
    navigator.navigateRequest.observe(viewLifecycleOwner) {
        navigator.handleDeeplink(
            bottomNavigationView = bottomNavigationView,
            fragmentManager = childFragmentManager,
            request = it.request()
        )
    }
}


/**
 * Activity extension that handles all the deeplink request in the app.
 *
 * @param bottomNavigationView this is the bottomNavigationView that contains the fragment for
 * multiple back stacks
 * @param request is a lambda function that returns the current [NavigateOnceDeeplinkRequest] for
 * modification or any validation. it requires [NavigateOnceDeeplinkRequest] as its return type
 *
 * How does it work?
 * This function observe for navigate request[NavigateOnceDeeplinkRequest] that may be originated
 * from anywhere in the app through function `DeeplinkNavigator.navigate()` or handleIntentForDeeplink
 * in your launcher Activity.
 * @see handleIntentForDeeplink
 */
fun AppCompatActivity.handleDeeplink(
    bottomNavigationView: BottomNavigationView,
    request: NavigateOnceDeeplinkRequest.() -> NavigateOnceDeeplinkRequest = { this }
) {
    val navigator = Navigator.getInstance()
    navigator.navigateRequest.observe(this) {
        navigator.handleDeeplink(
            bottomNavigationView = bottomNavigationView,
            fragmentManager = supportFragmentManager,
            request = it.request()
        )
    }
}

/**
 * Activity extension to specify how does you want the library to handle backstack of the activity
 * NavController.
 *
 * @param primaryFragmentId This is the fragment id from which [handleDeeplink] is called (BottomNavigationView
 * is also setup here)
 * @param avoidNavigationForFragmentIds List of all the fragment that should not be cleared for navigation. Like
 * Login flow or any other important flow that should not be cleared.
 * @param retainDeeplink what to do with deeplink in case user was on destination that is included in
 * avoidNavigationForFragmentIds list. true: should navigate once user comes back to primary fragment
 * false: discard the navigation
 *
 * This should only be called from primary activity. Activity that hosts fragment with bottom navigation.
 */
fun AppCompatActivity.backStackClearBehavior(
    primaryFragmentId: Int,
    avoidNavigationForFragmentIds: List<Int> = emptyList(),
    retainDeeplink: Boolean = avoidNavigationForFragmentIds.isEmpty()
) {
}

/**
 * Activity extension for retaining the deeplink and navigating once user lands on primary fragment.
 * Remember this function set the intent.data = null to only navigate to destination once.
 *
 * @param isIntentUpdated this specifies that whether the intent was launching intent for activity or
 * intent was updated via onNewIntent().
 * @param validateDeeplinkRequest: [NavigateOnceDeeplinkRequest] to use. User can pass validated deeplink.
 * example: if user is not logged in redirect the user to login deeplink. The original deeplink is discarded.
 * @param handleIntent returns the activity with original intent before setting it to null. So that user can perform
 * action like FirebaseDynamicLink.getInstance().getDynamicLink(intent).
 *
 */
fun AppCompatActivity.handleNewIntentForDeeplink(
    validateDeeplinkRequest: NavigateOnceDeeplinkRequest? = null,
    handleIntent: (intent: Intent?) -> Unit = {}
) {
    Navigator.getInstance().handleDeeplinkIntent(
        intent = intent,
        intentUpdated = true,
        validateDeeplinkRequest = validateDeeplinkRequest,
        handleIntent = handleIntent
    )
}

/**
 * This setups the navController for navigating to top level i.e activity destinations
 *
 * @param viewId Id of the FragmentContainerView which will host different fragments
 *
 * Please make sure to always call this function before any other library methods.
 *
 * Use NavHostFragment to find controller when used in activity onCreate
 * Issue: https://issuetracker.google.com/issues/142847973
 */
fun AppCompatActivity.setNavigationPolicy(
    intent: Intent?,
    validateDeeplinkRequest: NavigateOnceDeeplinkRequest? = null,
    handleIntent: (intent: Intent?) -> Unit = {}
) {
    val navigator = Navigator.getInstance()

    navigator.apply {
        handleDeeplinkIntent(
            intent = intent,
            intentUpdated = false,
            validateDeeplinkRequest = validateDeeplinkRequest,
            handleIntent = handleIntent
        )

        navigator.onDestinationChangeListener()
    }
}

fun AppCompatActivity.setNavigationPolicyWithPrimaryFragment(
    @IdRes fragmentContainerViewId: Int,
    @NavigationRes graphId: Int,
    @IdRes primaryFragmentId: Int,
    intent: Intent?,
    validateDeeplinkRequest: NavigateOnceDeeplinkRequest? = null,
    avoidNavigationForFragmentIds: List<Int> = emptyList(),
    navigateOnceOnPrimaryFragment: Boolean = true,
    handleIntent: (intent: Intent?) -> Unit = {}
) {
    val navigator = Navigator.getInstance()
    val navHostFragment =
        supportFragmentManager.findFragmentById(fragmentContainerViewId) as NavHostFragment
    val navController: NavController = navHostFragment.navController

    navigator.apply {
        handleDeeplinkIntent(
            intent = intent,
            intentUpdated = false,
            validateDeeplinkRequest = validateDeeplinkRequest,
            handleIntent = handleIntent
        )

        setActivityNavController(navController)
        setPrimaryNavigationId(primaryFragmentId)
        retainFragmentIds = avoidNavigationForFragmentIds
        retainDeeplink = navigateOnceOnPrimaryFragment
        navigator.onDestinationChangeListener()
    }

    // setGraph after intvent is handled
    if (graphId != null)
        navController.setGraph(graphId)
}



