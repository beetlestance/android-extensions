package com.beetlestance.androidextensions.navigation

import androidx.navigation.NavAction
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions

/**
 * The extension for [NavController] which uses provide [NavigateOnceDeeplinkRequest] to navigate
 * to a destination with given request parameters.
 */
fun NavController.navigateOnce(navigationRequest: NavigateOnceDeeplinkRequest) {
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
 * The extension for [NavController] which uses provide [NavigateOnceDirectionRequest] to navigate
 * to a destination with given request parameters.
 */
fun NavController.navigateOnce(navigationRequest: NavigateOnceDirectionRequest) {
    // Check if navController has action for direction and return if no action found
    val navigationAction =
        currentDestination?.getAction(navigationRequest.directions.actionId) ?: return

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
                val navOptions = it.createNavOptionsFor(navigationAction)
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