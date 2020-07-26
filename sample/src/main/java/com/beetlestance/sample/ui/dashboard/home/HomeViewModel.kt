package com.beetlestance.sample.ui.dashboard.home

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.beetlestance.sample.TopLevelNavigatorViewModelDelegate

class HomeViewModel @ViewModelInject constructor(topLevelNavigatorViewModelDelegate: TopLevelNavigatorViewModelDelegate) :
    ViewModel(), TopLevelNavigatorViewModelDelegate by topLevelNavigatorViewModelDelegate
