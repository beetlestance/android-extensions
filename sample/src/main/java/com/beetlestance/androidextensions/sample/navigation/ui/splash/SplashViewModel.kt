package com.beetlestance.androidextensions.sample.navigation.ui.splash

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.beetlestance.androidextensions.navigation.data.NavigateOnceDirectionRequest

class SplashViewModel @ViewModelInject constructor() : ViewModel() {


    fun navigateDirections(): NavigateOnceDirectionRequest {
        // Check for login or authentication
        // if (authenticated) navigateToDashboard else navigateToLoginRegistration
        return NavigateOnceDirectionRequest(
            SplashFragmentDirections.toDashboardFragment()
        )
    }
}
