package com.svanegas.revolut.currencies.ui

import androidx.lifecycle.MutableLiveData
import com.jakewharton.rxrelay2.BehaviorRelay
import com.svanegas.revolut.currencies.base.arch.BaseViewModel
import com.svanegas.revolut.currencies.entity.Currency
import com.svanegas.revolut.currencies.repository.CurrenciesRepository
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.text.NumberFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val TEXT_CHANGE_DEBOUNCE_DELAY_MILLIS = 100L
private const val DATA_POLL_DELAY_MILLIS = 1500L
private const val DEFAULT_BASE_CURRENCY_SYMBOL = "EUR"

class CurrenciesViewModel @Inject constructor(
    private val currenciesRepository: CurrenciesRepository
) : BaseViewModel() {

    private var selectedCurrency: Currency = getDefaultCurrency()

    // TODO: This would be to extend with some "Add currency" feature.
    private val allowedCurrencies = MutableLiveData(
        setOf(
            DEFAULT_BASE_CURRENCY_SYMBOL, "USD", "GBP", "CZK"
        )
    )

    private val currenciesMap = MutableLiveData<MutableMap<String, Currency>>(mutableMapOf())
    val currencies = MutableLiveData<List<Currency>>()

    private val textChangeRelay = BehaviorRelay.create<String>()
    private var textChangeRelayDisposable: Disposable? = null

    init {
        fetchData()
        initTextChangeRelay()
    }

    override fun onCleared() {
        textChangeRelayDisposable?.dispose()
        super.onCleared()
    }

    private fun initTextChangeRelay() {
        textChangeRelayDisposable = textChangeRelay
            .debounce(TEXT_CHANGE_DEBOUNCE_DELAY_MILLIS, TimeUnit.MILLISECONDS)
            .filter { it == selectedCurrency.symbol }
            .flatMap { notifyCurrenciesUpdated().toObservable() }
            .subscribe()
    }

    fun setCurrencyAsBase(symbol: String) {
        if (selectedCurrency.symbol == symbol) return

        val oldCurrency = currenciesMap.value?.get(symbol) ?: return
        val updatedCurrency = oldCurrency.copy(baseAt = Date())

        selectedCurrency = updatedCurrency
        currenciesMap.value?.put(oldCurrency.symbol, updatedCurrency)
        compositeDisposable += notifyCurrenciesUpdated().subscribe()

        fetchData()
    }

    private fun notifyCurrenciesUpdated(currencies: MutableMap<String, Currency>? = null): Single<List<Currency>> {
        if (currencies != null) currenciesMap.value = currencies

        return Single.fromCallable { currenciesMap.value }
            .subscribeOn(Schedulers.computation())
            .map { it.values.toList() }
            .map { it.sortedByDate() }
            .map { it.convertRates() }
            .map { it.assignFocusability() }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { this.currencies.value = it }
    }

    fun refreshAmounts(symbol: String) = textChangeRelay.accept(symbol)

    private fun fetchData() {
        compositeDisposable.clear()
        compositeDisposable += currenciesRepository
            .fetchCurrencies()
            .startWith(selectedCurrency)
            .toMap { it.symbol }
            .repeatWhen { it.delay(DATA_POLL_DELAY_MILLIS, TimeUnit.MILLISECONDS) }
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap { notifyCurrenciesUpdated(it).toFlowable() }
            .subscribeBy(
                onError = { Timber.e(it) }
            )
    }

    private fun CurrenciesRepository.fetchCurrencies() = this
        .fetchCurrencies(selectedCurrency.symbol)
        .observeOn(Schedulers.computation())
        .flattenAsFlowable { it.rates.entries }
//        .filter { allowedCurrencies.value?.contains(it.key) ?: false }
        .map {
            // Not really good way how to keep the previous [baseAt] value
            val existing = currenciesMap.value?.get(it.key) ?: Currency()
            existing.copy(symbol = it.key, ratio = it.value)
        }
        .map { it.copy(name = currenciesRepository.fetchCurrencyName(it.symbol)) }

    // TODO: Define where to get this default currency from
    private fun getDefaultCurrency() = Currency(
        symbol = DEFAULT_BASE_CURRENCY_SYMBOL,
        baseAt = Date(),
        name = currenciesRepository.fetchCurrencyName(DEFAULT_BASE_CURRENCY_SYMBOL),
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

    private fun List<Currency>.assignFocusability(): List<Currency> {
        this.forEachIndexed { index, currency -> currency.enabled = index == 0 }
        return this
    }
}

