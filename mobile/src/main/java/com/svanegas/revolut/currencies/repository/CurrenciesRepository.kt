package com.svanegas.revolut.currencies.repository

import com.svanegas.revolut.currencies.base.OpenForMocking
import com.svanegas.revolut.currencies.base.utility.applySchedulers
import com.svanegas.revolut.currencies.base.utility.asMaybe
import com.svanegas.revolut.currencies.entity.Currency
import com.svanegas.revolut.currencies.rest.CurrencyRouter
import io.reactivex.Maybe
import io.reactivex.Single
import io.realm.Realm
import io.realm.Sort
import io.realm.kotlin.where
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

private const val DEFAULT_BASE_CURRENCY_SYMBOL = "EUR"

@Singleton
@OpenForMocking
class CurrenciesRepository @Inject constructor(
    private val currencyRouter: CurrencyRouter,
    private val realm: Realm
) {

    fun fetchCurrencies(
        base: String = "",
        useCache: Boolean = false
    ): Single<MutableList<Currency>> {
        val cacheRequest = if (useCache) getCurrenciesCacheRequest() else Maybe.empty()
        val networkRequest = getCurrenciesNetworkRequest(base)
        return Maybe.concatArrayEager(cacheRequest, networkRequest)
            .firstElement()
            .toSingle()
    }

    fun getCurrenciesNetworkRequest(base: String): Maybe<MutableList<Currency>> = currencyRouter
        .getLatestCurrencies(base)
        .applySchedulers()
        .flattenAsFlowable { it.rates.entries }
        .map { saveOrUpdateCurrencyToCache(it) }
        .toList()
        .toMaybe()

    fun getCurrenciesCacheRequest(): Maybe<MutableList<Currency>> = realm
        .where<Currency>()
        .findAllAsync()
        .asMaybe()
        .filter { it.isNotEmpty() }
        .map { realm.copyFromRealm(it) }


    fun fetchCurrencyName(code: String): String = java.util.Currency
        .getInstance(code)
        .getDisplayName(Locale.getDefault())

    private fun saveOrUpdateCurrencyToCache(pair: MutableMap.MutableEntry<String, Double>): Currency {
        var cached = realm.where<Currency>()
            .equalTo(Currency.Keys.SYMBOL, pair.key)
            .findFirst() ?: Currency(symbol = pair.key, name = fetchCurrencyName(pair.key))

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

    fun fetchDefaultCurrency(): Currency {
        val currency = realm
            .where<Currency>()
            .sort(Currency.Keys.BASE_AT, Sort.DESCENDING)
            .findFirst() ?: return getDefaultCurrency()

        return realm.copyFromRealm(currency)
    }

    private fun getDefaultCurrency() = Currency(
        symbol = DEFAULT_BASE_CURRENCY_SYMBOL,
        baseAt = Date(),
        name = fetchCurrencyName(DEFAULT_BASE_CURRENCY_SYMBOL),
        amount = "10"
    )
}
