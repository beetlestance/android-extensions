package com.beetlestance.androidextensions.sample

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.beetlestance.androidextensions.navigation.extensions.handleDeeplinkIntent
import com.beetlestance.androidextensions.navigation.extensions.handleMultiFragmentBackStack
import com.beetlestance.androidextensions.navigation.extensions.handleOnNewDeeplinkIntent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.SampleApp_MainActivityTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        handleDeeplinkIntent(R.id.nav_host_fragment_container)
        handleMultiFragmentBackStack(R.id.nav_host_fragment_container, R.id.dashboardFragment)
    }

    /**
     *  Call super.onNewIntent before anything else
     *  @see FragmentActivity.onNewIntent
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleOnNewDeeplinkIntent(intent, R.id.nav_host_fragment_container)
    }

}
