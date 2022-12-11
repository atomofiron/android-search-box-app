package app.atomofiron.searchboxapp

import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.LayoutDirection
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.annotation.StyleableRes
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlin.math.max

val Fragment.anchorView: View get() = requireActivity().findViewById(R.id.joystick)

fun View.setContentMaxWidthRes(resId: Int) = setContentMaxWidth(resources.getDimensionPixelSize(resId))

fun View.setContentMaxWidth(value: Int) {
    var currentInset = 0
    addOnLayoutChangeListener { view, left, _, right, _, _, _, _, _ ->
        val width = right - left
        val inset = max(0, width - value) / 2
        val paddingLeft = paddingLeft - currentInset + inset
        val paddingRight = paddingRight - currentInset + inset
        currentInset = inset
        if (this.paddingLeft != paddingLeft || this.paddingRight != paddingRight) {
            view.updatePadding(left = paddingLeft, right = paddingRight)
        }
    }
}

inline fun Context.obtainStyledAttributes(
    attrs: AttributeSet?,
    @StyleableRes styleable: IntArray,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0,
    action: TypedArray.() -> Unit,
) {
    val styled = obtainStyledAttributes(attrs, styleable, defStyleAttr, defStyleRes)
    action(styled)
    styled.recycle()
}

fun View.isRtl(): Boolean = resources.isRtl()

fun Resources.isRtl(): Boolean = configuration.layoutDirection == LayoutDirection.RTL


fun RecyclerView.scrollToTop() {
    if (childCount == 0) return
    val topChild = getChildAt(0)
    val topHolder = getChildViewHolder(topChild)
    if (topHolder.absoluteAdapterPosition == 0) {
        smoothScrollToPosition(0)
        return
    }
    val spanCount = when (val manager = layoutManager) {
        is GridLayoutManager -> manager.spanCount
        is StaggeredGridLayoutManager -> manager.spanCount
        else -> 1
    }
    scrollToPosition(spanCount)
    post {
        smoothScrollToPosition(0)
    }
}
