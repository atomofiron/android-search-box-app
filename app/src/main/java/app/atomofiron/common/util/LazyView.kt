package app.atomofiron.common.util

import android.view.View

class LazyView<T : View>(private val id: Int, private val parentView: View) : Lazy<T> {
    private var nullableValue: T? = null

    override val value: T get() {
        val value = nullableValue ?: parentView.findViewById(id)
        nullableValue = value
        return value
    }

    override fun isInitialized(): Boolean = nullableValue != null
}

fun <T : View> lazyView(id: Int, parentView: View): LazyView<T> = LazyView(id, parentView)
