package co.touchlab.sessionize.platform

import org.koin.core.context.GlobalContext
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Suspends current execution while backJob runs in a background thread. When
 * multithreaded coroutines arrive this will be pretty useless, but good for now.
 */
internal suspend fun <R> backgroundSuspend(backJob: () -> R): R {
    return if(findConcurrent().allMainThread)
        backJob()
    else {
        val continuationContainer = ContinuationContainer(null)

        backgroundTask(backJob) {
            continuationContainer.continuation!!.resume(it)
        }

        suspendCoroutine<Any?> {
            continuationContainer.continuation = it
        } as R
    }
}

private fun findConcurrent() = GlobalContext.get().koin.get<Concurrent>()

internal fun <B> backgroundTask(backJob: () -> B, mainJob: (B) -> Unit){
    findConcurrent().backgroundTask(backJob, mainJob)
}

internal expect fun <B> backgroundTaskPlatform(backJob: () -> B, mainJob: (B) -> Unit)

internal expect fun <B> backToFront(b: () -> B, job: (B) -> Unit)

internal expect val mainThread: Boolean

internal fun assertMainThread() {
    if (!mainThread)
        throw IllegalStateException("Must be on main thread")
}

private class ContinuationContainer(var continuation: Continuation<Any?>?)

/**
 * Current time in millis. Like Java's System.currentTimeMillis()
 */
expect fun currentTimeMillis(): Long

expect fun logException(t: Throwable)

/**
 * Generates a unique string for use in tracking this user anonymously
 */
expect fun createUuid(): String

expect fun printThrowable(t:Throwable)