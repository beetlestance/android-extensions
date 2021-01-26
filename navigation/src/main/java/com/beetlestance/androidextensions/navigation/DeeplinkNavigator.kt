package com.beetlestance.androidextensions.navigation

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
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

    /**
     * Should be used whenever you are navigating with deeplinks
     * @param deepLink [Uri] for navigating
     */
    fun navigate(deepLink: Uri) {
        Navigator.getInstance().postForNavigation(NavigateOnceDeeplinkRequest(deepLink), null, true)
    }
}

/**
 * This is the primary class containing all the logic for handling deeplink navigation
 * This class can only have one instance.
 */
internal class Navigator private constructor() {

    /**
     * Add a destination changed listener on activity controller
     */
    internal var onDestinationChangeListener: NavController.OnDestinationChangedListener? = null

    // This is the fragment id of the fragment which contains the BottomNavigationView
    // In case of activity this will remain null.
    internal var parentNavHostContainerId: Int? = null

    // List of fragment Ids which will not be poped from back stack when deeplink is clicked
    var fragmentBackStackBehavior: Map<Int, DeeplinkNavigationPolicy> = mapOf()

    // Flag specifies we should clear the back stack or not based on retainFragmentIds and current destination
    internal var resetDestinationToPrimaryFragment: Boolean = false

    // LiveData that will be observed for any upcoming deeplink request
    private val navigatorDeeplink: MutableLiveData<NavigateOnceDeeplinkRequest> = MutableLiveData()
    internal val navigateRequest = navigatorDeeplink.toSingleEvent()


    private val clearBackStack: MutableLiveData<Boolean> = MutableLiveData(false)
    internal val popToPrimaryFragment = clearBackStack.toSingleEvent()

    internal var safeNavigationEnabled: Boolean = true

    /**
     * Sets primary navigation id
     */
    fun setPrimaryNavigationId(parentNavHostContainerId: Int) {
        this.parentNavHostContainerId = parentNavHostContainerId
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
        //  checks if parent can navigate to the destination
        when (canNavControllerHandleDeeplink(navController, request)) {
            true -> {
                navController?.navigateOnce(request)
            }
            else -> {
                bottomNavigationView.navigateDeeplink(
                    request = request,
                    fragmentManager = fragmentManager
                )
            }
        }
    }

    private fun canNavControllerHandleDeeplink(
        navController: NavController?,
        request: NavigateOnceDeeplinkRequest
    ) = navController?.graph?.hasDeepLink(request.deeplink) ?: false

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
        handleIntent: (intent: Intent?) -> Unit = {}
    ) {
        val deeplinkRequest = when {
            intent?.data != null -> NavigateOnceDeeplinkRequest(deeplink = intent.data ?: return)
            else -> null
        }

        deeplinkRequest?.let {
            postForNavigation(it, navController, false)
        }

        // run all the requirements specified before setting data to null
        handleIntent(intent)
        intent?.data = null
    }


    /**
     * This function is used everytime we want to publish a value to navigation livedata
     */
    fun postForNavigation(
        request: NavigateOnceDeeplinkRequest,
        navController: NavController?,
        ignoreBackStackNavigationPolicy: Boolean
    ) {
        if (isBottomNavigationAttachedToActivity()) {
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

    // Flag to check if bottom navigation is attached to an activity or a fragment.
    private fun isBottomNavigationAttachedToActivity(): Boolean = parentNavHostContainerId == null

    companion object {
        private var deeplinkNavigatorInstance: Navigator? = null

        // Provide single instance for [Navigator]
        @SuppressLint("SyntheticAccessor")
        fun getInstance(): Navigator {
            return deeplinkNavigatorInstance ?: Navigator().also { navigator ->
                deeplinkNavigatorInstance = navigator
            }
        }
    }
}

/**
 * Navigation policy for deeplinks
 */
enum class DeeplinkNavigationPolicy {
    // Whenever primary fragment comes to resume state, the navigation will take place
    NAVIGATE_ON_EXIT,

    // The current flow will be retained and the deeplink will be discarded
    RETAIN_AND_DISCARD,

    // The current flow will be exited to primary fragment and the navigation will happen
    EXIT_AND_NAVIGATE
}
