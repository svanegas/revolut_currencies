package com.svanegas.revolut.currencies.di

import com.svanegas.revolut.currencies.base.rest.client.RetrofitClient
import com.svanegas.revolut.currencies.rest.CurrencyRouter
import dagger.Module
import dagger.Provides
import retrofit2.create

@Module(
    includes = [
        CurrencyActivityBuilderModule::class,
        PollingStrategyModule::class
    ]
)
object CurrencyModule {
    @Provides
    @JvmStatic
    fun provideCurrencyRouter(retrofitClient: RetrofitClient): CurrencyRouter = retrofitClient
        .buildRetrofit()
        .create()
}