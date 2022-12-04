package app.atomofiron.searchboxapp.screens.explorer.fragment.list

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.R

class RootItemMarginDecorator : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        if (view.id != R.id.item_explorer_card) return

        val position = parent.getChildLayoutPosition(view)
        val layoutManager = parent.layoutManager as GridLayoutManager
        val spanCount = layoutManager.spanCount
        val spanSize = layoutManager.spanSizeLookup.getSpanSize(position)
        val margin = parent.resources.getDimensionPixelSize(R.dimen.content_margin)
        val count = spanCount / spanSize

        val sum = margin * spanCount.dec()

        outRect.left = when (position % count) {
            0 -> margin
            else -> margin / 2
        }
        outRect.right = when (position % count) {
            count.dec() -> margin
            else -> margin / 2
        }
    }
}