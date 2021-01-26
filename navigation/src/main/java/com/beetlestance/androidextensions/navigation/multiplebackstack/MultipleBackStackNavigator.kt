package com.beetlestance.androidextensions.navigation.multiplebackstack

import android.net.Uri
import android.util.SparseArray
import androidx.annotation.IdRes
import androidx.core.util.forEach
import androidx.core.util.set
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.beetlestance.androidextensions.navigation.R

class MultipleBackStackNavigator(
    private val navGraphIds: List<Int>,
    private val selectedStackId: Int,
    private val fragmentManager: FragmentManager,
    @IdRes private val containerId: Int
) {
    // Map of tags
    private val graphIdToTagMap = SparseArray<String>()
    private var firstFragmentGraphId = 0
    private var selectedItemTag: String? = null
    private var firstFragmentTag: String? = null
    private var isOnFirstFragment = selectedItemTag == firstFragmentTag

    private var onControllerChange: (NavController?) -> Unit = {}
    private var selectedStackChangeListener: (Int) -> Unit = {}

    // Stores the current NavController instance
    private var selectedNavController: NavController? = null
        set(value) {
            field = value
            onControllerChange(value)
        }

    init {
        setUpStacks()
        fragmentManager.addOnBackStackChangedListener {
            if (!isOnFirstFragment && !fragmentManager.isOnBackStack(firstFragmentTag ?: "")) {
                // set selected item
                selectedStackChangeListener(firstFragmentGraphId)
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

    private fun setUpStacks() {
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
        selectedItemTag = graphIdToTagMap[selectedStackId]
        firstFragmentTag = graphIdToTagMap[firstFragmentGraphId]
    }

    fun selectStack(newSelectedId: Int): Boolean {
        // Don't do anything if the state is state has already been saved.
        return if (fragmentManager.isStateSaved) {
            false
        } else {
            // Find the tag for requested stack
            val newlySelectedItemTag = graphIdToTagMap[newSelectedId]

            // different stack is selected than the requested one
            if (selectedItemTag != newlySelectedItemTag) {
                // Only first fragment will remain is backstack
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
                            R.anim.nav_default_enter_anim,
                            R.anim.nav_default_exit_anim,
                            R.anim.nav_default_pop_enter_anim,
                            R.anim.nav_default_pop_exit_anim
                        )
                        .attach(selectedFragment)
                        .setPrimaryNavigationFragment(selectedFragment)
                        .apply {
                            // Detach all other Fragments
                            graphIdToTagMap.forEach { _, fragmentTagIter ->
                                if (fragmentTagIter != newlySelectedItemTag) {
                                    detach(fragmentManager.findFragmentByTag(firstFragmentTag)!!)
                                }
                            }
                        }
                        .addToBackStack(firstFragmentTag)
                        .setReorderingAllowed(true)
                        .commit()
                }
                selectedItemTag = newlySelectedItemTag
                isOnFirstFragment = selectedItemTag == firstFragmentTag
                selectedNavController = selectedFragment.navController
                true
            } else {
                false
            }
        }
    }

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
                    selectedStackChangeListener(navHostFragment.navController.graph.id)
                }
                navHostFragment.navController.navigate(deeplink)
            }
        }
    }

    fun onStackChange(stackChange: (Int) -> Unit) {
        selectedStackChangeListener = stackChange
    }

    fun currentController(): NavController? = selectedNavController

    fun resetStack(selectedId: Int) {
        val newlySelectedItemTag = graphIdToTagMap[selectedId]
        val selectedFragment = fragmentManager.findFragmentByTag(newlySelectedItemTag)
                as NavHostFragment
        val navController = selectedFragment.navController
        // Pop the back stack to the start destination of the current navController graph
        navController.popBackStack(
            navController.graph.startDestination, false
        )
    }

    fun onControllerChange(controllerChange: (NavController?) -> Unit) {
        onControllerChange = controllerChange
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

    private fun getFragmentTag(index: Int) = "multipleStackFragment#$index"
}