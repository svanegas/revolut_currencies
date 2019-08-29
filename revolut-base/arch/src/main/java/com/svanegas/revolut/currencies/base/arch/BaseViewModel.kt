package com.svanegas.revolut.currencies.base.arch

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import com.svanegas.revolut.currencies.base.logWithTag
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

abstract class BaseViewModel : ViewModel() {

    val compositeDisposable = CompositeDisposable()

    @SuppressLint("StaticFieldLeak")
    @Inject
    lateinit var appContext: Context

    init {
        logWithTag(javaClass.simpleName, "init")
    }

    override fun onCleared() {
        logWithTag(javaClass.simpleName, "onCleared")
        compositeDisposable.dispose()
    }
}