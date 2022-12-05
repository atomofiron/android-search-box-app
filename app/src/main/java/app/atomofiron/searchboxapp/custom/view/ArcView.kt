package app.atomofiron.searchboxapp.custom.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.ColorUtils
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.utils.Const
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

class ArcView : View {
    companion object {
        private const val START = 120f
        private const val MAX = 300f
        private const val STEP_MAX = 3f
        private const val STEP_MIN = 0.1f
    }

    private var progress = 0f
    private var targetProgress = 0f
    private val rect = RectF()
    private val paint = Paint()

    private val colorProgress = context.findColorByAttr(R.attr.colorPrimary)
    private val colorTrack = ColorUtils.setAlphaComponent(colorProgress, Const.ALPHA_30_PERCENT)
    private val strokeWidth = resources.getDimension(R.dimen.arc_stroke_width)

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleRes: Int) : super(context, attrs, defStyleRes) {
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = context.findColorByAttr(R.attr.colorPrimary)
        paint.strokeWidth = strokeWidth
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        val offset = strokeWidth / 2
        rect.set(0f, 0f, width.toFloat(), height.toFloat())
        val width = rect.width()
        val height = rect.height()
        var dif = max(0f, height - width)
        rect.top += dif + offset
        rect.bottom -= dif + offset
        dif = max(0f, width - height)
        rect.left += dif + offset
        rect.right -= dif + offset
    }

    override fun onDraw(canvas: Canvas) {
        paint.color = colorTrack
        canvas.drawArc(rect, START, MAX, false, paint)
        paint.color = colorProgress
        canvas.drawArc(rect, START, progress, false, paint)
        if (progress != targetProgress) {
            var dif = targetProgress - progress
            val sign = dif.sign
            dif = abs(dif) / 8
            dif = max(STEP_MIN, dif)
            dif = min(STEP_MAX, dif) * sign
            progress += dif
            invalidate()
        }
    }

    fun set(progress: Long, max: Long) {
        targetProgress = (MAX / max) * progress
        invalidate()
    }
}