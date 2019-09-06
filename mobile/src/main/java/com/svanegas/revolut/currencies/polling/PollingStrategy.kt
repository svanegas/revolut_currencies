package com.svanegas.revolut.currencies.polling

import io.reactivex.Flowable

interface PollingStrategy {
    fun getPollingMethod(handler: Flowable<Any>): Flowable<Any>
}
