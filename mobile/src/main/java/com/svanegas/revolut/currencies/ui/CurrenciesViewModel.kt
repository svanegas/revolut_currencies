package com.svanegas.revolut.currencies.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jakewharton.rxrelay2.BehaviorRelay
import com.svanegas.revolut.currencies.base.arch.BaseViewModel
import com.svanegas.revolut.currencies.base.utility.notifyChange
import com.svanegas.revolut.currencies.entity.Currency
import com.svanegas.revolut.currencies.repository.CurrenciesRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import java.text.NumberFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val TEXT_CHANGE_DEBOUNCE_DELAY_MILLIS = 100L
private const val DATA_POLL_DELAY_MILLIS = 1500L

class CurrenciesViewModel @Inject constructor(
    private val currenciesRepository: CurrenciesRepository
) : BaseViewModel() {

    private var selectedCurrency: Currency = getDefaultCurrency()

    // TODO: This would be to extend with some "Add currency" feature.
    private val allowedCurrencies = MutableLiveData(
        setOf(
            "EUR", "USD", "GBP", "CZK"
        )
    )

    private val _currencies = MutableLiveData<MutableMap<String, Currency>>(mutableMapOf())
    val currencies: LiveData<MutableMap<String, Currency>> = _currencies

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
            .observeOn(AndroidSchedulers.mainThread())
            .filter { it == selectedCurrency.symbol }
            .subscribe {
                Timber.d("CACA - Sym: $it")
                _currencies.notifyChange()
            }
    }

    fun setCurrencyAsBase(symbol: String) {
        val oldCurrency = currencies.value?.get(symbol) ?: return
        val updatedCurrency = oldCurrency.copy(baseAt = Date())
        selectedCurrency = updatedCurrency
        _currencies.value?.put(oldCurrency.symbol, updatedCurrency)
        _currencies.notifyChange()

        fetchData()
    }

    fun prepareCurrenciesToPopulate(currencies: List<Currency>): List<Currency> = currencies
        .sortedByDate()
        .convertRates()
        .toList()

    fun refreshAmounts(symbol: String) = textChangeRelay.accept(symbol)

    private fun fetchData() {
        compositeDisposable.clear()
        compositeDisposable += currenciesRepository
            .fetchCurrencies()
            .startWith(selectedCurrency)
            .toMap { it.symbol }
            .repeatWhen { it.delay(DATA_POLL_DELAY_MILLIS, TimeUnit.MILLISECONDS) }
            .subscribeBy(
                onNext = { _currencies.value = it },
                onError = { Timber.e(it) }
            )
    }

    private fun CurrenciesRepository.fetchCurrencies() = this
        .fetchCurrencies(selectedCurrency.symbol)
        .flattenAsFlowable { it.rates.entries }
        .filter { allowedCurrencies.value?.contains(it.key) ?: false }
        .map {
            // Not really good way how to keep the previous [baseAt] value
            val existing = _currencies.value?.get(it.key) ?: Currency()
            existing.copy(symbol = it.key, ratio = it.value)
        }
        .map { it.copy(name = currenciesRepository.fetchCurrencyName(it.symbol)) }

    // TODO: Define where to get this default currency from
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

