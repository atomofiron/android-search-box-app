package ru.atomofiron.regextool.screens.explorer.adapter

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import java.lang.Math.min

class ItemSpaceDecorator(private val divide: (Int) -> Divider) : RecyclerView.ItemDecoration() {
    companion object {
        private var minNonZeroHeight = 0
    }
    enum class Divider {
        NO, SMALL, BIG
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        minNonZeroHeight = when {
            view.measuredHeight == 0 -> minNonZeroHeight
            minNonZeroHeight == 0 -> view.measuredHeight
            else -> min(minNonZeroHeight, view.measuredHeight)
        }

        val position = parent.getChildLayoutPosition(view)
        outRect.bottom += when (divide(position)) {
            Divider.NO -> 0
            Divider.SMALL -> minNonZeroHeight / 2
            Divider.BIG -> minNonZeroHeight
        }
    }
}