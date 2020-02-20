package ru.atomofiron.regextool.common.util

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.State
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

class SingleLiveEvent<T> : LifecycleEventObserver {
    private var listener: (() -> Unit)? = null
    private var parameterizedListener: ((T) -> Unit)? = null

    operator fun invoke() {
        if (isStarted) {
            listener?.invoke()
        }
    }
    operator fun invoke(any: T) {
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
        this.parameterizedListener = listener
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        val state = source.lifecycle.currentState
        this.state = state
        if (state == State.DESTROYED) {
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