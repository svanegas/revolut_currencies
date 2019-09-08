package com.svanegas.revolut.currencies

import com.crashlytics.android.Crashlytics
import com.svanegas.revolut.currencies.base.CurrenciesBaseConfig
import com.svanegas.revolut.currencies.base.analytics.CrashReportingTree
import com.svanegas.revolut.currencies.base.arch.BaseApplication
import com.svanegas.revolut.currencies.base.arch.extension.lazyUnsafe
import com.svanegas.revolut.currencies.di.DaggerAppComponent
import io.fabric.sdk.android.Fabric
import io.realm.Realm
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

        initFabric()
        initTimber()

        Realm.init(this)

        appComponent.inject(this)
    }

    @Suppress("ConstantConditionIf")
    private fun initFabric() {
        if (CurrenciesBaseConfig.IS_RELEASE_BUILD_TYPE) {
            Fabric.with(applicationContext, Crashlytics())
        }
    }

    @Suppress("ConstantConditionIf")
    private fun initTimber() {
        if (CurrenciesBaseConfig.IS_RELEASE_BUILD_TYPE) {
            // Report crashes for productionRelease
            if (CurrenciesBaseConfig.IS_PRODUCTION_FLAVOR_TYPE) {
                Timber.plant(CrashReportingTree())
            } else {
                // Log to console and report crashes for other release builds
                Timber.plant(Timber.DebugTree())
                Timber.plant(CrashReportingTree())
            }
        } else {
            // Log to console for debug builds
            Timber.plant(Timber.DebugTree())
        }
    }
}
