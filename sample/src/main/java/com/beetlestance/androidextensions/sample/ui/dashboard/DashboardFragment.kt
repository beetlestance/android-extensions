package com.beetlestance.androidextensions.sample.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.beetlestance.androidextensions.navigation.navigateOnce
import com.beetlestance.androidextensions.navigation.navigator.DeeplinkNavigator
import com.beetlestance.androidextensions.navigation.setupWithNavController
import com.beetlestance.androidextensions.sample.R
import com.beetlestance.androidextensions.sample.databinding.FragmentDashboardBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private val viewModel: DashboardViewModel by viewModels()

    @Inject
    lateinit var deeplinkNavigator: DeeplinkNavigator

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
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        setupBottomNavigationBar()
    }

    /**
     * Called on first creation and when restoring state.
     */
    private fun setupBottomNavigationBar() {
        // Setup the bottom navigation view with a list of navigation graphs
        requireBinding().dashboardFragmentBottomNavigation.setupWithNavController(
            navGraphIds = NAV_GRAPH_IDS,
            containerId = requireBinding().navHostFragmentDashboard.id,
            fragmentManager = childFragmentManager
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

        deeplinkNavigator.observerForTopLevelNavigation.observe(viewLifecycleOwner) {
            deeplinkNavigator.handleDeeplink(
                topLevelNavController = findNavController(),
                fragmentManager = childFragmentManager,
                bottomNavigationView = requireBinding().dashboardFragmentBottomNavigation,
                request = it
            )
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
