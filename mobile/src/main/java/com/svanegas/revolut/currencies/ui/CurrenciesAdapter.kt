package com.svanegas.revolut.currencies.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.svanegas.revolut.currencies.R
import com.svanegas.revolut.currencies.base.ui.CustomTextWatcher
import com.svanegas.revolut.currencies.databinding.CurrencyItemBinding
import com.svanegas.revolut.currencies.entity.Currency


interface CurrencyInteractionCallback {
    fun getOnFocusChangeListener(): View.OnFocusChangeListener
    fun onTextChanged(symbol: String)
}

class CurrenciesAdapter(
    private val interactionCallback: CurrencyInteractionCallback
) : RecyclerView.Adapter<CurrenciesAdapter.CurrencyViewHolder>() {

    private val currencies: MutableList<Currency> = mutableListOf()

    init {
        setHasStableIds(true)
    }

    fun setCurrencyList(currencyList: List<Currency>) {
        val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = currencies.size

            override fun getNewListSize(): Int = currencyList.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                currencies[oldItemPosition].symbol === currencyList[newItemPosition].symbol

            override fun areContentsTheSame(
                oldItemPosition: Int,
                newItemPosition: Int
            ): Boolean {
                val oldCurrency = currencies[oldItemPosition]
                val newCurrency = currencyList[newItemPosition]
                return when {
                    newCurrency.symbol != oldCurrency.symbol -> false
                    newCurrency.ratio != oldCurrency.ratio -> false
                    newCurrency.name != oldCurrency.name -> false
                    newCurrency.amount != oldCurrency.amount -> false
                    else -> true
                }
            }
        })
        currencies.apply {
            clear()
            addAll(currencyList)
        }
        result.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyViewHolder {
        val binding = DataBindingUtil
            .inflate<CurrencyItemBinding>(
                LayoutInflater.from(parent.context),
                R.layout.currency_item,
                parent,
                false
            )
        binding.callback = interactionCallback
        return CurrencyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CurrencyViewHolder, position: Int) {
        with(currencies[position]) {
            holder.binding.data = this
            holder.binding.convertInput.tag = this.symbol
            holder.binding.convertInput.addTextChangedListener(getTextWatcher(this.symbol))
        }
        holder.binding.executePendingBindings()
    }

    override fun getItemCount(): Int = currencies.size

    override fun getItemId(position: Int): Long = currencies[position].symbol.hashCode().toLong()

    // This is very ugly, but I can't come up with a better solution </3
    private fun getTextWatcher(symbol: String) = object : CustomTextWatcher() {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            interactionCallback.onTextChanged(symbol)
        }
    }

    class CurrencyViewHolder(val binding: CurrencyItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}