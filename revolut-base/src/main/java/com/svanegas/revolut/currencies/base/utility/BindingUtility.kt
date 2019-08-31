package com.svanegas.revolut.currencies.base.utility

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

enum class RecyclerAnimator {
    DEFAULT
}

@BindingAdapter("app:recyclerAnimator")
fun setRecyclerAnimator(recyclerView: RecyclerView, recyclerAnimator: RecyclerAnimator) {
    val itemAnimator: RecyclerView.ItemAnimator

    if (recyclerAnimator == RecyclerAnimator.DEFAULT) {
        itemAnimator = DefaultItemAnimator()
    } else {
        throw IllegalArgumentException()
    }

    recyclerView.itemAnimator = itemAnimator
}