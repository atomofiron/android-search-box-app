package app.atomofiron.common.util.insets

/** was created at december 4 2020 */

import android.util.LayoutDirection
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.Insets
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type

class ViewInsetsController private constructor(
    private val view: View,
    private val applyStart: Boolean,
    private val applyTop: Boolean,
    private val applyEnd: Boolean,
    private val applyBottom: Boolean,
    private val destination: Destination,
    private val withProxy: Boolean,
    private val typeMask: Int,
) : OnApplyWindowInsetsListener {
    companion object {

        val defaultTypeMask = Type.systemBars() or Type.ime()

        fun bindPadding(
            view: View,
            start: Boolean = false,
            top: Boolean = false,
            end: Boolean = false,
            bottom: Boolean = false,
            withProxy: Boolean = false,
            typeMask: Int = defaultTypeMask,
        ): ViewInsetsController {
            require(start || top || end || bottom)
            val controller = ViewInsetsController(view, start, top, end, bottom, Destination.PADDING, withProxy, typeMask)
            ViewCompat.setOnApplyWindowInsetsListener(view, controller)
            return controller
        }

        fun bindMargin(
            view: View,
            start: Boolean = false,
            top: Boolean = false,
            end: Boolean = false,
            bottom: Boolean = false,
            withProxy: Boolean = false,
            typeMask: Int = defaultTypeMask,
        ): ViewInsetsController {
            require(start || top || end || bottom)
            val controller = ViewInsetsController(view, start, top, end, bottom, Destination.MARGIN, withProxy, typeMask)
            ViewCompat.setOnApplyWindowInsetsListener(view, controller)
            return controller
        }

        fun getInsets(view: View, typeMask: Int = defaultTypeMask): Insets {
            val insets = ViewCompat.getRootWindowInsets(view) ?: WindowInsetsCompat.CONSUMED
            return insets.getInsets(typeMask)
        }
    }

    private enum class Destination {
        PADDING, MARGIN
    }

    private var insetStart = 0
    private var insetTop = 0
    private var insetEnd = 0
    private var insetBottom = 0

    private var listener: InsetsListener? = null
    private val isRtlEnabled get() = view.resources.configuration.layoutDirection == LayoutDirection.RTL

    override fun onApplyWindowInsets(view: View, windowInsets: WindowInsetsCompat): WindowInsetsCompat {
        when (destination) {
            Destination.PADDING -> view.updatePadding(windowInsets)
            Destination.MARGIN -> view.updateMargin(windowInsets)
        }
        if (withProxy) {
            ViewGroupInsetsProxy.dispatchChildrenWindowInsets(view, windowInsets)
        }
        listener?.invoke(view, windowInsets)
        return WindowInsetsCompat.CONSUMED
    }

    fun setListener(listener: InsetsListener) {
        this.listener = listener
    }

    fun updatePadding(start: Int? = null, top: Int? = null, end: Int? = null, bottom: Int? = null) {
        val paddingStart = (start?.let { it + insetStart } ?: view.paddingStart)
        val paddingTop = (top?.let { it + insetTop } ?: view.paddingTop)
        val paddingEnd = (end?.let { it + insetEnd } ?: view.paddingEnd)
        val paddingBottom = (bottom?.let { it + insetBottom } ?: view.paddingBottom)
        view.setPaddingRelative(paddingStart, paddingTop, paddingEnd, paddingBottom)
    }

    private val Insets.start: Int get() = if (isRtlEnabled) right else left

    private val Insets.end: Int get() = if (isRtlEnabled) left else right

    private fun View.updatePadding(windowInsets: WindowInsetsCompat) {
        val insets = windowInsets.getInsets(typeMask)
        var start = paddingStart
        if (applyStart) {
            start += insets.start - insetStart
            insetStart = insets.start
        }
        var top = paddingTop
        if (applyTop) {
            top += insets.top - insetTop
            insetTop = insets.top
        }
        var end = paddingEnd
        if (applyEnd) {
            end += insets.end - insetEnd
            insetEnd = insets.end
        }
        var bottom = paddingBottom
        if (applyBottom) {
            bottom += insets.bottom - insetBottom
            insetBottom = insets.bottom
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
            if (applyStart) {
                start += insets.start - insetStart
                insetStart = insets.start
            }
            var top = topMargin
            if (applyTop) {
                top += insets.top - insetTop
                insetTop = insets.top
            }
            var end = marginEnd
            if (applyEnd) {
                end += insets.end - insetEnd
                insetEnd = insets.end
            }
            var bottom = bottomMargin
            if (applyBottom) {
                bottom += insets.bottom - insetBottom
                insetBottom = insets.bottom
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