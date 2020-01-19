package ru.atomofiron.regextool.common.util

import java.util.*

open class KObservable<T : Any?>(value: T) {
    private var changed = false
    private val observers = Vector<(T) -> Unit>()
    var value: T = value
        private set

    @Synchronized
    fun addObserver(observer: (T) -> Unit) {
        if (!observers.contains(observer)) {
            observers.addElement(observer)
            // todo check state
            observer.invoke(value)
        }
    }

    @Synchronized
    fun removeObserver(o: (T) -> Unit) = observers.removeElement(o)

    @Synchronized
    fun notifyObservers(value: T) {
        this.value = value
        changed = true
        notifyObservers()
    }

    private fun notifyObservers() {
        val value: T
        val arrLocal: Array<(T) -> Unit>

        synchronized(this) {
            if (!changed) {
                return
            }
            value = this.value
            arrLocal = observers.toTypedArray()
            changed = false
        }

        for (i in arrLocal.indices) {
            arrLocal[i].invoke(value)
        }
    }

    @Synchronized
    fun clearObservers() = observers.removeAllElements()

    @Synchronized
    fun size(): Int = observers.size
}