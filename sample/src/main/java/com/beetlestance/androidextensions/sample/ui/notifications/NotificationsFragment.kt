package com.beetlestance.androidextensions.sample.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.beetlestance.androidextensions.navigation.data.NavigateOnceDeeplinkRequest
import com.beetlestance.androidextensions.navigation.DeeplinkNavigator
import com.beetlestance.androidextensions.sample.R
import com.beetlestance.androidextensions.sample.constants.FEED_DEEPLINK
import com.beetlestance.androidextensions.sample.constants.HOME_DEEPLINK
import com.beetlestance.androidextensions.sample.constants.SEARCH_DEEPLINK
import com.beetlestance.androidextensions.sample.databinding.FragmentNotificationsBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NotificationsFragment : Fragment() {

    private val viewModel: NotificationsViewModel by viewModels()
    private val args: NotificationsFragmentArgs by navArgs()

    @Inject
    lateinit var deeplinkNavigator: DeeplinkNavigator

    private var binding: FragmentNotificationsBinding? = null

    private fun requireBinding(): FragmentNotificationsBinding = requireNotNull(binding)

    var multipleInstancesAllowed: Boolean = false
    var shouldUpdateArguments: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return requireBinding().root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        args.input?.let {
            binding?.fragmentNotificationArguments?.text = "Arguments: $it"
        }

        // Notification fragment is of same scope as dashboard fragment, thus a share the same
        // NavHostFragment and we can navigate to bottomNavigation fragment only from dashboard
        // So first we exists from current flow and then navigate to desired fragment
        deeplinkNavigator.resetStackBeforeNavigation.observe(viewLifecycleOwner) {
            if (it) findNavController().popBackStack(R.id.dashboardFragment, false)
        }


        binding?.fragmentNotificationMultipleInstance?.setOnCheckedChangeListener { _, isChecked ->
            multipleInstancesAllowed = isChecked
        }

        binding?.fragmentNotificationUpdateArguments?.setOnCheckedChangeListener { _, isChecked ->
            shouldUpdateArguments = isChecked
        }

        binding?.fragmentNotificationOpenFeed?.setOnClickListener {
            val input =
                if (shouldUpdateArguments) binding?.fragmentNotificationInputArguments?.editText?.text?.toString() else null
            deeplinkNavigator.navigateToTopLevelDestination(
                NavigateOnceDeeplinkRequest(
                    deeplink = FEED_DEEPLINK.format(input).toUri(),
                    updateArguments = shouldUpdateArguments,
                    allowMultipleInstance = multipleInstancesAllowed
                )
            )
            deeplinkNavigator.clearBackStack(true)
        }

        binding?.fragmentNotificationOpenNotification?.setOnClickListener {
            val input =
                if (shouldUpdateArguments) binding?.fragmentNotificationInputArguments?.editText?.text?.toString() else null
            deeplinkNavigator.navigateToTopLevelDestination(
                NavigateOnceDeeplinkRequest(
                    deeplink = HOME_DEEPLINK.format(input).toUri(),
                    updateArguments = shouldUpdateArguments,
                    allowMultipleInstance = multipleInstancesAllowed
                )
            )
            deeplinkNavigator.clearBackStack(true)
        }

        binding?.fragmentNotificationOpenSearch?.setOnClickListener {
            val input =
                if (shouldUpdateArguments) binding?.fragmentNotificationInputArguments?.editText?.text?.toString() else null
            deeplinkNavigator.navigateToTopLevelDestination(
                NavigateOnceDeeplinkRequest(
                    deeplink = SEARCH_DEEPLINK.format(input).toUri(),
                    updateArguments = shouldUpdateArguments,
                    allowMultipleInstance = multipleInstancesAllowed
                )
            )
            deeplinkNavigator.clearBackStack(true)
        }
    }

}
