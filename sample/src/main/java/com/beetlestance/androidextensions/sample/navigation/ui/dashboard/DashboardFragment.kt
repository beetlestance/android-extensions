package com.beetlestance.androidextensions.sample.navigation.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.beetlestance.androidextensions.navigation.deprecated.data.NavigateOnceDeeplinkRequest
import com.beetlestance.androidextensions.navigation.deprecated.extensions.navigateOnce
import com.beetlestance.androidextensions.navigation.deprecated.extensions.setupMultipleBackStackBottomNavigation
import com.beetlestance.androidextensions.sample.R
import com.beetlestance.androidextensions.sample.databinding.FragmentDashboardBinding
import com.beetlestance.androidextensions.sample.utils.DeeplinkValidator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private val viewModel: DashboardViewModel by viewModels()

    private var binding: FragmentDashboardBinding? = null
    private var currentNavController: NavController? = null

    private fun requireBinding(): FragmentDashboardBinding = requireNotNull(binding)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            setupBottomNavigationBar()
        }

        requireBinding().dashboardFragmentToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.dashboard_notification -> {
                    findNavController().navigateOnce(viewModel.navigateToNotificationFragment())
                    true
                }
                else -> false
            }
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            setupBottomNavigationBar()
        }
    }

    /**
     * Called on first creation and when restoring state.
     */
    private fun setupBottomNavigationBar() {
        // Setup the bottom navigation view with a list of navigation graphs
        setupMultipleBackStackBottomNavigation(
            navGraphIds = NAV_GRAPH_IDS,
            containerId = requireBinding().navHostFragmentDashboard.id,
            bottomNavigationView = requireBinding().dashboardFragmentBottomNavigation,
            validatedRequest = ::validateDeeplink,
            onControllerChange = ::onControllerChange
        )
    }

    private fun onControllerChange(navController: NavController) {
        currentNavController = navController

        // Choose when to show/hide the Bottom Navigation View
        navController.addOnDestinationChangedListener { _, destination, _ ->

            when {
                TOP_LEVEL_DESTINATION.contains(destination.id) -> {
                    showNavigators()
                }
                else -> {
                    hideNavigators()
                }
            }
        }
    }

    private fun validateDeeplink(originalRequest: NavigateOnceDeeplinkRequest): NavigateOnceDeeplinkRequest {
        val validateDeeplink = DeeplinkValidator().validateDeeplink(originalRequest.deeplink)
        return originalRequest.copy(deeplink = validateDeeplink)
    }

    @Synchronized
    private fun showNavigators() {
        binding?.rootDashboardFragment?.transitionToStart()
    }

    @Synchronized
    private fun hideNavigators() {
        binding?.rootDashboardFragment?.transitionToEnd()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        currentNavController = null
        binding = null
    }

    companion object {
        private val NAV_GRAPH_IDS = listOf(
            R.navigation.nav_graph_home,
            R.navigation.nav_graph_feed,
            R.navigation.nav_graph_search
        )

        val TOP_LEVEL_DESTINATION: Set<Int> = setOf(
            R.id.homeFragment,
            R.id.feedFragment,
            R.id.searchFragment
        )
    }
}
