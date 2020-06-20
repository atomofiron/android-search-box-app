package ru.atomofiron.regextool.screens.root

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import ru.atomofiron.regextool.logD

class BottomSheetBehavior : CoordinatorLayout.Behavior<FrameLayout> {
    constructor() : super()

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: FrameLayout, dependency: View): Boolean {
        logD("onDependentViewChanged")
        return super.onDependentViewChanged(parent, child, dependency)
    }

    override fun onDependentViewRemoved(parent: CoordinatorLayout, child: FrameLayout, dependency: View) {
        logD("onDependentViewRemoved")
        super.onDependentViewRemoved(parent, child, dependency)
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: FrameLayout, layoutDirection: Int): Boolean {
        logD("onLayoutChild")
        return super.onLayoutChild(parent, child, layoutDirection)
    }

    override fun onMeasureChild(parent: CoordinatorLayout, child: FrameLayout, parentWidthMeasureSpec: Int, widthUsed: Int, parentHeightMeasureSpec: Int, heightUsed: Int): Boolean {
        logD("onMeasureChild")
        return super.onMeasureChild(parent, child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed)
    }

    override fun onNestedPreFling(coordinatorLayout: CoordinatorLayout, child: FrameLayout, target: View, velocityX: Float, velocityY: Float): Boolean {
        logD("onNestedPreFling velocityY $velocityY")
        return super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY)
    }

    override fun onNestedFling(coordinatorLayout: CoordinatorLayout, child: FrameLayout, target: View, velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        logD("onNestedFling velocityY $velocityY")
        return super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed)
    }

    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: FrameLayout, target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        logD("onNestedPreScroll dy $dy")
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
    }

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: FrameLayout, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int, consumed: IntArray) {
        logD("onNestedScroll dyConsumed $dyConsumed dyUnconsumed $dyUnconsumed")
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, consumed)
    }

    override fun onNestedScrollAccepted(coordinatorLayout: CoordinatorLayout, child: FrameLayout, directTargetChild: View, target: View, axes: Int, type: Int) {
        logD("onNestedScrollAccepted")
        super.onNestedScrollAccepted(coordinatorLayout, child, directTargetChild, target, axes, type)
    }

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: FrameLayout, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
        logD("onStartNestedScroll")
        return super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type)
    }

    override fun onStopNestedScroll(coordinatorLayout: CoordinatorLayout, child: FrameLayout, target: View, type: Int) {
        logD("onStopNestedScroll")
        super.onStopNestedScroll(coordinatorLayout, child, target, type)
    }

    override fun layoutDependsOn(parent: CoordinatorLayout, child: FrameLayout, dependency: View): Boolean {
        logD("layoutDependsOn dependency ${dependency.javaClass.simpleName}")
        return dependency is CoordinatorLayout
    }

    override fun blocksInteractionBelow(parent: CoordinatorLayout, child: FrameLayout): Boolean {
        return super.blocksInteractionBelow(parent, child)
    }
}