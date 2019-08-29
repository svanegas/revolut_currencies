package com.svanegas.revolut.currencies

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.svanegas.revolut.currencies.base.arch.BaseFragment

class CurrenciesFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_currencies, container, false)
}