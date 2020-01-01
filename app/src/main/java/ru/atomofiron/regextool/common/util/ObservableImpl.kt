package ru.atomofiron.regextool.common.util

@Suppress("ConvertSecondaryConstructorToPrimary")


class ObservableImpl<T> {

    var value: T
        set(value) {
            field = value
            notifyObservers()
        }

    constructor(value: T) {
        this.value = value
    }

    private val observers: MutableList<Observer<T>> = ArrayList()

    private fun notifyObservers() {
        observers.forEach { it.update(value) }
    }

    fun observe(observer: Observer<T>) {
        if (!observers.contains(observer)) {
            observers.add(observer)
            observer.update(value)
        }
    }

    fun unobserve(observer: Observer<T>) {
        observers.remove(observer)
    }

    interface Observer<T> {
        fun update(value: T)
    }
}


