package app.atomofiron.common.util

import java.util.*

open class KObservable<T : Any?>(value: T, private val single: Boolean = false) {
    private var changed = false
    private val observers = Vector<(T) -> Unit>()
    var value: T = value
        private set

    fun addObserver(removeCallback: RemoveObserverCallback, observer: (T) -> Unit) {
        addObserver(observer)

        removeCallback.addOneTimeObserver {
            removeObserver(observer)
        }
    }

    @Synchronized
    fun addObserver(observer: (T) -> Unit) {
        if (!observers.contains(observer)) {
            observers.addElement(observer)
            if (!single) {
                observer.invoke(value)
            }
        }
    }

    @Synchronized
    fun removeObserver(o: (T) -> Unit) = observers.removeElement(o)

    @Synchronized
    fun setAndNotify(value: T) {
        this.value = value
        changed = true
        notifyObservers()
    }

    @Synchronized
    fun justNotify() {
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

    class RemoveObserverCallback {
        private val observers = Vector<() -> Unit>()

        fun addOneTimeObserver(observer: () -> Unit) {
            observers.addElement(observer)
        }

        fun invoke() {
            observers.forEach { it() }
            observers.clear()
        }
    }
}