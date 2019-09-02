package com.svanegas.revolut.currencies.entity

import com.google.gson.annotations.SerializedName
import java.util.*

typealias CurrencyMap = HashMap<String, Double>

data class CurrencyResponse(
    @SerializedName("rates")
    val rates: CurrencyMap
)

data class Currency(
    val symbol: String = "",
    var ratio: Double = 1.0,
    val name: String = "",
    val baseAt: Date? = null,
    var amount: String = ""
)