package com.beetlestance.androidextensions.navigation.data

import com.beetlestance.androidextensions.navigation.R

data class NavAnimations(
    val enterAnimation: Int = R.anim.nav_default_enter_anim,
    val exitAnimation: Int = R.anim.nav_default_exit_anim,
    val popEnterAnimation: Int = R.anim.nav_default_pop_enter_anim,
    val popExitAnimation: Int = R.anim.nav_default_pop_exit_anim
)
