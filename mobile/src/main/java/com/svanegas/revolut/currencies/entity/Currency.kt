package com.svanegas.revolut.currencies.entity

import com.google.gson.annotations.SerializedName
import java.util.*

typealias CurrencyMap = HashMap<String, Double>

data class CurrencyResponse(
    @SerializedName("rates")
    val rates: CurrencyMap
)


sealed class CurrencyItem : TheSame {
    override fun isContentTheSame(other: Any) = this === other
    override fun isItemTheSame(other: Any) = this === other
}

data class Currency(
    val symbol: String = "",
    var ratio: Double = 1.0,
    val name: String = "",
    val baseAt: Date? = null,
    var amount: String = ""
) : CurrencyItem() {

    override fun isItemTheSame(other: Any): Boolean = when {
        this === other -> true
        other !is Currency -> false
        symbol == other.symbol -> true
        else -> false
    }

    override fun isContentTheSame(other: Any): Boolean = when {
        other !is Currency -> false
        symbol != other.symbol -> false
        ratio != other.ratio -> false
        name != other.name -> false
        amount != other.amount -> false
        else -> true
    }
}

object AddCurrencyItem : CurrencyItem()