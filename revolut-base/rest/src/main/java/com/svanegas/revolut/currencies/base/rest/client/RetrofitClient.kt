package com.svanegas.revolut.currencies.base.rest.client

import com.svanegas.revolut.currencies.base.CurrenciesBaseConfig
import okhttp3.Interceptor
import retrofit2.Retrofit
import javax.inject.Inject

class RetrofitClient @Inject constructor(
    private val builder: RetrofitClientBuilder
) {
    fun buildRetrofit(
        timeoutInSecond: Long = RetrofitClientBuilder.DEFAULT_TIMEOUT_SECONDS,
        baseUrl: String = CurrenciesBaseConfig.REST_BASE_URL,
        additionalInterceptors: Array<Interceptor> = emptyArray()
    ): Retrofit = builder.buildRetrofit(
        timeoutInSecond,
        baseUrl,
        arrayOf(*additionalInterceptors)
    )
}

