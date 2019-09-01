package com.svanegas.revolut.currencies.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.svanegas.revolut.currencies.base.arch.BaseViewModel
import com.svanegas.revolut.currencies.entity.Currency
import com.svanegas.revolut.currencies.repository.CurrenciesRepository
import io.reactivex.rxkotlin.combineLatest
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import javax.inject.Inject

class CurrenciesViewModel @Inject constructor(
    private val currenciesRepository: CurrenciesRepository
) : BaseViewModel() {

    // TODO: Move to LiveData
    private val selectedCurrency = "EUR"

    private val _currencies = MutableLiveData<Map<String, Currency>>(emptyMap())
    val currencies: LiveData<Map<String, Currency>> = _currencies

    init {
        fetchData()
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

    private fun CurrenciesRepository.fetchNames() = this
        .fetchCurrencyNames()
        .toFlowable()

    fun sortCurrenciesByDate(currencyList: List<Currency>) = currencyList
        .sortedByDescending { it.baseAt }

//    fun updateCurrencyBaseAtDate(currencyViewModel: CurrencyItemViewModel) {
//        val map = currencies.value!!.toMutableMap()
//        val symbol = currencyViewModel.content.symbol
//        val updatedCurrency = currencyViewModel.content.copy(baseAt = Date())
//        map[symbol] = CurrencyItemViewModel(updatedCurrency, this)
//
//        _currencies.value = map
//    }
}

