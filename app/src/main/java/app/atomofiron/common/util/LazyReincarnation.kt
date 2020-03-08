package app.atomofiron.common.util

class LazyReincarnation<T : Any>(private val initializer: LazyReincarnation<T>.() -> T) : Lazy<T> {
    private var nullableValue: T? = null

    private var recreationIsNeeded: (T) -> Boolean = { false }

    override val value: T get() {
        var instance = nullableValue ?: initializer()
        if (recreationIsNeeded(instance)) {
            instance = initializer()
        }
        nullableValue = instance
        return instance
    }

    override fun isInitialized(): Boolean = nullableValue != null

    fun recreateIf(predicate: (T) -> Boolean): LazyReincarnation<T> {
        recreationIsNeeded = predicate
        return this
    }

    fun wipe() {
        nullableValue = null
    }

    inline operator fun invoke(action: T.() -> Unit) = action(value)
}


public fun <T : Any> reincarnation(initializer: LazyReincarnation<T>.() -> T): LazyReincarnation<T> = LazyReincarnation(initializer)
