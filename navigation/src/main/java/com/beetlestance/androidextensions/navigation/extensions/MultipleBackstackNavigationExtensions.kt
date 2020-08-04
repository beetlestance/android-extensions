package com.beetlestance.androidextensions.navigation.extensions

import android.util.SparseArray
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.forEach
import androidx.core.util.set
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.beetlestance.androidextensions.navigation.Navigator
import com.beetlestance.androidextensions.navigation.data.NavAnimations
import com.beetlestance.androidextensions.navigation.data.NavigateOnceDeeplinkRequest
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
 * Ported from: https://github.com/android/architecture-components-samples/blob/master/NavigationAdvancedSample
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
    // Map of tags
    val graphIdToTagMap = SparseArray<String>()

    // Result. Mutable live data with the selected controlled
    val selectedNavController = MutableLiveData<NavController>()

    // First fragment graph index in the provided list
    var firstFragmentGraphId = 0

    // First create a NavHostFragment for each NavGraph ID
    //
    // Should attach the first selected fragment at the last, so that navController is correctly set on
    // [FragmentContainerView].
    // See the link below for the changes in Navigation library.
    // https://android.googlesource.com/platform/frameworks/support/+/523601f023afb95f861e94c149c50e4962ea42e3
    mNavGraphIds.forEachIndexed { index, navGraphId ->
        val fragmentTag: String = getFragmentTag(index)

        // Find or create the Navigation host fragment
        val navHostFragment =
            obtainNavHostFragment(
                fragmentManager,
                fragmentTag,
                navGraphId,
                mContainerId
            )

        // Obtain its id
        val graphId = navHostFragment.navController.graph.id

        if (index == 0) {
            firstFragmentGraphId = graphId
        }

        // Save to the map
        graphIdToTagMap[graphId] = fragmentTag

        // Detach nav host fragment depending on whether it's the selected item.
        if (selectedItemId != graphId) {
            detachNavHostFragment(
                fragmentManager,
                navHostFragment
            )
        }
    }

    // Selected Item NavHostFragment
    val navHostFragment =
        obtainNavHostFragment(
            fragmentManager,
            getFragmentTag(graphIdToTagMap.indexOfKey(selectedItemId)),
            selectedItemId,
            mContainerId
        )

    // Update liveData with the selected graph
    selectedNavController.value = navHostFragment.navController

    // Attach nav host fragment with the selected item.
    attachNavHostFragment(
        fragmentManager,
        navHostFragment,
        firstFragmentGraphId == selectedItemId
    )

    // Now connect selecting an item with swapping Fragments
    var selectedItemTag = graphIdToTagMap[selectedItemId]
    val firstFragmentTag = graphIdToTagMap[firstFragmentGraphId]
    var isOnFirstFragment = selectedItemTag == firstFragmentTag

    // When a navigation item is selected
    setOnNavigationItemSelectedListener { item ->
        // Don't do anything if the state is state has already been saved.
        if (fragmentManager.isStateSaved) {
            false
        } else {
            val newlySelectedItemTag = graphIdToTagMap[item.itemId]
            if (selectedItemTag != newlySelectedItemTag) {
                // Pop everything above the first fragment (the "fixed start destination")
                fragmentManager.popBackStack(
                    firstFragmentTag,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
                val selectedFragment = fragmentManager.findFragmentByTag(newlySelectedItemTag)
                        as NavHostFragment

                // Exclude the first fragment tag because it's always in the back stack.
                if (firstFragmentTag != newlySelectedItemTag) {
                    // Commit a transaction that cleans the back stack and adds the first fragment
                    // to it, creating the fixed started destination.
                    fragmentManager.beginTransaction()
                        .setCustomAnimations(
                            mNavAnimations.enterAnimation,
                            mNavAnimations.exitAnimation,
                            mNavAnimations.popEnterAnimation,
                            mNavAnimations.popExitAnimation
                        )
                        .attach(selectedFragment)
                        .setPrimaryNavigationFragment(selectedFragment)
                        .apply {
                            // Detach all other Fragments
                            graphIdToTagMap.forEach { _, fragmentTagIter ->
                                if (fragmentTagIter != newlySelectedItemTag) {
                                    detach(
                                        requireNotNull(
                                            fragmentManager.findFragmentByTag(
                                                firstFragmentTag
                                            )
                                        )
                                    )
                                }
                            }
                        }
                        .addToBackStack(firstFragmentTag)
                        .setReorderingAllowed(true)
                        .commit()
                }
                selectedItemTag = newlySelectedItemTag
                isOnFirstFragment = selectedItemTag == firstFragmentTag
                selectedNavController.value = selectedFragment.navController
                true
            } else {
                false
            }
        }
    }

    // Optional: on item reselected, pop back stack to the destination of the graph
    setupItemReselected(graphIdToTagMap, fragmentManager)

    handleDeeplink(
        lifecycleOwner = lifecycleOwner,
        fragmentManager = fragmentManager,
        activityNavController = activityNavController,
        request = validatedRequest
    )

    // Finally, ensure that we update our BottomNavigationView when the back stack changes
    fragmentManager.addOnBackStackChangedListener {
        if (!isOnFirstFragment && !fragmentManager.isOnBackStack(firstFragmentTag)) {
            selectedItemId = firstFragmentGraphId
        }

        // Reset the graph if the currentDestination is not valid (happens when the back
        // stack is popped after using the back button).
        selectedNavController.value?.let { controller ->
            if (controller.currentDestination == null) {
                controller.navigate(controller.graph.id)
            }
        }
    }

    selectedNavController.observe(lifecycleOwner) {
        onControllerChange(it)
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
 * from anywhere in the app through function `DeeplinkNavigator.navigate()` or handleIntentForDeeplink
 * in your launcher Activity.
 */

private fun BottomNavigationView.handleDeeplink(
    lifecycleOwner: LifecycleOwner,
    fragmentManager: FragmentManager,
    activityNavController: NavController?,
    request: NavigateOnceDeeplinkRequest.() -> NavigateOnceDeeplinkRequest = { this }
) {
    val navigator = Navigator.getInstance()
    navigator.navigateRequest.observe(lifecycleOwner) {
        navigator.handleDeeplink(
            navController = activityNavController,
            bottomNavigationView = this,
            fragmentManager = fragmentManager,
            request = it.request()
        )
    }
}

private fun BottomNavigationView.setupItemReselected(
    graphIdToTagMap: SparseArray<String>,
    fragmentManager: FragmentManager
) {
    setOnNavigationItemReselectedListener { item ->
        val newlySelectedItemTag = graphIdToTagMap[item.itemId]
        val selectedFragment = fragmentManager.findFragmentByTag(newlySelectedItemTag)
                as NavHostFragment
        val navController = selectedFragment.navController
        // Pop the back stack to the start destination of the current navController graph
        navController.popBackStack(
            navController.graph.startDestination, false
        )
    }
}

private fun detachNavHostFragment(
    fragmentManager: FragmentManager,
    navHostFragment: NavHostFragment
) {
    fragmentManager.beginTransaction()
        .detach(navHostFragment)
        .commitNow()
}

private fun attachNavHostFragment(
    fragmentManager: FragmentManager,
    navHostFragment: NavHostFragment,
    isPrimaryNavFragment: Boolean
) {
    fragmentManager.beginTransaction()
        .attach(navHostFragment)
        .apply {
            if (isPrimaryNavFragment) {
                setPrimaryNavigationFragment(navHostFragment)
            }
        }
        .commitNow()
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


