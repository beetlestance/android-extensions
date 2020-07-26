package com.beetlestance.sample.ui.dashboard

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.beetlestance.beetleextensions.navigation_extensions.NavigateOnceDirectionRequest
import com.beetlestance.sample.TopLevelNavigatorViewModelDelegate

class DashboardViewModel @ViewModelInject constructor(topLevelNavigatorViewModelDelegate: TopLevelNavigatorViewModelDelegate) :
    ViewModel(), TopLevelNavigatorViewModelDelegate by topLevelNavigatorViewModelDelegate {

    fun navigateToNotificationFragment(): NavigateOnceDirectionRequest {
        return NavigateOnceDirectionRequest(
            directions = DashboardFragmentDirections.toNotificationsFragment()
        )
    }
}

