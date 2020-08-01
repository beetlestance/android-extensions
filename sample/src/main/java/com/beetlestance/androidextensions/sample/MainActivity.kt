package com.beetlestance.androidextensions.sample

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.beetlestance.androidextensions.navigation.DeeplinkNavigationPolicy
import com.beetlestance.androidextensions.navigation.extensions.handleDeeplinkIntent
import com.beetlestance.androidextensions.navigation.extensions.handleOnNewDeeplinkIntent
import com.beetlestance.androidextensions.navigation.extensions.setUpDeeplinkNavigationBehavior
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.SampleApp_MainActivityTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setUpDeeplinkNavigationBehavior(
            navHostFragmentId = R.id.nav_host_fragment_container,
            primaryFragmentId = R.id.dashboardFragment,
            fragmentBackStackBehavior = TOP_LEVEL_DESTINATION_BEHAVIOR
        )
        handleDeeplinkIntent()
    }

    /**
     *  Call super.onNewIntent before anything else
     *  @see FragmentActivity.onNewIntent
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleOnNewDeeplinkIntent()
    }

    companion object {
        val TOP_LEVEL_DESTINATION_BEHAVIOR = mapOf(
            R.id.notificationsFragment to DeeplinkNavigationPolicy.NAVIGATE_ON_EXIT
        )
    }
}
