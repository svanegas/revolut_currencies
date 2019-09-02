package com.svanegas.revolut.currencies.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jakewharton.rxrelay2.BehaviorRelay
import com.svanegas.revolut.currencies.base.arch.BaseViewModel
import com.svanegas.revolut.currencies.base.utility.notifyChange
import com.svanegas.revolut.currencies.entity.Currency
import com.svanegas.revolut.currencies.repository.CurrenciesRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.combineLatest
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import java.text.NumberFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val TEXT_CHANGE_DEBOUNCE_DELAY = 100L

class CurrenciesViewModel @Inject constructor(
    private val currenciesRepository: CurrenciesRepository
) : BaseViewModel() {

    // TODO: Move to LiveData
    private var selectedCurrency = "EUR"

    // TODO: This would be to extend with some "Add currency" feature.
    private val allowedCurrencies = MutableLiveData(
        setOf(
            "EUR", "USD", "GBP", "CZK"
        )
    )

    private val _currencies = MutableLiveData<MutableMap<String, Currency>>(mutableMapOf())
    val currencies: LiveData<MutableMap<String, Currency>> = _currencies

    private val textChangeRelay = BehaviorRelay.create<String>()

    init {
        fetchData()
        initTextChangeRelay()
    }

    private fun initTextChangeRelay() {
        compositeDisposable += textChangeRelay
            .debounce(TEXT_CHANGE_DEBOUNCE_DELAY, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .filter { it == selectedCurrency }
            .subscribe {
                Timber.d("CACA - Sym: $it")
                _currencies.notifyChange()
            }
    }

    fun setCurrencyAsBase(symbol: String) {
        val oldCurrency = currencies.value?.get(symbol) ?: return
        val updatedCurrency = oldCurrency.copy(baseAt = Date())
        selectedCurrency = symbol
        _currencies.value?.put(oldCurrency.symbol, updatedCurrency)
        _currencies.notifyChange()
    }

    fun prepareCurrenciesToPopulate(currencies: List<Currency>): List<Currency> = currencies
        .sortedByDate()
        .convertRates()
        .toList()

    fun refreshValues(symbol: String) = textChangeRelay.accept(symbol)

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
        .filter { allowedCurrencies.value?.contains(it.symbol) ?: false }

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
        val numberFormat = NumberFormat.getInstance(Locale.getDefault())
        return try {
            result = numberFormat.parse(amount).toDouble()
            result *= ratio

            numberFormat.format(result)
        } catch (ex: Exception) {
            Timber.e(ex)
            ""
        }
    }
}

