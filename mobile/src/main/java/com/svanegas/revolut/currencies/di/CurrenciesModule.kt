package com.svanegas.revolut.currencies.di

import androidx.lifecycle.ViewModel
import com.svanegas.revolut.currencies.base.di.ViewModelKey
import com.svanegas.revolut.currencies.ui.CurrenciesActivity
import com.svanegas.revolut.currencies.ui.CurrenciesFragment
import com.svanegas.revolut.currencies.ui.CurrenciesViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
abstract class CurrenciesModule {

    @ContributesAndroidInjector
    abstract fun contributeCurrenciesActivity(): CurrenciesActivity

    @ContributesAndroidInjector
    abstract fun contributeCurrenciesFragment(): CurrenciesFragment

    @Binds
    @IntoMap
    @ViewModelKey(CurrenciesViewModel::class)
    abstract fun bindCurrenciesViewModel(currenciesViewModel: CurrenciesViewModel): ViewModel
}