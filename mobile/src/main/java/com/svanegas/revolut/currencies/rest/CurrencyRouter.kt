package com.svanegas.revolut.currencies.rest

import com.svanegas.revolut.currencies.entity.CurrencyNames
import com.svanegas.revolut.currencies.entity.CurrencyResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface CurrencyRouter {
    @GET("latest")
    fun getLatestCurrencies(@Query("base") base: String): Single<CurrencyResponse>

    // TODO: Temporary way to get currency names, pending official endpoint.
    @GET
    fun getCurrencyNames(@Url url: String = "https://openexchangerates.org/api/currencies.json"): Single<CurrencyNames>
}