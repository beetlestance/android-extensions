package com.beetlestance.androidextensions.navigation.experimental

import android.util.SparseArray
import androidx.core.util.forEach
import androidx.core.util.set
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.beetlestance.androidextensions.navigation.extensions.mNavAnimations

class MultiNavHost(
    private val navGraphIds: List<Int>,
    private val containerId: Int,
    private val primaryFragmentIndex: Int,
    private val fragmentManager: FragmentManager,
    private val isSingleTopReplacement: Boolean = false
) {
    // Map of tags
    private val graphIdToTagMap: SparseArray<String> = SparseArray<String>()

    private var selectedNavGraphId: Int = 0

    private var navController: NavController? = null

    private var primaryFragmentId: Int = 0

    fun create(selectedNavId: Int) {
        selectedNavGraphId = selectedNavId

        navGraphIds.forEachIndexed { index, navGraphId ->
            val fragmentTag: String = getFragmentTag(index)

            // Find or create the Navigation host fragment
            val navHostFragment = obtainNavHostFragment(
                fragmentManager = fragmentManager,
                fragmentTag = fragmentTag,
                navGraphId = navGraphId,
                containerId = containerId
            )

            // Obtain its id
            val graphId = navHostFragment.navController.graph.id

            if (index == primaryFragmentIndex) primaryFragmentId = graphId

            // Save to the map
            graphIdToTagMap[graphId] = fragmentTag

            // Attach or detach nav host fragment depending on whether it's the selected item.
            if (selectedNavGraphId == graphId) {
                navController = navHostFragment.navController
                attachNavHostFragment(
                    fragmentManager = fragmentManager,
                    navHostFragment = navHostFragment,
                    isPrimaryNavFragment = isOnPrimaryFragment()
                )
            } else {
                detachNavHostFragment(
                    fragmentManager = fragmentManager,
                    navHostFragment = navHostFragment
                )
            }

        }
    }

    fun selectSiblings(navGraphId: Int): Boolean {
        // Now connect selecting an item with swapping Fragments
        val selectedItemTag = selectedFragmentTag()
        val firstFragmentTag = graphIdToTagMap[primaryFragmentId]
        // Don't do anything if the state is state has already been saved.
        return if (fragmentManager.isStateSaved) {
            false
        } else {
            val newlySelectedItemTag = graphIdToTagMap[navGraphId]
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
                selectedNavGraphId = navGraphId
                navController = selectedFragment.navController
                true
            } else {
                false
            }
        }
    }

    fun reselectSiblings(navGraphId: Int) {
        val newlySelectedItemTag = graphIdToTagMap[navGraphId]
        val selectedFragment = fragmentManager.findFragmentByTag(newlySelectedItemTag)
                as NavHostFragment
        val navController = selectedFragment.navController
        // Pop the back stack to the start destination of the current navController graph
        navController.popBackStack(
            navController.graph.startDestination, false
        )
    }

    private fun isPrimaryFragmentInBackStack(): Boolean =
        fragmentManager.isOnBackStack(graphIdToTagMap[primaryFragmentId])

    private fun isOnPrimaryFragment(): Boolean = selectedNavGraphId == primaryFragmentId

    private fun selectedFragmentTag(): String = graphIdToTagMap[selectedNavGraphId]

    fun observeBackStack(onStackChange: (Int) -> Unit) {
        fragmentManager.addOnBackStackChangedListener {
            if (!isOnPrimaryFragment() && !isPrimaryFragmentInBackStack()) {
                onStackChange(primaryFragmentId)
            }

            // Reset the graph if the currentDestination is not valid (happens when the back
            // stack is popped after using the back button).
            navController?.let { navController ->
                if (navController.currentDestination == null) {
                    navController.navigate(navController.graph.id)
                }
            }
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
                if (isPrimaryNavFragment) setPrimaryNavigationFragment(navHostFragment)
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
            if (getBackStackEntryAt(index).name == backStackName) return true
        }
        return false
    }

    @Suppress("HardCodedStringLiteral")
    private fun getFragmentTag(index: Int) = "multiNavHost#$index"

}