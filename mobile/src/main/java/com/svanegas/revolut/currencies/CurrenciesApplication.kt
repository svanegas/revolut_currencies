package com.svanegas.revolut.currencies

import com.svanegas.revolut.currencies.base.arch.BaseApplication
import com.svanegas.revolut.currencies.base.arch.extension.lazyUnsafe
import com.svanegas.revolut.currencies.di.DaggerAppComponent

class CurrenciesApplication : BaseApplication() {

    internal val appComponent by lazyUnsafe {
        DaggerAppComponent
            .builder()
            .application(this)
            .build()
    }
}