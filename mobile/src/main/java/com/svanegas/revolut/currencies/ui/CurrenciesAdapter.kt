package com.svanegas.revolut.currencies.ui

import androidx.databinding.ViewDataBinding
import com.svanegas.revolut.currencies.R
import com.svanegas.revolut.currencies.base.ui.adapter.BaseDataBoundListAdapter
import org.alfonz.adapter.AdapterView
import org.alfonz.adapter.BR
import org.alfonz.adapter.BaseDataBoundRecyclerViewHolder

interface CurrencyItemView : AdapterView {
    fun onCurrencyClick(currency: CurrencyItemViewModel)
}

class CurrenciesAdapter(
    view: CurrencyItemView,
    val viewModel: CurrenciesViewModel
) : BaseDataBoundListAdapter<CurrencyItemViewModel>(view) {
    init {
        setHasStableIds(true)
    }

    override fun getItemLayoutId(position: Int): Int = R.layout.currency_item

    override fun bindItem(
        holder: BaseDataBoundRecyclerViewHolder<ViewDataBinding>,
        position: Int,
        payloads: MutableList<Any>?
    ) {
        super.bindItem(holder, position, payloads)
        holder.binding.setVariable(BR.viewModel, viewModel)
    }

    override fun getItemId(position: Int): Long = with(getItem(position)) {
        return content.symbol.hashCode().toLong()
    }
}