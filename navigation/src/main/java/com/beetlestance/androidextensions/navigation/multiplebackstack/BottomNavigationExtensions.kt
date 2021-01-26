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
        containerId = containerId,
        stackListener = object : MultipleBackStackNavigator.StackListener {
            override fun onControllerChange(navController: NavController?) {
                onControllerChange(navController)
            }

            override fun onStackChange(stackId: Int) {
                selectedItemId = stackId
            }
        }
    )

    multipleBackStackNavigator.setUpStacks(selectedItemId)

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
