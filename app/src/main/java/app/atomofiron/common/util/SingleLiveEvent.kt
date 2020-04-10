package app.atomofiron.common.util

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.State
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import ru.atomofiron.regextool.log2

class SingleLiveEvent<T> : LifecycleEventObserver {
    private var listener: (() -> Unit)? = null
    private var parameterizedListener: ((T) -> Unit)? = null
    var data: T? = null
        private set

    operator fun invoke() {
        if (isStarted) {
            listener?.invoke()
        }
    }
    operator fun invoke(any: T) {
        data = any
        if (isStarted) {
            parameterizedListener?.invoke(any)
        }
    }

    private var state: State = State.DESTROYED
    // LiveData тоже отслеживает состояние STARTED
    private val isStarted: Boolean get() = state.isAtLeast(State.STARTED)

    fun observeEvent(source: LifecycleOwner, listener: () -> Unit) {
        check()
        source.lifecycle.addObserver(this)
        this.listener = listener
    }

    fun observeData(source: LifecycleOwner, listener: (T) -> Unit) {
        check()
        source.lifecycle.addObserver(this)
        log2("observeData source: ${source.javaClass.simpleName}")
        this.parameterizedListener = listener
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        val state = source.lifecycle.currentState
        this.state = state
        if (state == State.DESTROYED) {
            log2("DESTROYED source: ${source.javaClass.simpleName}")
            source.lifecycle.removeObserver(this)
            listener = null
            parameterizedListener = null
        }
    }

    private fun check() {
        val notObserved = listener == null && parameterizedListener == null
        require(notObserved) { IllegalStateException("Already") }
    }
}