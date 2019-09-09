package com.svanegas.revolut.currencies.ui.list

import android.app.Activity
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.svanegas.revolut.currencies.R
import com.svanegas.revolut.currencies.base.arch.BaseFragmentViewModel
import com.svanegas.revolut.currencies.base.arch.BaseView
import com.svanegas.revolut.currencies.base.arch.statefullayout.PlaceholderErrorWithRetry
import com.svanegas.revolut.currencies.base.arch.statefullayout.SwipeRefreshState
import com.svanegas.revolut.currencies.databinding.CurrencyItemBinding
import com.svanegas.revolut.currencies.databinding.FragmentCurrenciesBinding
import com.svanegas.revolut.currencies.entity.AddCurrencyItem
import com.svanegas.revolut.currencies.ui.search.CurrencySearchActivity

interface CurrenciesView : BaseView, SwipeRefreshState, PlaceholderErrorWithRetry

class CurrenciesFragment : BaseFragmentViewModel<CurrenciesViewModel, FragmentCurrenciesBinding>(),
    CurrenciesView, CurrencyInteractionCallback {

    companion object {
        const val ADD_CURRENCY_REQUEST_CODE = 11

        fun newInstance() = CurrenciesFragment()
    }

    private lateinit var currenciesAdapter: CurrenciesAdapter

    override fun setupViewModel() = findViewModel<CurrenciesViewModel>()

    override fun inflateBindingLayout(inflater: LayoutInflater): FragmentCurrenciesBinding =
        FragmentCurrenciesBinding.inflate(inflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currenciesAdapter = CurrenciesAdapter(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
    }

    override fun onPullToRefresh() {
        viewModel.fetchData()
        viewModel.swipeRefreshing.value = false // Doesn't update UI for unknown reason
    }

    override fun onErrorRetryClick() = viewModel.fetchData()

    private fun setupToolbar() {
        val toolbar: Toolbar = binding.appToolbarContainer.findViewById(R.id.toolbar)
        toolbar.title = getString(R.string.currencies_list_title)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
    }

    private fun setupRecyclerView() {
        binding.currenciesRecycler.setHasFixedSize(true)
        binding.currenciesRecycler.adapter = currenciesAdapter
        binding.currenciesRecycler.layoutManager = LinearLayoutManager(requireContext())

        val swipeHandler = object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                viewHolder as CurrenciesAdapter.CurrencyItemViewHolder
                viewHolder.binding as CurrencyItemBinding
                viewModel.onCurrencyDeleted(viewHolder.binding.data ?: return)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.currenciesRecycler)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.currencies.observe(viewLifecycleOwner, Observer {
            currenciesAdapter.setCurrencyList(listOf(AddCurrencyItem) + it)
            // DiffUtil is not working as expected, this is very sad.
            currenciesAdapter.notifyDataSetChanged()
        })
    }

    override fun getOnFocusChangeListener() = View.OnFocusChangeListener { view, isFocused ->
        if (isFocused) {
            view.tag?.toString()?.let {
                viewModel.refreshFocusedCurrency(it)
            }
        }
    }

    override fun onTextChanged(symbol: String) = viewModel.refreshAmounts(symbol)

    override fun onCurrencyClick(symbol: String, view: View) {
        // We focus the EditText, so it will trigger the focusChangeListener
        (view.focusSearch(View.FOCUS_RIGHT) as? EditText)?.let {
            it.requestFocus()
            it.setSelection(it.text.count())
            val imm = view.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(it, SHOW_IMPLICIT)
        }
    }

    override fun onAddCurrencyClick() {
        startActivityForResult(
            Intent(requireContext(), CurrencySearchActivity::class.java),
            ADD_CURRENCY_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ADD_CURRENCY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            viewModel.reloadAllowedCurrencies()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}