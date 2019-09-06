package com.svanegas.revolut.currencies.base.utility

import android.view.View
import android.widget.EditText
import androidx.databinding.BindingAdapter
import androidx.databinding.BindingConversion
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.svanegas.revolut.currencies.base.R


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

@BindingAdapter("app:onFocusChange")
fun setOnFocusChange(text: EditText, listener: View.OnFocusChangeListener) {
    text.onFocusChangeListener = listener
}

@BindingAdapter("app:refreshColors")
fun SwipeRefreshLayout.setRefreshColors(
    previousSetColors: Boolean,
    setColors: Boolean
) {
    if (setColors == previousSetColors) return // optimize setting it only once
    val colors = resources.getIntArray(R.array.swipe_refresh)
    setColorSchemeColors(*colors)
}

/**
 * Conversion for Visibility - we can pass boolean as parameter in visible property
 */
@BindingConversion
fun convertBooleanToVisibility(visible: Boolean): Int {
    return if (visible) View.VISIBLE else View.GONE
}