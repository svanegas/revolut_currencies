package com.svanegas.revolut.currencies.entity

import com.google.gson.annotations.SerializedName

typealias CurrencyMap = HashMap<String, Double>

data class CurrencyResponse(
    @SerializedName("rates")
    val rates: CurrencyMap
)

data class Currency(
    val symbol: String,
    val value: Double
)