package com.beetlestance.androidextensions.navigation

internal object DeeplinkNavigation {
    internal var navGraphIds: List<Int> = emptyList()
    internal var containerId: Int? = null

    var NAV_ENTER_ANIM = R.anim.fragment_open_enter
    var NAV_EXIT_ANIM = R.anim.fragment_open_exit
    var NAV_POP_ENTER_ANIM = R.anim.fragment_close_enter
    var NAV_POP_EXIT_ANIM = R.anim.fragment_close_exit

    fun setComponents(
        navGraphIds: List<Int>,
        containerId: Int,
        navAnimations: NavAnimations
    ) {
        this.navGraphIds = navGraphIds
        this.containerId = containerId
        NAV_ENTER_ANIM = navAnimations.enterAnimation
        NAV_EXIT_ANIM = navAnimations.exitAnimation
        NAV_POP_ENTER_ANIM = navAnimations.popEnterAnimation
        NAV_POP_EXIT_ANIM = navAnimations.popExitAnimation
    }
}