package com.svanegas.revolut.currencies.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.svanegas.revolut.currencies.base.arch.BaseFragmentViewModel
import com.svanegas.revolut.currencies.base.arch.BaseView
import com.svanegas.revolut.currencies.databinding.FragmentCurrenciesBinding
import timber.log.Timber

interface CurrenciesView : BaseView

class CurrenciesFragment : BaseFragmentViewModel<CurrenciesViewModel, FragmentCurrenciesBinding>(),
    CurrenciesView, CurrencyInteractionCallback {

    companion object {
        fun newInstance() = CurrenciesFragment()
    }

    private lateinit var newCurrenciesAdapter: NewCurrenciesAdapter
    private var lastFocusedSymbol = ""

    override fun setupViewModel() = findViewModel<CurrenciesViewModel>()

    override fun inflateBindingLayout(inflater: LayoutInflater): FragmentCurrenciesBinding =
        FragmentCurrenciesBinding.inflate(inflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        newCurrenciesAdapter = NewCurrenciesAdapter(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.currenciesRecycler.adapter = newCurrenciesAdapter
        binding.currenciesRecycler.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.currencies.observe(viewLifecycleOwner, Observer {
            // TODO: Is doubled maybe because initially is emptyList
            Timber.d("CACA - Venga")
            val currencies = viewModel.prepareCurrenciesToPopulate(it.values.toList())
            newCurrenciesAdapter.setCurrencyList(currencies)
        })
    }

    override fun getOnFocusChangeListener() = View.OnFocusChangeListener { view, isFocused ->
        if (isFocused) {
            val symbol = view.tag?.toString()
            if (symbol != null && symbol != lastFocusedSymbol) {
                Timber.d("CACA - Setting as base with $symbol")
                lastFocusedSymbol = symbol
                viewModel.setCurrencyAsBase(symbol)
            }
        }
    }

    override fun onTextChanged(symbol: String) {
        viewModel.refreshValues(symbol)
    }
}