package app.atomofiron.common.util

import android.view.View

class LazyView<T : View>(private val id: Int, private val parentView: View) : Lazy<T> {
    private var nullableValue: T? = null

    override val value: T get() {
        if (nullableValue == null) {
            nullableValue = parentView.findViewById(id)!!
        }
        return nullableValue!!
    }

    override fun isInitialized(): Boolean = nullableValue != null
}

fun <T : View> lazyView(id: Int, parentView: View): LazyView<T> = LazyView(id, parentView)
