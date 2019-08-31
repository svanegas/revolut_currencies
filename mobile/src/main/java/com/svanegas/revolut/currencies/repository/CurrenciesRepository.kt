package com.svanegas.revolut.currencies.repository

import com.svanegas.revolut.currencies.base.utility.applySchedulers
import com.svanegas.revolut.currencies.entity.CurrencyNames
import com.svanegas.revolut.currencies.entity.CurrencyResponse
import com.svanegas.revolut.currencies.rest.CurrencyRouter
import io.reactivex.Single
import javax.inject.Inject

class CurrenciesRepository @Inject constructor(
    private val currencyRouter: CurrencyRouter
) {

    fun fetchCurrencies(base: String): Single<CurrencyResponse> = currencyRouter
        .getLatestCurrencies(base)
        .applySchedulers()

    fun fetchCurrencyNames(): Single<CurrencyNames> = currencyRouter
        .getCurrencyNames()
        .applySchedulers()
}