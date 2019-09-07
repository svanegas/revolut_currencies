package com.svanegas.revolut.currencies.repository

import com.svanegas.revolut.currencies.base.OpenForMocking
import com.svanegas.revolut.currencies.base.utility.applySchedulers
import com.svanegas.revolut.currencies.entity.Currency
import com.svanegas.revolut.currencies.rest.CurrencyRouter
import io.reactivex.Single
import io.realm.Realm
import io.realm.kotlin.where
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OpenForMocking
class CurrenciesRepository @Inject constructor(
    private val currencyRouter: CurrencyRouter,
    private val realm: Realm
) {

    fun fetchCurrencies(base: String): Single<MutableList<Currency>> = currencyRouter
        .getLatestCurrencies(base)
        .applySchedulers()
        .flattenAsFlowable { it.rates.entries }
        .map { saveOrUpdateCurrencyToCache(it) }
        .toList()


    fun fetchCurrencyName(code: String): String = java.util.Currency
        .getInstance(code)
        .getDisplayName(Locale.getDefault())

    private fun saveOrUpdateCurrencyToCache(pair: MutableMap.MutableEntry<String, Double>): Currency {
        var cached = realm.where<Currency>()
            .equalTo(Currency.Keys.SYMBOL, pair.key)
            .findFirst() ?: Currency(symbol = pair.key)

        realm.executeTransaction {
            cached.apply {
                ratio = pair.value
            }
            cached = it.copyToRealmOrUpdate(cached)
        }
        return realm.copyFromRealm(cached)
    }

    fun saveCurrenciesToCache(currencies: List<Currency>) {
        realm.executeTransaction {
            it.copyToRealmOrUpdate(currencies)
        }
    }
}