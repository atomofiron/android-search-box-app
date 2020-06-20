package ru.atomofiron.regextool.custom.view.bottom_sheet

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.view.NestedScrollingParent3
import androidx.core.view.ViewCompat
import androidx.core.view.ViewCompat.NestedScrollType
import androidx.core.view.ViewCompat.ScrollAxis
import androidx.core.widget.NestedScrollView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.common.util.moveChildrenFrom
import ru.atomofiron.regextool.R

class BottomScrollingSheetView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : CoordinatorLayout(context, attrs, defStyleAttr), NestedScrollingParent3 {

    init {
        moveChildrenFrom(R.layout.view_scroll_bottom_sheet)
        setBackgroundColor(context.findColorByAttr(R.attr.colorOverlay))
    }

    // NestedScrollingParent

    override fun onStartNestedScroll(child: View, target: View, @ScrollAxis axes: Int): Boolean {
        return false // true if this ViewParent accepts the nested scroll operation
    }

    override fun onNestedScrollAccepted(child: View, target: View, @ScrollAxis axes: Int) {
    }

    override fun onStopNestedScroll(target: View) {
    }

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
    }

    override fun onNestedFling(target: View, velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return false // true if this parent consumed or otherwise reacted to the fling
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        return false // true if this parent consumed the fling ahead of the target view
    }

    override fun getNestedScrollAxes(): Int = ViewCompat.SCROLL_AXIS_VERTICAL

    // NestedScrollingParent2

    override fun onStartNestedScroll(child: View, target: View, @ScrollAxis axes: Int, @NestedScrollType type: Int): Boolean {
        return false // true if this ViewParent accepts the nested scroll operation
    }

    override fun onNestedScrollAccepted(child: View, target: View, @ScrollAxis axes: Int, @NestedScrollType type: Int) {
    }

    override fun onStopNestedScroll(target: View, @NestedScrollType type: Int) {
    }

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, @NestedScrollType type: Int) {
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, @NestedScrollType type: Int) {
    }

    // NestedScrollingParent3

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, @NestedScrollType type: Int, consumed: IntArray) {
    }
}