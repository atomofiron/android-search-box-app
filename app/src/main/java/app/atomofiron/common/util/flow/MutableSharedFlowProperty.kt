package app.atomofiron.common.util.flow

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.reflect.KProperty

class MutableSharedFlowProperty<T>() : MutableSharedFlow<T> by sharedFlow() {

    constructor(value: T) : this() {
        this.value = value
    }

    operator fun getValue(any: Any, property: KProperty<*>): T = value
    operator fun setValue(any: Any, property: KProperty<*>, value: T) = emitNow(value)
}