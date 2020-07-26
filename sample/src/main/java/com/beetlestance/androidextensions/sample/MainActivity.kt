package com.beetlestance.androidextensions.sample

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import com.beetlestance.androidextensions.navigation.NavigateOnceDeeplinkRequest
import com.beetlestance.androidextensions.sample.event.Event
import com.beetlestance.androidextensions.sample.ui.dashboard.DashboardFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.SampleApp_MainActivityTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        handleDeeplink(intent, intentUpdated = false)
    }

    /**
     *  Call super.onNewIntent before anything else
     *  @see FragmentActivity.onNewIntent
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeeplink(intent, intentUpdated = true)
    }

    private fun handleDeeplink(intent: Intent?, intentUpdated: Boolean) {
        val deepLink = intent?.data ?: return // return if nothing to handle

        // set data to null otherwise setGraph in NavController will try to handle deeplink
        // and result into app restart
        intent.data = null
        if (intentUpdated && viewModel.handleDeeplinkIfAny == null) {
            /** @see DashboardFragment.onViewCreated */
            viewModel.navigatorDeeplink.value = Event(NavigateOnceDeeplinkRequest(deepLink))
            // In case of navigating through system tray notification need to pop current destination
            // if not DashboardFragment
            viewModel.clearBackStack.value = Event(isDashboardFragment().not())
        } else {
            /** @see DashboardFragment.setupBottomNavigationBar */
            viewModel.handleDeeplinkIfAny = NavigateOnceDeeplinkRequest(deepLink)
        }
    }

    /**
     * check if current fragment is DashboardFragment
     */
    private fun isDashboardFragment(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_container)
        return navController.currentDestination?.id == R.id.dashboardFragment
    }
}
