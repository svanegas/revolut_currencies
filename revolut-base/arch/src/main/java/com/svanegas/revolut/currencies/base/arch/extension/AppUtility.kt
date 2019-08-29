package com.svanegas.revolut.currencies.base.arch.extension

/**
 * Shortcut for lazy with no thread safety
 */
fun <T> lazyUnsafe(initializer: () -> T) = lazy(LazyThreadSafetyMode.NONE, initializer)