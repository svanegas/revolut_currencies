package com.svanegas.revolut.currencies.ui

import androidx.lifecycle.MutableLiveData
import com.jakewharton.rxrelay2.BehaviorRelay
import com.svanegas.revolut.currencies.base.OpenForMocking
import com.svanegas.revolut.currencies.base.arch.BaseViewModel
import com.svanegas.revolut.currencies.entity.Currency
import com.svanegas.revolut.currencies.polling.PollingStrategy
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
private const val DEFAULT_BASE_CURRENCY_SYMBOL = "EUR"

@OpenForMocking
class CurrenciesViewModel @Inject constructor(
    private val currenciesRepository: CurrenciesRepository,
    private val pollingStrategy: PollingStrategy
) : BaseViewModel() {

    internal var selectedCurrency: Currency = getDefaultCurrency()

    // TODO: This would be to extend with some "Add currency" feature.
    internal val allowedCurrencies = MutableLiveData(
        setOf(
            DEFAULT_BASE_CURRENCY_SYMBOL, "USD", "GBP", "CZK"
        )
    )

    internal val currenciesMap = MutableLiveData<MutableMap<String, Currency>>(mutableMapOf())
    val currencies = MutableLiveData<List<Currency>>()

    internal val textChangeRelay = BehaviorRelay.create<String>()
    private var textChangeRelayDisposable: Disposable? = null

    init {
        fetchData()
        initTextChangeRelay()
    }

    override fun onCleared() {
        textChangeRelayDisposable?.dispose()
        super.onCleared()
    }

    internal fun initTextChangeRelay() {
        textChangeRelayDisposable = textChangeRelay
            .debounce(TEXT_CHANGE_DEBOUNCE_DELAY_MILLIS, TimeUnit.MILLISECONDS)
            .filter { it == selectedCurrency.symbol }
            .flatMap { notifyCurrenciesUpdated().toObservable() }
            .subscribe()
    }

    fun setCurrencyAsBase(symbol: String) {
        val oldCurrency = currenciesMap.value?.get(symbol) ?: return
        val updatedCurrency = oldCurrency.copy(baseAt = Date())
        selectedCurrency = updatedCurrency
        currenciesMap.value?.put(oldCurrency.symbol, updatedCurrency)
        compositeDisposable += notifyCurrenciesUpdated().subscribe()

        fetchData()
    }

    internal fun notifyCurrenciesUpdated(currencies: MutableMap<String, Currency>? = null): Single<List<Currency>> {
        if (currencies != null) currenciesMap.value = currencies

        return Single.fromCallable { currenciesMap.value }
            .subscribeOn(Schedulers.computation())
            .map { getListOfCurrenciesFromMap(it) }
            .map { sortedByDate(it) }
            .map { convertRates(it) }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { this.currencies.value = it }
    }

    internal fun getListOfCurrenciesFromMap(map: MutableMap<String, Currency>) = map
        .values
        .toList()

    fun refreshAmounts(symbol: String) = textChangeRelay.accept(symbol)

    fun fetchData() {
        compositeDisposable.clear()
        compositeDisposable += fetchCurrencies()
            .startWith(selectedCurrency)
            .toMap { it.symbol }
            .repeatWhen { pollingStrategy.getPollingMethod(it) }
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap { notifyCurrenciesUpdated(it).toFlowable() }
            .subscribeBy(
                onError = { Timber.e(it) }
            )
    }

    internal fun fetchCurrencies() = currenciesRepository
        .fetchCurrencies(selectedCurrency.symbol)
        .observeOn(Schedulers.computation())
        .flattenAsFlowable { it.rates.entries }
        .filter { isCurrencyAllowed(it.key) }
        .map { getCurrencyWithExistingBaseAt(it) }
        .map { it.copy(name = currenciesRepository.fetchCurrencyName(it.symbol)) }

    internal fun getCurrencyWithExistingBaseAt(currencyEntry: MutableMap.MutableEntry<String, Double>): Currency {
        // Not really good way how to keep the previous [baseAt] value
        val existing = currenciesMap.value?.get(currencyEntry.key) ?: Currency()
        return existing.copy(symbol = currencyEntry.key, ratio = currencyEntry.value)
    }

    internal fun isCurrencyAllowed(symbol: String) = allowedCurrencies.value
        ?.contains(symbol)
        ?: false

    // TODO: Define where to get this default currency from
    private fun getDefaultCurrency() = Currency(
        symbol = DEFAULT_BASE_CURRENCY_SYMBOL,
        baseAt = Date(),
        name = currenciesRepository.fetchCurrencyName(DEFAULT_BASE_CURRENCY_SYMBOL),
        amount = "10"
    )

    internal fun sortedByDate(currencies: List<Currency>) = currencies
        .sortedByDescending { it.baseAt }


    internal fun convertRates(currencies: List<Currency>): List<Currency> {
        val source = currencies.firstOrNull() ?: return currencies

        return currencies
            .mapIndexed { index, item ->
                if (index == 0) item
                else {
                    item.amount = convertValue(source.amount, item.ratio)
                    item
                }
            }
            .toList()
    }

    internal fun convertValue(amount: String, ratio: Double): String {
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

