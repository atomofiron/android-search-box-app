package ru.atomofiron.regextool.custom.view.bottom_sheet

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.widget.FrameLayout
import androidx.core.view.NestedScrollingParent3
import androidx.core.view.ViewCompat
import androidx.core.view.ViewCompat.NestedScrollType
import androidx.core.view.ViewCompat.ScrollAxis
import androidx.core.widget.NestedScrollView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.common.util.moveChildrenFrom
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.logD
import kotlin.math.max
import kotlin.math.min

class BottomScrollingSheetView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), SheetScrollView.OnScrollListener {

    companion object {
        private const val SECOND = 1000L
        private const val DURATION = 256L
        private const val FIRST = 0
        private const val CONTENT_VIEW_INDEX = 1
        private const val UNDEFINED = -1
        private const val VISIBLE = 1f
    }

    init {
        moveChildrenFrom(R.layout.view_scroll_bottom_sheet)
    }

    var onClosedToOpenedListener: () -> Unit = { }

    private val overlay: View = findViewById(R.id.bottom_sheet_overlay)
    private val viewScroll: SheetScrollView = findViewById(R.id.bottom_scrolling)
    private val viewContainer: ViewGroup = findViewById(R.id.bottom_container)
    lateinit var contentView: View private set
    private var contentViewId: Int = UNDEFINED; private set

    private var lastY = 0f
    private var speedSum = 0f
    private var speedPerFrame = 0f
    private var direction = Slide.UNDEFINED
    private var allowNestedScrolling = false

    val isSheetShown: Boolean get() = when (state) {
        State.OPENED -> true
        State.OPEN -> true
        State.SCROLL -> true
        State.CLOSE -> false
        State.CLOSED -> false
    }

    protected var state = State.CLOSED; private set
    private var keepOverlayVisible = false
    private var notifyWhenOpened = false

    private val menuHeight: Int get() = viewContainer.measuredHeight
    private val scrollOpened: Int get() = -menuHeight
    private val scrollClosed: Int = 0

    init {
        val widthPixels = resources.displayMetrics.widthPixels
        val heightPixels = resources.displayMetrics.heightPixels
        val sizePixels = min(widthPixels, heightPixels)
        val maxWidth = resources.getDimensionPixelOffset(R.dimen.bottom_sheet_view_max_width)
        viewContainer.layoutParams.width = min(sizePixels, maxWidth)
    }

    override fun onStartScroll() {
    }

    override fun onStopScroll() {
    }

    override fun onScroll(dy: Int) {
    }

    private fun clearContainer() {
        while (viewContainer.childCount > CONTENT_VIEW_INDEX) {
            viewContainer.removeViewAt(CONTENT_VIEW_INDEX)
        }
    }

    fun setView(id: Int): View {
        if (id != contentViewId) {
            clearContainer()
            LayoutInflater.from(context).inflate(id, viewContainer)
            setView(viewContainer.getChildAt(CONTENT_VIEW_INDEX))
            contentViewId = id
        }
        return contentView
    }

    fun setView(view: View) {
        when  {
            !::contentView.isInitialized -> Unit
            contentView === view -> return
            viewContainer.getChildAt(CONTENT_VIEW_INDEX) !== view -> clearContainer()
            else -> Unit
        }
        contentViewId = UNDEFINED
        contentView = view

        val tag = context.getString(R.string.bottom_sheet_main_view_tag)
        val mainView = view.findViewWithTag(tag) ?: view
        mainView.overScrollMode = View.OVER_SCROLL_NEVER
        mainView.isNestedScrollingEnabled = false
        (mainView as? ViewGroup)?.clipToPadding = false

        var bottom = resources.getDimensionPixelSize(R.dimen.bottom_tab_bar_height)
        when {
            mainView.paddingBottom < bottom -> bottom += mainView.paddingBottom
            mainView.paddingBottom > bottom -> bottom = mainView.paddingBottom
        }
        mainView.setPadding(mainView.paddingLeft, mainView.paddingTop, mainView.paddingRight, bottom)

        when {
            view.parent == null -> viewContainer.addView(view)
            view.parent === viewContainer -> Unit
            else -> throw Exception("View already has another parent!")
        }
    }

    fun show() = setState(State.OPEN)

    /** @return sheet was opened */
    fun hide(): Boolean {
        val wasSheetShown = isSheetShown
        setState(State.CLOSE)
        return wasSheetShown && !isSheetShown
    }

