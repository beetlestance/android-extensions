package com.beetlestance.androidextensions.navigation

object DeeplinkNavigation {
    internal var navGraphIds: List<Int> = emptyList()
    internal var containerId: Int? = null

    var NAV_ENTER_ANIM = R.anim.fragment_open_enter
    var NAV_EXIT_ANIM = R.anim.fragment_open_exit
    var NAV_POP_ENTER_ANIM = R.anim.fragment_close_enter
    var NAV_POP_EXIT_ANIM = R.anim.fragment_close_exit

    fun setEssentialComponents(
        navGraphIds: List<Int>,
        containerId: Int
    ): DeeplinkNavigation {
        this.navGraphIds = navGraphIds
        this.containerId = containerId
        return this
    }

    fun setCustomAnimationForBottomNavigation(
        enterAnimation: Int,
        exitAnimation: Int,
        popEnterAnimation: Int,
        popExitAnimation: Int
    ) {
        NAV_ENTER_ANIM = enterAnimation
        NAV_EXIT_ANIM = exitAnimation
        NAV_POP_ENTER_ANIM = popEnterAnimation
        NAV_POP_EXIT_ANIM = popExitAnimation
    }
}