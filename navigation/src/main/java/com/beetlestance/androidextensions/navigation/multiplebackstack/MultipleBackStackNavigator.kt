package com.beetlestance.androidextensions.navigation.multiplebackstack

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.View
import androidx.activity.addCallback
import androidx.annotation.IdRes
import androidx.annotation.IntRange
import androidx.core.util.forEach
import androidx.core.util.set
import androidx.fragment.app.*
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.beetlestance.androidextensions.navigation.R
import com.beetlestance.androidextensions.navigation.deprecated.extensions.mNavGraphIds
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @param fragmentManager to be used to attach all NavHostFragments that are created by this class
 *
 * Responsibility::
 * To manage multiple [NavHostFragment] on the provided [FragmentManager]
 *
 * Expectations::
 * 1. Create [NavHostFragment] for each graph
 * 2. Choose any one of the [NavHostFragment] at runtime
 * 3. Create a backstack for user navigation on each [NavHostFragment]
 *
 * Ideas:
 * 1. Navigation can be based on index, as it will be easy to manage, but it breaks the default
 * Navigation pattern. Graph ids are provided which will also be attached to any components
 */
class MultipleBackStackNavigator(
    private val navGraphIds: List<Int>,
    private val fragmentManager: FragmentManager,
    @IdRes private val containerId: Int,
    private val primarySelectedIndex: Int,
    @IntRange(from = 1, to = Long.MAX_VALUE) private val backstackHistoryCount: Int = 1
) {
    // Map of graphId to Tags
    // Stores NavHostFragment tag with graphIds
    private val graphIdToTagMap = SparseArray<String>()

    // Primary fragment graphId
    private var primaryNavHostTag: String = ""

    // Currently selected NavHostIndex
    private var selectedNavHostTag: String = ""

    private val canModifyHistory: AtomicBoolean = AtomicBoolean(true)
    private val backStackHistory: MutableList<String> = mutableListOf()

    private var stackListener: StackListener? = null

    // Stores the current NavController instance
    private var selectedNavController: NavController? = null
        set(value) {
            field = value
            stackListener?.onControllerChange(value)
        }


    init {
        fragmentManager.addOnBackStackChangedListener {
            Log.w("BackStackChangeListener", "BackStack is managed by MultipleBackStackNavigator")
        }
    }

    private fun removeBackstack() {
        // remove last history
        val removedHistory = backStackHistory.removeAt(backStackHistory.lastIndex)
        // if the removed history and next history is same remove again
        // this can happen when backstack history count is reached and we remove old history
        if (removedHistory == backStackHistory.lastOrNull()) {
            backStackHistory.removeAt(backStackHistory.lastIndex)
        }
    }

    private fun addBackstack(name: String) {
        if (backStackHistory.size > backstackHistoryCount) {
            backStackHistory.removeAt(1)
        }
        // should not add same history twice in a row
        if (backStackHistory.lastOrNull() != name) backStackHistory.add(name)
    }

    @Synchronized
    fun popBackstack(): Boolean {
        removeBackstack()
        return if (backStackHistory.isNotEmpty()) {
            val newHost =
                fragmentManager.findFragmentByTag(backStackHistory.last()) as NavHostFragment
            stackListener?.onStackChange(newHost.navController.graph.id)
            true
        } else {
            false
        }
    }

    @Synchronized
    fun pushToBackstack(name: String) {
        addBackstack(name)
    }

    fun setFragmentStackListener(stackListener: StackListener) {
        this.stackListener = stackListener
    }


    /**
     * Responsibility::
     * To setUp fragmentManager with multiple [NavHostFragment], one for each graph
     * Attaches the required NavHostFragment to container
     */
    fun setUpNavHostFragments(selectedId: Int) {
        navGraphIds.forEachIndexed { index, navGraphId ->
            // get unique fragment tag for each NavHostFragment
            val fragmentTag = getFragmentTag(index)

            // Find or create the Navigation host fragment
            // Find will be in case of restore fragment state
            val navHostFragment = obtainNavHostFragment(
                fragmentManager,
                fragmentTag,
                navGraphId,
                containerId
            )

            // Obtain graph id, this will be same as the graphId you pass to any component
            // example:
            // menu item in case for BottomNavigationView
            // android:id="@+id/your_nav_graph_id"
            val graphId = navHostFragment.navController.graph.id

            // Save to the map
            graphIdToTagMap[graphId] = fragmentTag

            // Attach or detach nav host fragment depending on whether it's the selected one.
            // When app starts selected history and primaryId is same
            // this should be done with primaryIndex
            if (index == primarySelectedIndex) {
                primaryNavHostTag = fragmentTag
                // push primary fragment to backstack
                pushToBackstack(fragmentTag)
                selectedNavHostTag = fragmentTag
                selectedNavController = navHostFragment.navController
                attachNavHostFragment(
                    fragmentManager,
                    navHostFragment,
                    primarySelectedIndex == index
                )
            } else {
                detachNavHostFragment(fragmentManager, navHostFragment)
            }
        }

        // move to selectedId here
        selectNavHostFragment(selectedId)
    }

    fun selectNavHostFragment(selectedId: Int): Boolean {
        // Don't do anything if the state is state has already been saved.
        if (fragmentManager.isStateSaved) return false

        // Find the tag for selected graphs NavHost
        val navHostFragmentTag = graphIdToTagMap[selectedId]

        // Find the fragment associated with the tag
        val fragment = fragmentManager.findFragmentByTag(navHostFragmentTag) as NavHostFragment


        // pop backstack if any
        fragmentManager.popBackStack(primaryNavHostTag, FragmentManager.POP_BACK_STACK_INCLUSIVE)

        pushToBackstack(navHostFragmentTag)

        fragmentManager.commitNow {
            setCustomAnimations(
                R.anim.nav_default_enter_anim,
                R.anim.nav_default_exit_anim,
                R.anim.nav_default_pop_enter_anim,
                R.anim.nav_default_pop_exit_anim
            )

            // detach previous fragment first
            detach(fragmentManager.findFragmentByTag(selectedNavHostTag)!!)

            // reattaches the fragment which was detached
            attach(fragment)
            setPrimaryNavigationFragment(fragment)

            // this allows this transaction to be written in any order and apply optimizations later
            setReorderingAllowed(true)
        }

        val fragmentNavController = fragment.navController
        selectedNavController = fragmentNavController
        selectedNavHostTag = navHostFragmentTag
        // if all the stack is popped
        if (fragmentNavController.currentDestination == null) {
            fragmentNavController.navigate(selectedId)
        }
        return true
    }

    private fun validateBackstack() {
        if (fragmentManager.isStateSaved) return
        if (fragmentManager.backStackEntryCount > 3) {
            val popToBackstackEntryName =
                fragmentManager.getBackStackEntryAt(fragmentManager.backStackEntryCount - 3).name
            fragmentManager.popBackStack(
                popToBackstackEntryName,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }
    }

    private fun FragmentTransaction.detachLastFragment() {
        val lastEntryName = fragmentManager.lastBackStackOrPrimaryEntryTag()
        detach(fragmentManager.findFragmentByTag(lastEntryName)!!)
    }

    private fun FragmentManager.lastBackStackOrPrimaryEntryTag(): String {
        return if (backStackEntryCount > 0) getBackStackEntryAt(backStackEntryCount - 1).name!!
        else primaryNavHostTag
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
                stackListener?.onStackChange(navHostFragment.navController.graph.id)
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

    /**
     * Responsibility::
     * Creates a [NavHostFragment] and adds it to activity lifecycle
     */
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

        fragmentManager.commitNow {
            // Add a fragment to the activity state.
            add(containerId, navHostFragment, fragmentTag)
        }

        return navHostFragment
    }

    private fun detachNavHostFragment(
        fragmentManager: FragmentManager,
        navHostFragment: NavHostFragment
    ) {
        // Commits this transaction synchronously. Any added fragments will be
        // initialized and brought completely to the lifecycle state of their host
        // and any removed fragments will be torn down accordingly before this
        // call returns
        fragmentManager.commitNow {
            // Detach the given fragment from the UI.  This is the same state as
            // when it is put on the back stack: the fragment is removed from
            // the UI, however its state is still being actively managed by the
            // fragment manager
            detach(navHostFragment)
        }
    }

    private fun attachNavHostFragment(
        fragmentManager: FragmentManager,
        navHostFragment: NavHostFragment,
        isPrimaryNavFragment: Boolean
    ) {
        fragmentManager.commitNow {
            // Re-attach a fragment after it had previously been detached from
            // the UI with using {@link detachNavHostFragment}.  This
            // causes its view hierarchy to be re-created, attached to the UI,
            // and displayed.
            attach(navHostFragment)

            if (isPrimaryNavFragment) {
                setPrimaryNavigationFragment(navHostFragment)
            }
        }
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
    private fun getFragmentTag(index: Int) = "multipleNavHostFragment#$index"

    interface StackListener {
        fun onControllerChange(navController: NavController?) {}
        fun onStackChange(graphId: Int) {}
    }
}

