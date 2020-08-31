package com.beetlestance.androidextensions.navigation.extensions

import android.net.Uri
import androidx.navigation.*
import androidx.navigation.fragment.FragmentNavigator
import com.beetlestance.androidextensions.navigation.Navigator
import com.beetlestance.androidextensions.navigation.data.NavigateOnceDeeplinkRequest
import com.beetlestance.androidextensions.navigation.data.NavigateOnceDirectionRequest


/**
 * The extension for [NavController] which uses provide [deeplink] to navigate
 * to a destination with given request parameters.
 */
fun NavController.navigateOnce(deeplink: Uri) {
    navigateOnce(NavigateOnceDeeplinkRequest(deeplink = deeplink))
}

/**
 * The extension for [NavController] which uses provide [NavigateOnceDeeplinkRequest] to navigate
 * to a destination with given request parameters.
 */
fun NavController.navigateOnce(navigationRequest: NavigateOnceDeeplinkRequest) {
    val navigator = Navigator.getInstance()
    // check if controller can open this deeplink
    val containsDeeplink = graph.hasDeepLink(navigationRequest.deeplink)

    if (containsDeeplink.not() && navigator.safeNavigationEnabled) return

    // Check if the destination we want to open is already on the top of the backstack
    val alreadyNavigated: Boolean =
        currentDestination?.hasDeepLink(navigationRequest.deeplink) == true

    when {
        // navigate to destination create new instance if already in backstack
        navigationRequest.allowMultipleInstance -> {
            navigate(navigationRequest.deeplink)
        }

        // if already navigated, do nothing
        alreadyNavigated && navigationRequest.updateArguments.not() -> {
            return
        }

        // Remove Current fragment instance of destination
        // and navigate to update arguments
        alreadyNavigated && navigationRequest.allowMultipleInstance.not() -> {
            currentDestination?.let {
                val navOptions =
                    createNavOptions(
                        it.id,
                        true,
                        null
                    )
                navigate(navigationRequest.deeplink, navOptions)
            }
        }

        // navigate to new destinations
        else -> {
            navigate(navigationRequest.deeplink)
        }
    }
}

/**
 * The extension for [NavController] which uses provide [directions] to navigate
 * to a destination with given request parameters.
 */
fun NavController.navigateOnce(
    directions: NavDirections,
    extras: FragmentNavigator.Extras? = null,
    navOptions: NavOptions? = null
) {
    navigateOnce(
        NavigateOnceDirectionRequest(
            directions = directions,
            navigatorExtras = extras,
            navOptions = navOptions
        )
    )
}

/**
 * The extension for [NavController] which uses provide [NavigateOnceDirectionRequest] to navigate
 * to a destination with given request parameters.
 */
fun NavController.navigateOnce(navigationRequest: NavigateOnceDirectionRequest) {
    val navigator = Navigator.getInstance()
    // Check if navController has action for direction and return if no action found
    val navigationAction =
        currentDestination?.getAction(navigationRequest.directions.actionId)

    if (navigationAction == null && navigator.safeNavigationEnabled) return

    if (navigationAction == null) throw IllegalArgumentException(
        "Navigation Destination " + navigationRequest.directions.actionId
                + " does not belong to graph"
    )

    // check if destination is already on the top of backstack
    val alreadyNavigated = navigationAction.destinationId == currentDestination?.id

    when {
        // navigate to destination create new instance if already in top of backstack
        navigationRequest.allowMultipleInstance -> {
            internalNavigateOnce(navigationRequest)
        }

        // if already navigated and arguments should not be updated, do nothing
        alreadyNavigated && navigationRequest.updateArguments.not() -> {
            return
        }

        // Remove Current fragment instance of destination
        // and navigate to update arguments
        alreadyNavigated && navigationRequest.allowMultipleInstance.not() -> {
            currentDestination?.let {
                val navOptions =
                    it.createNavOptionsFor(navigationAction, navigationRequest.navOptions)
                navigate(
                    navigationRequest.directions.actionId,
                    navigationRequest.directions.arguments,
                    navOptions,
                    navigationRequest.navigatorExtras
                )
            }
        }

        // check extras and navigate to destination
        else -> {
            navigate(navigationRequest.directions)
        }
    }
}

private fun NavController.internalNavigateOnce(navigationRequest: NavigateOnceDirectionRequest) {
    when {
        // navigate to destination with extras
        navigationRequest.navigatorExtras != null -> {
            navigateOnceWithExtras(navigationRequest)
        }

        // navigate to destination
        else -> {
            navigate(navigationRequest.directions)
        }
    }
}

private fun NavController.navigateOnceWithExtras(navigationRequest: NavigateOnceDirectionRequest) {
    navigate(navigationRequest.directions, requireNotNull(navigationRequest.navigatorExtras))
}

private fun NavDestination.createNavOptionsFor(
    action: NavAction,
    navOptions: NavOptions?
): NavOptions {
    // check if action has defaultNavOptions
    val defaultNavOptions: NavOptions? = navOptions ?: action.navOptions
    // popTo destinationId if no defaultNavOptions
    val popUpTo: Int = if (defaultNavOptions?.popUpTo == null || defaultNavOptions.popUpTo == -1) id
    else defaultNavOptions.popUpTo

    val isPopUpToInclusive = defaultNavOptions?.isPopUpToInclusive ?: true

    return createNavOptions(
        popUpTo,
        isPopUpToInclusive,
        defaultNavOptions
    )
}

private fun createNavOptions(
    popUpTo: Int,
    isPopUpToInclusive: Boolean,
    defaultNavOptions: NavOptions?
): NavOptions {
    return NavOptions.Builder()
        .setPopUpTo(popUpTo, isPopUpToInclusive)
        // Set Custom animation for action
        .setEnterAnim(defaultNavOptions?.enterAnim ?: mNavAnimations.enterAnimation)
        .setExitAnim(defaultNavOptions?.exitAnim ?: mNavAnimations.exitAnimation)
        .setPopEnterAnim(defaultNavOptions?.popEnterAnim ?: mNavAnimations.popEnterAnimation)
        .setPopExitAnim(defaultNavOptions?.popExitAnim ?: mNavAnimations.popExitAnimation)
        .build()
}
