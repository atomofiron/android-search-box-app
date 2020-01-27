package ru.atomofiron.regextool.screens.explorer.adapter

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class ItemSpaceDecorator(private val divide: (Int) -> Divider) : RecyclerView.ItemDecoration() {
    enum class Divider {
        NO, SMALL, BIG
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        val position = parent.getChildLayoutPosition(view)
        outRect.bottom = when (divide(position)) {
            Divider.NO -> 0
            Divider.SMALL -> view.measuredHeight / 2
            Divider.BIG -> view.measuredHeight
        }
    }
}