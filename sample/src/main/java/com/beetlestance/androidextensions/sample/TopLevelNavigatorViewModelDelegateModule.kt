package com.beetlestance.androidextensions.sample

import com.beetlestance.androidextensions.navigation.DeeplinkNavigator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton


@Module
@InstallIn(ApplicationComponent::class)
object TopLevelNavigatorViewModelDelegateModule {

    @Singleton
    @Provides
    fun provideTopLevelNavigator(): DeeplinkNavigator = DeeplinkNavigator.getTopLevelNavigator()
}

