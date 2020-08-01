package com.beetlestance.androidextensions.sample.navigation

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.beetlestance.androidextensions.navigation.DeeplinkNavigationPolicy
import com.beetlestance.androidextensions.navigation.extensions.handleDeeplinkIntent
import com.beetlestance.androidextensions.navigation.extensions.setUpDeeplinkNavigationBehavior
import com.beetlestance.androidextensions.sample.R
import com.beetlestance.androidextensions.sample.databinding.ActivityNavigationWithPrimaryFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NavigationActivityWithPrimaryFragment : AppCompatActivity() {

    lateinit var binding: ActivityNavigationWithPrimaryFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.SampleApp_MainActivityTheme)
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationWithPrimaryFragmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpDeeplinkNavigationBehavior(
            navHostFragmentId = binding.navHostFragmentContainer.id,
            primaryFragmentId = R.id.dashboardFragment,
            fragmentBackStackBehavior = TOP_LEVEL_FRAGMENT_BEHAVIOR,
            graphId = R.navigation.nav_graph_main_activity
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
        handleDeeplinkIntent()
    }

    companion object {
        val TOP_LEVEL_FRAGMENT_BEHAVIOR: Map<Int, DeeplinkNavigationPolicy> = mapOf(
            R.id.notificationsFragment to DeeplinkNavigationPolicy.EXIT_AND_NAVIGATE
        )
    }


}
