package com.beetlestance.androidextensions.navigation.extensions

import android.util.SparseArray
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.beetlestance.androidextensions.navigation.Navigator
import com.beetlestance.androidextensions.navigation.data.NavAnimations
import com.beetlestance.androidextensions.navigation.data.NavigateOnceDeeplinkRequest
import com.beetlestance.androidextensions.navigation.experimental.MultiNavHost
import com.google.android.material.bottomnavigation.BottomNavigationView

// global variable to store user provided navGraphids
internal var mNavGraphIds: List<Int> = emptyList()
    private set

// global variable to store user provided containerId
internal var mContainerId: Int = -1
    private set

// global variable to store user provided custom animations
internal var mNavAnimations: NavAnimations = NavAnimations()
    private set

/**
 * This function
 *
 * @param navGraphIds the graph ids to setup with [BottomNavigationView]. [navGraphIds] should
 * be in the exact order in which the [BottomNavigationView] menu is displayed.
 * @param containerId The container in which [NavHostFragment] will attach to.
 * @param bottomNavigationView BottomNavigationView to setup multiple NavHostFragment on.
 * @param navAnimations Set specific animation resources to run for the fragments that are
 * entering and exiting while selecting bottomNavigation item.
 * */
fun Fragment.setupMultipleBackStackBottomNavigation(
    navGraphIds: List<Int>,
    containerId: Int,
    bottomNavigationView: BottomNavigationView,
    navAnimations: NavAnimations = NavAnimations(),
    validatedRequest: NavigateOnceDeeplinkRequest.() -> NavigateOnceDeeplinkRequest = { this },
    onControllerChange: (NavController) -> Unit
) {
    storeNavDefaults(navGraphIds, containerId, navAnimations)
    bottomNavigationView.setupMultipleBackStack(
        fragmentManager = childFragmentManager,
        activityNavController = findNavController(),
        lifecycleOwner = viewLifecycleOwner,
        validatedRequest = validatedRequest,
        onControllerChange = onControllerChange
    )
}

/**
 * @param navGraphIds the graph ids to setup with [BottomNavigationView]. [navGraphIds] should
 * be in the exact order in which the [BottomNavigationView] menu is displayed.
 * @param containerId The container in which [NavHostFragment] will attach to.
 * @param navAnimations Set specific animation resources to run for the fragments that are
 * entering and exiting while selecting bottomNavigation item.
 * */
fun AppCompatActivity.setupMultipleBackStackBottomNavigation(
    navGraphIds: List<Int>,
    containerId: Int,
    bottomNavigationView: BottomNavigationView,
    navAnimations: NavAnimations = NavAnimations(),
    validatedRequest: NavigateOnceDeeplinkRequest.() -> NavigateOnceDeeplinkRequest = { this },
    onControllerChange: (NavController) -> Unit
) {
    storeNavDefaults(navGraphIds, containerId, navAnimations)
    bottomNavigationView.setupMultipleBackStack(
        fragmentManager = supportFragmentManager,
        activityNavController = null,
        lifecycleOwner = this,
        validatedRequest = validatedRequest,
        onControllerChange = onControllerChange
    )
}

private fun storeNavDefaults(
    navGraphIds: List<Int>,
    containerId: Int,
    navAnimations: NavAnimations
) {
    // Store all the information in above objects
    mNavGraphIds = navGraphIds
    mContainerId = containerId
    mNavAnimations = navAnimations
}

/**
 * Ported from:
 * https://github.com/android/architecture-components-samples/blob/master/NavigationAdvancedSample
 *
 * Manages the various graphs needed for a [BottomNavigationView].
 * This sample is a workaround until the Navigation Component supports multiple back stacks.
 *
 * @param fragmentManager The [FragmentManager] which will be used to attach [NavHostFragment]
 */
