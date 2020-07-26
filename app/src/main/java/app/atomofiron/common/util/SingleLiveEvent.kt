package app.atomofiron.common.util

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.State
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import app.atomofiron.searchboxapp.logI

class SingleLiveEvent<T> : LifecycleEventObserver {
    private var listener: (() -> Unit)? = null
    private var dataListener: ((T) -> Unit)? = null
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
            dataListener?.invoke(any)
        }
    }

    private var state: State = State.DESTROYED
    // LiveData тоже отслеживает состояние STARTED
    private val isStarted: Boolean get() = state.isAtLeast(State.STARTED)

    suspend fun collect(channel: BroadcastChannel<T>) = channel.asFlow().collect {
        when (it) {
            is Unit -> invoke()
            else -> invoke(it)
        }
    }

    fun observeEvent(source: LifecycleOwner, listener: () -> Unit) {
        check()
        source.lifecycle.addObserver(this)
        this.listener = listener
    }

    fun observeData(source: LifecycleOwner, listener: (T) -> Unit) {
        check()
        source.lifecycle.addObserver(this)
        logI("observeData source: ${source.javaClass.simpleName}")
        this.dataListener = listener
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        val state = source.lifecycle.currentState
        this.state = state
        if (state == State.DESTROYED) {
            logI("Destroyed. source: ${source.javaClass.simpleName}")
            source.lifecycle.removeObserver(this)
            listener = null
            dataListener = null
        }
    }

    private fun check() {
        val notObserved = listener == null && dataListener == null
        require(notObserved) { IllegalStateException("Already") }
    }
}