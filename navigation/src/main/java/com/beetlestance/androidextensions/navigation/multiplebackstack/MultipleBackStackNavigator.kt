package com.beetlestance.androidextensions.navigation.multiplebackstack

import android.net.Uri
import android.util.SparseArray
import androidx.annotation.IdRes
import androidx.core.util.set
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.beetlestance.androidextensions.navigation.R

class MultipleBackStackNavigator(
    private val navGraphIds: List<Int>,
    private val fragmentManager: FragmentManager,
    @IdRes private val containerId: Int,
    private val stackListener: StackListener
) {
    // Map of graphId to Tags
    private val graphIdToTagMap = SparseArray<String>()

    // FirstFragment GraphId
    private var firstFragmentGraphId = 0

    private val selectedStackTag: String?
        get() = graphIdToTagMap[selectedStackId]

    // Selected stack at any moment
    private var selectedStackId: Int = 0

    // First stack of the list
    private val firstFragmentTag: String?
        get() = graphIdToTagMap[firstFragmentGraphId]

    // Check if the first stack is selected
    private var isOnFirstFragment = selectedStackTag == firstFragmentTag

    // Stores the current NavController instance
    private var selectedNavController: NavController? = null
        set(value) {
            field = value
            stackListener.onControllerChange(value)
        }

    init {

        // Validate the stack at every back stack change
        fragmentManager.addOnBackStackChangedListener {
            val backStackEntryTag = fragmentManager.lastBackStackEntryTag()
            if (selectedStackTag != backStackEntryTag) {
                val index = graphIdToTagMap.indexOfValue(backStackEntryTag)
                if (index < 0) {
                    selectedStackId = firstFragmentGraphId
                    stackListener.onStackChange(firstFragmentGraphId)
                } else {
                    val graphId = graphIdToTagMap.keyAt(index)
                    selectedStackId = graphId
                    stackListener.onStackChange(graphId)
                }
            }

            // Reset the graph if the currentDestination is not valid (happens when the back
            // stack is popped after using the back button).
            selectedNavController?.let { controller ->
                if (controller.currentDestination == null) {
                    controller.navigate(controller.graph.id)
                }
            }
        }
    }

    private fun FragmentManager.lastBackStackEntryTag(): String? {
        return if (backStackEntryCount == 0) null
        else getBackStackEntryAt(backStackEntryCount - 1).name
    }

    /**
     * Creates multiple NavHostFragment to hold stacks
     * also, Sets firstFragmentTag
     * also, attaches and detaches necessary navHostFragment to container
     */
    fun setUpStacks(selectedStackId: Int) {
        // First create a NavHostFragment for each NavGraph ID
        navGraphIds.forEachIndexed { index, navGraphId ->
            val fragmentTag = getFragmentTag(index)

            // Find or create the Navigation host fragment
            val navHostFragment = obtainNavHostFragment(
                fragmentManager,
                fragmentTag,
                navGraphId,
                containerId
            )

            // Obtain its id
            val graphId = navHostFragment.navController.graph.id

            if (index == 0) {
                firstFragmentGraphId = graphId
            }

            // Save to the map
            graphIdToTagMap[graphId] = fragmentTag

            // Attach or detach nav host fragment depending on whether it's the selected item.
            if (selectedStackId == graphId) {
                selectedNavController = navHostFragment.navController
                attachNavHostFragment(fragmentManager, navHostFragment, index == 0)
            } else {
                detachNavHostFragment(fragmentManager, navHostFragment)
            }
        }

        // graphIdTagMap is created
        this.selectedStackId = selectedStackId
    }

    /**
     * Attaches provided navHost to container allowing first NavHost to be in
     * backstack always
     */
    fun selectStack(newSelectedId: Int): Boolean {
        // Don't do anything if the state is state has already been saved.
        return if (fragmentManager.isStateSaved) {
            false
        } else {
            // Find the tag for requested stack
            val newlySelectedItemTag = graphIdToTagMap[newSelectedId]

            // different stack is selected than the current one
            if (selectedStackTag != newlySelectedItemTag) {

                val selectedFragment = fragmentManager.findFragmentByTag(newlySelectedItemTag)
                        as NavHostFragment

                // Commit a transaction that adds to the current backstack,
                // creating the fixed started destination.
                fragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.nav_default_enter_anim,
                        R.anim.nav_default_exit_anim,
                        R.anim.nav_default_pop_enter_anim,
                        R.anim.nav_default_pop_exit_anim
                    )
                    .attach(selectedFragment)
                    .setPrimaryNavigationFragment(selectedFragment)
                    .apply {
                        // add previous entry stack entry to backstack
                        fragmentManager.createBackStack(this)
                    }
                    .addToBackStack(newlySelectedItemTag)
                    .setReorderingAllowed(true)
                    .commit()

                selectedStackId = newSelectedId
                isOnFirstFragment = selectedStackTag == firstFragmentTag
                selectedNavController = selectedFragment.navController
                true
            } else {
                true
            }
        }
    }

    private fun FragmentManager.createBackStack(
        transaction: FragmentTransaction
    ) {
        if (backStackEntryCount > 3) {
            val entryName = getBackStackEntryAt(backStackEntryCount - 3).name
            fragmentManager.popBackStack(
                entryName,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }

        if (backStackEntryCount > 0) {
            val lastEntryName = getBackStackEntryAt(backStackEntryCount - 1).name
            transaction.detach(findFragmentByTag(lastEntryName)!!)
        } else {
            transaction.detach(findFragmentByTag(firstFragmentTag)!!)
        }
    }

    /**
     * Navigate to the deeplink provided
     * Triggers the stack change: to be used by components to select correct items
     */
    fun navigateToDeeplink(deeplink: Uri) {
        navGraphIds.forEachIndexed { index, navGraphId ->
            val fragmentTag = getFragmentTag(index)

            // Find or create the Navigation host fragment
            val navHostFragment = obtainNavHostFragment(
                fragmentManager,
                fragmentTag,
                navGraphId,
                containerId
            )

            // Handle deeplink
            val canHandleDeeplink = navHostFragment.navController.graph.hasDeepLink(deeplink)

            if (canHandleDeeplink) {
                if (selectedStackId != navHostFragment.navController.graph.id) {
                    stackListener.onStackChange(navHostFragment.navController.graph.id)
                }
                navHostFragment.navController.navigate(deeplink)
            }
        }
    }

    fun currentController(): NavController? = selectedNavController

    /**
     * Resets navHost to start destination of the graph
     */
    fun resetStack(stackId: Int) {
        val newlySelectedItemTag = graphIdToTagMap[stackId]
        val selectedFragment =
            fragmentManager.findFragmentByTag(newlySelectedItemTag) as NavHostFragment
        val navController = selectedFragment.navController
        // Pop the back stack to the start destination of the current navController graph
        navController.popBackStack(navController.graph.startDestination, false)
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

    // Tag to be applied on NavHostFragment for each graph
    private fun getFragmentTag(index: Int) = "multipleStackFragment#$index"

    interface StackListener {
        fun onControllerChange(navController: NavController?) {}
        fun onStackChange(stackId: Int) {}
    }
}

