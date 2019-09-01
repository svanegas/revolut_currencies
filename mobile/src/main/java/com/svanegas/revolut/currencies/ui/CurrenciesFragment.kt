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

interface CurrenciesView : BaseView, CurrencyItemView

class CurrenciesFragment : BaseFragmentViewModel<CurrenciesViewModel, FragmentCurrenciesBinding>(),
    CurrenciesView, CurrencyInteractionCallback {

    companion object {
        fun newInstance() = CurrenciesFragment()
    }

    //    private lateinit var currenciesAdapter: CurrenciesAdapter
    private lateinit var newCurrenciesAdapter: NewCurrenciesAdapter

    override fun setupViewModel() = findViewModel<CurrenciesViewModel>()

    override fun inflateBindingLayout(inflater: LayoutInflater): FragmentCurrenciesBinding =
        FragmentCurrenciesBinding.inflate(inflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        currenciesAdapter = CurrenciesAdapter(this, viewModel)
        newCurrenciesAdapter = NewCurrenciesAdapter(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        binding.currenciesRecycler.adapter = currenciesAdapter
        binding.currenciesRecycler.adapter = newCurrenciesAdapter
        binding.currenciesRecycler.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.currencies.observe(viewLifecycleOwner, Observer {
            // TODO: Is doubled maybe because initially is emptyList
            Timber.d("CACA - Venga")
            val sortedCurrencies = viewModel.sortCurrenciesByDate(it.values.toList())
            newCurrenciesAdapter.setCurrencyList(sortedCurrencies)
//            newCurrenciesAdapter.submitList(sortedCurrencies)
        })
    }

    override fun getOnFocusChangeListener() = View.OnFocusChangeListener { view, isFocused ->
        if (isFocused) Timber.d("CACA - isFocused: $isFocused | tag: ${view.tag}")
    }

    override fun onCurrencyClick(currency: CurrencyItemViewModel) {
//        viewModel.updateCurrencyBaseAtDate(currency)
    }
}