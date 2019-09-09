package com.svanegas.revolut.currencies.ui.search

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
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
import com.svanegas.revolut.currencies.databinding.FragmentCurrencySearchBinding
import com.svanegas.revolut.currencies.entity.Currency

interface CurrencySearchView : BaseView, SwipeRefreshState, PlaceholderErrorWithRetry {
    fun onCurrencyClick(currency: Currency)
}

class CurrencySearchFragment :
    BaseFragmentViewModel<CurrencySearchViewModel, FragmentCurrencySearchBinding>(),
    CurrencySearchView {

    companion object {
        const val SELECTED_CURRENCY_KEY = "selected_currency"

        fun newInstance() = CurrencySearchFragment()
    }

    private lateinit var currencySearchAdapter: CurrencySearchAdapter

    override fun setupViewModel() = findViewModel<CurrencySearchViewModel>()

    override fun inflateBindingLayout(inflater: LayoutInflater): FragmentCurrencySearchBinding =
        FragmentCurrencySearchBinding.inflate(inflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currencySearchAdapter = CurrencySearchAdapter(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.currencies.observe(viewLifecycleOwner, Observer {
            currencySearchAdapter.setCurrencyList(it)
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            requireActivity().onBackPressed()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onPullToRefresh() = viewModel.fetchData()

    override fun onErrorRetryClick() = viewModel.fetchData()

    private fun setupToolbar() {
        with(binding.toolbarLayout.findViewById<Toolbar>(R.id.toolbar)) {
            title = getString(R.string.currencies_search_title)
            (activity as AppCompatActivity).setSupportActionBar(this)
        }
        with((activity as AppCompatActivity).supportActionBar!!) {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
    }

    private fun setupRecyclerView() {
        binding.currenciesRecycler.setHasFixedSize(true)
        binding.currenciesRecycler.adapter = currencySearchAdapter
        binding.currenciesRecycler.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onCurrencyClick(currency: Currency) {
        viewModel.toggleAllowed(currency)

        val data = Intent().apply {
            putExtra(SELECTED_CURRENCY_KEY, currency.symbol)
        }

        requireActivity().apply {
            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }
}