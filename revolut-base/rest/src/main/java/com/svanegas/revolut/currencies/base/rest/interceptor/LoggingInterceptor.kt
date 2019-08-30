package com.svanegas.revolut.currencies.base.rest.interceptor

import com.svanegas.revolut.currencies.base.CurrenciesBaseConfig
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber

object LoggingInterceptor {
    @JvmStatic
    fun http(): Interceptor = HttpLoggingInterceptor(
        object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                Timber.v(message)
            }
        }
    ).apply {
        level = when (CurrenciesBaseConfig.LOGS) {
            true -> HttpLoggingInterceptor.Level.BODY
            false -> HttpLoggingInterceptor.Level.NONE
        }
    }
}
