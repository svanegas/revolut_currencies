package com.svanegas.revolut.currencies.base.utility

import android.content.Context
import android.net.ConnectivityManager

fun isConnectedToInternet(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    return connectivityManager?.activeNetworkInfo?.isConnected ?: false
}