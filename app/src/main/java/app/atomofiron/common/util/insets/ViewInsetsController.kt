package app.atomofiron.common.util.insets

/** was created at december 4 2020 */

import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.Insets
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type

class ViewInsetsController private constructor(
    private val view: View,
    private val forLeft: Boolean,
    private val forTop: Boolean,
    private val forRight: Boolean,
    private val forBottom: Boolean,
    private val usePadding: Boolean,
    private val doProxy: Boolean,
) : OnApplyWindowInsetsListener {
    companion object {
        val typeMask = Type.systemBars() or Type.ime()

        fun bindPadding(
            view: View,
            left: Boolean = false,
            top: Boolean = false,
            right: Boolean = false,
            bottom: Boolean = false,
            doProxy: Boolean = false,
        ): ViewInsetsController {
            require(left || top || right || bottom)
            val controller = ViewInsetsController(view, left, top, right, bottom, usePadding = true, doProxy)
            ViewCompat.setOnApplyWindowInsetsListener(view, controller)
            return controller
        }

        fun bindMargin(
            view: View,
            left: Boolean = false,
            top: Boolean = false,
            right: Boolean = false,
            bottom: Boolean = false,
            doProxy: Boolean = false,
        ): ViewInsetsController {
            require(left || top || right || bottom)
            val controller = ViewInsetsController(view, left, top, right, bottom, usePadding = false, doProxy)
            ViewCompat.setOnApplyWindowInsetsListener(view, controller)
            return controller
        }

        fun getInsets(view: View): Insets {
            val insets = ViewCompat.getRootWindowInsets(view) ?: WindowInsetsCompat.CONSUMED
            return insets.getInsets(typeMask)
        }
    }

    private var systemPaddingStart = 0
    private var systemPaddingTop = 0
    private var systemPaddingEnd = 0
    private var systemPaddingBottom = 0

    private var listener: OnApplyWindowInsetsListener? = null

    override fun onApplyWindowInsets(view: View, windowInsets: WindowInsetsCompat): WindowInsetsCompat {
        when {
            usePadding -> view.updatePadding(windowInsets)
            else -> view.updateMargin(windowInsets)
        }
        if (doProxy) {
            ViewGroupInsetsProxy.dispatchChildrenWindowInsets(view, windowInsets)
        }
        listener?.onApplyWindowInsets(view, windowInsets)
        return WindowInsetsCompat.CONSUMED
    }

    fun setListener(listener: OnApplyWindowInsetsListener) {
        this.listener = listener
    }

    fun updatePadding(start: Int? = null, top: Int? = null, end: Int? = null, bottom: Int? = null) {
        val paddingStart = (start?.let { it + systemPaddingStart } ?: view.paddingStart)
        val paddingTop = (top?.let { it + systemPaddingTop } ?: view.paddingTop)
        val paddingEnd = (end?.let { it + systemPaddingEnd } ?: view.paddingEnd)
        val paddingBottom = (bottom?.let { it + systemPaddingBottom } ?: view.paddingBottom)
        view.setPaddingRelative(paddingStart, paddingTop, paddingEnd, paddingBottom)
    }

    private fun View.updatePadding(windowInsets: WindowInsetsCompat) {
        val insets = windowInsets.getInsets(typeMask)
        var start = paddingStart
        if (forLeft) {
            start += insets.left - systemPaddingStart
            systemPaddingStart = insets.left
        }
        var top = paddingTop
        if (forTop) {
            top += insets.top - systemPaddingTop
            systemPaddingTop = insets.top
        }
        var end = paddingEnd
        if (forRight) {
            end += insets.right - systemPaddingEnd
            systemPaddingEnd = insets.right
        }
        var bottom = paddingBottom
        if (forBottom) {
            bottom += insets.bottom - systemPaddingBottom
            systemPaddingBottom = insets.bottom
        }
        val needUpdate = start != paddingStart || top != paddingTop || end != paddingEnd || bottom != paddingBottom
        if (needUpdate) {
            setPaddingRelative(start, top, end, bottom)
        }
    }

    private fun View.updateMargin(windowInsets: WindowInsetsCompat) {
        (layoutParams as ViewGroup.MarginLayoutParams).run {
            val insets = windowInsets.getInsets(typeMask)
            var start = marginStart
            if (forLeft) {
                start += insets.left - systemPaddingStart
                systemPaddingStart = insets.left
            }
            var top = topMargin
            if (forTop) {
                top += insets.top - systemPaddingTop
                systemPaddingTop = insets.top
            }
            var end = marginEnd
            if (forRight) {
                end += insets.right - systemPaddingEnd
                systemPaddingEnd = insets.right
            }
            var bottom = bottomMargin
            if (forBottom) {
                bottom += insets.bottom - systemPaddingBottom
                systemPaddingBottom = insets.bottom
            }
            val needUpdate = marginStart != start || topMargin != top || marginEnd != end || bottomMargin != bottom
            if (needUpdate) {
                marginStart = start
                topMargin = top
                marginEnd = end
                bottomMargin = bottom
                layoutParams = this
            }
        }
    }
}