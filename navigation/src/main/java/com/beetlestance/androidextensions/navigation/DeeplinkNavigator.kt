package com.beetlestance.androidextensions.navigation

import android.content.Intent
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import com.beetlestance.androidextensions.navigation.DeeplinkNavigationPolicy.RETAIN_AND_DISCARD
import com.beetlestance.androidextensions.navigation.data.NavigateOnceDeeplinkRequest
import com.beetlestance.androidextensions.navigation.extensions.navigateDeeplink
import com.beetlestance.androidextensions.navigation.extensions.navigateOnce
import com.beetlestance.androidextensions.navigation.util.toSingleEvent
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * This object contains all the exposed methods from Navigator class
 */
object DeeplinkNavigator {
    /**
     * Should be used whenever you are navigating with deeplinks
     * @param request [NavigateOnceDeeplinkRequest] for navigating
     */
    fun navigate(request: NavigateOnceDeeplinkRequest) {
        Navigator.getInstance().postForNavigation(request, null, true)
    }
}

/**
 * This is the primary class containing all the logic for handling deeplink navigation
 * This class can only have one instance.
 */
internal class Navigator private constructor() {

    // Flag to check if bottom navigation is attached to an activity or a fragment.
    internal var isBottomNavigationAttachedToActivity: Boolean = false

    // This is the fragment id of the fragment which contains the BottomNavigationView
    // In case of activity this will remain null.
    private var primaryFragmentId: Int? = null

    // This is the fragment id of the fragment which contains the BottomNavigationView
    // In case of activity this will remain null.
    internal var parentNavHostContainerId: Int? = null

    // List of fragment Ids which will not be poped from back stack when deeplink is clicked
    var fragmentBackStackBehavior: Map<Int, DeeplinkNavigationPolicy> = mapOf()

    // Flag specifies we should clear the back stack or not based on retainFragmentIds and current destination
    private var resetDestinationToPrimaryFragment: Boolean = false

    // This checks if setGraph from activity has already handled the deeplink or not
    // In case of activity this check is not necessary
    //
    // The value is set to false whenever get is called
    private var hasSetGraphHandledDeeplink: Boolean = false
        get() {
            val mHasSetGraphHandledDeeplink = field
            return mHasSetGraphHandledDeeplink.also {
                hasSetGraphHandledDeeplink = false
            }
        }

    // LiveData that will be observed for any upcoming deeplink request
    private val navigatorDeeplink: MutableLiveData<NavigateOnceDeeplinkRequest> = MutableLiveData()
    internal val navigateRequest = navigatorDeeplink.toSingleEvent()


    private val clearBackStack: MutableLiveData<Boolean> = MutableLiveData(false)
    internal val popToPrimaryFragment = clearBackStack.toSingleEvent()

    /**
     * Sets primary navigation id
     */
    fun setPrimaryNavigationId(primaryFragmentId: Int, parentNavHostContainerId: Int) {
        this.parentNavHostContainerId = parentNavHostContainerId
        this.primaryFragmentId = primaryFragmentId
    }

    /**
     * Contains the logic for navigating to a specific destination
     * This will be called everytime a deeplink navigation happens
     */
    internal fun handleDeeplink(
        navController: NavController?,
        bottomNavigationView: BottomNavigationView,
        fragmentManager: FragmentManager,
        request: NavigateOnceDeeplinkRequest
    ) {
        // If the BottomNavigationView is attached to activity, all the deeplinks will
        // be handled by BottomNavigationView itself
        if (isBottomNavigationAttachedToActivity) {
            bottomNavigationView.navigateDeeplink(
                request = request,
                fragmentManager = fragmentManager
            )
        } else {
            //  checks if parent can navigate to the destination
            val isParentWorthyEnough = navController?.graph?.hasDeepLink(request.deeplink)
            when {
                isParentWorthyEnough == true && hasSetGraphHandledDeeplink.not() -> {
                    navController.navigateOnce(request)
                }
                else -> {
                    bottomNavigationView.navigateDeeplink(
                        request = request,
                        fragmentManager = fragmentManager
                    )
                }
            }
        }
    }

    /**
     * Check the Intent for deeplink or any request provided by user
     * This will be called from activities onCreate as well as onNewIntent.
     *
     * If the app is launched with an intent and activity graph can handle the destination
     * that means we do not want to handle the deeplink.
     *
     * Please be careful that this functions sets the intent to null
     */
    internal fun handleDeeplinkIntent(
        intent: Intent?,
        navController: NavController?,
        intentUpdated: Boolean,
        validateDeeplinkRequest: NavigateOnceDeeplinkRequest? = null,
        handleIntent: (intent: Intent?) -> Unit = {}
    ) {
        val deeplinkRequest = when {
            validateDeeplinkRequest != null -> validateDeeplinkRequest
            intent?.data != null -> NavigateOnceDeeplinkRequest(deeplink = intent.data!!)
            else -> null
        }

        deeplinkRequest?.let {
            // If the deeplink was handled by activity graph do not post it for navigation
            if (isBottomNavigationAttachedToActivity) {
                postForNavigation(it, null, false)
            } else if (navController!!.graph.hasDeepLink(it.deeplink) && intentUpdated.not()) {
                hasSetGraphHandledDeeplink = intentUpdated.not()
            } else {
                postForNavigation(it, navController, false)
            }

        }

        // run all the requirements specified before setting data to null
        handleIntent(intent)
        intent?.data = null
    }

    /**
     * Add a destination changed listener on activity controller
     */
    internal val onDestinationChangeListener =
        NavController.OnDestinationChangedListener { _, destination, _ ->
            // check if back stack should be cleared on not
            resetDestinationToPrimaryFragment = destination.id != primaryFragmentId &&
                    fragmentBackStackBehavior[destination.id] == DeeplinkNavigationPolicy.EXIT_AND_NAVIGATE
        }


    /**
     * This function is used everytime we want to publish a value to navigation livedata
     */
    fun postForNavigation(
        request: NavigateOnceDeeplinkRequest,
        navController: NavController?,
        ignoreBackStackNavigationPolicy: Boolean
    ) {
        if (isBottomNavigationAttachedToActivity) {
            navigatorDeeplink.postValue(request)
        } else {
            clearBackStack(ignoreBackStackNavigationPolicy)
            val currentDestination = navController?.currentDestination?.id
            if (currentDestination == null || fragmentBackStackBehavior[currentDestination] != RETAIN_AND_DISCARD) {
                navigatorDeeplink.postValue(request)
            }
        }
    }

    /**
     * Checks if backstack should be cleared or not
     */
    private fun clearBackStack(ignoreBackStackNavigationPolicy: Boolean) {
        clearBackStack.postValue(ignoreBackStackNavigationPolicy || resetDestinationToPrimaryFragment)
    }

    companion object {
        private var deeplinkNavigatorInstance: Navigator? = null

        // Provide single instance for [Navigator]
        fun getInstance(): Navigator {
            return deeplinkNavigatorInstance ?: Navigator().also { navigator ->
                deeplinkNavigatorInstance = navigator
            }
        }
    }
}

enum class DeeplinkNavigationPolicy {
    NAVIGATE_ON_EXIT,
    RETAIN_AND_DISCARD,
    EXIT_AND_NAVIGATE
}
