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
import java.text.NumberFormat
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

    fun setCurrencyAsBase(tag: String) {
        val oldCurrency = currencies.value?.get(tag) ?: return
        val updatedCurrency = oldCurrency.copy(baseAt = Date())
        _currencies.value?.put(oldCurrency.symbol, updatedCurrency)
        _currencies.notifyChange()
    }

    fun prepareCurrenciesToPopulate(currencies: List<Currency>): List<Currency> = currencies
        .sortedByDate()
        .convertRates()
        .toList()

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
        baseAt = Date(),
        amount = "10"
    )

    private fun List<Currency>.sortedByDate() = this
        .sortedByDescending { it.baseAt }


    private fun List<Currency>.convertRates(): List<Currency> {
        val source = this.firstOrNull() ?: return this

        return this
            .mapIndexed { index, item ->
                if (index == 0) item
                else {
                    item.amount = convertValue(source.amount, item.ratio)
                    item
                }
            }
            .toList()
    }

    private fun convertValue(amount: String, ratio: Double): String {
        var result: Double
        return try {
            result = amount.toDouble()
            result *= ratio

            NumberFormat
                .getInstance(Locale.getDefault())
                .format(result)
        } catch (ex: NumberFormatException) {
            ""
        }
    }
}

