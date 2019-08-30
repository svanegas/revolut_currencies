package com.svanegas.revolut.currencies.base.rest.di

import com.svanegas.revolut.currencies.base.rest.client.RetrofitClient
import dagger.Module
import dagger.Provides
import dagger.Reusable
import okhttp3.OkHttpClient
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
object RestModule {
    /**
     * WARNING: we want to keep it singleton and use .newBuilder() because otherwise uses a lot of memory
     */
    @Provides
    @JvmStatic
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder().build()

    /**
     * Serves for rest response handlers (may be removed in future)
     */
    @Provides
    @JvmStatic
    @Singleton
    fun provideRetrofit(retrofitClient: RetrofitClient): Retrofit = retrofitClient.buildRetrofit()

    @Provides
    @JvmStatic
    @Reusable
    fun provideCallAdapterFactory(): CallAdapter.Factory = RxJava2CallAdapterFactory.create()

    @Provides
    @JvmStatic
    @Reusable
    fun provideConverterFactory(): Converter.Factory = GsonConverterFactory.create()
}
