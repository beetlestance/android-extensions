package com.beetlestance.androidextensions.sample.ui.dashboard.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.beetlestance.androidextensions.sample.databinding.FragmentSearchBinding
import com.beetlestance.sample.ui.dashboard.search.SearchFragmentArgs

class SearchFragment : Fragment() {

    private val viewModel: SearchViewModel by viewModels()
    private val args: SearchFragmentArgs by navArgs()

    private var binding: FragmentSearchBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentSearchBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        args.input?.let {
            binding?.fragmentSearchArguments?.text = "Arguments: $it"
        }
    }

}
