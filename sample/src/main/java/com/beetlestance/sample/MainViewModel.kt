package com.beetlestance.sample

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel

class MainViewModel @ViewModelInject constructor(topLevelNavigatorViewModelDelegate: TopLevelNavigatorViewModelDelegate) :
    ViewModel(), TopLevelNavigatorViewModelDelegate by topLevelNavigatorViewModelDelegate