private fun BottomNavigationView.setupMultipleBackStack(
    fragmentManager: FragmentManager,
    lifecycleOwner: LifecycleOwner,
    activityNavController: NavController?,
    onControllerChange: (NavController) -> Unit,
    validatedRequest: NavigateOnceDeeplinkRequest.() -> NavigateOnceDeeplinkRequest = { this }
) {

    val multiNavHost = MultiNavHost(
        navGraphIds = mNavGraphIds,
        containerId = mContainerId,
        primaryFragmentIndex = 0,
        fragmentManager = fragmentManager,
        isSingleTopReplacement = true
    )

    multiNavHost.create(selectedItemId)

    // When a navigation item is selected
    setOnNavigationItemSelectedListener { item -> multiNavHost.selectSiblings(item.itemId) }

    // Optional: on item reselected, pop back stack to the destination of the graph
    setOnNavigationItemReselectedListener { item -> multiNavHost.reselectSiblings(item.itemId) }

    handleDeeplink(
        lifecycleOwner = lifecycleOwner,
        fragmentManager = fragmentManager,
        activityNavController = activityNavController,
        request = validatedRequest
    )

    multiNavHost.observeBackStack { itemId ->
        selectedItemId = itemId
    }
}

/**
 * Fragment extension that handles all the deeplink request in the app.
 *
 * @param request is a lambda function that returns the current [NavigateOnceDeeplinkRequest] for
 * modification or any validation. it requires [NavigateOnceDeeplinkRequest] as its return type
 *
 * How does it work?
 * This function observe for navigate request[NavigateOnceDeeplinkRequest] that may be originated
 * from anywhere in the app through function `DeeplinkNavigator.navigate()`
 * or handleIntentForDeeplink in your launcher Activity.
 */

private fun BottomNavigationView.handleDeeplink(
    lifecycleOwner: LifecycleOwner,
    fragmentManager: FragmentManager,
    activityNavController: NavController?,
    request: NavigateOnceDeeplinkRequest.() -> NavigateOnceDeeplinkRequest = { this }
) {
    val navigator = Navigator.getInstance()
    navigator.navigateRequest.observe(owner = lifecycleOwner) {
        navigator.handleDeeplink(
            navController = activityNavController,
            bottomNavigationView = this,
            fragmentManager = fragmentManager,
            request = it.request()
        )
    }
}

private fun obtainNavHostFragment(
    fragmentManager: FragmentManager,
    fragmentTag: String,
    navGraphId: Int,
    containerId: Int
): NavHostFragment {
    // If the Nav Host fragment exists, return it
    val existingFragment = fragmentManager.findFragmentByTag(fragmentTag) as NavHostFragment?
    existingFragment?.let { return it }

    // Otherwise, create it and return it.
    val navHostFragment = NavHostFragment.create(navGraphId)
    fragmentManager.beginTransaction()
        .add(containerId, navHostFragment, fragmentTag)
        .commitNow()
    return navHostFragment
}

private fun FragmentManager.isOnBackStack(backStackName: String): Boolean {
    val backStackCount = backStackEntryCount
    for (index in 0 until backStackCount) {
        if (getBackStackEntryAt(index).name == backStackName) {
            return true
        }
    }
    return false
}

@Suppress("HardCodedStringLiteral")
private fun getFragmentTag(index: Int) = "bottomNavigation#$index"

// Handle deeplink
internal fun BottomNavigationView.navigateDeeplink(
    fragmentManager: FragmentManager,
    request: NavigateOnceDeeplinkRequest
) {

    mNavGraphIds.forEachIndexed { index, navGraphId ->
        val fragmentTag = getFragmentTag(index)

        // Find or create the Navigation host fragment
        val navHostFragment =
            obtainNavHostFragment(
                fragmentManager,
                fragmentTag,
                navGraphId,
                mContainerId
            )

        // Handle deeplink
        val canHandleDeeplink = navHostFragment.navController.graph.hasDeepLink(request.deeplink)

        if (canHandleDeeplink) {
            if (selectedItemId != navHostFragment.navController.graph.id) {
                selectedItemId = navHostFragment.navController.graph.id
            }
            navHostFragment.lifecycleScope.launchWhenResumed {
                // Wait for fragment to restore state from backStack
                // otherwise navigate will be ignored
                // Ignoring navigate() call: FragmentManager has already saved its state
                navHostFragment.navController.navigateOnce(request)
            }
        }
    }
}


