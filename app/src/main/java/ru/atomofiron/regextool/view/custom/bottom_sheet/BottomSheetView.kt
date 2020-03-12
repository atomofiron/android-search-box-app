package ru.atomofiron.regextool.view.custom.bottom_sheet

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.util.calculateDurationWithScale
import app.atomofiron.common.util.findColorByAttr
import ru.atomofiron.regextool.R
import kotlin.math.max
import kotlin.math.min

open class BottomSheetView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), ValueAnimator.AnimatorUpdateListener {
    companion object {
        private const val SECOND = 1000L
        private const val DURATION = 512L
        private const val FIRST = 0
        private const val TRANSPARENT = 0f
        private const val VISIBLE = 1f
    }

    private val overlay: View = View(context)
    private lateinit var view: View

    private var lastY = 0f
    private var speedSum = 0f
    private var speedPerFrame = 0f
    private var direction = Slide.UNDEFINED

    val isSheetShown: Boolean get() = when (state) {
        State.OPENED -> true
        State.OPEN -> true
        State.REOPEN -> true
        State.CLOSE -> false
        State.CLOSED -> false
    }

    private var loop = ValueAnimator.ofFloat(0f, 1f)
    private var animator = ValueAnimator.ofFloat(0f, 1f)
    private var state = State.CLOSED

    private val menuHeight: Int get() = view.measuredHeight
    private val minTop: Int get() = measuredHeight - view.measuredHeight
    private val maxTop: Int get() = measuredHeight
    private val curTop: Int get() = view.top

    private val accelerateInterpolator = AccelerateInterpolator()
    private val decelerateInterpolator = DecelerateInterpolator()
    private val accelerateDecelerateInterpolator = AccelerateDecelerateInterpolator()

    private val duration: Long get() = context.calculateDurationWithScale(DURATION)

    init {
        overlay.isFocusable = true
        overlay.isClickable = true
        overlay.setBackgroundColor(context.findColorByAttr(R.attr.colorOverlay))
        overlay.layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
        overlay.alpha = TRANSPARENT
        addView(overlay, 0)

        loop.duration = SECOND
        loop.repeatMode = ValueAnimator.RESTART
        loop.repeatCount = ValueAnimator.INFINITE
        loop.addUpdateListener { loop() }
        loop.start()
    }

    fun setView(id: Int) {
        val view = LayoutInflater.from(context).inflate(id, null, false)
        setView(view)
    }

    fun setView(view: View) {
        this.view = view
        view.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        view.setBackgroundResource(R.drawable.bg_bottom_sheet)
        view.isNestedScrollingEnabled = false

        val widthPixels = resources.displayMetrics.widthPixels
        val maxWidth = resources.getDimensionPixelOffset(R.dimen.bottom_sheet_view_max_width)
        val width = Math.min(widthPixels, maxWidth)
        view.layoutParams = LayoutParams(width, LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        }
        val top = resources.getDimensionPixelSize(R.dimen.menu_corner_radius)
        val bottom = resources.getDimensionPixelSize(R.dimen.bottom_tab_bar_height)
        view.setPadding(0, top, 0, bottom)
        addView(view)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (childCount > 1) {
            view = getChildAt(1)
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
        val fromState = this.state
        this.state = state
        if (determineState(view.top)) {
            return
        }

        when (fromState) {
            State.OPEN -> when (state) {
                State.OPENED -> animator.cancel()
                State.CLOSED -> throw UnsupportedOperationException()
                State.OPEN -> Unit
                State.CLOSE -> startAnimator(curTop + menuHeight, accelerateInterpolator)
                State.REOPEN -> animator.cancel()
            }
            State.OPENED -> when (state) {
                State.OPENED -> Unit
                State.CLOSED -> throw UnsupportedOperationException()
                State.OPEN -> throw UnsupportedOperationException()
                State.CLOSE -> startAnimator(curTop + menuHeight, accelerateInterpolator)
                State.REOPEN -> animator.cancel()
            }
            State.CLOSE -> when (state) {
                State.OPENED -> throw UnsupportedOperationException()
                State.CLOSED -> animator.cancel()
                State.OPEN -> throw UnsupportedOperationException()
                State.CLOSE -> Unit
                State.REOPEN -> animator.cancel()
            }
            State.CLOSED -> when (state) {
                State.OPENED -> throw UnsupportedOperationException()
                State.CLOSED -> Unit
                State.OPEN -> startAnimator(minTop, decelerateInterpolator)
                State.CLOSE -> throw UnsupportedOperationException()
                State.REOPEN -> throw UnsupportedOperationException()
            }
            State.REOPEN -> when (state) {
                State.OPENED -> Unit
                State.CLOSED -> Unit
                State.OPEN -> startAnimator(minTop, accelerateDecelerateInterpolator)
                State.CLOSE -> startAnimator(curTop + menuHeight, accelerateInterpolator)
                State.REOPEN -> Unit
            }
        }

        when (state) {
            State.REOPEN -> loop.resume()
            else -> loop.pause()
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
        animator = ValueAnimator.ofInt(curTop, to)
        animator.duration = duration
        animator.interpolator = interpolator
        animator.addUpdateListener(this)
        animator.start()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return false//super.onTouchEvent(event)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        val default = false//super.onInterceptTouchEvent(event)
        if (event.getPointerId(event.actionIndex) != FIRST) {
            return default
        }
        if (state == State.CLOSED) {
            return true
        }

        when {
            event.action == MotionEvent.ACTION_DOWN -> {
                lastY = event.y
                direction = Slide.UNDEFINED
                setState(State.REOPEN)
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

    private fun loop() {
        speedPerFrame = speedSum
        speedSum = 0f

        if (state == State.REOPEN) {
            setMenuTop(view.top + speedPerFrame.toInt())
        }
    }

    private fun hideIfSlideDown(outside: Boolean = true) {
        when {
            direction == Slide.UP -> setState(State.OPEN)
            outside -> setState(State.CLOSE)
            view.top == minTop -> setState(State.OPENED)
            view.top == maxTop -> setState(State.CLOSED)
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
        if (view.top != top) {
            view.top = top
        }

        overlay.alpha = (parentHeight - curTop).toFloat() / height
        view.alpha = VISIBLE

        determineState(top)
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        val value = animation.animatedValue as Int
        if (speedPerFrame == 0f) {
            setMenuTop(value)
        } else {
            val nextTopBySpeed = view.top + speedPerFrame.toInt()
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
            view.top = maxTop
        }
    }

    private enum class Slide {
        UNDEFINED, UP, DOWN
    }

    private enum class State {
        OPENED, CLOSED, OPEN, CLOSE, REOPEN
    }
}