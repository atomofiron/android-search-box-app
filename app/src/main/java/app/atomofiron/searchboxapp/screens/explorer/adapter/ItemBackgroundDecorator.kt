package app.atomofiron.searchboxapp.screens.explorer.adapter

import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.screens.explorer.adapter.util.getSortedChildren

class ItemBackgroundDecorator(
    val atFirst: Boolean = true,
) : RecyclerView.ItemDecoration() {

    private val paint = Paint()
    private var colorDefined = false
    var enabled = false

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(canvas, parent, state)

        if (!colorDefined) {
            colorDefined = true
            paint.color = ContextCompat.getColor(parent.context, R.color.item_explorer_background)
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