package com.svanegas.revolut.currencies.base.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object AppModule {
    @Provides
    @Singleton
    @JvmStatic
    fun provideAppContext(application: Application): Context = application.applicationContext
}