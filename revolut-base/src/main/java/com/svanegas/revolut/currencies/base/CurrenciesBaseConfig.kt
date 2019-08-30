package com.svanegas.revolut.currencies.base

object CurrenciesBaseConfig {
    const val LOGS = BuildConfig.LOGS
    const val ENVIRONMENT_NAME = BuildConfig.FLAVOR

    const val IS_PRODUCTION_FLAVOR_TYPE = ENVIRONMENT_NAME == "production"
    const val IS_RELEASE_BUILD_TYPE = BuildConfig.BUILD_TYPE == "release"

    // --- urls to web
    const val REST_BASE_URL = BuildConfig.REST_BASE_URL
}