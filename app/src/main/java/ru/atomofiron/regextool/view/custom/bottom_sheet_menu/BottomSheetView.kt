package ru.atomofiron.regextool.view.custom.bottom_sheet_menu

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.animation.Interpolator
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.common.util.findColorByAttr
import ru.atomofiron.regextool.utils.calculateDurationWithScale
import kotlin.math.max
import kotlin.math.min

class BottomSheetView : FrameLayout, ValueAnimator.AnimatorUpdateListener {
    companion object {
        private const val DURATION = 512L
        private const val FIRST = 0
        private const val TRANSPARENT = 0f
        private const val VISIBLE = 1f
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        overlay = View(context)
        overlay.isFocusable = true
        overlay.isClickable = true
        overlay.setBackgroundColor(context.findColorByAttr(R.attr.colorOverlay))
        overlay.layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
        overlay.alpha = TRANSPARENT
        addView(overlay)

        menu = RecyclerView(context)
        menu.setBackgroundResource(R.drawable.bg_bottom_sheet)
        menu.isNestedScrollingEnabled = false
        menu.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        menu.layoutManager = LinearLayoutManager(context)
        menu.adapter = BottomSheetViewAdapter()
        menu.clipToPadding = false
        menu.layoutParams = LayoutParams(MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.BOTTOM
        }
        val top = resources.getDimensionPixelSize(R.dimen.menu_corner_radius)
        val bottom = resources.getDimensionPixelSize(R.dimen.action_bar_size)
        menu.setPadding(0, top, 0, bottom)
        addView(menu)
    }

    private val overlay: View
    private val menu: RecyclerView

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

    private var animator = ValueAnimator.ofFloat(0f, 1f)

    fun show() = setState(State.OPEN)

    fun hide() = setState(State.CLOSE)

    private var state = State.CLOSED

    private val menuHeight: Int get() = menu.measuredHeight
    private val minTop: Int get() = measuredHeight - menu.measuredHeight
    private val maxTop: Int get() = measuredHeight
    private val curTop: Int get() = menu.top

    private val accelerateInterpolator = AccelerateInterpolator()
    private val decelerateInterpolator = DecelerateInterpolator()
    private val accelerateDecelerateInterpolator = AccelerateDecelerateInterpolator()

    private var lastMove = 0L
    private val duration: Long get() = context.calculateDurationWithScale(DURATION)
    private var frameTime: Long = 0L

    private fun setState(state: State) {
        val fromState = this.state
        this.state = state
        if (determineState(menu.top)) {
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

        if (state == State.OPENED || state == State.CLOSED) {
            speedPerFrame = 0f
            speedSum = 0f
            frameTime = (1000f / display.refreshRate).toLong()
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
                speedPerFrame = 0f
                speedSum = 0f
                lastY = event.y
                direction = Slide.UNDEFINED
                setState(State.REOPEN)
            }
            animator.isStarted -> Unit
            event.action == MotionEvent.ACTION_UP -> {
                hideIfSlideDown(event.y < minTop)
                // to avoid click on a menu item
                return direction != Slide.UNDEFINED
            }
            event.action == MotionEvent.ACTION_MOVE -> {
                if (System.currentTimeMillis() - lastMove > frameTime) {
                    lastMove = System.currentTimeMillis()
                    speedPerFrame = speedSum
                    speedSum = 0f
                }

                val speed = event.y - lastY
                speedSum += speed
                lastY = event.y
                direction = when {
                    speed < 0 -> Slide.UP
                    speed > 0 -> Slide.DOWN
                    else -> direction
                }

                setMenuTop(menu.top + speed.toInt())
            }
        }

        return default
    }

    private fun hideIfSlideDown(outside: Boolean = true) {
        when {
            direction == Slide.UP -> setState(State.OPEN)
            outside -> setState(State.CLOSE)
            menu.top == minTop -> setState(State.OPENED)
            menu.top == maxTop -> setState(State.CLOSED)
            direction != Slide.UP -> setState(State.CLOSE)
        }
    }

    @Suppress("UnnecessaryVariable", "NAME_SHADOWING")
    private fun setMenuTop(top: Int) {
        val parentHeight = measuredHeight
        val height = menuHeight
        val minTop = minTop
        val maxTop = maxTop
        val top = max(minTop, min(maxTop, top))
        if (menu.top != top) {
            menu.top = top
        }

        overlay.alpha = (parentHeight - curTop).toFloat() / height
        menu.alpha = VISIBLE

        determineState(top)
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        val value = animation.animatedValue as Int
        if (speedPerFrame == 0f) {
            setMenuTop(value)
        } else {
            val nextTopBySpeed = menu.top + speedPerFrame.toInt()
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
            menu.top = maxTop
        }
    }

    private enum class Slide {
        UNDEFINED, UP, DOWN
    }

    private enum class State {
        OPENED, CLOSED, OPEN, CLOSE, REOPEN
    }
}