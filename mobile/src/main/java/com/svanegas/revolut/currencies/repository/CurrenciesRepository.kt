package com.svanegas.revolut.currencies.repository

import com.svanegas.revolut.currencies.base.OpenForMocking
import com.svanegas.revolut.currencies.base.utility.applySchedulers
import com.svanegas.revolut.currencies.entity.Currency
import com.svanegas.revolut.currencies.entity.CurrencyResponse
import com.svanegas.revolut.currencies.rest.CurrencyRouter
import io.reactivex.Single
import io.realm.Realm
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OpenForMocking
class CurrenciesRepository @Inject constructor(
    private val currencyRouter: CurrencyRouter,
    private val realm: Realm
) {

    fun fetchCurrencies(base: String): Single<CurrencyResponse> = currencyRouter
        .getLatestCurrencies(base)
        .applySchedulers()

    fun fetchCurrencyName(code: String): String = java.util.Currency
        .getInstance(code)
        .getDisplayName(Locale.getDefault())

    fun saveCurrenciesToCache(currencies: List<Currency>) {
        realm.executeTransaction {
            it.copyToRealmOrUpdate(currencies)
        }
    }
}