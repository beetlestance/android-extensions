package com.beetlestance.androidextensions.navigation.data

import com.beetlestance.androidextensions.navigation.R

data class NavAnimations(
    var enterAnimation: Int = R.anim.fragment_open_enter,
    var exitAnimation: Int = R.anim.fragment_open_exit,
    var popEnterAnimation: Int = R.anim.fragment_close_enter,
    var popExitAnimation: Int = R.anim.fragment_close_exit
)