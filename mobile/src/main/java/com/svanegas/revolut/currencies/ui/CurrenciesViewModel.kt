package com.svanegas.revolut.currencies.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.svanegas.revolut.currencies.base.arch.BaseViewModel
import com.svanegas.revolut.currencies.base.utility.notifyChange
import com.svanegas.revolut.currencies.entity.Currency
import com.svanegas.revolut.currencies.repository.CurrenciesRepository
import io.reactivex.rxkotlin.combineLatest
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class CurrenciesViewModel @Inject constructor(
    private val currenciesRepository: CurrenciesRepository
) : BaseViewModel() {

    // TODO: Move to LiveData
    private val selectedCurrency = "EUR"

    private val _currencies = MutableLiveData<MutableMap<String, Currency>>(mutableMapOf())
    val currencies: LiveData<MutableMap<String, Currency>> = _currencies

    init {
        fetchData()
    }

    fun sortCurrenciesByDate(currencyList: List<Currency>) = currencyList
        .sortedByDescending { it.baseAt }

    fun setCurrencyAsBase(tag: String) {
        val oldCurrency = currencies.value?.get(tag) ?: return
        val updatedCurrency = oldCurrency.copy(baseAt = Date())
        _currencies.value?.put(oldCurrency.symbol, updatedCurrency)
        _currencies.notifyChange()
    }

    private fun fetchData() {
        compositeDisposable += currenciesRepository
            .fetchCurrencies()
            .combineLatest(currenciesRepository.fetchNames())
            .map { (currency, currencyNames) ->
                currency.copy(name = currencyNames[currency.symbol].orEmpty())
            }
            .toMap { it.symbol }
            .subscribeBy(
                onSuccess = { _currencies.value = it },
                onError = { Timber.e(it) }
            )
    }

    private fun CurrenciesRepository.fetchCurrencies() = this
        .fetchCurrencies(selectedCurrency)
        .flattenAsFlowable { it.rates.entries }
        .map { Currency(it.key, it.value) }
        .startWith(getDefaultCurrency())

    private fun CurrenciesRepository.fetchNames() = this
        .fetchCurrencyNames()
        .toFlowable()

    private fun getDefaultCurrency() = Currency(
        symbol = "EUR",
        value = 1.0,
        baseAt = Date()
    )


//    fun updateCurrencyBaseAtDate(currencyViewModel: CurrencyItemViewModel) {
//        val map = currencies.value!!.toMutableMap()
//        val symbol = currencyViewModel.content.symbol
//        val updatedCurrency = currencyViewModel.content.copy(baseAt = Date())
//        map[symbol] = CurrencyItemViewModel(updatedCurrency, this)
//
//        _currencies.value = map
//    }
}

