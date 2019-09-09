package com.svanegas.revolut.currencies.ui.list

import android.os.Bundle
import com.svanegas.revolut.currencies.R
import com.svanegas.revolut.currencies.base.arch.BaseActivity

class CurrenciesActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.container_fragment, CurrenciesFragment.newInstance())
                commit()
            }
        }
    }
}
