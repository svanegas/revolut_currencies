package com.svanegas.revolut.currencies.entity

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

typealias CurrencyMap = HashMap<String, Double>

data class CurrencyResponse(
    @SerializedName("rates")
    val rates: CurrencyMap
)

open class Currency(
    @PrimaryKey var symbol: String = "",
    var ratio: Double = 1.0,
    var name: String = "",
    var baseAt: Date? = null,
    var amount: String = ""
) : RealmObject() {

    object Keys {
        const val SYMBOL = "symbol"
        const val RATIO = "ratio"
        const val NAME = "name"
        const val BASE_AT = "baseAt"
        const val AMOUNT = "amount"
    }
}