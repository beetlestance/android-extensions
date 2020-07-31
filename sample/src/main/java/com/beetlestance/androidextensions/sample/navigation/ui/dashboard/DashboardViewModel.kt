package com.beetlestance.androidextensions.sample.navigation.ui.dashboard

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.beetlestance.androidextensions.navigation.data.NavigateOnceDirectionRequest

class DashboardViewModel @ViewModelInject constructor() : ViewModel() {

    fun navigateToNotificationFragment(): NavigateOnceDirectionRequest {
        return NavigateOnceDirectionRequest(
            directions = DashboardFragmentDirections.toNotificationsFragment()
        )
    }
}

