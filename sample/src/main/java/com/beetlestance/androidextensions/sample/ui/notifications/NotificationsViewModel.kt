package com.beetlestance.androidextensions.sample.ui.notifications

import androidx.core.net.toUri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.beetlestance.androidextensions.navigation.NavigateOnceDeeplinkRequest
import com.beetlestance.androidextensions.sample.TopLevelNavigatorViewModelDelegate
import com.beetlestance.androidextensions.sample.constants.FEED_DEEPLINK
import com.beetlestance.androidextensions.sample.constants.HOME_DEEPLINK
import com.beetlestance.androidextensions.sample.constants.SEARCH_DEEPLINK
import com.beetlestance.androidextensions.sample.event.Event

class NotificationsViewModel @ViewModelInject constructor() :
    ViewModel()
