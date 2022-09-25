package app.atomofiron.searchboxapp.screens.curtain.util

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import app.atomofiron.searchboxapp.BuildConfig
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.getColorByAttr
import java.lang.Deprecated
import kotlin.math.min

open class CurtainBackground(context: Context) : Drawable() {
    companion object {
        private const val MAX_SATURATION = 200
    }
    private val cornerRadius = context.resources.getDimensionPixelSize(R.dimen.corner_radius)
    private val curtainColor = context.getColorByAttr(android.R.attr.colorBackground)
    private val overlayColor = context.getColorByAttr(R.attr.colorOverlay)
    private val latchColor = ContextCompat.getColor(context, R.color.latch)
    private val latchRect = RectF()
    private val paint = Paint()

    private var insetTop = 0
    private var viewTop = 0
    private var viewBottom = 0
    private var paddingHorizontal = 0

    private var contentTop = 0
    private val trueBounds = Rect()
    private var transitCornerRadius = 0

    private var saturationColor = 0

    val outline = OutlineDrawable()

    init {
        paint.isAntiAlias = true
        val latchHeight = context.resources.getDimension(R.dimen.curtain_latch_height)
        latchRect.top = (context.resources.getDimension(R.dimen.curtain_padding_top) - latchHeight) / 2
        latchRect.right = context.resources.getDimension(R.dimen.curtain_latch_width)
        latchRect.bottom = latchRect.top + latchHeight
    }

    override fun setAlpha(alpha: Int) = Unit

    override fun setColorFilter(colorFilter: ColorFilter?) = Unit

    @Deprecated
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)
        updateTrueBounds()
    }

    override fun draw(canvas: Canvas) {
        canvas.drawColor(saturationColor)
        paint.color = curtainColor
        val radius = transitCornerRadius.toFloat()
        val left = trueBounds.left.toFloat()
        val top = trueBounds.top.toFloat()
        val right = trueBounds.right.toFloat()
        val bottom = trueBounds.bottom.toFloat()
        canvas.drawRoundRect(left, top, right, bottom, radius, radius, paint)

        paint.color = latchColor
        val dx = (bounds.width() - latchRect.width()) / 2
        canvas.translate(dx, contentTop.toFloat())
        canvas.drawRoundRect(latchRect, latchRect.height(), latchRect.height(), paint)
    }

    fun setSaturation(value: Float) {
        val alpha = (MAX_SATURATION * value).toInt()
        saturationColor = ColorUtils.setAlphaComponent(overlayColor, alpha)
        invalidateSelf()
    }

    fun updateTop(viewTop: Int) = updateTrueBounds(insetTop = 0, viewTop, viewBottom = bounds.bottom, paddingHorizontal = 0)

    fun updateTrueBounds(insetTop: Int, viewTop: Int, viewBottom: Int, paddingHorizontal: Int) {
        this.insetTop = insetTop
        this.viewTop = viewTop
        this.viewBottom = viewBottom
        this.paddingHorizontal = paddingHorizontal
        contentTop = viewTop + insetTop
        updateTrueBounds()
    }

    private fun updateTrueBounds() {
        calcTrueBounds(insetTop, viewTop, viewBottom, paddingHorizontal)
        calcCornerRadius(insetTop, viewTop)
        outline.update(trueBounds.top - viewTop, transitCornerRadius)
    }

    private var flag = false
    private var underStatusBar = false
    private fun calcTrueBounds(insetTop: Int, viewTop: Int, viewBottom: Int, paddingHorizontal: Int) {
        when {
            !BuildConfig.DEBUG -> Unit
            viewTop > insetTop && flag == underStatusBar -> underStatusBar = !underStatusBar
            viewTop <= insetTop -> flag = underStatusBar
        }
        val top = when {
            viewTop >= insetTop -> viewTop + insetTop
            underStatusBar -> viewTop + insetTop * viewTop / insetTop
            else -> viewTop + insetTop
        }
        val left = bounds.left + paddingHorizontal
        val right = bounds.right - paddingHorizontal
        val bottom = viewBottom
        if (left != trueBounds.left || top != trueBounds.top || right != trueBounds.right || bottom != trueBounds.bottom) {
            trueBounds.set(left, top, right, bottom)
            invalidateSelf()
        }
    }

    private fun calcCornerRadius(insetTop: Int, viewTop: Int) {
        val cornerRadius = when {
            insetTop <= 0 -> min(cornerRadius, viewTop)
            trueBounds.top < insetTop -> min(cornerRadius, trueBounds.top)
            else -> cornerRadius
        }
        if (cornerRadius != transitCornerRadius) {
            transitCornerRadius = cornerRadius
            invalidateSelf()
        }
    }

    class OutlineDrawable : Drawable() {
        private var cornerRadius = 0
        private var outlineTop = 0

        override fun draw(canvas: Canvas) = Unit

        override fun setAlpha(alpha: Int) = Unit

        override fun setColorFilter(colorFilter: ColorFilter?) = Unit

        @Deprecated
        override fun getOpacity(): Int = PixelFormat.OPAQUE

        override fun getOutline(outline: Outline) {
            super.getOutline(outline)
            val radius = cornerRadius.toFloat()
            outline.setRoundRect(bounds.left, outlineTop, bounds.right, bounds.bottom, radius)
        }

        fun update(outlineTop: Int, cornerRadius: Int) {
            if (this.outlineTop != outlineTop || this.cornerRadius != cornerRadius) {
                this.cornerRadius = cornerRadius
                this.outlineTop = outlineTop
                invalidateSelf()
            }
        }
    }
}