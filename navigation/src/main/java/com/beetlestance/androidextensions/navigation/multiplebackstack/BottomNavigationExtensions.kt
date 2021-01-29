package com.beetlestance.androidextensions.navigation.multiplebackstack

import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import com.google.android.material.bottomnavigation.BottomNavigationView


fun BottomNavigationView.setUpWithMultipleBackStack(
    navGraphIds: List<Int>,
    fragmentManager: FragmentManager,
    containerId: Int,
    primaryIndex: Int = 0,
    backstackHistoryCount: Int,
    onControllerChange: (NavController?) -> Unit = {}
): MultipleBackStackNavigator {
    val multipleBackStackNavigator = MultipleBackStackNavigator(
        navGraphIds = navGraphIds,
        fragmentManager = fragmentManager,
        containerId = containerId,
        primarySelectedIndex = primaryIndex,
        backstackHistoryCount = backstackHistoryCount
    )

    multipleBackStackNavigator.setFragmentStackListener(stackListener = object :
        MultipleBackStackNavigator.StackListener {
        override fun onControllerChange(navController: NavController?) {
            onControllerChange(navController)
        }

        override fun onStackChange(graphId: Int) {
            selectedItemId = graphId
        }
    })


    multipleBackStackNavigator.setUpNavHostFragments()
    multipleBackStackNavigator.selectNavHostFragment(selectedItemId)

    setOnNavigationItemSelectedListener { item ->
        multipleBackStackNavigator.selectNavHostFragment(item.itemId)
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
