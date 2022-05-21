package app.atomofiron.common.util.flow

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.FlowCollector
import kotlin.coroutines.CoroutineContext

class LiveDataFlow<T : Any?> : DataFlow<T> {
    private val immediate: Boolean
    private val context: CoroutineContext get() = when {
        immediate -> Dispatchers.Main.immediate
        else -> Dispatchers.Main
    }

    constructor(value: T, single: Boolean = false, immediate: Boolean = true) : super(value, single) {
        this.immediate = immediate
    }

    constructor(single: Boolean = false, immediate: Boolean = true) : super(single) {
        this.immediate = immediate
    }

    fun collect(lifecycle: Lifecycle, action: FlowCollector<T>) {
        val scope = CoroutineScope(context)
        lifecycle.addObserver(LifecycleScopeCanceler(scope))
        collect(scope, action)
    }

    private class LifecycleScopeCanceler(private val scope: CoroutineScope) : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() = scope.cancel()
    }
}

fun <T> Fragment.fragmentCollect(
        flow: LiveDataFlow<T>,
        action: FlowCollector<T>,
) = flow.collect(lifecycle, action)

fun <T> Fragment.viewCollect(
        flow: LiveDataFlow<T>,
        action: FlowCollector<T>,
) = flow.collect(viewLifecycleOwner.lifecycle, action)