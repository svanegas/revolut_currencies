package com.svanegas.revolut.currencies.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.svanegas.revolut.currencies.R
import com.svanegas.revolut.currencies.databinding.CurrencySearchItemBinding
import com.svanegas.revolut.currencies.entity.Currency

class CurrenciesSearchAdapter(
) : RecyclerView.Adapter<CurrenciesSearchAdapter.CurrencySearchItemViewHolder>() {

    private val currencies: MutableList<Currency> = mutableListOf()

    init {
        setHasStableIds(true)
    }

    fun setCurrencyList(currencyList: List<Currency>) {
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

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CurrencySearchItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: CurrencySearchItemBinding =
            DataBindingUtil.inflate(inflater, R.layout.currency_search_item, parent, false)
        return CurrencySearchItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CurrencySearchItemViewHolder, position: Int) {
        holder.binding.data = currencies[position]
        holder.binding.executePendingBindings()
    }

    override fun getItemCount(): Int = currencies.size

    override fun getItemId(position: Int): Long = currencies[position].symbol.hashCode().toLong()

    class CurrencySearchItemViewHolder(val binding: CurrencySearchItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}