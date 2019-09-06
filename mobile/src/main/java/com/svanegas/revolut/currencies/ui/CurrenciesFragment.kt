package com.svanegas.revolut.currencies.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.svanegas.revolut.currencies.R
import com.svanegas.revolut.currencies.base.arch.BaseFragmentViewModel
import com.svanegas.revolut.currencies.base.arch.BaseView
import com.svanegas.revolut.currencies.base.arch.statefullayout.PlaceholderErrorWithRetry
import com.svanegas.revolut.currencies.base.arch.statefullayout.SwipeRefreshState
import com.svanegas.revolut.currencies.databinding.FragmentCurrenciesBinding

interface CurrenciesView : BaseView, SwipeRefreshState, PlaceholderErrorWithRetry

class CurrenciesFragment : BaseFragmentViewModel<CurrenciesViewModel, FragmentCurrenciesBinding>(),
    CurrenciesView, CurrencyInteractionCallback {

    companion object {
        fun newInstance() = CurrenciesFragment()
    }

    private lateinit var currenciesAdapter: CurrenciesAdapter
    private var lastFocusedSymbol = ""

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
        binding.currenciesRecycler.adapter = currenciesAdapter
        binding.currenciesRecycler.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onPullToRefresh() {
        viewModel.fetchData()
        viewModel.swipeRefreshing.value = false // Doesn't update UI for unknown reason
    }

    override fun onErrorRetryClick() = viewModel.fetchData()

    private fun setupToolbar() {
        val toolbar: Toolbar = binding.toolbarLayout.findViewById(R.id.toolbar)
        toolbar.title = getString(R.string.currencies_list_title)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.currencies.observe(viewLifecycleOwner, Observer {
            currenciesAdapter.setCurrencyList(it)
            // DiffUtil is not working as expected, this is very sad.
            currenciesAdapter.notifyDataSetChanged()
        })
    }

    override fun getOnFocusChangeListener() = View.OnFocusChangeListener { view, isFocused ->
        if (isFocused) {
            val symbol = view.tag?.toString()
            if (symbol != null && symbol != lastFocusedSymbol) {
                lastFocusedSymbol = symbol
                viewModel.setCurrencyAsBase(symbol)
            }
        }
    }

    override fun onTextChanged(symbol: String) {
        viewModel.refreshAmounts(symbol)
    }
}