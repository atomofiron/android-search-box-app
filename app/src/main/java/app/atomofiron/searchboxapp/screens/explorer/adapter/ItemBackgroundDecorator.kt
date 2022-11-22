package app.atomofiron.searchboxapp.screens.explorer.adapter

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.screens.explorer.adapter.util.getSortedChildren

class ItemBackgroundDecorator(
    val atFirst: Boolean = true,
) : RecyclerView.ItemDecoration() {
    companion object {
        fun Context.getExplorerItemBackground(): Int {
            val colorSurface = findColorByAttr(R.attr.colorSurfaceVariant)
            return ColorUtils.setAlphaComponent(colorSurface, Byte.MAX_VALUE.toInt())
        }
    }

    private val paint = Paint()
    private var colorDefined = false
    var enabled = false

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(canvas, parent, state)

        if (!colorDefined) {
            colorDefined = true
            paint.color = parent.context.getExplorerItemBackground()
        }

        if (!enabled) return

        parent.getSortedChildren().forEach {
            val child = it.value
            val position = parent.getChildLayoutPosition(child)

            val remainder = if (atFirst) 0 else 1
            if (position % 2 == remainder) {
                child.run {
                    canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
                }
            }
        }
    }
}