package ru.atomofiron.regextool.screens.explorer.adapter

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.screens.explorer.adapter.util.getSortedChildren

class ItemBackgroundDecorator(val atFirst: Boolean = true) : RecyclerView.ItemDecoration() {
    companion object {
        private const val UNDEFINED = -1
    }
    private val background = ShapeDrawable()
    private var color = UNDEFINED
    var enabled = false

    init {
        background.paint.color = Color.BLUE
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(canvas, parent, state)

        if (!enabled) return

        if (color == UNDEFINED) {
            color = ContextCompat.getColor(parent.context, R.color.item_explorer_background)
            background.paint.color = color
        }

        parent.getSortedChildren().forEach {
            val child = it.value
            val position = parent.getChildLayoutPosition(child)

            val remainder = if (atFirst) 0 else 1
            if (position % 2 == remainder) {
                background.setBounds(child.left, child.top, child.right, child.bottom)
                background.draw(canvas)
            }
        }
    }
}