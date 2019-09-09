package com.svanegas.revolut.currencies.ui.list

import androidx.lifecycle.MutableLiveData
import com.jakewharton.rxrelay2.BehaviorRelay
import com.svanegas.revolut.currencies.base.OpenForMocking
import com.svanegas.revolut.currencies.base.arch.BaseViewModel
import com.svanegas.revolut.currencies.base.arch.statefullayout.StatefulLayout
import com.svanegas.revolut.currencies.base.arch.statefullayout.SwipeRefreshHolder
import com.svanegas.revolut.currencies.entity.AllowedCurrencies
import com.svanegas.revolut.currencies.entity.Currency
import com.svanegas.revolut.currencies.polling.PollingStrategy
import com.svanegas.revolut.currencies.repository.CurrenciesRepository
import io.reactivex.Flowable
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
private const val TEXT_FOCUS_THROTTLE_DELAY_MILLIS = 1000L
private const val MAX_CURRENCIES_IN_LIST = 6

@OpenForMocking
class CurrenciesViewModel @Inject constructor(
    private val currenciesRepository: CurrenciesRepository,
    private val pollingStrategy: PollingStrategy
) : BaseViewModel(), SwipeRefreshHolder {

    val state = MutableLiveData(StatefulLayout.PROGRESS)
    override val swipeRefreshing = MutableLiveData(false)

    internal val currenciesMap = MutableLiveData<MutableMap<String, Currency>>(mutableMapOf())
    val currencies = MutableLiveData<List<Currency>>()

    internal var selectedCurrency: Currency = loadSelectedCurrency()
    internal var allowedCurrencies = loadAllowedCurrencies()

    internal val textChangeRelay = BehaviorRelay.create<String>()
    private var textChangeRelayDisposable: Disposable? = null

    internal val textFocusRelay = BehaviorRelay.create<String>()
    private var textFocusRelayDisposable: Disposable? = null

    private var useCache = true

    init {
        fetchData()
        initTextChangeRelay()
        initTextFocusRelay()
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

    private fun initTextFocusRelay() {
        textFocusRelayDisposable = textFocusRelay
            .throttleLatest(TEXT_FOCUS_THROTTLE_DELAY_MILLIS, TimeUnit.MILLISECONDS)
            .filter { it != selectedCurrency.symbol }
            .subscribeBy(
                onNext = { setCurrencyAsBase(it) }
            )
    }

    fun setCurrencyAsBase(symbol: String) {
        (currenciesMap.value?.get(symbol) ?: return).apply {
            baseAt = Date()
            selectedCurrency = this
        }

        fetchData() // will call notifyCurrenciesUpdated()
    }

    internal fun notifyCurrenciesUpdated(currencies: MutableMap<String, Currency>? = null): Single<List<Currency>> {
        if (currencies != null) currenciesMap.value = currencies

        return Single.fromCallable { currenciesMap.value }
            .subscribeOn(Schedulers.computation())
            .map { getListOfCurrenciesFromMap(it) }
            .map { sortedByDate(it) }
            .map { convertRates(it) }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { currenciesRepository.saveCurrenciesToCache(it) }
            .doOnSuccess { this.currencies.value = it }
    }

    internal fun getListOfCurrenciesFromMap(map: MutableMap<String, Currency>) = map
        .values
        .toList()

    fun refreshAmounts(symbol: String) = textChangeRelay.accept(symbol)

    fun refreshFocusedCurrency(symbol: String) = textFocusRelay.accept(symbol)

    fun fetchData() {
        compositeDisposable.clear()
        compositeDisposable += Flowable.fromCallable { useCache }
            .flatMap { fetchCurrencies(it) }
            .doOnSubscribe {
                if (state.value != StatefulLayout.CONTENT) state.value = StatefulLayout.PROGRESS
            }
            .subscribeOn(AndroidSchedulers.mainThread())
            .startWith(selectedCurrency)
            .toMap { it.symbol }
            .repeatWhen { pollingStrategy.getPollingMethod(it) }
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap { notifyCurrenciesUpdated(it).toFlowable() }
            .subscribeBy(
                onNext = { setupDisplayState() },
                onError = { handleError(it) }
            )
    }

    private fun handleError(error: Throwable) {
        Timber.e(error)
        state.value = when {
            !isOnline() -> StatefulLayout.OFFLINE
            else -> StatefulLayout.ERROR
        }
    }

    internal fun setupDisplayState() {
        state.value =
            if (currencies.value.isNullOrEmpty()) StatefulLayout.EMPTY else StatefulLayout.CONTENT
    }

    internal fun fetchCurrencies(useCache: Boolean = false) = currenciesRepository
        .fetchCurrencies(selectedCurrency.symbol, useCache)
        // We will only use cache the very first time. Polling won't use cache
        .doOnSuccess { this.useCache = false }
        .observeOn(Schedulers.computation())
        .flattenAsFlowable { it }
        .filter { isCurrencyAllowed(it.symbol) }

    internal fun isCurrencyAllowed(symbol: String) = allowedCurrencies
        .contains(symbol)

    internal fun loadSelectedCurrency() = if (currencies.value.isNullOrEmpty()) {
        currenciesRepository.fetchDefaultCurrency()
    } else {
        currencies.value!!.first()
    }

    fun reloadAllowedCurrencies() {
        allowedCurrencies = keepCurrenciesToLimit(loadAllowedCurrencies())

        useCache = true
        fetchData()
    }

    /**
     * NOTE: This method is added in order to avoid the glitch about focusing a currency
     * that is in the bottom. What will happen is that there will be a loop of focus, caused by
     * the behavior of the scrolling when the currency was focused.
     */
    fun keepCurrenciesToLimit(allowedCurrencies: AllowedCurrencies): AllowedCurrencies {
        if (allowedCurrencies.currencies.size > MAX_CURRENCIES_IN_LIST) {
            val lastCurrency = currencies.value?.lastOrNull() ?: return allowedCurrencies

            allowedCurrencies.currencies.remove(lastCurrency.symbol)
            currenciesRepository.saveAllowedCurrenciesToCache(allowedCurrencies)
        }
        return allowedCurrencies
    }

    fun onCurrencyDeleted(data: Currency) {
        allowedCurrencies.currencies.remove(data.symbol)
        currenciesRepository.saveAllowedCurrenciesToCache(allowedCurrencies)

        reloadAllowedCurrencies()
    }

    private fun loadAllowedCurrencies(): AllowedCurrencies = currenciesRepository
        .fetchAllowedCurrencies()

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
