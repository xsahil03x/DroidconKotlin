package co.touchlab.sessionize.architecture

import co.touchlab.sessionize.platform.Concurrent
import co.touchlab.sessionize.platform.backToFront
import co.touchlab.stately.concurrency.ThreadLocalRef
import co.touchlab.stately.freeze
import org.koin.core.KoinComponent
import org.koin.core.inject

class MainThreadPubSub<T> : BasePub<T>(), Sub<T>, KoinComponent {
    private val subSetLocal = ThreadLocalRef<MutableCollection<Sub<T>>>()
    private val concurrent: Concurrent by inject()

    init {
        subSetLocal.set(mutableSetOf())
    }

    override fun subs(): MutableCollection<Sub<T>> = subSetLocal.get()!!

    override fun onNext(next: T) {
        next.freeze()
        if(concurrent.allMainThread){
            applyNextValue(next)
        }else {
            backToFront({ next }) {
                applyNextValue(it)
            }
        }
    }

    override fun onError(t: Throwable) {
        t.freeze()
        if(concurrent.allMainThread){
            applyError(t)
        }else {
            backToFront({ t }) {
                applyError(it)
            }
        }
    }
}