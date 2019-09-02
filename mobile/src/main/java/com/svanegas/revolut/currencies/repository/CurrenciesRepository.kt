package com.svanegas.revolut.currencies.repository

import com.svanegas.revolut.currencies.base.utility.applySchedulers
import com.svanegas.revolut.currencies.entity.CurrencyResponse
import com.svanegas.revolut.currencies.rest.CurrencyRouter
import io.reactivex.Single
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrenciesRepository @Inject constructor(
    private val currencyRouter: CurrencyRouter
) {

    fun fetchCurrencies(base: String): Single<CurrencyResponse> = currencyRouter
        .getLatestCurrencies(base)
        .applySchedulers()

    fun fetchCurrencyName(code: String): String = Currency
        .getInstance(code)
        .getDisplayName(Locale.getDefault())
}