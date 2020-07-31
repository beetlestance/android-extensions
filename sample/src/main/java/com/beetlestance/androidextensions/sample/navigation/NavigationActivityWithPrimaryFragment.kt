package com.beetlestance.androidextensions.sample.navigation

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.beetlestance.androidextensions.navigation.extensions.backStackClearBehavior
import com.beetlestance.androidextensions.navigation.extensions.handleIntentForDeeplink
import com.beetlestance.androidextensions.navigation.extensions.setUpNavHostFragmentId
import com.beetlestance.androidextensions.sample.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NavigationActivityWithPrimaryFragment : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.SampleApp_MainActivityTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation_with_primary_fragment)

        setUpNavHostFragmentId(R.id.nav_host_fragment_container)
        backStackClearBehavior(
            primaryFragmentId = R.id.dashboardFragment,
            avoidNavigationForFragmentIds = listOf(R.id.notificationsFragment),
            retainDeeplink = false
        )
        handleIntentForDeeplink(false)
    }

    /**
     *  Call super.onNewIntent before anything else
     *  @see FragmentActivity.onNewIntent
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntentForDeeplink(true)
    }

}
