package com.beetlestance.androidextensions.navigation.multiplebackstack

import android.net.Uri
import android.os.Parcelable
import android.util.Log
import android.util.SparseArray
import androidx.annotation.IdRes
import androidx.core.util.set
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commitNow
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.beetlestance.androidextensions.navigation.R
import java.util.*

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
class MultipleBackStackManager(
    private val navGraphIds: List<Int>,
    private val fragmentManager: FragmentManager,
    @IdRes private val containerId: Int,
    private val primarySelectedIndex: Int,
    private val historyEnabled: Boolean = true
) {
    // Map of graphId to Tags
    // Stores NavHostFragment tag with graphIds
    private val graphIdToTagMap = SparseArray<String>()

    // Currently selected NavHost tag
    private var selectedGraphId: Int = 0

    private val multipleBackStackHistory: MultipleBackStackHistory = MultipleBackStackHistory()

    private var stackListener: StackListener? = null

    // Stores the current NavController instance
    private var selectedNavController: NavController? = null
        set(value) {
            field = value
            stackListener?.onControllerChange(value)
        }


    fun setFragmentStackListener(stackListener: StackListener) {
        this.stackListener = stackListener
    }

    init {
        createNavHostFragments()

        // this calls means nothing, as there is no backstack being created
        fragmentManager.addOnBackStackChangedListener {
            Log.w("BackStackChangeListener", "BackStack is managed by MultipleBackStackNavigator")
        }
    }

    /**
     * Responsibility:
     * To setUp fragmentManager with multiple [NavHostFragment], one for each graph
     * Attaches the required NavHostFragment to container
     */
    private fun createNavHostFragments() {
        navGraphIds.forEachIndexed { index, navGraphId ->
            // get unique fragment tag for each NavHostFragment
            val fragmentTag = getFragmentTag(index)

            // Find or create the Navigation host fragment
            // Find will be in case of restore fragment state
            // Adds all the NavHostFragment to active fragment transactions
            // These will be actively managed by FragmentManager
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
            if (index == primarySelectedIndex) {
                // push primary fragment to backstack
                pushToBackstack(graphId)
                selectedGraphId = graphId
                selectedNavController = navHostFragment.navController

                // attach the primary fragment to container
                attachNavHostFragment(
                    fragmentManager,
                    navHostFragment,
                    primarySelectedIndex == index
                )
            } else {
                detachNavHostFragment(fragmentManager, navHostFragment)
            }
        }
    }

    fun selectNavHostFragment(selectedId: Int): Boolean {
        return selectNavHostFragment(selectedId, true)
    }

    /**
     * Responsibility:
     * To select the particular [NavHostFragment] base on graph id
     */
    private fun selectNavHostFragment(selectedId: Int, addToBackStack: Boolean): Boolean {
        // Don't do anything if the state is state has already been saved.
        if (fragmentManager.isStateSaved) return false

        // Find the tag for selected graphs NavHost
        val navHostFragmentTag = graphIdToTagMap[selectedId]

        // currently selected NavHostTag
        val selectedNavHostTag = graphIdToTagMap[selectedGraphId]

        // Find the fragment associated with the tag
        val fragment = fragmentManager.findFragmentByTag(navHostFragmentTag) as NavHostFragment

        // if selected and incoming fragment is same do not do anything
        if (selectedGraphId == selectedId) return true

        // we are not managing any backstack from FragmentManager itself
        // pop backstack if any
        // fragmentManager.popBackStack(
        // primaryNavHostTag,
        // FragmentManager.POP_BACK_STACK_INCLUSIVE
        // )

        fragmentManager.commitNow {
            setCustomAnimations(
                R.anim.nav_default_enter_anim,
                R.anim.nav_default_exit_anim,
                R.anim.nav_default_pop_enter_anim,
                R.anim.nav_default_pop_exit_anim
            )

            // detach previous fragment
            detach(fragmentManager.findFragmentByTag(selectedNavHostTag)!!)

            // reattaches the fragment which was detached
            attach(fragment)

            // create this fragment as primary fragment
            setPrimaryNavigationFragment(fragment)

            // this allows this transaction to be written in any order and apply optimizations later
            setReorderingAllowed(true)
        }

        // add this fragment to backstack
        if (addToBackStack) pushToBackstack(selectedId)

        val fragmentNavController = fragment.navController
        selectedNavController = fragmentNavController
        selectedGraphId = selectedId
        // if all the stack is popped
        if (fragmentNavController.currentDestination == null) {
            fragmentNavController.navigate(selectedId)
        }

        stackListener?.onStackChange(selectedId)
        return true
    }

    /**
     * Navigate to the deeplink provided
     * Triggers the stack change: to be used by components to select correct items
     */
    fun navigateToDeeplink(deeplink: Uri, navOptions: NavOptions) {
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
                navHostFragment.navController.navigate(deeplink, navOptions)
            }
        }
    }

    @Synchronized
    fun popBackstack(): Boolean {
        val backStackChanged = multipleBackStackHistory.pop()
        // select the correct history
        if (backStackChanged) {
            selectNavHostFragment(multipleBackStackHistory.current(), false)
        }
        return backStackChanged
    }

    @Synchronized
    fun pushToBackstack(entry: Int) {
        // do not push history if not enabled
        if (historyEnabled.not()) return
        multipleBackStackHistory.push(entry)
    }

    // clears all the history
    fun clearBackstackHistory() {
        multipleBackStackHistory.clear()
    }

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


/*
* class to maintain backstack history
* primary history must always be in backstack
*/
data class MultipleBackStackHistory(
    private val backStackHistory: ArrayList<Int> = arrayListOf()
) {

    val size: Int get() = backStackHistory.size

    val isEmpty: Boolean get() = backStackHistory.isEmpty()

    // do not allow history count if not enabled
    // remove duplicate then add history
    // remove oldest history in case stack size is full
    fun push(entry: Int) {
        backStackHistory.run {
            // this prevents primary fragment to be removed from history
            val indexIfExists = indexOf(entry)
            if (indexIfExists > 0) {
                remove(entry)
            }
            add(entry)
        }
    }

    fun pop(): Boolean {
        // if only primary fragment is there, nothing to pop
        if (size <= 1) return false

        // remove last history
        val removedHistory = backStackHistory.removeLast()

        // check if duplicate is present
        val isDuplicateEntryFound = removedHistory == backStackHistory.lastOrNull()

        // if duplicate entry is there pop() history again
        return if (isDuplicateEntryFound) pop() else true
    }

    fun pop(exit: Int): Boolean {
        return backStackHistory.remove(exit)
    }

    // provides current backstack history always
    fun current(): Int = backStackHistory.last()

    // always keep primary history in backstack
    fun clear() {
        backStackHistory.clear()
    }
}