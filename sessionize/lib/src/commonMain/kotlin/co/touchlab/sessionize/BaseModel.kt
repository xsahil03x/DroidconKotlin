package co.touchlab.sessionize

import co.touchlab.sessionize.platform.logException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.koin.core.KoinComponent
import org.koin.core.inject
import kotlin.coroutines.CoroutineContext

open class BaseModel : CoroutineScope, KoinComponent {

    private val mainContext: CoroutineContext by inject()
    private val job = Job()
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        showError(throwable)
    }

    open fun showError(t: Throwable) {
        logException(t)
    }

    override val coroutineContext: CoroutineContext
        get() = mainContext + job + exceptionHandler

    open fun onDestroy() {
        job.cancel()
    }
}