package com.beetlestance.sample.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.beetlestance.androidextensions.navigation.NavigateOnceDeeplinkRequest
import com.beetlestance.androidextensions.navigation.navigateDeeplink
import com.beetlestance.androidextensions.navigation.navigateOnce
import com.beetlestance.androidextensions.navigation.setupWithNavController
import com.beetlestance.sample.R
import com.beetlestance.sample.databinding.FragmentDashboardBinding
import com.beetlestance.sample.event.observeEvent
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

        requireBinding().dashboardFragmentToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.dashboard_notification -> {
                    findNavController().navigateOnce(viewModel.navigateToNotificationFragment())
                    true
                }
                else -> false
            }
        }

        viewModel.navigatorDeeplink.observeEvent(viewLifecycleOwner) {
            handleDeeplinkOrElse(it) { deeplink -> bottomNavigationHandleDeeplink(deeplink) }
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        setupBottomNavigationBar()
    }

    /**
     * Called on first creation and when restoring state.
     */
    private fun setupBottomNavigationBar() {
        var deepLink: NavigateOnceDeeplinkRequest? = null
        viewModel.handleDeeplinkIfAny?.let {
            handleDeeplinkOrElse(it) { validDeeplink ->
                deepLink = validDeeplink
            }
            viewModel.handleDeeplinkIfAny = null
        }

        // Setup the bottom navigation view with a list of navigation graphs
        requireBinding().dashboardFragmentBottomNavigation.setupWithNavController(
            navGraphIds = NAV_GRAPH_IDS,
            fragmentManager = childFragmentManager,
            containerId = R.id.nav_host_fragment_dashboard,
            request = deepLink
        ).observe(viewLifecycleOwner) { navController ->
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
    }

    @Synchronized
    private fun showNavigators() {
        binding?.rootDashboardFragment?.transitionToStart()
    }

    @Synchronized
    private fun hideNavigators() {
        binding?.rootDashboardFragment?.transitionToEnd()
    }

    private fun handleDeeplinkOrElse(
        request: NavigateOnceDeeplinkRequest,
        handleDeeplink: (NavigateOnceDeeplinkRequest) -> Unit
    ): Boolean {
        // validate and modify your deeplink to your need
        // val validDeepLink = deeplinkValidator.validateDeeplink(deepLink)

        // check if deeplink belongs to activity graph or bottomNavigation graphs
        val canHandleDeeplink = findNavController().graph.hasDeepLink(request.deeplink)
        return if (canHandleDeeplink) {
            // handle in currentDestination graph
            findNavController().navigateOnce(request)
            true
        } else {
            // handel through bottomNavigation
            handleDeeplink(request)
            false
        }
    }

    private fun bottomNavigationHandleDeeplink(request: NavigateOnceDeeplinkRequest) {
        requireBinding().dashboardFragmentBottomNavigation.post {
            requireBinding().dashboardFragmentBottomNavigation.navigateDeeplink(
                navGraphIds = NAV_GRAPH_IDS,
                fragmentManager = childFragmentManager,
                containerId = R.id.nav_host_fragment_dashboard,
                request = request
            )
        }
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
