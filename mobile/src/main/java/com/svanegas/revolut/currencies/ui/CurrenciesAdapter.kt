package com.svanegas.revolut.currencies.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.svanegas.revolut.currencies.R
import com.svanegas.revolut.currencies.base.ui.CustomTextWatcher
import com.svanegas.revolut.currencies.databinding.AddCurrencyItemBinding
import com.svanegas.revolut.currencies.databinding.CurrencyItemBinding
import com.svanegas.revolut.currencies.entity.AddCurrencyItem
import com.svanegas.revolut.currencies.entity.Currency
import com.svanegas.revolut.currencies.entity.CurrencyItem


interface CurrencyInteractionCallback {
    fun getOnFocusChangeListener(): View.OnFocusChangeListener
    fun onCurrencyClick(symbol: String, view: View)
    fun onTextChanged(symbol: String)
    fun onAddCurrencyClick()
}

class CurrenciesAdapter(
    private val interactionCallback: CurrencyInteractionCallback
) : RecyclerView.Adapter<CurrenciesAdapter.CurrencyItemViewHolder>() {

    private val currencies: MutableList<CurrencyItem> = mutableListOf()

    init {
        setHasStableIds(true)
    }

    fun setCurrencyList(currencyList: List<CurrencyItem>) {
        val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = currencies.size

            override fun getNewListSize(): Int = currencyList.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                currencies[oldItemPosition].isItemTheSame(currencyList[newItemPosition])

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                currencies[oldItemPosition].isContentTheSame(currencyList[newItemPosition])
        })

        currencies.apply {
            clear()
            addAll(currencyList)
        }
        result.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyItemViewHolder =
        when (viewType) {
            R.layout.add_currency_item -> createHolder<AddCurrencyItemBinding>(parent, viewType)
            R.layout.currency_item -> createHolder<CurrencyItemBinding>(parent, viewType)
            else -> throw IllegalStateException("The $viewType is not supported, make sure you specify it in getItemViewType()")
        }

    private fun <T : ViewDataBinding> createHolder(
        parent: ViewGroup,
        @LayoutRes layoutId: Int
    ): CurrencyItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: T = DataBindingUtil.inflate(inflater, layoutId, parent, false)
        return CurrencyItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CurrencyItemViewHolder, position: Int) {
        when (val item = currencies[position]) {
            is AddCurrencyItem -> {
                holder.binding as AddCurrencyItemBinding
                holder.binding.callback = interactionCallback
            }
            is Currency -> {
                holder.binding as CurrencyItemBinding
                holder.binding.callback = interactionCallback
                holder.binding.data = item
                holder.binding.convertInput.tag = item.symbol
                holder.binding.convertInput.addTextChangedListener(getTextWatcher(item.symbol))
            }
        }
        holder.binding.executePendingBindings()
    }

    override fun getItemCount(): Int = currencies.size

    override fun getItemId(position: Int): Long = when (val item = currencies[position]) {
        is AddCurrencyItem -> -1
        is Currency -> item.symbol.hashCode().toLong()
        else -> throw IllegalStateException("Unknown CurrencyItem type")
    }

    override fun getItemViewType(position: Int) = when (currencies[position]) {
        is AddCurrencyItem -> R.layout.add_currency_item
        is Currency -> R.layout.currency_item
        else -> throw IllegalStateException("Unknown CurrencyItem type")
    }

    // This is very ugly, but I can't come up with a better solution </3
    private fun getTextWatcher(symbol: String) = object : CustomTextWatcher() {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            interactionCallback.onTextChanged(symbol)
        }
    }

    class CurrencyItemViewHolder(val binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root)
}