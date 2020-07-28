package com.beetlestance.androidextensions.sample

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import com.beetlestance.androidextensions.navigation.navigator.DeeplinkNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var deeplinkNavigator: DeeplinkNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.SampleApp_MainActivityTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        deeplinkNavigator.handleDeeplinkIntent(intent, false)
    }

    /**
     *  Call super.onNewIntent before anything else
     *  @see FragmentActivity.onNewIntent
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        deeplinkNavigator.handleDeeplinkIntent(intent, true)
    }

    /**
     * check if current fragment is DashboardFragment
     */
    private fun isDashboardFragment(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_container)
        return navController.currentDestination?.id == R.id.dashboardFragment
    }
}
