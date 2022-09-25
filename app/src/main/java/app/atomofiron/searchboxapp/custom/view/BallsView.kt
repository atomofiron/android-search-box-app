package app.atomofiron.searchboxapp.custom.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.searchboxapp.R
import kotlin.math.min

class BallsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), ValueAnimator.AnimatorUpdateListener {
    companion object {
        private const val DURATION = 1024L
    }

    private val oneBall: Boolean
    private val animator = ValueAnimator.ofFloat(0f, (Math.PI * 2).toFloat())
    private var radians = 0.0
    private val paintCircle = Paint()
    private val paintBall = Paint()

    private val ballCirclePath = Path()

    init {
        val styled = context.obtainStyledAttributes(attrs, R.styleable.BallsView, defStyleAttr, 0)
        oneBall = styled.getBoolean(R.styleable.BallsView_oneBall, true)
        styled.recycle()

        animator.duration = DURATION
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.RESTART
        animator.interpolator = LinearInterpolator()

        val colorAccent = context.findColorByAttr(R.attr.colorAccent)
        paintCircle.color = colorAccent
        paintBall.color = colorAccent
        if (oneBall) {
            paintBall.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }

        paintBall.isAntiAlias = true
        paintCircle.isAntiAlias = true

        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        animator.addUpdateListener(this)
        updateAnimation()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator.removeUpdateListener(this)
        updateAnimation()
    }

    override fun setVisibility(visibility: Int) {
        if (this.visibility != visibility) {
            super.setVisibility(visibility)
            updateAnimation()
        }
    }

    private fun updateAnimation() {
        val enable = isAttachedToWindow && visibility == VISIBLE
        when {
            !enable -> animator.pause()
            animator.isPaused -> animator.resume()
            !animator.isStarted -> animator.start()
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        val width = (right - left).toFloat()
        val height = (bottom - top).toFloat()
        val centerX = width / 2
        val centerY = height / 2
        var size = min(width, height)
        size -= size % 2
        val radius = size / 6
        val radiusMask = size / 2
        ballCirclePath.addCircle(centerX, centerY - radiusMask - radius, radiusMask, Path.Direction.CW)
        ballCirclePath.addCircle(centerX, centerY + radiusMask + radius, radiusMask, Path.Direction.CW)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width.toFloat() / 2
        val centerY = height.toFloat() / 2
        var size = Math.min(width, height).toFloat()
        size -= size % 2
        val radius = size / 6
        val radiusRotate = size / (if (oneBall) 4 else 3)
        val radiusMask = size / 2
        val sin = Math.sin(radians) * radiusRotate
        val cos = Math.cos(radians) * radiusRotate
        val x1 = centerX + cos
        val y1 = centerY + sin

        if (oneBall) {
            canvas.drawCircle(centerX, centerY, radiusMask, paintCircle)
            canvas.drawCircle(x1.toFloat(), y1.toFloat(), radius, paintBall)
        } else {
            val x2 = centerX - cos
            val y2 = centerY - sin
            canvas.clipPath(ballCirclePath)
            canvas.drawCircle(x1.toFloat(), y1.toFloat(), radius, paintBall)
            canvas.drawCircle(x2.toFloat(), y2.toFloat(), radius, paintBall)
        }
    }

    override fun onAnimationUpdate(animator: ValueAnimator) {
        radians = (animator.animatedValue as Float).toDouble()
        invalidate()
    }
}