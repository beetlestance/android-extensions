package com.beetlestance.androidextensions.navigation

import android.content.Intent
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
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
        Navigator.getInstance().postForNavigation(request, true)
    }
}

/**
 * This is the primary class containing all the logic for handling deeplink navigation
 * This class can only have one instance.
 */
internal class Navigator private constructor() {
    // We do no want to attach multiple destination change listener
    private var isDestinationChangedListenerAttached: Boolean = false

    // Flag to check if bottom navigation is attached to an activity or a fragment.
    internal var isBottomNavigationAttachedToActivity: Boolean = false

    // This is navController for activity
    private var activityNavController: NavController? = null

    // This is the fragment id of the fragment which contains the BottomNavigationView
    // In case of activity this will remain null.
    private var primaryFragmentId: Int? = null

    // List of fragment Ids which will not be poped from back stack when deeplink is clicked
    var retainFragmentIds: List<Int> = emptyList()

    // Flag species whether we should navigate to the destination once primary fragment is visible
    // This is the case when use was on the one of retained fragment list destination.
    var retainDeeplink: Boolean = true

    // Flag specifies we should clear the back stack or not based on retainFragmentIds and current destination
    private var shouldClearBackStack: Boolean = false

    // Specifies if navigation from deeplink should happen or not
    private var shouldNavigateForDeeplink: Boolean = true

    // Livedata that will be observed for any upcoming deeplink request
    private val navigatorDeeplink: MutableLiveData<NavigateOnceDeeplinkRequest> = MutableLiveData()
    internal val navigateRequest = navigatorDeeplink.toSingleEvent()

    /**
     * Contains the logic for navigating to a specific destination
     * This will be called everytime a deeplink navigation happens
     */
    internal fun handleDeeplink(
        bottomNavigationView: BottomNavigationView,
        fragmentManager: FragmentManager,
        request: NavigateOnceDeeplinkRequest
    ) {
        // Should not navigate if back stack should not be cleared and
        // deeplink should not be retained
        if (shouldNavigateForDeeplink.not()) {
            return
        }

        // If the BottomNavigationView is attached to activity, all the deeplinks will
        // be handled by BottomNavigationView itself
        if (isBottomNavigationAttachedToActivity) {
            bottomNavigationView.navigateDeeplink(
                request = request,
                fragmentManager = fragmentManager
            )
        } else {
            //  checks if parent can navigate to the destination
            val isParentWorthyEnough = activityNavController?.graph?.hasDeepLink(request.deeplink)
            when {
                isParentWorthyEnough == true -> {
                    activityNavController?.navigateOnce(request)
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
            postForNavigation(it, false)
        }

        // run all the requirements specified before setting data to null
        handleIntent(intent)
        intent?.data = null
    }

    /**
     * Add a destination changed listener on activity controller
     */
    @Synchronized
    internal fun onDestinationChangeListener() {
        // attach destination change listener only once
        if (isDestinationChangedListenerAttached)
            return
        else
            isDestinationChangedListenerAttached = true

        activityNavController?.addOnDestinationChangedListener { _, destination, _ ->
            // check if back stack should be cleared on not
            shouldClearBackStack = destination.id !in retainFragmentIds
                    && destination.id != primaryFragmentId
        }
    }


    /**
     * This function is used everytime we want to publish a value to navigation livedata
     */
    fun postForNavigation(request: NavigateOnceDeeplinkRequest, forced: Boolean) {
        clearBackStack(forced)
        navigatorDeeplink.postValue(request)
    }

    /**
     * Checks if backstack should be cleared or not
     */
    private fun clearBackStack(forced: Boolean) {
        if (primaryFragmentId == null)
            return

        if (shouldClearBackStack || forced) {
            shouldNavigateForDeeplink = true
            activityNavController?.popBackStack(primaryFragmentId!!, false)
        } else {
            shouldNavigateForDeeplink = retainDeeplink
        }

    }

    /**
     * Sets the activityNavController for later use
     */
    fun setActivityNavController(navController: NavController) {
        activityNavController = navController
    }


    /**
     * Sets primary navigation id
     */
    fun setPrimaryNavigationId(primaryFragmentId: Int?) {
        this.primaryFragmentId = primaryFragmentId
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
