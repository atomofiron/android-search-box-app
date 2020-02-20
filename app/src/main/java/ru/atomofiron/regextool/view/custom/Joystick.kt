package ru.atomofiron.regextool.view.custom

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import ru.atomofiron.regextool.R

class Joystick @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {
    private val image = ContextCompat.getDrawable(context, R.drawable.ic_circle)

    private val blurPaint = Paint()
    private val defaultPaint = Paint()

    private var trackTouchEvent = false

    private val maxBlurRadius = 4 * resources.displayMetrics.density
    private var brightnes = 0f
    private val glowAnimator = ValueAnimator.ofFloat(0f, (Math.PI / 2).toFloat())

    init {
        background = ContextCompat.getDrawable(
                context,
                R.drawable.ic_circle
        )
        setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_esc))
        defaultPaint.color = ContextCompat.getColor(context, R.color.red_soft)
        glowAnimator.duration = 256L
        glowAnimator.addUpdateListener { animator ->
            val value = animator.animatedValue as Float
            brightnes = 1f - Math.sin(value.toDouble()).toFloat()
            invalidate()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                glowAnimator.cancel()
                trackTouchEvent = true
                brightnes = 1f
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                if (trackTouchEvent) {
                    val contains = event.x > 0f && event.y > 0f &&
                            event.x.toInt() < width && event.y.toInt() < height

                    if (!contains) {
                        trackTouchEvent = false
                        glowAnimator.start()
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                if (trackTouchEvent) {
                    glowAnimator.start()
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        invalidate()
        return super.performClick()
    }

    override fun draw(canvas: Canvas) {
        val width = measuredWidth
        val height = measuredHeight

        if (brightnes != 0f) {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val mCanvas = Canvas(bitmap)
            background.draw(mCanvas)
            blurPaint.maskFilter = BlurMaskFilter(maxBlurRadius * brightnes, BlurMaskFilter.Blur.NORMAL)
            val glow = bitmap.extractAlpha(blurPaint, IntArray(2))
            canvas.drawBitmap(glow, (width - glow.width).toFloat() / 2, (height - glow.height).toFloat() / 2, defaultPaint)
        }
        super.draw(canvas)
    }
}