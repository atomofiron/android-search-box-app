package app.atomofiron.common.util.insets

/** was created at december 4 2020 */

import android.view.View
import android.view.ViewGroup
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

typealias InsetsListener = (WindowInsetsCompat) -> Unit

class ViewGroupInsetsProxy private constructor(private val listener: InsetsListener?) : OnApplyWindowInsetsListener {
    companion object {
        fun consume(viewGroup: View) = ViewCompat.setOnApplyWindowInsetsListener(viewGroup) { _, _ ->
            WindowInsetsCompat.CONSUMED
        }

        fun set(viewGroup: View, listener: InsetsListener? = null): ViewGroupInsetsProxy {
            viewGroup as ViewGroup
            val proxy = ViewGroupInsetsProxy(listener)
            ViewCompat.setOnApplyWindowInsetsListener(viewGroup, proxy)
            return proxy
        }

        fun dispatchChildrenWindowInsets(viewGroup: View, insets: WindowInsetsCompat): WindowInsetsCompat {
            viewGroup as ViewGroup
            val windowInsets = insets.toWindowInsets()
            for (index in 0 until viewGroup.childCount) {
                val child = viewGroup.getChildAt(index)
                child.dispatchApplyWindowInsets(windowInsets)
            }
            return WindowInsetsCompat.CONSUMED
        }
    }

    override fun onApplyWindowInsets(viewGroup: View, insets: WindowInsetsCompat): WindowInsetsCompat {
        dispatchChildrenWindowInsets(viewGroup, insets)
        listener?.invoke(insets)
        return WindowInsetsCompat.CONSUMED
    }
}