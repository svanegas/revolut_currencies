package com.svanegas.revolut.currencies.di

import com.svanegas.revolut.currencies.base.di.AppCommonModule
import com.svanegas.revolut.currencies.base.rest.di.RestModule
import dagger.Module
import dagger.android.AndroidInjectionModule

@Module(
    includes = [
        AndroidInjectionModule::class,
        ViewModelModule::class,
        AppCommonModule::class,
        RestModule::class
    ]
)
object BaseModule
