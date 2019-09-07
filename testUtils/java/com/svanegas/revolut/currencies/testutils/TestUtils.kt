package com.svanegas.revolut.currencies.testutils

import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.stubbing.OngoingStubbing

fun setupRxSchedulers() {
    RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
    RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
    RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
    RxJavaPlugins.setNewThreadSchedulerHandler { Schedulers.trampoline() }
}

inline fun <reified T> whenever(methodCall: T): OngoingStubbing<T> = `when`(methodCall)

fun setupViewModelTest(testClass: Any) {
    MockitoAnnotations.initMocks(testClass)
    setupRxSchedulers()
}

/**
 * Following two methods were added because it won't allow to use Mockito.any() on nullable
 * objects.
 */

fun <T> kotlinAny(): T {
    Mockito.any<T>()
    return uninitialized()
}

@Suppress("UNCHECKED_CAST")
fun <T> uninitialized(): T = null as T