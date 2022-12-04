package app.atomofiron.searchboxapp.screens.explorer.fragment

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.screens.explorer.fragment.roots.RootAdapter
import kotlin.math.max
import kotlin.math.min

class ExplorerSpanSizeLookup(
    recyclerView: RecyclerView,
    private val layoutManager: GridLayoutManager,
    private val rootsAdapter: RootAdapter,
) : GridLayoutManager.SpanSizeLookup() {

    init {
        updateSpanCount(recyclerView)
    }

    override fun getSpanSize(position: Int): Int = when {
        position < rootsAdapter.itemCount -> 1
        else -> layoutManager.spanCount
    }

    fun updateSpanCount(recyclerView: RecyclerView) {
        val spanCount = recyclerView.run {
            val minWidth = resources.getDimensionPixelSize(R.dimen.column_min_width)
            var frameWidth = width
            if (frameWidth == 0) {
                frameWidth = resources.displayMetrics.widthPixels
            }
            frameWidth -= paddingStart + paddingEnd
            val max = max(1, rootsAdapter.itemCount)
            min(max, frameWidth / minWidth)
        }
        if (spanCount != layoutManager.spanCount) {
            layoutManager.spanCount = spanCount
            rootsAdapter.notifyDataSetChanged()
        }
    }
}