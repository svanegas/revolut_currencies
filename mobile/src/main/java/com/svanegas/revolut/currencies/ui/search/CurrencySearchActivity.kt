package com.svanegas.revolut.currencies.ui.search

import android.os.Bundle
import com.svanegas.revolut.currencies.R
import com.svanegas.revolut.currencies.base.arch.BaseActivity

class CurrencySearchActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.container_fragment, CurrencySearchFragment.newInstance())
                commit()
            }
        }
    }
}
