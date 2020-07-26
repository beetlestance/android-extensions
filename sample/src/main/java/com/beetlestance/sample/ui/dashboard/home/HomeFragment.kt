package com.beetlestance.sample.ui.dashboard.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.beetlestance.androidextensions.navigation.NavigateOnceDeeplinkRequest
import com.beetlestance.sample.constants.FEED_DEEPLINK
import com.beetlestance.sample.constants.NOTIFICATION_DEEPLINK
import com.beetlestance.sample.constants.SEARCH_DEEPLINK
import com.beetlestance.sample.databinding.FragmentHomeBinding
import com.beetlestance.sample.event.Event
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()
    private val args: HomeFragmentArgs by navArgs()
    private var binding: FragmentHomeBinding? = null

    var multipleInstancesAllowed: Boolean = false
    var shouldUpdateArguments: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        args.input?.let {
            binding?.fragmentHomeArguments?.text = "Arguments: $it"
        }

        binding?.fragmentHomeMultipleInstance?.setOnCheckedChangeListener { _, isChecked ->
            multipleInstancesAllowed = isChecked
        }

        binding?.fragmentHomeUpdateArguments?.setOnCheckedChangeListener { _, isChecked ->
            shouldUpdateArguments = isChecked
        }

        binding?.fragmentHomeOpenFeed?.setOnClickListener {
            val input =
                if (shouldUpdateArguments) binding?.fragmentHomeInputArguments?.editText?.text?.toString() else null
            viewModel.navigatorDeeplink.value = Event(
                NavigateOnceDeeplinkRequest(
                    deeplink = FEED_DEEPLINK.format(input).toUri(),
                    updateArguments = shouldUpdateArguments,
                    allowMultipleInstance = multipleInstancesAllowed
                )
            )
        }

        binding?.fragmentHomeOpenNotification?.setOnClickListener {
            val input =
                if (shouldUpdateArguments) binding?.fragmentHomeInputArguments?.editText?.text?.toString() else null
            viewModel.navigatorDeeplink.value = Event(
                NavigateOnceDeeplinkRequest(
                    deeplink = NOTIFICATION_DEEPLINK.format(input).toUri(),
                    updateArguments = shouldUpdateArguments,
                    allowMultipleInstance = multipleInstancesAllowed
                )
            )
        }

        binding?.fragmentHomeOpenSearch?.setOnClickListener {
            val input =
                if (shouldUpdateArguments) binding?.fragmentHomeInputArguments?.editText?.text?.toString() else null
            viewModel.navigatorDeeplink.value = Event(
                NavigateOnceDeeplinkRequest(
                    deeplink = SEARCH_DEEPLINK.format(input).toUri(),
                    updateArguments = shouldUpdateArguments,
                    allowMultipleInstance = multipleInstancesAllowed
                )
            )
        }
    }
}
