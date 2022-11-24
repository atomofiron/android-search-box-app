package app.atomofiron.searchboxapp.screens.explorer.adapter

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.screens.explorer.adapter.util.getSortedChildren

class ItemBackgroundDecorator : RecyclerView.ItemDecoration() {
    companion object {
        fun Context.getExplorerItemBackground(): Int {
            val colorSurface = findColorByAttr(R.attr.colorSurfaceVariant)
            return ColorUtils.setAlphaComponent(colorSurface, Byte.MAX_VALUE.toInt())
        }
    }

    private val paint = Paint()
    var enabled = false

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        paint.color = parent.context.getExplorerItemBackground()
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(canvas, parent, state)

        if (!enabled) return

        parent.getSortedChildren().forEach {
            val child = it.value
            val position = parent.getChildLayoutPosition(child)

            if (position % 2 == 0) {
                child.run {
                    canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
                }
            }
        }
    }
}