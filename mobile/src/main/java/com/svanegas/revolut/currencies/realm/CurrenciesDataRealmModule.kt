package com.svanegas.revolut.currencies.realm

import com.svanegas.revolut.currencies.entity.AllowedCurrencies
import com.svanegas.revolut.currencies.entity.Currency
import io.realm.annotations.RealmModule

@RealmModule(library = true, classes = [Currency::class, AllowedCurrencies::class])
class CurrenciesDataRealmModule