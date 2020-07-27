package com.beetlestance.androidextensions.sample.ui.dashboard.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.beetlestance.androidextensions.sample.databinding.FragmentFeedBinding

class FeedFragment : Fragment() {

    private val viewModel: FeedViewModel by viewModels()
    private val args: FeedFragmentArgs by navArgs()

    private var binding: FragmentFeedBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        args.input?.let {
            binding?.fragmentFeedArguments?.text = "Arguments: $it"
        }
    }

}
