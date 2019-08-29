package com.svanegas.revolut.currencies.di

import android.app.Application
import com.svanegas.revolut.currencies.base.di.AppModule
import com.svanegas.revolut.currencies.base.di.ViewModelModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidInjectionModule::class,
        AppModule::class,
        ViewModelModule::class,
        CurrenciesModule::class
    ]
)
interface AppComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }
}