package com.beetlestance.beetleextensions.navigation_extensions

import android.net.Uri
import androidx.navigation.*
import androidx.navigation.fragment.FragmentNavigator

/**
 * Check current destination has action to navigate or if already navigated update arguments
 * @param navigationRequest [NavigateOnceDeeplinkRequest]
 * [navigationRequest.deeplink] [Uri] for destination
 * [navigationRequest.allowMultipleInstance] [Boolean] if false destination will open times
 * if true only existing will be removed from backStack
 * [navigationRequest.updateArguments] [Boolean] by default set to true, set false if destination doesn't require
 * argument update. If false same instance from backStack will be used
 */
fun NavController.navigateOnce(navigationRequest: NavigateOnceDeeplinkRequest) {
    // Find deeplink mapped nav Id and compare with currentDestination nav Id
    val alreadyNavigated: Boolean =
        currentDestination?.hasDeepLink(navigationRequest.deeplink) == true

    when {
        navigationRequest.allowMultipleInstance -> {
            // navigate to destination create new instance if already in backstack
            navigate(navigationRequest.deeplink)
        }
        alreadyNavigated && navigationRequest.updateArguments.not() -> {
            // if already navigated, do nothing
            return
        }
        alreadyNavigated && navigationRequest.allowMultipleInstance.not() -> {
            // Remove Current fragment instance of destination
            // and navigate to update arguments
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
        else -> {
            // navigate to new destinations
            navigate(navigationRequest.deeplink)
        }
    }
}

/**
 * Check current destination has action to navigate or if already navigated update arguments
 * @param navigationRequest [NavigateOnceDirectionRequest]
 * [navigationRequest.directions] [NavDirections] for destination
 * [navigationRequest.navigatorExtras] [FragmentNavigator.Extras] passed to FragmentNavigator to enable Fragment specific behavior
 * such as shared transitions for destination
 * [navigationRequest.allowMultipleInstance] [Boolean] if false destination will open times
 * if true only existing will be removed from backStack
 * [navigationRequest.updateArguments] [Boolean] by default set to true, set false if destination doesn't require
 * argument update. If false same instance from backStack will be used
 */
fun NavController.navigateOnce(navigationRequest: NavigateOnceDirectionRequest) {
    // Check if navController has action for direction and return if no action found
    val navigationAction =
        currentDestination?.getAction(navigationRequest.directions.actionId) ?: return

    // compare directionsDestination nav Id with currentDestination nav Id
    val alreadyNavigated = navigationAction.destinationId == currentDestination?.id

    when {
        navigationRequest.allowMultipleInstance -> {
            // navigate to destination create new instance if already in backstack
            internalNavigateOnce(navigationRequest)
        }
        alreadyNavigated && navigationRequest.updateArguments.not() -> {
            // if already navigated, do nothing
            return
        }
        alreadyNavigated && navigationRequest.allowMultipleInstance.not() -> {
            // Remove Current fragment instance of destination
            // and navigate to update arguments
            currentDestination?.let {
                val navOptions = it.createNavOptionsFor(navigationAction)
                navigate(
                    navigationRequest.directions.actionId,
                    navigationRequest.directions.arguments,
                    navOptions,
                    navigationRequest.navigatorExtras
                )
            }
        }
        else -> {
            // check extras and navigate to destination
            navigate(navigationRequest.directions)
        }
    }
}

private fun NavController.internalNavigateOnce(navigationRequest: NavigateOnceDirectionRequest) {
    when {
        navigationRequest.navigatorExtras != null -> {
            // navigate to destination with extras
            navigateOnceWithExtras(navigationRequest)
        }
        else -> {
            // navigate to destination
            navigate(navigationRequest.directions)
        }
    }
}

private fun NavController.navigateOnceWithExtras(navigationRequest: NavigateOnceDirectionRequest) {
    navigate(navigationRequest.directions, requireNotNull(navigationRequest.navigatorExtras))
}

private fun NavDestination.createNavOptionsFor(action: NavAction): NavOptions {
    // check if action has defaultNavOptions
    val defaultNavOptions: NavOptions? = action.navOptions
    // popTo destinationId if no defaultNavOptions
    val popUpTo: Int = defaultNavOptions?.popUpTo ?: id
    //  set isPopUpToInclusive true if no defaultNavOptions
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
        .setEnterAnim(defaultNavOptions?.enterAnim ?: NAV_ENTER_ANIM)
        .setExitAnim(defaultNavOptions?.exitAnim ?: NAV_EXIT_ANIM)
        .setPopEnterAnim(defaultNavOptions?.popEnterAnim ?: NAV_POP_ENTER_ANIM)
        .setPopExitAnim(defaultNavOptions?.popExitAnim ?: NAV_POP_EXIT_ANIM)
        .build()
}
