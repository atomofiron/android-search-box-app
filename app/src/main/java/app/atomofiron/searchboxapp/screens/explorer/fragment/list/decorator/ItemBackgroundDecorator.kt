package app.atomofiron.searchboxapp.screens.explorer.fragment.list.decorator

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.getSortedChildren

class ItemBackgroundDecorator : RecyclerView.ItemDecoration() {
    companion object {
        fun Context.getExplorerItemBackground(): Int = findColorByAttr(R.attr.topRadioGroupBackground)
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
            if (child.id != R.id.item_explorer) return@forEach
            val position = parent.getChildLayoutPosition(child)

            if (position % 2 == 0) {
                canvas.drawRect(0f, child.top.toFloat(), parent.width.toFloat(), child.bottom.toFloat(), paint)
            }
        }
    }
}