package com.svanegas.revolut.currencies.entity

import com.svanegas.revolut.currencies.base.OpenForMocking
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

@OpenForMocking
open class AllowedCurrencies(
    @PrimaryKey
    var key: Long = 0,
    var currencies: RealmList<String> = RealmList()
) : RealmObject() {

    object Keys {
        const val KEY = "key"
        const val CURRENCIES = "currencies"
    }

    fun contains(symbol: String): Boolean = currencies.contains(symbol)
}