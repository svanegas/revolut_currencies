package com.svanegas.revolut.currencies.ui.search

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
import com.svanegas.revolut.currencies.databinding.FragmentCurrencySearchBinding

interface CurrencySearchView : BaseView, SwipeRefreshState, PlaceholderErrorWithRetry

class CurrencySearchFragment :
    BaseFragmentViewModel<CurrencySearchViewModel, FragmentCurrencySearchBinding>(),
    CurrencySearchView {

    companion object {
        fun newInstance() = CurrencySearchFragment()
    }

    private lateinit var currenciesSearchAdapter: CurrenciesSearchAdapter

    override fun setupViewModel() = findViewModel<CurrencySearchViewModel>()

    override fun inflateBindingLayout(inflater: LayoutInflater): FragmentCurrencySearchBinding =
        FragmentCurrencySearchBinding.inflate(inflater)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currenciesSearchAdapter = CurrenciesSearchAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
    }

    override fun onPullToRefresh() {

    }

    override fun onErrorRetryClick() {

    }

    private fun setupToolbar() {
        val toolbar: Toolbar = binding.toolbarLayout.findViewById(R.id.toolbar)
        toolbar.title = getString(R.string.currencies_search_title)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
    }

    private fun setupRecyclerView() {
        binding.currenciesRecycler.setHasFixedSize(true)
        binding.currenciesRecycler.adapter = currenciesSearchAdapter
        binding.currenciesRecycler.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.currencies.observe(viewLifecycleOwner, Observer {
            currenciesSearchAdapter.setCurrencyList(it)
        })
    }
}