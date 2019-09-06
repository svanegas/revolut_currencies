package com.svanegas.revolut.currencies.base.arch.statefullayout

import androidx.lifecycle.MutableLiveData

interface SwipeRefreshState {
    fun onPullToRefresh()
}

interface SwipeRefreshHolder {
    val swipeRefreshing: MutableLiveData<Boolean>
}

interface PlaceholderErrorWithRetry {
    fun onErrorRetryClick()
}