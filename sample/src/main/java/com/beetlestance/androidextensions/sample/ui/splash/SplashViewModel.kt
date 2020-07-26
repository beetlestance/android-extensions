package com.beetlestance.androidextensions.sample.ui.splash

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.beetlestance.androidextensions.navigation.NavigateOnceDirectionRequest
import com.beetlestance.sample.ui.splash.SplashFragmentDirections

class SplashViewModel @ViewModelInject constructor() : ViewModel() {


    fun navigateDirections(): NavigateOnceDirectionRequest {
        // Check for login or authentication
        // if (authenticated) navigateToDashboard else navigateToLoginRegistration
        return NavigateOnceDirectionRequest(
            SplashFragmentDirections.toDashboardFragment()
        )
    }
}
