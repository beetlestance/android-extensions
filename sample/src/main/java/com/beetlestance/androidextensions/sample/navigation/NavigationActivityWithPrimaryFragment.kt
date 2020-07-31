package com.beetlestance.androidextensions.sample.navigation

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.beetlestance.androidextensions.navigation.extensions.handleNewIntentForDeeplink
import com.beetlestance.androidextensions.navigation.extensions.setNavigationPolicyWithPrimaryFragment
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

        setNavigationPolicyWithPrimaryFragment(
            fragmentContainerViewId = binding.navHostFragmentContainer.id,
            primaryFragmentId = R.id.dashboardFragment,
            graphId = R.navigation.nav_graph_main_activity,
            intent = intent,
            navigateOnceOnPrimaryFragment = true
        )
    }

    /**
     *  Call super.onNewIntent before anything else
     *  @see FragmentActivity.onNewIntent
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNewIntentForDeeplink()
    }

}
