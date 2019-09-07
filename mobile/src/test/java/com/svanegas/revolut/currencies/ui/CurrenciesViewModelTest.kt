package com.svanegas.revolut.currencies.ui

import com.svanegas.revolut.currencies.base.arch.statefullayout.StatefulLayout
import com.svanegas.revolut.currencies.entity.Currency
import com.svanegas.revolut.currencies.entity.CurrencyResponse
import com.svanegas.revolut.currencies.polling.PollingStrategy
import com.svanegas.revolut.currencies.repository.CurrenciesRepository
import com.svanegas.revolut.currencies.testutils.InstantExecutorExtension
import com.svanegas.revolut.currencies.testutils.kotlinAny
import com.svanegas.revolut.currencies.testutils.setupViewModelTest
import com.svanegas.revolut.currencies.testutils.whenever
import io.reactivex.Flowable
import io.reactivex.Single
import net.bytebuddy.utility.RandomString
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import java.util.*

@ExtendWith(InstantExecutorExtension::class)
class CurrenciesViewModelTest {

    @Mock
    private lateinit var repository: CurrenciesRepository
    @Mock
    private lateinit var pollingStrategy: PollingStrategy

    private lateinit var viewModel: CurrenciesViewModel

    private val random = Random()
    private val randomString = RandomString()

    @BeforeEach
    fun setup() {
        setupViewModelTest(this)

        whenever(repository.fetchCurrencyName(anyString())).thenReturn("")
        whenever(repository.fetchCurrencies(anyString())).thenReturn(Single.never())

        viewModel = spy(CurrenciesViewModel(repository, pollingStrategy))
    }

    @Test
    fun currenciesList_initValue_returnsNull() {
        assertEquals(null, viewModel.currencies.value)
    }

    @Test
    fun initTextChangeRelay_whenIsSelectedCurrency_callsNotifyCurrenciesUpdated() {
        val symbol = randomString.nextString()
        viewModel.selectedCurrency = Currency(symbol = symbol)
        viewModel.textChangeRelay.accept(symbol)

        whenever(viewModel.notifyCurrenciesUpdated(kotlinAny())).thenReturn(Single.never())

        viewModel.initTextChangeRelay()
        verify(viewModel).notifyCurrenciesUpdated(null)
    }

    @Test
    fun initTextChangeRelay_whenIsNotSelectedCurrency_doesNotCallNotifyCurrenciesUpdated() {
        val symbol = randomString.nextString()
        viewModel.selectedCurrency = Currency(symbol = symbol)
        viewModel.textChangeRelay.accept("$symbol$symbol")

        whenever(viewModel.notifyCurrenciesUpdated(kotlinAny())).thenReturn(Single.never())

        viewModel.initTextChangeRelay()
        verify(viewModel, times(0)).notifyCurrenciesUpdated(null)
    }

    @Test
    fun setCurrencyAsBase_setsBaseAtDate() {
        val symbol = randomString.nextString()
        val currency = Currency(symbol = symbol, baseAt = null)
        viewModel.currenciesMap.value = mutableMapOf(symbol to currency)

        viewModel.setCurrencyAsBase(symbol)

        assertNotNull(viewModel.selectedCurrency.baseAt)
    }

    @Test
    fun setCurrencyAsBase_setsSelectedCurrency() {
        val symbol = randomString.nextString()
        val currency = Currency(symbol = symbol, baseAt = null)
        viewModel.currenciesMap.value = mutableMapOf(symbol to currency)

        viewModel.setCurrencyAsBase(symbol)

        assertEquals(symbol, viewModel.selectedCurrency.symbol)
    }

    @Test
    fun setCurrencyAsBase_putsUpdatedCurrencyIntoCurrenciesMap() {
        val symbol = randomString.nextString()
        val currency = Currency(symbol = symbol, baseAt = null)
        viewModel.currenciesMap.value = mutableMapOf(symbol to currency)

        viewModel.setCurrencyAsBase(symbol)

        val updatedCurrency = viewModel.currenciesMap.value!![symbol]!!
        assertNotNull(updatedCurrency.baseAt)
    }

    @Test
    fun setCurrencyAsBase_callsNotifyCurrenciesUpdated() {
        val symbol = randomString.nextString()
        viewModel.currenciesMap.value = mutableMapOf(symbol to Currency())

        viewModel.setCurrencyAsBase(symbol)

        verify(viewModel).notifyCurrenciesUpdated()
    }

