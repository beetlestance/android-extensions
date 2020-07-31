package com.beetlestance.androidextensions.sample.navigation

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
import com.beetlestance.androidextensions.navigation.extensions.handleDeeplink
import com.beetlestance.androidextensions.navigation.extensions.handleNewIntentForDeeplink
import com.beetlestance.androidextensions.navigation.extensions.setNavigationPolicy
import com.beetlestance.androidextensions.navigation.extensions.setupMultipleBackStackBottomNavigation
import com.beetlestance.androidextensions.sample.R
import com.beetlestance.androidextensions.sample.databinding.ActivityNavigationBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NavigationActivity : AppCompatActivity() {

    lateinit var binding: ActivityNavigationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.SampleApp_MainActivityTheme)
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setNavigationPolicy(
            fragmentContainerViewId = binding.navHostNavigationActivity.id,
            intent = intent
        )

        if (savedInstanceState == null) {
            setUpBottomNavigation()
        }
    }


    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        setUpBottomNavigation()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNewIntentForDeeplink()
    }


    private fun setUpBottomNavigation() {
        setupMultipleBackStackBottomNavigation(
            navGraphIds = NAV_GRAPH_IDS,
            containerId = binding.navHostNavigationActivity.id,
            bottomNavigationView = binding.navigationActivityBottomNavigation
        ).observe(this) { navController ->
            // Choose when to show/hide the Bottom Navigation View
            navController.addOnDestinationChangedListener { _, destination, _ ->

            }
        }

        handleDeeplink(binding.navigationActivityBottomNavigation)
    }


    companion object {
        private val NAV_GRAPH_IDS = listOf(
            R.navigation.nav_graph_home,
            R.navigation.nav_graph_feed,
            R.navigation.nav_graph_search
        )
    }
}