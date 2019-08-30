package com.svanegas.revolut.currencies.rest

import com.svanegas.revolut.currencies.entity.CurrencyResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrencyRouter {
    @GET("latest")
    fun getLatestCurrencies(@Query("base") base: String): Single<CurrencyResponse>
}