package app.atomofiron.common.util.flow

import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.lang.NullPointerException

fun <T> dataFlow(single: Boolean = false): MutableSharedFlow<T> {
    val replayLimit = if (single) 0 else 1
    return MutableSharedFlow(replayLimit, extraBufferCapacity = Int.MAX_VALUE)
}

fun <T> dataFlow(value: T, single: Boolean = false): MutableSharedFlow<T> {
    val flow = dataFlow<T>(single)
    flow.value = value
    return flow
}

val <T> SharedFlow<T>.isInitialized: Boolean get() = replayCache.isNotEmpty()

val <T> SharedFlow<T>.value: T get() = replayCache.last()

var <T> MutableSharedFlow<T>.value: T
    get() = replayCache.last()
    set(value) {
        @Suppress("EXPERIMENTAL_API_USAGE")
        GlobalScope.launch(start = CoroutineStart.UNDISPATCHED) {
            emit(value)
        }
    }

fun <T> MutableSharedFlow<T>.emitLast() = when {
    isInitialized -> value = value
    else -> throw NullPointerException("The flow doesn't contains any data to emit.")
}

fun <T> MutableSharedFlow<T>.emitNow(value: T) {
    this.value = value
}

operator fun MutableSharedFlow<Unit>.invoke() = emitNow(Unit)

fun <T> Flow<T>.collect(scope: CoroutineScope, collector: FlowCollector<T>) {
    scope.launch {
        collect(collector)
    }
}

fun <T> Fragment.viewCollect(
    flow: SharedFlow<T>,
    immediate: Boolean = true,
    collector: FlowCollector<T>,
) {
    val context = when {
        immediate -> Dispatchers.Main.immediate
        else -> Dispatchers.Main
    }
    viewLifecycleOwner.lifecycle.coroutineScope.launch(context) {
        flow.collect(collector)
    }
}

fun <T> Fragment.fragmentCollect(
    flow: SharedFlow<T>,
    immediate: Boolean = true,
    collector: FlowCollector<T>,
) {
    val context = when {
        immediate -> Dispatchers.Main.immediate
        else -> Dispatchers.Main
    }
    lifecycle.coroutineScope.launch(context) {
        flow.collect(collector)
    }
}

fun <T> Flow<T>.debounceAfterFirst(duration: Long = 100L): Flow<T> = flow {
    var delayedJob: Job? = null
    var delayedValue: T? = null
    var hasDelayedValue = false

    fun post() {
        delayedJob?.cancel()
        delayedJob = CoroutineScope(Dispatchers.Default).launch {
            delay(duration)
            if (hasDelayedValue) {
                hasDelayedValue = false
                emit(delayedValue as T)
                delayedValue = null
            }
        }
    }
    collect { value ->
        if (delayedJob?.isActive == true) {
            delayedValue = value
            hasDelayedValue = true
            post()
        } else {
            emit(value)
            post()
        }
    }
}