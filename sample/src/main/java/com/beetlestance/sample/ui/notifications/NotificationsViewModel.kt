package com.beetlestance.sample.ui.notifications

import androidx.core.net.toUri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.beetlestance.beetleextensions.navigation_extensions.NavigateOnceDeeplinkRequest
import com.beetlestance.sample.TopLevelNavigatorViewModelDelegate
import com.beetlestance.sample.constants.FEED_DEEPLINK
import com.beetlestance.sample.constants.HOME_DEEPLINK
import com.beetlestance.sample.constants.SEARCH_DEEPLINK
import com.beetlestance.sample.event.Event

class NotificationsViewModel @ViewModelInject constructor(topLevelNavigatorViewModelDelegate: TopLevelNavigatorViewModelDelegate) :
    ViewModel(), TopLevelNavigatorViewModelDelegate by topLevelNavigatorViewModelDelegate {


    fun navigateToHome() {
        HOME_DEEPLINK.toUri().apply {
            navigatorDeeplink.value = Event(
                NavigateOnceDeeplinkRequest(
                    this
                )
            )
            clearBackStack.value = Event(true)
        }
    }

    fun navigateToFeed() {
        FEED_DEEPLINK.toUri().apply {
            navigatorDeeplink.value = Event(
                NavigateOnceDeeplinkRequest(
                    this
                )
            )
            clearBackStack.value = Event(true)
        }
    }

    fun navigateToSearch() {
        SEARCH_DEEPLINK.toUri().apply {
            navigatorDeeplink.value = Event(
                NavigateOnceDeeplinkRequest(
                    this
                )
            )
            clearBackStack.value = Event(true)
        }
    }
}
