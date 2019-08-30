package com.svanegas.revolut.currencies

import com.svanegas.revolut.currencies.base.CurrenciesBaseConfig
import com.svanegas.revolut.currencies.base.arch.BaseApplication
import com.svanegas.revolut.currencies.base.arch.extension.lazyUnsafe
import com.svanegas.revolut.currencies.di.DaggerAppComponent
import timber.log.Timber

class CurrenciesApplication : BaseApplication() {

    internal val appComponent by lazyUnsafe {
        DaggerAppComponent
            .builder()
            .application(this)
            .build()
    }

    override fun onCreate() {
        super.onCreate()

        appComponent.inject(this)

        initTimber()
    }

    private fun initTimber() {
        if (CurrenciesBaseConfig.IS_RELEASE_BUILD_TYPE) {
            // Report crashes for productionRelease
            if (CurrenciesBaseConfig.IS_PRODUCTION_FLAVOR_TYPE) {
                Timber.plant(FakeCrashReportingTree())
            } else {
                // Log to console and report crashes for other release builds
                Timber.plant(Timber.DebugTree())
                Timber.plant(FakeCrashReportingTree())
            }
        } else {
            // Log to console for debug builds
            Timber.plant(Timber.DebugTree())
        }
    }
}

/**
 * This should be a class that will report logs somewhere in a crash reporting platform.
 */
private class FakeCrashReportingTree : Timber.DebugTree()