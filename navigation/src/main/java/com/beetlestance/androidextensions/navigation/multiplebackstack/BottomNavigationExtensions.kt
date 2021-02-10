package com.beetlestance.androidextensions.navigation.multiplebackstack

import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import com.google.android.material.bottomnavigation.BottomNavigationView


fun BottomNavigationView.setUpWithMultipleBackStack(
    navGraphIds: List<Int>,
    fragmentManager: FragmentManager,
    containerId: Int,
    primaryIndex: Int = 0,
    historyEnabled: Boolean = false,
    onControllerChange: (NavController?) -> Unit = {}
): MultipleBackStackManager {
    val multipleBackStackNavigator = MultipleBackStackManager(
        navGraphIds = navGraphIds,
        fragmentManager = fragmentManager,
        containerId = containerId,
        primarySelectedIndex = primaryIndex,
        historyEnabled = historyEnabled
    )

    multipleBackStackNavigator.setFragmentStackListener(stackListener = object :
        MultipleBackStackManager.StackListener {
        override fun onControllerChange(navController: NavController?) {
            onControllerChange(navController)
        }

        override fun onStackChange(graphId: Int) {
            selectedItemId = graphId
        }
    })

    multipleBackStackNavigator.selectNavHostFragment(selectedItemId)

    setOnNavigationItemSelectedListener { item ->
        multipleBackStackNavigator.selectNavHostFragment(item.itemId)
    }

    setupItemReselected(multipleBackStackNavigator)

    return multipleBackStackNavigator
}

private fun BottomNavigationView.setupItemReselected(
    multipleBackStackNavigator: MultipleBackStackManager
) {
    setOnNavigationItemReselectedListener { item ->
        multipleBackStackNavigator.resetStack(item.itemId)
    }
}
