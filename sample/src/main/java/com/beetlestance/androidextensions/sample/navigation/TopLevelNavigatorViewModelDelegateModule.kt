package com.beetlestance.androidextensions.sample.navigation

import com.beetlestance.androidextensions.navigation.DeeplinkNavigator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object TopLevelNavigatorViewModelDelegateModule {

    @Singleton
    @Provides
    fun provideTopLevelNavigator(): DeeplinkNavigator = DeeplinkNavigator
}

