package com.svanegas.revolut.currencies.di

import android.app.Application
import com.svanegas.revolut.currencies.CurrenciesApplication
import com.svanegas.revolut.currencies.base.di.AppCommonModule
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        BaseModule::class,
        CurrencyModule::class
    ]
)
interface AppComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    fun inject(currenciesApplication: CurrenciesApplication)
}