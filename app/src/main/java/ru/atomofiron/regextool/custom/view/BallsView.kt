package ru.atomofiron.regextool.custom.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import app.atomofiron.common.util.findColorByAttr
import ru.atomofiron.regextool.R

class BallsView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), ValueAnimator.AnimatorUpdateListener {
    companion object {
        private const val DURATION = 512L
    }

    private val animator = ValueAnimator.ofFloat(0f, Math.PI.toFloat())
    private var radians = 0.0
    private val paintMask = Paint()
    private val paintCircle = Paint()
    private val paintColoredMask = Paint()

    init {
        animator.duration = DURATION
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.RESTART
        animator.interpolator = LinearInterpolator()

        paintMask.color = Color.BLACK
        paintCircle.color = Color.BLACK
        paintCircle.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        paintColoredMask.color = context.findColorByAttr(R.attr.colorAccent)
        paintColoredMask.xfermode = PorterDuffXfermode(PorterDuff.Mode.XOR)

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
        when (isAttachedToWindow && visibility == VISIBLE) {
            true -> animator.start()
            else -> animator.pause()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2
        val centerY = height / 2
        val size = Math.min(width, height).toFloat()
        val radius = size / 6
        val radiusRotate = size / 3
        val radiusMask = size / 2
        val sin = Math.sin(radians) * radiusRotate
        val cos = Math.cos(radians) * radiusRotate
        val x1 = centerX + cos
        val y1 = centerY + sin
        val x2 = centerX - cos
        val y2 = centerY - sin
        canvas.drawCircle(centerX.toFloat(), centerY - radiusMask - radius, radiusMask, paintMask)
        canvas.drawCircle(centerX.toFloat(), centerY + radiusMask + radius, radiusMask, paintMask)
        canvas.drawCircle(x1.toFloat(), y1.toFloat(), radius, paintCircle)
        canvas.drawCircle(x2.toFloat(), y2.toFloat(), radius, paintCircle)
        canvas.drawCircle(centerX.toFloat(), centerY - radiusMask - radius, radiusMask, paintColoredMask)
        canvas.drawCircle(centerX.toFloat(), centerY + radiusMask + radius, radiusMask, paintColoredMask)
    }

    override fun onAnimationUpdate(animator: ValueAnimator) {
        radians = (animator.animatedValue as Float).toDouble()
        invalidate()
    }
}