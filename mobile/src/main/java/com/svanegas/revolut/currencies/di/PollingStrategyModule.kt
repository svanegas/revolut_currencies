package com.svanegas.revolut.currencies.di

import com.svanegas.revolut.currencies.polling.PollingStrategy
import com.svanegas.revolut.currencies.polling.PollingStrategyImpl
import dagger.Module
import dagger.Provides

@Module
object PollingStrategyModule {
    @Provides
    @JvmStatic
    fun providePollingStrategy(): PollingStrategy = PollingStrategyImpl()
}