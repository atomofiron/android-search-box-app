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
import app.atomofiron.searchboxapp.obtainStyledAttributes
import app.atomofiron.searchboxapp.utils.Const
import kotlin.math.max

class SystemUiBackgroundView : View {
    companion object {
        fun Context.getSystemBarsColor(): Int {
            val color = getColorByAttr(R.attr.colorBackground)
            return ColorUtils.setAlphaComponent(color, Const.ALPHA_67_PERCENT)
        }
    }

    private var leftInset = 0
    private var topInset = 0
    private var rightInset = 0
    private var bottomInset = 0

    private val paint = Paint()

    private var drawStatusBar: Boolean = true
    private var drawNavigationBar: Boolean = true

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        paint.color = context.getSystemBarsColor()

        context.obtainStyledAttributes(attrs, R.styleable.SystemUiBackgroundView, defStyleAttr) {
            drawStatusBar = getBoolean(R.styleable.SystemUiBackgroundView_statusBar, drawStatusBar)
            drawNavigationBar = getBoolean(R.styleable.SystemUiBackgroundView_navigationBar, drawNavigationBar)
        }
    }

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        val statusBars = WindowInsetsCompat.toWindowInsetsCompat(insets).getInsets(Type.statusBars())
        val navigationBars = WindowInsetsCompat.toWindowInsetsCompat(insets).getInsets(Type.navigationBars())
        val tappableElement = WindowInsetsCompat.toWindowInsetsCompat(insets).getInsets(Type.tappableElement())
        leftInset = max(statusBars.left, navigationBars.left.only(tappableElement.left))
        topInset = statusBars.top
        rightInset = max(statusBars.right, navigationBars.right.only(tappableElement.right))
        bottomInset = max(statusBars.bottom, navigationBars.bottom.only(tappableElement.bottom))
        invalidate()
        return insets
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!drawStatusBar && !drawNavigationBar) return

        val leftInset = leftInset.toFloat()
        val topInset = topInset.toFloat()
        val rightInset = rightInset.toFloat()
        val bottomInset = bottomInset.toFloat()

        val right = width.toFloat()
        val bottom = height.toFloat()

        if (drawStatusBar) {
            canvas.drawRect(0f, 0f, right, topInset, paint)
        }
        if (drawNavigationBar) {
            val navigationTop = if (drawStatusBar) topInset else 0f
            canvas.drawRect(0f, navigationTop, leftInset, bottom - bottomInset, paint)
            canvas.drawRect(right - rightInset, navigationTop, right, bottom - bottomInset, paint)
            canvas.drawRect(0f, bottom - bottomInset, right, bottom, paint)
        }
    }

    fun update(statusBar: Boolean = drawStatusBar, navigationBar: Boolean = drawNavigationBar) {
        drawStatusBar = statusBar
        drawNavigationBar = navigationBar
        invalidate()
    }

    private fun Int.only(value: Int): Int = when (this) {
        value -> this
        else -> 0
    }
}