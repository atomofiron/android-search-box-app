package app.atomofiron.searchboxapp.screens.explorer.fragment

import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.screens.explorer.fragment.roots.RootAdapter

class ExplorerSpanSizeLookup(
    recyclerView: RecyclerView,
    private val layoutManager: GridLayoutManager,
    private val rootsAdapter: RootAdapter,
) : GridLayoutManager.SpanSizeLookup() {

    init {
        recyclerView.addOnLayoutChangeListener { _, left, _, right, _, oldLeft, _, oldRight, _ ->
            if (right - left != oldRight - oldLeft) {
                recyclerView.updateSpanCount()
            }
        }
        recyclerView.updateSpanCount()
    }

    override fun getSpanSize(position: Int): Int = when {
        position < rootsAdapter.itemCount -> 1
        else -> layoutManager.spanCount
    }

    private fun View.updateSpanCount() {
        val minWidth = resources.getDimensionPixelSize(R.dimen.column_min_width)
        var frameWidth = width
        if (frameWidth == 0) {
            frameWidth = resources.displayMetrics.widthPixels
        }
        frameWidth -= paddingStart + paddingEnd
        layoutManager.spanCount = frameWidth / minWidth
        rootsAdapter.notifyDataSetChanged()
    }
}