package ru.atomofiron.regextool.custom.view.bottom_sheet

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.ViewCompat.NestedScrollType
import androidx.core.view.ViewCompat.ScrollAxis
import androidx.core.widget.NestedScrollView
import ru.atomofiron.regextool.logD
import kotlin.math.max
import kotlin.math.min

class SheetScrollView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr) {

    private val child: View get() = getChildAt(0)
    private val canScrollUp: Int get() = child.bottom - bottom - scrollY

    private var listener: OnScrollStopListener? = null
    private var scrollAlreadyStopped = true
    private var childIsFling = false

    init {
        overScrollMode = View.OVER_SCROLL_NEVER
        isNestedScrollingEnabled = false
    }

    fun setListener(listener: OnScrollStopListener) {
        this.listener = listener
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val height = measuredHeight
        if (paddingTop != height) {
            setPaddingRelative(paddingStart, height, paddingEnd, paddingBottom)
        }

        val child = child
        if (child != null && child.layoutParams.height > height * 2) {
            child.layoutParams.height = height * 2
        }
    }

    // NestedScrollingParent

    override fun onStartNestedScroll(child: View, target: View, @ScrollAxis axes: Int): Boolean {
        val accept = super.onStartNestedScroll(child, target, axes)
        logD("onStartNestedScroll_p $accept child ${child.javaClass.simpleName} target ${target.javaClass.simpleName}")
        // true if this ViewParent accepts the nested scroll operation
        return accept
    }

    override fun onNestedScrollAccepted(child: View, target: View, @ScrollAxis axes: Int) {
        logD("onNestedScrollAccepted_p child ${child.javaClass.simpleName} target ${target.javaClass.simpleName}")
        super.onNestedScrollAccepted(child, target, axes)
    }

    override fun onStopNestedScroll(target: View) {
        logD("onStopNestedScroll_p ${target.javaClass.simpleName}")
        super.onStopNestedScroll(target)
    }

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
        logD("onNestedScroll_p ${target.javaClass.simpleName}")
        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        logD("onNestedPreScroll_p ${target.javaClass.simpleName}")
        super.onNestedPreScroll(target, dx, dy, consumed)
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        logD("onNestedPreFling_p ${target.javaClass.simpleName}")
        // true if this parent consumed the fling ahead of the target view
        return canScrollUp > 0 || super.onNestedPreFling(target, velocityX, velocityY)
    }

    override fun onNestedFling(target: View, velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        logD("onNestedFling_p ${target.javaClass.simpleName}")
        // true if this parent consumed or otherwise reacted to the fling
        childIsFling = true
        return super.onNestedFling(target, velocityX, velocityY, consumed)
    }

    override fun getNestedScrollAxes(): Int = ViewCompat.SCROLL_AXIS_VERTICAL

    // NestedScrollingParent2

    override fun onStartNestedScroll(child: View, target: View, @ScrollAxis axes: Int, @NestedScrollType type: Int): Boolean {
        val accept = super.onStartNestedScroll(child, target, axes, type)
        logD("onStartNestedScroll_p2 $accept child ${child.javaClass.simpleName} target ${target.javaClass.simpleName}")
        // true if this ViewParent accepts the nested scroll operation
        return accept
    }

    override fun onNestedScrollAccepted(child: View, target: View, @ScrollAxis axes: Int, @NestedScrollType type: Int) {
        logD("onNestedScrollAccepted_p2 child ${child.javaClass.simpleName} target ${target.javaClass.simpleName}")
        //super.onNestedScrollAccepted(child, target, axes, type)
    }

    override fun onStopNestedScroll(target: View, @NestedScrollType type: Int) {
        logD("onStopNestedScroll_p2 ${target.javaClass.simpleName}")
        //super.onStopNestedScroll(target, type)
        if (!scrollAlreadyStopped) {
            scrollAlreadyStopped = true
            listener?.onStopScroll()
        }
    }

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, @NestedScrollType type: Int) {
        logD("onNestedScroll_p2 ${target.javaClass.simpleName}")
        //super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, @NestedScrollType type: Int) {
        logD("onNestedPreScroll_p2 bottom $bottom child.bottom ${child.bottom} consumed ${consumed[1]} dy $dy ${target.javaClass.simpleName}")
        //super.onNestedPreScroll(target, dx, dy, consumed, type)
        val consume = min(canScrollUp, max(0, dy))
        consumed[1] = consume
        scrollBy(0, consume)
    }

    // NestedScrollingParent3

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, @NestedScrollType type: Int, consumed: IntArray) {
        logD("onNestedScroll_p3 ${target.javaClass.simpleName}")
        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type)
        scrollAlreadyStopped = false
    }

    // NestedScrollingChild

    override fun startNestedScroll(@ScrollAxis axes: Int): Boolean {
        logD("startNestedScroll_c")
        return super.startNestedScroll(axes)
    }

    override fun stopNestedScroll() {
        logD("stopNestedScroll_c")
        super.stopNestedScroll()
    }

    override fun hasNestedScrollingParent(): Boolean {
        val has = super.hasNestedScrollingParent()
        logD("hasNestedScrollingParent_c $has")
        return has
    }

    override fun dispatchNestedScroll(dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, offsetInWindow: IntArray?): Boolean {
        logD("dispatchNestedScroll_c")
        return super.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow)
    }

    override fun dispatchNestedPreScroll(dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?): Boolean {
        logD("dispatchNestedPreScroll_c")
        return super.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow)
    }

    override fun dispatchNestedFling(velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        logD("dispatchNestedFling_c")
        return super.dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        logD("dispatchNestedPreFling_c")
        return super.dispatchNestedPreFling(velocityX, velocityY)
    }

    // NestedScrollingChild2

    override fun startNestedScroll(@ScrollAxis axes: Int, @NestedScrollType type: Int): Boolean {
        val accept = super.startNestedScroll(axes, type)
        logD("startNestedScroll_c2 $accept")
        scrollAlreadyStopped = false
        return accept
    }

    override fun stopNestedScroll(@NestedScrollType type: Int) {
        logD("stopNestedScroll_c2")
        super.stopNestedScroll(type)
        if (!scrollAlreadyStopped) {
            scrollAlreadyStopped = true
            listener?.onStopScroll()
        }
    }

    override fun hasNestedScrollingParent(@NestedScrollType type: Int): Boolean {
        val has = super.hasNestedScrollingParent(type)
        logD("hasNestedScrollingParent_c2 $has")
        return has
    }

    override fun dispatchNestedScroll(dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, offsetInWindow: IntArray?, @NestedScrollType type: Int): Boolean {
        logD("dispatchNestedScroll_c2")
        return super.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type)
    }

    override fun dispatchNestedPreScroll(dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?, @NestedScrollType type: Int): Boolean {
        logD("dispatchNestedPreScroll_c2")
        return super.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type)
    }

    // NestedScrollingChild3

    override fun dispatchNestedScroll(dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, offsetInWindow: IntArray?, type: Int, consumed: IntArray) {
        logD("dispatchNestedScroll_c3")
        super.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type, consumed)
    }

    interface OnScrollStopListener {
        fun onStopScroll()
    }
}