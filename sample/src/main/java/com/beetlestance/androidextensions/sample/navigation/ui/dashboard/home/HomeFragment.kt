package com.beetlestance.androidextensions.sample.navigation.ui.dashboard.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.beetlestance.androidextensions.navigation.deprecated.DeeplinkNavigator
import com.beetlestance.androidextensions.navigation.deprecated.data.NavigateOnceDeeplinkRequest
import com.beetlestance.androidextensions.sample.databinding.FragmentHomeBinding
import com.beetlestance.androidextensions.sample.navigation.constants.FEED_DEEPLINK
import com.beetlestance.androidextensions.sample.navigation.constants.NOTIFICATION_DEEPLINK
import com.beetlestance.androidextensions.sample.navigation.constants.SEARCH_DEEPLINK
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()
    private val args: HomeFragmentArgs by navArgs()
    private var binding: FragmentHomeBinding? = null

    @Inject
    lateinit var deeplinkNavigator: DeeplinkNavigator

    private var multipleInstancesAllowed: Boolean = false
    private var shouldUpdateArguments: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding?.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        args.input?.let {
            binding?.fragmentHomeArguments?.text = "Arguments: $it"
        }

        binding?.fragmentHomeMultipleInstance?.setOnCheckedChangeListener { _, isChecked ->
            multipleInstancesAllowed = isChecked
            if (isChecked) {
                binding?.fragmentHomeUpdateArguments?.isChecked = true
            }
        }

        binding?.fragmentHomeUpdateArguments?.setOnCheckedChangeListener { _, isChecked ->
            shouldUpdateArguments = isChecked
        }

        binding?.fragmentHomeOpenFeed?.setOnClickListener {
            val input = if (shouldUpdateArguments) {
                binding?.fragmentHomeInputArguments?.editText?.text?.toString()
            } else null

            deeplinkNavigator.navigate(
                NavigateOnceDeeplinkRequest(
                    deeplink = FEED_DEEPLINK.format(input).toUri(),
                    updateArguments = shouldUpdateArguments,
                    allowMultipleInstance = multipleInstancesAllowed
                )
            )
        }

        binding?.fragmentHomeOpenNotification?.setOnClickListener {
            val input = if (shouldUpdateArguments) {
                binding?.fragmentHomeInputArguments?.editText?.text?.toString()
            } else null

            deeplinkNavigator.navigate(
                NavigateOnceDeeplinkRequest(
                    deeplink = NOTIFICATION_DEEPLINK.format(input).toUri(),
                    updateArguments = shouldUpdateArguments,
                    allowMultipleInstance = multipleInstancesAllowed
                )
            )
        }

        binding?.fragmentHomeOpenSearch?.setOnClickListener {
            val input = if (shouldUpdateArguments) {
                binding?.fragmentHomeInputArguments?.editText?.text?.toString()
            } else null

            deeplinkNavigator.navigate(
                NavigateOnceDeeplinkRequest(
                    deeplink = SEARCH_DEEPLINK.format(input).toUri(),
                    updateArguments = shouldUpdateArguments,
                    allowMultipleInstance = multipleInstancesAllowed
                )
            )
        }
    }
}
