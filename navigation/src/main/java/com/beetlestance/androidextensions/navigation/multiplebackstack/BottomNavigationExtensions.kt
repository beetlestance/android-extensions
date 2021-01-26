package com.beetlestance.androidextensions.navigation.multiplebackstack

import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView

fun BottomNavigationView.setUpWithMultipleBackStack(
    navGraphIds: List<Int>,
    fragmentManager: FragmentManager,
    containerId: Int
): MultipleBackStackNavigator {
    val multipleBackStackNavigator = MultipleBackStackNavigator(
        navGraphIds = navGraphIds,
        fragmentManager = fragmentManager,
        selectedStackId = this.selectedItemId,
        containerId = containerId
    )

    multipleBackStackNavigator.onStackChange { selectedItemId = it }

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