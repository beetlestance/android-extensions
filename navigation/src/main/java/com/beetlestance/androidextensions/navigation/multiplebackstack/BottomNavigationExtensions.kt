package com.beetlestance.androidextensions.navigation.multiplebackstack

import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import com.google.android.material.bottomnavigation.BottomNavigationView


fun BottomNavigationView.setUpWithMultipleBackStack(
    navGraphIds: List<Int>,
    fragmentManager: FragmentManager,
    containerId: Int,
    onControllerChange: (NavController?) -> Unit = {}
): MultipleBackStackNavigator {
    val multipleBackStackNavigator = MultipleBackStackNavigator(
        navGraphIds = navGraphIds,
        fragmentManager = fragmentManager,
        selectedStackId = this.selectedItemId,
        containerId = containerId,
        stackListener = object : StackListener {
            override fun onControllerChange(navController: NavController?) {
                onControllerChange(navController)
            }

            override fun onStackChange(stackId: Int) {
                selectedItemId = stackId
            }
        }
    )

    setOnNavigationItemSelectedListener { item ->
        multipleBackStackNavigator.selectStack(item.itemId)
    }

    setupItemReselected(multipleBackStackNavigator)

    return multipleBackStackNavigator
}

private fun BottomNavigationView.setupItemReselected(
    multipleBackStackNavigator: MultipleBackStackNavigator
) {
    setOnNavigationItemReselectedListener { item ->
        multipleBackStackNavigator.resetStack(item.itemId)
    }
}
