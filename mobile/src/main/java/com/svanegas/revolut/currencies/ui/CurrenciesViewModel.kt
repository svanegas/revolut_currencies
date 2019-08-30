package com.svanegas.revolut.currencies.ui

import com.svanegas.revolut.currencies.base.arch.BaseViewModel
import com.svanegas.revolut.currencies.repository.CurrenciesRepository
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import javax.inject.Inject

class CurrenciesViewModel @Inject constructor(
    private val currenciesRepository: CurrenciesRepository
) : BaseViewModel() {

    // TODO: Move to LiveData
    private val selectedCurrency = "EUR"

    init {
        compositeDisposable += currenciesRepository
            .fetchCurrencies(selectedCurrency)
            .subscribeBy(
                onSuccess = { Timber.d("Got $it") },
                onError = { Timber.e(it) }
            )
    }
}

