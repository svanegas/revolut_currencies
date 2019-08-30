package com.svanegas.revolut.currencies.repository

import com.svanegas.revolut.currencies.base.utility.applySchedulers
import com.svanegas.revolut.currencies.entity.Currency
import com.svanegas.revolut.currencies.rest.CurrencyRouter
import io.reactivex.Single
import javax.inject.Inject

class CurrenciesRepository @Inject constructor(
    private val currencyRouter: CurrencyRouter
) {

    fun fetchCurrencies(base: String): Single<List<Currency>> = currencyRouter
        .getLatestCurrencies(base)
        .applySchedulers()
        .flattenAsFlowable { it.rates.entries }
        .map { Currency(it.key, it.value) }
        .toList()
}