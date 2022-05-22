package app.atomofiron.common.util.flow

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.reflect.KProperty

class MutableDataFlowProperty<T>() : MutableSharedFlow<T> by dataFlow() {

    constructor(value: T) : this() {
        this.value = value
    }

    operator fun getValue(any: Any, property: KProperty<*>): T = value
    operator fun setValue(any: Any, property: KProperty<*>, value: T) = emitNow(value)
}