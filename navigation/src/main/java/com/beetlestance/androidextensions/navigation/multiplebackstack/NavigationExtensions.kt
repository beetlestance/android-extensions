package com.beetlestance.androidextensions.navigation.multiplebackstack

import android.net.Uri
import androidx.annotation.IdRes
import androidx.annotation.NavigationRes
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment

fun FragmentActivity.navController(@IdRes fragmentContainerId: Int): NavController {
    val navHostFragment =
        supportFragmentManager.findFragmentById(fragmentContainerId) as NavHostFragment
    return navHostFragment.navController
}

fun FragmentActivity.setUpGraph(
    navController: NavController,
    @NavigationRes graphId: Int
) {
    // extract deeplink so that graph will not be able to handle incoming deeplink
    val deeplink = intent.data
    intent.data = null
    navController.setGraph(graphId)

    // once setGraph is called set the deeplink again so that validations can be performed
    // we need to set the data to null again
    intent.data = deeplink
}

fun NavController.navigateOnce(
    deeplink: Uri,
    popUpTo: Int = -1,
    popUpToInclusive: Boolean = false,
    singleTop: Boolean = false,
    updateArguments: Boolean = false
) {
    val alreadyNavigated: Boolean = currentDestination?.hasDeepLink(deeplink) ?: false

    when {
        // single instance is allowed, and it is already on top of stack
        // should not update arguments then do nothing
        singleTop && alreadyNavigated && updateArguments.not() -> {
            return
        }
        // no popUpTo destination is specified - if there the current destination will be removed
        // required destination is in top of the stack
        // single instance is allowed
        // should update arguments - then we need to recreate the fragment
        popUpTo == -1 && popUpToInclusive && alreadyNavigated && singleTop && updateArguments -> {
            val navOptions =
                createNavOptions(currentDestination?.id ?: -1, popUpToInclusive, singleTop)
            navigate(deeplink, navOptions)
        }

        // else navigate
        else -> {
            val navOptions = createNavOptions(popUpTo, popUpToInclusive, singleTop)
            navigate(deeplink, navOptions)
        }
    }
}

private fun NavController.createNavOptions(
    popUpTo: Int = -1,
    popUpToInclusive: Boolean = false,
    singleTop: Boolean = false
): NavOptions {
    val navOptionsBuilder = NavOptions.Builder()
        .setLaunchSingleTop(singleTop)
        .setPopUpTo(popUpTo, popUpToInclusive)
    return navOptionsBuilder.build()
}
