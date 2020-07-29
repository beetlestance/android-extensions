package com.beetlestance.androidextensions.sample

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.beetlestance.androidextensions.navigation.extensions.handleIntentForDeeplink
import com.beetlestance.androidextensions.navigation.extensions.setBackStackPopBehavior
import com.beetlestance.androidextensions.navigation.extensions.setUpNavHostFragmentId
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.SampleApp_MainActivityTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setUpNavHostFragmentId(R.id.nav_host_fragment_container)
        handleIntentForDeeplink(false)
        setBackStackPopBehavior(R.id.dashboardFragment)
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
