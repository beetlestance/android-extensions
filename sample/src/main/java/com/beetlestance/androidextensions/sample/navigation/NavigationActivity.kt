package com.beetlestance.androidextensions.sample.navigation

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.beetlestance.androidextensions.navigation.deprecated.data.NavigateOnceDeeplinkRequest
import com.beetlestance.androidextensions.navigation.deprecated.extensions.handleDeeplinkIntent
import com.beetlestance.androidextensions.navigation.deprecated.extensions.setupMultipleBackStackBottomNavigation
import com.beetlestance.androidextensions.sample.R
import com.beetlestance.androidextensions.sample.databinding.ActivityNavigationBinding
import com.beetlestance.androidextensions.sample.utils.DeeplinkValidator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
@SuppressLint("GoogleAppIndexingApiWarning")
class NavigationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNavigationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.SampleApp_MainActivityTheme)
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handleDeeplinkIntent()

        if (savedInstanceState == null) {
            setUpBottomNavigation()
        }
    }


    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        setUpBottomNavigation()
        lifecycleScope
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeeplinkIntent()
    }


    private fun setUpBottomNavigation() {
        setupMultipleBackStackBottomNavigation(
            navGraphIds = NAV_GRAPH_IDS,
            containerId = binding.navHostNavigationActivity.id,
            bottomNavigationView = binding.navigationActivityBottomNavigation,
            validatedRequest = ::validateDeeplink,
            onControllerChange = ::onControllerChange
        )
    }

    private fun onControllerChange(navController: NavController) {
        // Choose when to show/hide the Bottom Navigation View
        navController.addOnDestinationChangedListener { _, destination, _ ->

        }
    }

    private fun validateDeeplink(originalRequest: NavigateOnceDeeplinkRequest): NavigateOnceDeeplinkRequest {
        val validateDeeplink = DeeplinkValidator().validateDeeplink(originalRequest.deeplink)
        return originalRequest.copy(deeplink = validateDeeplink)
    }


    companion object {
        private val NAV_GRAPH_IDS = listOf(
            R.navigation.nav_graph_home,
            R.navigation.nav_graph_feed,
            R.navigation.nav_graph_search
        )
    }
}