    @Test
    fun setCurrencyAsBase_callsFetchData() {
        val symbol = randomString.nextString()
        viewModel.currenciesMap.value = mutableMapOf(symbol to Currency())

        viewModel.setCurrencyAsBase(symbol)

        verify(viewModel).fetchData()
    }

    @Test
    fun notifyCurrenciesUpdated_whenCurrenciesNotNull_setsCurrenciesToMap() {
        val currencies = mutableMapOf<String, Currency>(
            randomString.nextString() to Currency(),
            randomString.nextString() to Currency(),
            randomString.nextString() to Currency()
        )

        viewModel.notifyCurrenciesUpdated(currencies)

        assertEquals(currencies, viewModel.currenciesMap.value)
    }

    @Test
    fun notifyCurrenciesUpdated_whenCurrenciesNull_doesNotSetCurrenciesToMap() {
        val currencies = mutableMapOf<String, Currency>(
            randomString.nextString() to Currency(),
            randomString.nextString() to Currency(),
            randomString.nextString() to Currency()
        )
        viewModel.currenciesMap.value = currencies

        viewModel.notifyCurrenciesUpdated()

        assertEquals(currencies, viewModel.currenciesMap.value)
    }

    @Test
    fun notifyCurrenciesUpdated_callsGetListOfCurrenciesFromMap() {
        val currencies = mutableMapOf<String, Currency>(
            randomString.nextString() to Currency(),
            randomString.nextString() to Currency(),
            randomString.nextString() to Currency()
        )
        viewModel.currenciesMap.value = currencies

        viewModel.notifyCurrenciesUpdated()
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertOf { verify(viewModel).getListOfCurrenciesFromMap(currencies) }
    }

    @Test
    fun notifyCurrenciesUpdated_callsSortedByDate() {
        val currencies = listOf(Currency(), Currency(), Currency())
        viewModel.currenciesMap.value = mutableMapOf()
        whenever(viewModel.getListOfCurrenciesFromMap(mutableMapOf())).thenReturn(currencies)

        viewModel.notifyCurrenciesUpdated()
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertOf { verify(viewModel).sortedByDate(currencies) }
    }

    @Test
    fun notifyCurrenciesUpdated_callsConvertRates() {
        val currencies = listOf(Currency(), Currency(), Currency())
        viewModel.currenciesMap.value = mutableMapOf()
        whenever(viewModel.getListOfCurrenciesFromMap(mutableMapOf())).thenReturn(currencies)
        whenever(viewModel.sortedByDate(currencies)).thenReturn(currencies)

        viewModel.notifyCurrenciesUpdated()
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertOf { verify(viewModel).convertRates(currencies) }
    }

    @Test
    fun notifyCurrenciesUpdated_setsFinalValueToCurrencies() {
        val currencies = listOf(Currency(), Currency(), Currency())
        viewModel.currenciesMap.value = mutableMapOf()
        viewModel.currencies.value = listOf()
        whenever(viewModel.getListOfCurrenciesFromMap(mutableMapOf())).thenReturn(currencies)
        whenever(viewModel.sortedByDate(currencies)).thenReturn(currencies)
        whenever(viewModel.sortedByDate(currencies)).thenReturn(currencies)

        viewModel.notifyCurrenciesUpdated()
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValue { it == viewModel.currencies.value }
    }

    @Test
    fun fetchData_callsNotifyCurrenciesUpdated() {
        val currency = Currency(symbol = randomString.nextString())
        doReturn(Flowable.just(currency)).`when`(viewModel).fetchCurrencies()
        doReturn(Flowable.never<Any>()).`when`(pollingStrategy).getPollingMethod(kotlinAny())

        viewModel.fetchData()

        verify(viewModel).notifyCurrenciesUpdated(kotlinAny())
    }

    @Test
    fun fetchData_startsWithSelectedCurrency() {
        val currency = Currency(symbol = randomString.nextString())
        viewModel.selectedCurrency = currency
        doReturn(Flowable.empty<Currency>()).`when`(viewModel).fetchCurrencies()
        doReturn(Flowable.never<Any>()).`when`(pollingStrategy).getPollingMethod(kotlinAny())

        viewModel.fetchData()

        verify(viewModel).notifyCurrenciesUpdated(mutableMapOf(currency.symbol to currency))
    }

    @Test
    fun setupDisplayState_whenCurrenciesIsEmpty_setsStateAsEmpty() {
        viewModel.currencies.value = emptyList()

        viewModel.setupDisplayState()

        assertEquals(StatefulLayout.EMPTY, viewModel.state.value)
    }

