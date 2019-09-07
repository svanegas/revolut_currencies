package com.svanegas.revolut.currencies.polling

import io.reactivex.Flowable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val DATA_POLL_DELAY_MILLIS = 1500L

class PollingStrategyImpl @Inject constructor() : PollingStrategy {

    override fun getPollingMethod(handler: Flowable<Any>): Flowable<Any> = handler
        .delay(DATA_POLL_DELAY_MILLIS, TimeUnit.MILLISECONDS)
}