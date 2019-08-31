package com.svanegas.revolut.currencies.base.analytics

import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.services.common.CommonUtils
import timber.log.Timber
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.*

/**
 * Timber tree for logging into Crashlytics.
 *
 * We log all levels of messages and they get associated with specific crash.
 * All Crashlytics logs are flushed when calling Crashlytics.logException().
 *
 * WARNING: This class extends from the Timber.DebugTree to improve the logs - we gain access to a tag that is
 * represented by the calling class name.
 * When we start to use some form of code obfuscation (e.g. ProGuard) we should probably change it
 * since the class names will get obfuscated.
 */
class CrashReportingTree : Timber.DebugTree() {

    override fun formatMessage(message: String, args: Array<Any>): String {
        return String.format(Locale.US, message, *args)
    }

    override fun log(priority: Int, tag: String?, message: String, throwable: Throwable?) {
        val priorityString = CommonUtils.logPriorityToString(priority)
        val messageWithPrefix = "| $priorityString/$tag: $message"

        Crashlytics.log(messageWithPrefix)

        if (throwable != null) {
            logCrashlyticsError(throwable)
        }
    }

    /**
     * Do not report some of the errors as non-fatal but only log them
     */
    private fun logCrashlyticsError(error: Throwable) {
        if (error.isIgnored || error.cause.isIgnored) {
            Crashlytics.log(error.message)
        } else {
            Crashlytics.logException(error)
        }
    }

    private val Throwable?.isIgnored
        get() = when (this) {
            is SocketTimeoutException,
            is UnknownHostException -> true
            else -> false
        }
}
