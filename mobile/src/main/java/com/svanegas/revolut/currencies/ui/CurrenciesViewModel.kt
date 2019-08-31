package com.svanegas.revolut.currencies.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.svanegas.revolut.currencies.base.arch.BaseViewModel
import com.svanegas.revolut.currencies.entity.Currency
import com.svanegas.revolut.currencies.repository.CurrenciesRepository
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import javax.inject.Inject

class CurrenciesViewModel @Inject constructor(
    currenciesRepository: CurrenciesRepository
) : BaseViewModel() {

    // TODO: Move to LiveData
    private val selectedCurrency = "EUR"

    private val _currencies = MutableLiveData<List<CurrencyItemViewModel>>(emptyList())
    val currencies: LiveData<List<CurrencyItemViewModel>> = _currencies

    init {
        compositeDisposable += currenciesRepository
            .fetchCurrencies(selectedCurrency)
            .flattenAsFlowable { it.rates.entries }
            .map { Currency(it.key, it.value) }
            .map { CurrencyItemViewModel(it, appContext) }
            .toList()
            .subscribeBy(
                onSuccess = { _currencies.value = it },
                onError = { Timber.e(it) }
            )
    }
}

