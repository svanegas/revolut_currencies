package com.svanegas.revolut.currencies.base.rest.client

import androidx.annotation.VisibleForTesting
import com.svanegas.revolut.currencies.base.CurrenciesBaseConfig
import com.svanegas.revolut.currencies.base.rest.interceptor.LoggingInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Retrofit client build which may be injected with dagger.
 * Serves as usual logic for all routers, but customizable
 */
class RetrofitClientBuilder @Inject constructor(
    private val callAdapterFactory: CallAdapter.Factory,
    private val converterFactory: Converter.Factory,
    private val okHttpClient: OkHttpClient
) {
    companion object {
        const val DEFAULT_TIMEOUT_SECONDS = 30L
    }

    fun buildRetrofit(
        timeoutInSecond: Long = DEFAULT_TIMEOUT_SECONDS,
        baseUrl: String = CurrenciesBaseConfig.REST_BASE_URL,
        interceptors: Array<Interceptor>
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(buildClient(timeoutInSecond, interceptors))
        .addConverterFactory(converterFactory)
        .addCallAdapterFactory(callAdapterFactory)
        .build()

    @VisibleForTesting
    fun buildClient(
        timeoutInSecond: Long,
        newInterceptors: Array<Interceptor>
    ): OkHttpClient = okHttpClient.newBuilder()
        .connectTimeout(timeoutInSecond, TimeUnit.SECONDS)
        .readTimeout(timeoutInSecond, TimeUnit.SECONDS)
        .writeTimeout(timeoutInSecond, TimeUnit.SECONDS)
        .apply {
            // add all specified interceptors
            newInterceptors.forEach { addInterceptor(it) }
        }
        // Add LoggingInterceptor as the last one so we log all our custom headers.
        .addInterceptor(LoggingInterceptor.http())
        .build()
}
