package com.svanegas.revolut.currencies.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.svanegas.revolut.currencies.R
import com.svanegas.revolut.currencies.base.arch.BaseFragment
import com.svanegas.revolut.currencies.base.arch.BaseView

interface CurrenciesView : BaseView

class CurrenciesFragment : BaseFragment() {

    companion object {
        fun newInstance() = CurrenciesFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_currencies, container, false)
}