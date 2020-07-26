package com.beetlestance.sample

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton


@Module
@InstallIn(ApplicationComponent::class)
abstract class TopLevelNavigatorViewModelDelegateModule {

    @Singleton
    @Binds
    abstract fun bindTopLevelNavigator(navigator: NavigatorViewModelDelegate): TopLevelNavigatorViewModelDelegate
}

