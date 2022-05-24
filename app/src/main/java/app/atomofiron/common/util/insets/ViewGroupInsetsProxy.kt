package app.atomofiron.common.util.insets

/** was created at december 4 2020 */

import android.view.View
import android.view.ViewGroup
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

typealias InsetsListener = (View, WindowInsetsCompat) -> Unit

class ViewGroupInsetsProxy private constructor(
    private val listener: InsetsListener?,
) : OnApplyWindowInsetsListener {
    companion object {

        fun consume(view: View) = ViewCompat.setOnApplyWindowInsetsListener(view) { _, _ ->
            WindowInsetsCompat.CONSUMED
        }

        fun set(viewGroup: View, listener: InsetsListener? = null): ViewGroupInsetsProxy {
            viewGroup as ViewGroup
            val proxy = ViewGroupInsetsProxy(listener)
            ViewCompat.setOnApplyWindowInsetsListener(viewGroup, proxy)
            return proxy
        }

        fun dispatchChildrenWindowInsets(viewGroup: View, insets: WindowInsetsCompat) {
            viewGroup as ViewGroup
            val windowInsets = insets.toWindowInsets()
            for (index in 0 until viewGroup.childCount) {
                val child = viewGroup.getChildAt(index)
                child.dispatchApplyWindowInsets(windowInsets)
            }
        }
    }

    override fun onApplyWindowInsets(view: View, insets: WindowInsetsCompat): WindowInsetsCompat {
        dispatchChildrenWindowInsets(view, insets)
        listener?.invoke(view, insets)
        return WindowInsetsCompat.CONSUMED
    }
}