    @Test
    fun fetchCurrencies_callsFetchCurrenciesWithSelectedSymbol() {
        val currency = Currency(symbol = randomString.nextString())
        viewModel.selectedCurrency = currency
        whenever(repository.fetchCurrencies(anyString())).thenReturn(Single.never())

        viewModel.fetchCurrencies()

        verify(repository).fetchCurrencies(currency.symbol)
    }

    @Test
    fun fetchCurrencies_emitsOnlyAllowedCurrencies() {
        val allowedCurrency = Currency(symbol = randomString.nextString())
        val notAllowedCurrency = Currency(symbol = randomString.nextString())

        val response = CurrencyResponse(
            hashMapOf(
                allowedCurrency.symbol to random.nextDouble(),
                notAllowedCurrency.symbol to random.nextDouble()
            )
        )
        whenever(repository.fetchCurrencies(anyString())).thenReturn(Single.just(response))
        whenever(viewModel.isCurrencyAllowed(allowedCurrency.symbol)).thenReturn(true)
        whenever(viewModel.isCurrencyAllowed(notAllowedCurrency.symbol)).thenReturn(false)

        viewModel.fetchCurrencies()
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValue { it.symbol == allowedCurrency.symbol }
    }

    @Test
    fun fetchCurrencies_whenCurrencyExists_keepsTheBaseAtDate() {
        val existingCurrency = Currency(
            symbol = randomString.nextString(),
            ratio = random.nextDouble(),
            baseAt = Date()
        )
        viewModel.currenciesMap.value = mutableMapOf(existingCurrency.symbol to existingCurrency)

        val newRatio = existingCurrency.ratio * 2
        val newCurrency = mutableMapOf(
            existingCurrency.symbol to newRatio
        )

        with(viewModel.getCurrencyWithExistingBaseAt(newCurrency.entries.first())) {
            assertEquals(this.symbol, existingCurrency.symbol)
            assertEquals(this.baseAt, existingCurrency.baseAt)
            assertEquals(this.ratio, newRatio)
        }
    }

    @Test
    fun fetchCurrencies_whenCurrencyDoesNotExist_doesNotKeepTheBaseAtDate() {
        viewModel.currenciesMap.value = mutableMapOf()

        val currency = mutableMapOf(
            randomString.nextString() to random.nextDouble()
        )

        with(viewModel.getCurrencyWithExistingBaseAt(currency.entries.first())) {
            assertNull(this.baseAt)
        }
    }

    @Test
    fun isCurrencyAllowed_whenAllowed_returnsTrue() {
        val symbol = randomString.nextString()
        viewModel.allowedCurrencies.value = setOf(symbol)

        assertTrue(viewModel.isCurrencyAllowed(symbol))
    }

    @Test
    fun isCurrencyAllowed_whenNotAllowed_returnsFalse() {
        val symbol = randomString.nextString()
        viewModel.allowedCurrencies.value = setOf()

        assertFalse(viewModel.isCurrencyAllowed(symbol))
    }

    @Test
    fun isCurrencyAllowed_whenAllowedCurrenciesAreNull_returnsFalse() {
        val symbol = randomString.nextString()
        viewModel.allowedCurrencies.value = null

        assertFalse(viewModel.isCurrencyAllowed(symbol))
    }

    @Test
    fun convertRates_whenEmptyCurrencies_returnsEmpty() {
        assertEquals(emptyList<Currency>(), viewModel.convertRates(emptyList()))
    }

    @Test
    fun convertRates_whenOnlyOneValue_doesNotCallConvertValue() {
        viewModel.convertRates(listOf(Currency()))

        verify(viewModel, times(0)).convertValue(anyString(), anyDouble())
    }

    @Test
    fun convertRates_whenMoreThanOneValue_callsConvertValue() {
        val source = Currency(amount = random.nextDouble().toString())
        val target = Currency(ratio = random.nextDouble())

        viewModel.convertRates(listOf(source, target))

        verify(viewModel).convertValue(source.amount, target.ratio)
    }

    @Test
    fun convertRates_whenMoreThanOneValue_modifiesAmount() {
        val newAmount = random.nextDouble().toString()
        whenever(viewModel.convertValue(anyString(), anyDouble())).thenReturn(newAmount)

        with(viewModel.convertRates(listOf(Currency(), Currency()))) {
            assertEquals(this[1].amount, newAmount)
        }
    }
}