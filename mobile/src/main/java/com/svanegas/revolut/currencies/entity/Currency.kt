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

interface CurrencyItem : TheSame {
    override fun isContentTheSame(other: Any) = this === other
    override fun isItemTheSame(other: Any) = this === other
}

open class Currency(
    @PrimaryKey var symbol: String = "",
    var ratio: Double = 1.0,
    var name: String = "",
    var baseAt: Date? = null,
    var amount: String = ""
) : RealmObject(), CurrencyItem {

    object Keys {
        const val SYMBOL = "symbol"
        const val RATIO = "ratio"
        const val NAME = "name"
        const val BASE_AT = "baseAt"
        const val AMOUNT = "amount"
    }

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

object AddCurrencyItem : CurrencyItem
