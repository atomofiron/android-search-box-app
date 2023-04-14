package app.atomofiron.searchboxapp.screens.curtain.util

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.graphics.ColorUtils
import app.atomofiron.common.util.findBooleanByAttr
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.getColorByAttr

open class CurtainBackground(context: Context) : Drawable() {

    private val cornerRadius = context.resources.getDimension(R.dimen.corner_extra_large)
    private val curtainColor = context.getColorByAttr(R.attr.colorBackground)
    private val strokeWidth = context.resources.getDimension(R.dimen.stroke_width)
    private val dragHandleColor = ColorUtils.setAlphaComponent(context.getColorByAttr(R.attr.colorOnSurfaceVariant), 102)
    private val dragHandleRect = RectF()
    private val dragHandleWidth = context.resources.getDimension(R.dimen.drag_handle_width)
    private val dragHandleMargin = context.resources.getDimension(R.dimen.drag_handle_margin)
    private val strokeColor = when {
        context.findBooleanByAttr(R.attr.isBlackDeep) -> ColorUtils.compositeColors(dragHandleColor, curtainColor)
        else -> Color.TRANSPARENT
    }
    private val paint = Paint()
    private val boundsF = RectF()

    init {
        paint.isAntiAlias = true
        dragHandleRect.top = dragHandleMargin
        dragHandleRect.bottom = dragHandleMargin + context.resources.getDimension(R.dimen.drag_handle_height)
    }

    override fun setAlpha(alpha: Int) = Unit

    override fun setColorFilter(colorFilter: ColorFilter?) = Unit

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)
        boundsF.set(bounds)
        val center = (left + right) / 2
        dragHandleRect.left = center - dragHandleWidth / 2
        dragHandleRect.right =  center + dragHandleWidth / 2
    }

    override fun getOutline(outline: Outline) {
        super.getOutline(outline)
        outline.setRoundRect(bounds.left, bounds.top, bounds.right, bounds.bottom, cornerRadius)
    }

    override fun draw(canvas: Canvas) {
        var radius = cornerRadius

        paint.color = strokeColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth * 2
        boundsF.run {
            canvas.drawRoundRect(left, top, right, bottom, radius, radius, paint)
        }

        paint.color = curtainColor
        paint.style = Paint.Style.FILL
        boundsF.run {
            canvas.drawRoundRect(left, top, right, bottom, radius, radius, paint)
        }

        radius = dragHandleRect.height()
        paint.color = dragHandleColor
        canvas.drawRoundRect(dragHandleRect, radius, radius, paint)
    }
}