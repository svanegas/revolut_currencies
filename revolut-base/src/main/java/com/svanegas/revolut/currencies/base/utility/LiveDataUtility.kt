package com.svanegas.revolut.currencies.base.utility

import androidx.lifecycle.MutableLiveData

fun <T> MutableLiveData<T>.notifyChange() {
    this.value = this.value
}
