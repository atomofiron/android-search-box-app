package ru.atomofiron.regextool.screens.explorer.adapter

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.screens.explorer.adapter.util.getSortedChildren

class ItemBackgroundDecorator(val atFirst: Boolean = true) : RecyclerView.ItemDecoration() {
    private lateinit var background: ShapeDrawable
    var enabled = false

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(canvas, parent, state)

        if (!enabled) return

        if (!::background.isInitialized) {
            background = ShapeDrawable()
            background.paint.color = ContextCompat.getColor(parent.context, R.color.item_explorer_background)
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