package app.atomofiron.searchboxapp.custom.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.WindowInsets
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.getColorByAttr
import app.atomofiron.searchboxapp.utils.Const
import kotlin.math.max

class SystemUiBackgroundView : View {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var leftInset = 0
    private var topInset = 0
    private var rightInset = 0
    private var bottomInset = 0

    private val paint = Paint()

    init {
        paint.color = context.getColorByAttr(R.attr.colorBackground)
        paint.color = ColorUtils.setAlphaComponent(paint.color, Const.ALPHA_50_PERCENT)
    }

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets? {
        val statusBars = WindowInsetsCompat.toWindowInsetsCompat(insets).getInsets(Type.statusBars())
        val navigationBars = WindowInsetsCompat.toWindowInsetsCompat(insets).getInsets(Type.navigationBars())
        val tappableElement = WindowInsetsCompat.toWindowInsetsCompat(insets).getInsets(Type.tappableElement())
        leftInset = max(statusBars.left, navigationBars.left.only(tappableElement.left))
        topInset = statusBars.top
        rightInset = max(statusBars.right, navigationBars.right.only(tappableElement.right))
        bottomInset = max(statusBars.bottom, navigationBars.bottom.only(tappableElement.bottom))
        invalidate()
        return WindowInsetsCompat.CONSUMED.toWindowInsets()
    }

    private fun Int.only(value: Int): Int = when (this) {
        value -> this
        else -> 0
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val leftInset = leftInset.toFloat()
        val topInset = topInset.toFloat()
        val rightInset = rightInset.toFloat()
        val bottomInset = bottomInset.toFloat()

        val right = width.toFloat()
        val bottom = bottom.toFloat()

        canvas.drawRect(0f, 0f, right, topInset, paint)
        canvas.drawRect(0f, bottom - bottomInset, right, bottom, paint)

        canvas.drawRect(0f, topInset, leftInset, bottom - bottomInset, paint)
        canvas.drawRect(right - rightInset, topInset, right, bottom - bottomInset, paint)
    }
}