    private fun setState(state: State) {
        keepOverlayVisible = false
        val fromState = this.state
        this.state = state
        val isReopen = fromState == State.CLOSE && state == State.OPEN
        when {
            isReopen -> Unit
            determineState() -> return
        }

        when (fromState) {
            State.OPEN -> when (state) {
                State.OPENED -> Unit
                State.CLOSED -> throw UnsupportedOperationException()
                State.OPEN -> Unit
                State.CLOSE -> startAnimator()
                State.SCROLL -> Unit
            }
            State.OPENED -> when (state) {
                State.OPENED -> Unit
                State.CLOSED -> throw UnsupportedOperationException()
                State.OPEN -> throw UnsupportedOperationException()
                State.CLOSE -> startAnimator()
                State.SCROLL -> Unit
            }
            State.CLOSE -> when (state) {
                State.OPENED -> throw UnsupportedOperationException()
                State.CLOSED -> Unit
                State.OPEN -> {
                    keepOverlayVisible = true
                    // todo scroll to closed setMenuTop(maxTop)
                    startAnimator()
                }
                State.CLOSE -> Unit
                State.SCROLL -> Unit
            }
            State.CLOSED -> when (state) {
                State.OPENED -> throw UnsupportedOperationException()
                State.CLOSED -> Unit
                State.OPEN -> startAnimator()
                State.CLOSE -> throw UnsupportedOperationException()
                State.SCROLL -> Unit
            }
            State.SCROLL -> when (state) {
                State.OPENED -> Unit
                State.CLOSED -> Unit
                State.OPEN -> startAnimator()
                State.CLOSE -> startAnimator()
                State.SCROLL -> Unit
            }
        }

        when (state) {
            State.CLOSE, State.CLOSED -> notifyWhenOpened = true
            State.OPENED -> if (notifyWhenOpened) {
                notifyWhenOpened = false
                onClosedToOpenedListener.invoke()
            }
            else -> Unit
        }
    }

    /** @return state was determined */
    private fun determineState(): Boolean {
        when {
            state == State.CLOSE && viewScroll.scrollY == scrollClosed -> setState(State.CLOSED)
            state == State.OPEN && viewScroll.scrollY == scrollOpened -> setState(State.OPENED)
            else -> return false
        }
        return true
    }

    private fun startAnimator() {
        // todo
        when (state) {
            State.CLOSE -> viewScroll.smoothScrollTo(0, scrollClosed)
            State.OPEN -> viewScroll.smoothScrollTo(0, scrollOpened)
            else -> throw Exception()
        }
    }
/*
    override fun onStartNestedScroll(child: View?, target: View?, nestedScrollAxes: Int): Boolean {
        allowNestedScrolling = true
        return true
    }

    override fun onStopNestedScroll(child: View) {
        when {
            !allowNestedScrolling -> hideIfSlideDown(outside = false)
            else -> allowNestedScrolling = false
        }
        super.onStopNestedScroll(child)
    }

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
        if (dyUnconsumed < 0) {
            allowNestedScrolling = false
            setState(State.SCROLL)
        }

        var topDif = -dyUnconsumed
        when {
            !allowNestedScrolling -> {
                direction = when {
                    dyUnconsumed > 0 -> Slide.UP
                    dyUnconsumed < 0 -> Slide.DOWN
                    dyConsumed > 0 -> Slide.UP
                    dyConsumed < 0 -> Slide.DOWN
                    else -> direction
                }
                topDif -= dyConsumed
            }
        }
        setMenuTop(viewContainer.top + topDif)
        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed)
    }*/

/*    fun onInterceptTouchEv(event: MotionEvent): Boolean {
        val default = false//super.onInterceptTouchEvent(event)
        if (state == State.CLOSED) {
            return true
        }
        if (allowNestedScrolling) {
            return default
        }

        when {
            event.action == MotionEvent.ACTION_DOWN -> {
                lastY = event.y
                direction = Slide.UNDEFINED
                setState(State.SCROLL)
            }
            animator.isStarted -> Unit
            event.action == MotionEvent.ACTION_MOVE -> {
                val speed = event.y - lastY
                speedSum += speed
                lastY = event.y
                direction = when {
                    speed < 0 -> Slide.UP
                    speed > 16 -> Slide.DOWN
                    else -> direction
                }
            }
            event.action == MotionEvent.ACTION_UP -> {
                hideIfSlideDown(event.y < minTop)
                // to avoid click on a menu item
                return direction != Slide.UNDEFINED
            }
        }

        return default
    }*/

    private fun hideIfSlideDown(outside: Boolean = true) {
        when {
            direction == Slide.UP -> setState(State.OPEN)
            outside -> setState(State.CLOSE)
            viewScroll.scrollY == -viewContainer.height -> setState(State.OPENED)
            viewScroll.scrollY == 0 -> setState(State.CLOSED)
            direction == Slide.DOWN -> setState(State.CLOSE)
            direction == Slide.UNDEFINED -> setState(State.OPEN)
        }
    }

/*    private fun onAnimationUpdate(animation: ValueAnimator) {
        val value = animation.animatedValue as Int
        if (speedPerFrame == 0f) {
            setMenuTop(value)
        } else {
            val nextTopBySpeed = viewContainer.top + speedPerFrame.toInt()
            val overrideOpen = state == State.OPEN && value > nextTopBySpeed
            val overrideClose = state == State.CLOSE && value < nextTopBySpeed
            if (overrideOpen || overrideClose) {
                setMenuTop(nextTopBySpeed)
            } else {
                setMenuTop(value)
            }
        }
    }*/

    private enum class Slide {
        UNDEFINED, UP, DOWN
    }

    protected enum class State {
        OPENED, CLOSED, OPEN, CLOSE, SCROLL
    }
}