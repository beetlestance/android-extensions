package com.beetlestance.androidextensions.navigation.experimental

import android.util.SparseArray
import androidx.core.util.forEach
import androidx.core.util.set
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.beetlestance.androidextensions.navigation.data.NavigateOnceDeeplinkRequest
import com.beetlestance.androidextensions.navigation.extensions.mNavAnimations
import com.beetlestance.androidextensions.navigation.extensions.navigateOnce

class MultiNavHost(
    private val navGraphIds: List<Int>,
    private val containerId: Int,
    private val primaryFragmentIndex: Int,
    private val fragmentManager: FragmentManager,
    private val multiNavHostBackStackPolicy: MultiNavHostBackStackPolicy,
    private val multipleBackStackCount: Int = navGraphIds.size
) {
    // Map of tags
    private val graphIdToTagMap: SparseArray<String> = SparseArray<String>()

    private var primaryFragmentId: Int = 0

    private var selectedNavGraphId: Int = 0

    private var lastRetainedBackStackEntry: String? = null

    private var navController: NavController? = null

    fun create(selectedNavId: Int) {
        selectedNavGraphId = selectedNavId

        navGraphIds.forEachIndexed { index, navGraphId ->
            val fragmentTag: String = getFragmentTag(index)

            // Find or create the Navigation host fragment
            val navHostFragment = obtainNavHostFragment(
                fragmentTag = fragmentTag,
                navGraphId = navGraphId
            )

            // Obtain its id
            val graphId = navHostFragment.navController.graph.id

            if (index == primaryFragmentIndex) primaryFragmentId = graphId

            // Save to the map
            graphIdToTagMap[graphId] = fragmentTag

            if (primaryFragmentId == graphId) {
                attachNavHostFragment(navHostFragment)
                navController = navHostFragment.navController
            } else {
                detachNavHostFragment(navHostFragment)
            }
        }
    }

    fun selectSiblings(navGraphId: Int): Boolean {
        // Don't do anything if the state is state has already been saved.
        return if (fragmentManager.isStateSaved) {
            false
        } else {
            val newlySelectedItemTag = graphIdToTagMap[navGraphId]

            val selectedFragment = fragmentManager.findFragmentByTag(newlySelectedItemTag)
                    as NavHostFragment

            if (!selectedFragment.isAdded) {
                swapBackStackEntry(
                    fragmentTag = newlySelectedItemTag,
                    navHostFragment = selectedFragment
                )
            }
            selectedNavGraphId = navGraphId
            navController = selectedFragment.navController
            true
        }
    }

    fun reselectSiblings(navGraphId: Int) {
        val newlySelectedItemTag = graphIdToTagMap[navGraphId]
        val selectedFragment = fragmentManager.findFragmentByTag(newlySelectedItemTag)
                as NavHostFragment
        val navController = selectedFragment.navController
        // Pop the back stack to the start destination of the current navController graph
        navController.popBackStack(navController.graph.startDestination, false)
    }

    fun observeBackStack(onStackChange: (Int) -> Unit) {
        fragmentManager.addOnBackStackChangedListener {
            navController?.let { navController ->
                // Reset the graph if the currentDestination is not valid (happens when the back
                // stack is popped after using the back button).
                if (navController.currentDestination == null) {
                    navController.navigate(navController.graph.id)
                } else {
                    val newBackStackId = fragmentManager.currentBackStackId()

                    when (multiNavHostBackStackPolicy) {
                        MultiNavHostBackStackPolicy.NO_BACK_STACK -> Unit
                        MultiNavHostBackStackPolicy.PRIMARY_BACK_STACK -> {
                            if (newBackStackId == null) onStackChange(primaryFragmentId)
                        }
                        MultiNavHostBackStackPolicy.MULTIPLE_BACK_STACK -> {
                            validateStackAndReset()
                            if (newBackStackId != null) onStackChange(newBackStackId)
                            else onStackChange(primaryFragmentId)
                        }
                    }
                }
            }
        }
    }

    private fun obtainNavHostFragment(
        fragmentTag: String,
        navGraphId: Int
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

    private fun swapBackStackEntry(
        fragmentTag: String,
        navHostFragment: NavHostFragment
    ) {
        if (navHostFragment.isDetached) attachNavHostFragment(navHostFragment)

        val backStackTag: String? = createAndValidateBackStackTag(fragmentTag)

        fragmentManager.beginTransaction()
            .setCustomAnimations(
                mNavAnimations.enterAnimation,
                mNavAnimations.exitAnimation,
                mNavAnimations.popEnterAnimation,
                mNavAnimations.popExitAnimation
            )
            .setReorderingAllowed(true)
            .setPrimaryNavigationFragment(navHostFragment)
            .replace(containerId, navHostFragment, fragmentTag)
            .apply {
                if (backStackTag == null) disallowAddToBackStack()
                else addToBackStack(backStackTag)
            }
            .commitAllowingStateLoss()
    }

    fun navigate(
        request: NavigateOnceDeeplinkRequest,
        onMatchFound: (Int) -> Unit
    ) {
        navGraphIds.forEachIndexed { index, navGraphId ->
            val fragmentTag = getFragmentTag(index)

            // Find or create the Navigation host fragment
            val navHostFragment = obtainNavHostFragment(
                fragmentTag = fragmentTag,
                navGraphId = navGraphId
            )

            // Handle deeplink
            val canHandleDeeplink =
                navHostFragment.navController.graph.hasDeepLink(request.deeplink)

            if (canHandleDeeplink) {
                if (selectedNavGraphId != navHostFragment.navController.graph.id) {
                    onMatchFound(navHostFragment.navController.graph.id)
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

    private fun detachNavHostFragment(navHostFragment: NavHostFragment) {
        fragmentManager.beginTransaction()
            .detach(navHostFragment)
            .commitNow()
    }

    private fun attachNavHostFragment(navHostFragment: NavHostFragment) {
        fragmentManager.beginTransaction()
            .attach(navHostFragment)
            .setPrimaryNavigationFragment(navHostFragment)
            .commitNow()
    }

    private fun createAndValidateBackStackTag(fragmentTag: String): String? {
        return when (multiNavHostBackStackPolicy) {
            MultiNavHostBackStackPolicy.NO_BACK_STACK -> null
            MultiNavHostBackStackPolicy.PRIMARY_BACK_STACK -> {
                val primaryBackStackTag = generateBackStackName(
                    backStackIndex = 0,
                    fragmentTag = graphIdToTagMap[primaryFragmentId]
                )
                fragmentManager.popBackStack(
                    primaryBackStackTag,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
                primaryBackStackTag
            }
            MultiNavHostBackStackPolicy.MULTIPLE_BACK_STACK -> {
                val backStackCount = fragmentManager.backStackEntryCount
                if (backStackCount > multipleBackStackCount) {
                    val lastRetainedBackStackCount = backStackCount + 1 - multipleBackStackCount
                    lastRetainedBackStackEntry = generateBackStackName(
                        backStackIndex = lastRetainedBackStackCount,
                        fragmentTag = fragmentTag
                    )
                }
                generateBackStackName(
                    backStackIndex = backStackCount + 1,
                    fragmentTag = fragmentTag
                )
            }
        }
    }

    private fun FragmentManager.isOnBackStack(backStackName: String): Boolean {
        val backStackCount = backStackEntryCount
        for (index in 0 until backStackCount) {
            if (getBackStackEntryAt(index).name == backStackName) return true
        }
        return false
    }

    private fun FragmentManager.currentBackStackId(): Int? {
        return currentBackStackEntry()?.toFragmentTag()?.let { name ->
            graphIdToTagMap.getKeyAt(name)
        }
    }

    private fun FragmentManager.currentBackStackEntry(): FragmentManager.BackStackEntry? {
        return if (backStackEntryCount > 0) {
            getBackStackEntryAt(backStackEntryCount - 1)
        } else null
    }

    private fun validateStackAndReset() {
        val currentBackStackEntry = fragmentManager.currentBackStackEntry()?.name ?: return
        if (isLastRetainedBackEntry(currentBackStackEntry)) {
            fragmentManager.popBackStack(
                graphIdToTagMap[primaryFragmentId],
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }
    }

    private fun FragmentManager.BackStackEntry?.toFragmentTag(): String? {
        return this?.name?.split("-")?.getOrNull(1)
    }

    private fun isLastRetainedBackEntry(currentBackStackEntryTag: String): Boolean {
        return lastRetainedBackStackEntry != null && lastRetainedBackStackEntry == currentBackStackEntryTag
    }

    private fun <T> SparseArray<T>.getKeyAt(value: T): Int? {
        forEach { key, v -> if (v == value) return key }
        return null
    }

    @Suppress("HardCodedStringLiteral")
    private fun getFragmentTag(index: Int) = "multiNavHost#$index"

    private fun generateBackStackName(backStackIndex: Int, fragmentTag: String): String {
        return "$backStackIndex-$fragmentTag"
    }

    enum class MultiNavHostBackStackPolicy {
        NO_BACK_STACK,
        PRIMARY_BACK_STACK,
        MULTIPLE_BACK_STACK
    }
}