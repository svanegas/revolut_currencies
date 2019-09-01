package com.svanegas.revolut.currencies.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.svanegas.revolut.currencies.R
import com.svanegas.revolut.currencies.databinding.NewCurrencyItemBinding
import com.svanegas.revolut.currencies.entity.Currency

interface CurrencyInteractionCallback {
    fun getOnFocusChangeListener(): View.OnFocusChangeListener
}

class NewCurrenciesAdapter(
    private val interactionCallback: CurrencyInteractionCallback
) : RecyclerView.Adapter<NewCurrenciesAdapter.CurrencyViewHolder>() {

    private var currencies: List<Currency> = emptyList()

    init {
        setHasStableIds(true)
    }

    fun setCurrencyList(currencyList: List<Currency>) {
        val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = currencies.size

            override fun getNewListSize(): Int = currencyList.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                currencies[oldItemPosition].symbol == currencyList[newItemPosition].symbol

            override fun areContentsTheSame(
                oldItemPosition: Int,
                newItemPosition: Int
            ): Boolean {
                val oldCurrency = currencies[newItemPosition]
                val newCurrency = currencyList[newItemPosition]
                return when {
                    newCurrency.symbol != oldCurrency.symbol -> false
                    newCurrency.value != oldCurrency.value -> false
                    else -> true
                }
            }
        })
        currencies = currencyList
        result.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyViewHolder {
        val binding = DataBindingUtil
            .inflate<NewCurrencyItemBinding>(
                LayoutInflater.from(parent.context),
                R.layout.new_currency_item,
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
        }
        holder.binding.executePendingBindings()
    }

    override fun getItemCount(): Int = currencies.size

    override fun getItemId(position: Int): Long = currencies[position].symbol.hashCode().toLong()

    class CurrencyViewHolder(val binding: NewCurrencyItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}