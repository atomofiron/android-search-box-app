package app.atomofiron.common.util.flow

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

open class DataFlow<T : Any?>(
    protected val single: Boolean = false
) {
    constructor(value: T, single: Boolean = false) : this(single) {
        this.value = value
    }

    private val replay: Int get() = if (single) 0 else 1

    private val flow = MutableSharedFlow<T>(replay, extraBufferCapacity = Int.MAX_VALUE)

    private val scope = CoroutineScope(Dispatchers.Unconfined)

    private var data: T? = null

    val isInitialized: Boolean get() = single || flow.replayCache.isNotEmpty()

    var value: T
        get() = data as T
        set(value) {
            data = value
            scope.launch {
                flow.emit(value)
            }
        }

    fun emit(value: T = this.value) {
        this.value = value
    }

    suspend fun collect(collector: FlowCollector<T>) {
        flow.collect(collector)
    }

    fun collect(scope: CoroutineScope, collector: FlowCollector<T>) {
        scope.launch {
            collect(collector)
        }
    }
}