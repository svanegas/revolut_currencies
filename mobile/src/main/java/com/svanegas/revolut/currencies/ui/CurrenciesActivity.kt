package com.svanegas.revolut.currencies.ui

import android.os.Bundle
import com.svanegas.revolut.currencies.R
import com.svanegas.revolut.currencies.base.arch.BaseActivity

class CurrenciesActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.container_fragment, CurrenciesFragment.newInstance())
            commit()
        }
    }
}
