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
import app.atomofiron.common.util.moveChildrenFrom
import ru.atomofiron.regextool.R
import kotlin.math.max
import kotlin.math.min

open class BottomSheetView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    companion object {
        private const val SECOND = 1000L
        private const val DURATION = 256L
        private const val FIRST = 0
        private const val CONTENT_VIEW_INDEX = 1
        private const val UNDEFINED = -1
        private const val VISIBLE = 1f
    }

    init {
        moveChildrenFrom(R.layout.view_bottom_sheet)
    }

    var onClosedToOpenedListener: () -> Unit = { }

    private val overlay: View = findViewById(R.id.bottom_sheet_overlay)
    private val viewContainer: ViewGroup = findViewById(R.id.bottom_container)
    val anchorView: View get() = viewContainer
    lateinit var contentView: View private set
    var contentViewId: Int = UNDEFINED; private set

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

    private var frameLoop = ValueAnimator.ofFloat(0f, 1f)
    private var animator = ValueAnimator.ofFloat(0f, 1f)
    protected var state = State.CLOSED; private set
    private var keepOverlayVisible = false
    private var notifyWhenOpened = false

    private val menuHeight: Int get() = viewContainer.measuredHeight
    private val minTop: Int get() = measuredHeight - viewContainer.measuredHeight
    private val maxTop: Int get() = measuredHeight
    private val curTop: Int get() = viewContainer.top

    private val accelerateInterpolator = AccelerateInterpolator()
    private val decelerateInterpolator = DecelerateInterpolator()
    private val accelerateDecelerateInterpolator = AccelerateDecelerateInterpolator()

    private val loopListener: (ValueAnimator) -> Unit = ::loop
    private val animatorListener: (ValueAnimator) -> Unit = ::onAnimationUpdate

    init {
        frameLoop.duration = SECOND
        frameLoop.repeatMode = ValueAnimator.RESTART
        frameLoop.repeatCount = ValueAnimator.INFINITE
        frameLoop.start()

        val widthPixels = resources.displayMetrics.widthPixels
        val maxWidth = resources.getDimensionPixelOffset(R.dimen.bottom_sheet_view_max_width)
        viewContainer.layoutParams.width = Math.min(widthPixels, maxWidth)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        frameLoop.addUpdateListener(loopListener)
        animator.addUpdateListener(animatorListener)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        frameLoop.removeUpdateListener(loopListener)
        animator.removeUpdateListener(animatorListener)
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

        view.overScrollMode = View.OVER_SCROLL_NEVER
        view.isNestedScrollingEnabled = false
        (view as? ViewGroup)?.clipToPadding = false

        var bottom = resources.getDimensionPixelSize(R.dimen.bottom_tab_bar_height)
        when {
            view.paddingBottom < bottom -> bottom += view.paddingBottom
            view.paddingBottom > bottom -> bottom = view.paddingBottom
        }
        view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, bottom)
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
        if (!isReopen && determineState(viewContainer.top)) {
            return
        }

        when (fromState) {
            State.OPEN -> when (state) {
                State.OPENED -> animator.cancel()
                State.CLOSED -> throw UnsupportedOperationException()
                State.OPEN -> Unit
                State.CLOSE -> startAnimator(curTop + menuHeight, accelerateInterpolator)
                State.SCROLL -> animator.cancel()
            }
            State.OPENED -> when (state) {
                State.OPENED -> Unit
                State.CLOSED -> throw UnsupportedOperationException()
                State.OPEN -> throw UnsupportedOperationException()
                State.CLOSE -> startAnimator(curTop + menuHeight, accelerateInterpolator)
                State.SCROLL -> animator.cancel()
            }
            State.CLOSE -> when (state) {
                State.OPENED -> throw UnsupportedOperationException()
                State.CLOSED -> animator.cancel()
                State.OPEN -> {
                    keepOverlayVisible = true
                    setMenuTop(maxTop)
                    startAnimator(minTop, decelerateInterpolator)
                }
                State.CLOSE -> Unit
                State.SCROLL -> animator.cancel()
            }
            State.CLOSED -> when (state) {
                State.OPENED -> throw UnsupportedOperationException()
                State.CLOSED -> Unit
                State.OPEN -> startAnimator(minTop, decelerateInterpolator)
                State.CLOSE -> throw UnsupportedOperationException()
                State.SCROLL -> Unit
            }
            State.SCROLL -> when (state) {
                State.OPENED -> Unit
                State.CLOSED -> Unit
                State.OPEN -> startAnimator(minTop, accelerateDecelerateInterpolator)
                State.CLOSE -> startAnimator(curTop + menuHeight, accelerateInterpolator)
                State.SCROLL -> Unit
            }
        }

        when (state) {
            State.SCROLL -> frameLoop.resume()
            else -> frameLoop.pause()
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
    private fun determineState(menuTop: Int): Boolean {
        when {
            state == State.CLOSE && menuTop >= maxTop -> setState(State.CLOSED)
            state == State.OPEN && menuTop <= minTop -> setState(State.OPENED)
            else -> return false
        }
        return true
    }

    private fun startAnimator(to: Int, interpolator: Interpolator) {
        animator.removeUpdateListener(animatorListener)
        animator.cancel()
        animator = ValueAnimator.ofInt(curTop, to)
        animator.duration = DURATION
        animator.interpolator = interpolator
        animator.addUpdateListener(animatorListener)
        animator.start()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return false//super.onTouchEvent(event)
    }


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
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        val default = false//super.onInterceptTouchEvent(event)
        if (event.getPointerId(event.actionIndex) != FIRST) {
            return default
        }
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
    }

    private fun loop(animator: ValueAnimator) {
        speedPerFrame = speedSum
        speedSum = 0f

        if (state == State.SCROLL) {
            setMenuTop(viewContainer.top + speedPerFrame.toInt())
        }
    }

    private fun hideIfSlideDown(outside: Boolean = true) {
        when {
            direction == Slide.UP -> setState(State.OPEN)
            outside -> setState(State.CLOSE)
            viewContainer.top == minTop -> setState(State.OPENED)
            viewContainer.top == maxTop -> setState(State.CLOSED)
            direction == Slide.DOWN -> setState(State.CLOSE)
            direction == Slide.UNDEFINED -> setState(State.OPEN)
        }
    }

    @Suppress("UnnecessaryVariable", "NAME_SHADOWING")
    private fun setMenuTop(top: Int) {
        val parentHeight = measuredHeight
        val height = menuHeight
        val minTop = minTop
        val maxTop = maxTop
        val top = max(minTop, min(maxTop, top))
        if (viewContainer.top != top) {
            viewContainer.top = top
        }

        overlay.alpha = when (keepOverlayVisible) {
            true -> VISIBLE
            else -> (parentHeight - curTop).toFloat() / height
        }
        viewContainer.alpha = VISIBLE

        determineState(top)
    }

    private fun onAnimationUpdate(animation: ValueAnimator) {
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
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (state == State.CLOSED) {
            viewContainer.top = maxTop
        }
    }

    private enum class Slide {
        UNDEFINED, UP, DOWN
    }

    protected enum class State {
        OPENED, CLOSED, OPEN, CLOSE, SCROLL
    }
}