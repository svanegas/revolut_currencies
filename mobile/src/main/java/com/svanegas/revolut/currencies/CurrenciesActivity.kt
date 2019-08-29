package com.svanegas.revolut.currencies

import android.os.Bundle
import com.svanegas.revolut.currencies.base.arch.BaseActivity

class CurrenciesActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_currencies)
    }
}
