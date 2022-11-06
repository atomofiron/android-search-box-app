package app.atomofiron.common.util.flow

import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UniqueStateFlow<T>(value: T) : StateFlow<T> {

    private val flow = MutableStateFlow(Unique(value))

    override val replayCache: List<T> = flow.replayCache.map { it.value }

    override val value: T get() = flow.value.value

    fun set(value: T) {
        flow.value = Unique(value)
    }

    override suspend fun collect(collector: FlowCollector<T>): Nothing {
        flow.collect {
            collector.emit(it.value)
        }
    }
}