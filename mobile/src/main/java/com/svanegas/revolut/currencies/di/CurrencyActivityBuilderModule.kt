package com.svanegas.revolut.currencies.di

import androidx.lifecycle.ViewModel
import com.svanegas.revolut.currencies.base.di.ViewModelKey
import com.svanegas.revolut.currencies.ui.*
import com.svanegas.revolut.currencies.ui.list.CurrenciesFragment
import com.svanegas.revolut.currencies.ui.list.CurrenciesViewModel
import com.svanegas.revolut.currencies.ui.search.CurrencySearchFragment
import com.svanegas.revolut.currencies.ui.search.CurrencySearchViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
abstract class CurrencyActivityBuilderModule {

    @ContributesAndroidInjector
    abstract fun contributeCurrenciesActivity(): CurrenciesActivity

    @ContributesAndroidInjector
    abstract fun contributeCurrenciesFragment(): CurrenciesFragment

    @Binds
    @IntoMap
    @ViewModelKey(CurrenciesViewModel::class)
    abstract fun bindCurrenciesViewModel(currenciesViewModel: CurrenciesViewModel): ViewModel

    @ContributesAndroidInjector
    abstract fun contributeCurrencySarchFragment(): CurrencySearchFragment

    @Binds
    @IntoMap
    @ViewModelKey(CurrencySearchViewModel::class)
    abstract fun bindCurrencySearchViewModel(currencySearchViewModel: CurrencySearchViewModel): ViewModel
}