package com.svanegas.revolut.currencies.entity

import com.google.gson.annotations.SerializedName
import java.util.*

typealias CurrencyMap = HashMap<String, Double>
typealias CurrencyNames = HashMap<String, String>

data class CurrencyResponse(
    @SerializedName("rates")
    val rates: CurrencyMap
)

data class Currency(
    val symbol: String = "",
    var value: Double = 0.0,
    val name: String = "",
    val baseAt: Date? = null
)