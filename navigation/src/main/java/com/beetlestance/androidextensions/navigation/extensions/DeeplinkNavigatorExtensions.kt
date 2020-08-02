package com.beetlestance.androidextensions.navigation.extensions

import android.content.Intent
import androidx.annotation.IdRes
import androidx.annotation.NavigationRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.beetlestance.androidextensions.navigation.DeeplinkNavigationPolicy
import com.beetlestance.androidextensions.navigation.Navigator
import com.beetlestance.androidextensions.navigation.data.NavigateOnceDeeplinkRequest
import com.google.android.material.bottomnavigation.BottomNavigationView



/**
 * Activity extension to specify how does you want the library to handle backstack of the activity
 * NavController.
 *
 * @param navHostFragmentId [FragmentContainerView] id for activity
 * @param primaryFragmentId This is the fragment id from which [handleDeeplink] is called (BottomNavigationView
 * is also setup here)
 * @param graphId activity graph, this is graph is to be set on activity fragment container
 * @param fragmentBackStackBehavior map of all the fragment deeplink with the behavior you want.
 * If the deeplink is not specified the default behavior will be [DeeplinkNavigationPolicy.EXIT_AND_NAVIGATE]
 *
 * This should only be called from primary activity. Activity that hosts fragment with bottom navigation.
 */
fun AppCompatActivity.setUpDeeplinkNavigationBehavior(
    @IdRes navHostFragmentId: Int,
    @IdRes primaryFragmentId: Int,
    @NavigationRes graphId: Int,
    fragmentBackStackBehavior: Map<Int, DeeplinkNavigationPolicy> = mapOf()
) {
    val navigator = Navigator.getInstance()
    navigator.setPrimaryNavigationId(primaryFragmentId, navHostFragmentId)
    val navController = getNavController(navHostFragmentId)
    navigator.fragmentBackStackBehavior = fragmentBackStackBehavior

    // extract deeplink so that setGraph can not manually handle the deeplink
    val deeplink = intent.data
    intent.data = null
    navController.setGraph(graphId)

    // once setGraph is called set the deeplink again so that validations can be prformed
    // we set the data to null internally
    intent.data = deeplink

    navController.addOnDestinationChangedListener(navigator.onDestinationChangeListener)

    navigator.popToPrimaryFragment.observe(this) {
        navController.popBackStack(primaryFragmentId, false)
    }
}

/**
 * Activity extension for retaining the deeplink and navigating once user lands on primary fragment.
 * Remember this function set the intent.data = null to only navigate to destination once.
 *
 * @param validateDeeplinkRequest: [NavigateOnceDeeplinkRequest] to use. User can pass validated deeplink.
 * example: if user is not logged in redirect the user to login deeplink. The original deeplink is discarded.
 * @param handleIntent returns the activity with original intent before setting it to null. So that user can perform
 * action like FirebaseDynamicLink.getInstance().getDynamicLink(intent).
 *
 */
fun AppCompatActivity.handleDeeplinkIntent(
    handleIntent: (intent: Intent?) -> Unit = {}
) {
    val navigator = Navigator.getInstance()
    navigator.handleDeeplinkIntent(
        intent = intent,
        navController = navigator.parentNavHostContainerId?.let { getNavController(it) },
        handleIntent = handleIntent
    )
}

/**
 * Find the [NavController] for the activity
 * @param fragmentContainerId Id of the FragmentContainerView which will host all the fragments
 *
 * Use NavHostFragment to find controller when used in activity onCreate
 * Issue: https://issuetracker.google.com/issues/142847973
 */
private fun AppCompatActivity.getNavController(@IdRes fragmentContainerId: Int): NavController {
    val navHostFragment =
        supportFragmentManager.findFragmentById(fragmentContainerId) as NavHostFragment
    return navHostFragment.navController
}
