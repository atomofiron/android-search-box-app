package ru.atomofiron.regextool.common.util

class SharedData<T>(value: T) {
    var value: T = value
        set(value) {
            field = value
            notifyValueChanged()
        }

    private val observers: MutableList<(T) -> Unit> = ArrayList()

    private fun notifyValueChanged() {
        observers.forEach { it(value) }
    }

    fun observe(observer: (T) -> Unit) {
        if (!observers.contains(observer)) {
            observers.add(observer)
            observer(value)
        }
    }

    fun unobserve(observer: (T) -> Unit) {
        observers.remove(observer)
    }
}
