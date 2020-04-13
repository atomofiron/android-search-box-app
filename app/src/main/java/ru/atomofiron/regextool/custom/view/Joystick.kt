package ru.atomofiron.regextool.custom.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import app.atomofiron.common.util.findBooleanByAttr
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.model.JoystickComposition

class Joystick @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    companion object {
        private const val GLOW_DURATION = 256L
        private const val BLUR_RADIUS_DP = 4f
    }
    private val circle = ContextCompat.getDrawable(context, R.drawable.ic_joystick)!!
    private val icon = ContextCompat.getDrawable(context, R.drawable.ic_esc)!!
    private val iconSize = resources.getDimensionPixelSize(R.dimen.icon_size)

    private val paintBlur = Paint()
    private val glowingPaint = Paint()

    private var trackTouchEvent = false

    private val density = resources.displayMetrics.density
    private val maxBlurRadius = BLUR_RADIUS_DP * density
    private var brightness = 0f
    private val glowAnimator = ValueAnimator.ofFloat(0f, (Math.PI / 2).toFloat())

    private lateinit var composition: JoystickComposition
    private lateinit var bitmap: Bitmap

    init {
        glowAnimator.duration = GLOW_DURATION
        glowAnimator.addUpdateListener { animator ->
            val value = animator.animatedValue as Float
            brightness = 1f - Math.sin(value.toDouble()).toFloat()
            invalidate()
        }
    }

    private fun initBitmap() {
        val width = measuredWidth
        val currentBitmap = if (::bitmap.isInitialized) bitmap else null
        when {
            width == 0 -> return
            currentBitmap?.width == width -> Unit
            else -> {
                currentBitmap?.recycle()
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            }
        }
    }

    fun setComposition(composition: JoystickComposition = this.composition) {
        this.composition = composition
        val isDark = context.findBooleanByAttr(R.attr.isDarkTheme)

        val circleColor = composition.color(isDark)
        circle.colorFilter = PorterDuffColorFilter(circleColor, PorterDuff.Mode.SRC_IN)
        glowingPaint.color = composition.glow(isDark)

        val limit = if (isDark) 0.4 else 0.6
        val isCircleLight = ColorUtils.calculateLuminance(circleColor) > limit
        val iconColor = when {
            isCircleLight -> ContextCompat.getColor(context, R.color.black)
            else -> ContextCompat.getColor(context, R.color.white)
        }
        icon.colorFilter = PorterDuffColorFilter(iconColor, PorterDuff.Mode.SRC_IN)
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                glowAnimator.cancel()
                trackTouchEvent = true
                brightness = 1f
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

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        initBitmap()

        val width = measuredWidth
        val height = measuredHeight

        if (brightness != 0f) {
            val mCanvas = Canvas(bitmap)
            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            circle.setBounds(0, 0, width, height)
            circle.draw(mCanvas)
            paintBlur.maskFilter = BlurMaskFilter(maxBlurRadius * brightness, BlurMaskFilter.Blur.NORMAL)
            val glowing = bitmap.extractAlpha(paintBlur, IntArray(2))
            val left = (width - glowing.width).toFloat() / 2
            val top = (height - glowing.height).toFloat() / 2
            canvas.drawBitmap(glowing, left, top, glowingPaint)
        }
        val offset = (density * brightness).toInt()
        circle.setBounds(offset, offset, width - offset, height - offset)
        circle.draw(canvas)

        val radius = iconSize / 2
        val left = width / 2 - radius + offset
        val top = height / 2 - radius + offset
        val right = width / 2 + radius - offset
        val bottom = height / 2 + radius - offset
        icon.setBounds(left, top, right, bottom)
        icon.draw(canvas)
    }
}