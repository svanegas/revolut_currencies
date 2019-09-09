package com.svanegas.revolut.currencies.ui.search

import androidx.lifecycle.MutableLiveData
import com.svanegas.revolut.currencies.base.arch.BaseViewModel
import com.svanegas.revolut.currencies.base.arch.statefullayout.StatefulLayout
import com.svanegas.revolut.currencies.base.arch.statefullayout.SwipeRefreshHolder
import com.svanegas.revolut.currencies.entity.AllowedCurrencies
import com.svanegas.revolut.currencies.entity.Currency
import com.svanegas.revolut.currencies.repository.CurrenciesRepository
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import javax.inject.Inject

class CurrencySearchViewModel @Inject constructor(
    private val currenciesRepository: CurrenciesRepository
) : BaseViewModel(), SwipeRefreshHolder {

    val state = MutableLiveData(StatefulLayout.PROGRESS)
    override val swipeRefreshing = MutableLiveData(false)

    val currencies = MutableLiveData<List<Currency>>()
    internal val allowedCurrencies = loadAllowedCurrencies()

    init {
        fetchData()
    }

    fun fetchData() {
        compositeDisposable.clear()
        compositeDisposable += currenciesRepository
            .fetchCurrencies(useCache = true)
            .map { currencies -> currencies.sortedBy { it.symbol } }
            .doOnSuccess { currencies.value = it }
            .subscribeBy(
                onSuccess = { setupDisplayState() },
                onError = { handleError(it) }
            )
    }

    internal fun setupDisplayState() {
        state.value =
            if (currencies.value.isNullOrEmpty()) StatefulLayout.EMPTY else StatefulLayout.CONTENT
    }

    private fun loadAllowedCurrencies(): AllowedCurrencies = currenciesRepository
        .fetchAllowedCurrencies()

    private fun handleError(error: Throwable) {
        Timber.e(error)
        state.value = when {
            !isOnline() -> StatefulLayout.OFFLINE
            else -> StatefulLayout.ERROR
        }
    }

    // TODO Make only add
    fun toggleAllowed(currency: Currency) {
        if (!allowedCurrencies.contains(currency.symbol)) {
            allowedCurrencies.currencies.add(currency.symbol)
        } else {
            allowedCurrencies.currencies.remove(currency.symbol)
        }
        currenciesRepository.saveAllowedCurrenciesToCache(allowedCurrencies)
    }
}