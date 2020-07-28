package com.beetlestance.androidextensions.sample.ui.dashboard

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.beetlestance.androidextensions.navigation.NavigateOnceDirectionRequest
import com.beetlestance.androidextensions.sample.TopLevelNavigatorViewModelDelegate

class DashboardViewModel @ViewModelInject constructor() :
    ViewModel() {

    fun navigateToNotificationFragment(): NavigateOnceDirectionRequest {
        return NavigateOnceDirectionRequest(
            directions = DashboardFragmentDirections.toNotificationsFragment()
        )
    }
}

