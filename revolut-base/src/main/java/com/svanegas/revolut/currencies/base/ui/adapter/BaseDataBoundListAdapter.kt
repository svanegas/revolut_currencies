package com.svanegas.revolut.currencies.base.ui.adapter

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import org.alfonz.adapter.AdapterView
import org.alfonz.adapter.BaseDataBoundRecyclerAdapter
import org.alfonz.adapter.BaseDataBoundRecyclerViewHolder

/**
 * ListAdapter which calculates diff on background thread and applies it on main thread.
 * Is able to be used with data binding.
 *
 * @param view to be bound into items
 * @param initialData this items will be shown the first time adapter is created
 * @param diffItemCallback diff item callback, may be overriden for custom logic
 */
abstract class BaseDataBoundListAdapter<T : TheSame>(
    private val view: AdapterView,
    initialData: List<T>? = null,
    diffItemCallback: DiffUtil.ItemCallback<T> = DefaultDiffItemCallback<T>()
) : BaseDataBoundRecyclerAdapter<ViewDataBinding>() {

    private val helper = AsyncListDiffer<T>(this, diffItemCallback)

    init {
        initialData?.let { submitList(it) }
    }

    override fun bindItem(
        holder: BaseDataBoundRecyclerViewHolder<ViewDataBinding>,
        position: Int,
        payloads: MutableList<Any>?
    ) {
        holder.binding.setVariable(org.alfonz.adapter.BR.view, view)
        holder.binding.setVariable(org.alfonz.adapter.BR.data, getItem(position))
    }

    fun submitList(list: List<T>?) = helper.submitList(list)

    fun getItem(position: Int): T = helper.currentList[position]

    override fun getItemCount(): Int = helper.currentList.size

}

/**
 * Default implementation of diff item callback.
 * Using objects implementing [TheSame], so basically just calls the functions
 */
open class DefaultDiffItemCallback<T : TheSame> : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(left: T, right: T): Boolean = left.isItemTheSame(right)
    override fun areContentsTheSame(left: T, right: T): Boolean = left.isContentTheSame(right)
}

interface TheSame {
    fun isItemTheSame(other: Any): Boolean
    fun isContentTheSame(other: Any): Boolean
}