package com.svanegas.revolut.currencies.ui

import android.content.Context
import androidx.databinding.BaseObservable
import com.svanegas.revolut.currencies.base.ui.adapter.TheSame
import com.svanegas.revolut.currencies.entity.Currency

open class CurrencyItemViewModel constructor(
    val content: Currency,
    val appContext: Context
) : BaseObservable(), TheSame {

    override fun isItemTheSame(other: Any): Boolean = when {
        this === other -> true
        other !is CurrencyItemViewModel -> false
        content.symbol == other.content.symbol -> true
        else -> false
    }

    override fun isContentTheSame(other: Any): Boolean = when {
        other !is CurrencyItemViewModel -> false
        content.value != other.content.value -> false
        else -> true
    }
}
