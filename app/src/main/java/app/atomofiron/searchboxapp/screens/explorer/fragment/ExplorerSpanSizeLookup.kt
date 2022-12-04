package app.atomofiron.searchboxapp.screens.explorer.fragment

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.screens.explorer.fragment.roots.RootAdapter

class ExplorerSpanSizeLookup(
    recyclerView: RecyclerView,
    private val layoutManager: GridLayoutManager,
    private val rootsAdapter: RootAdapter,
) : GridLayoutManager.SpanSizeLookup() {

    private val resources = recyclerView.resources

    init {
        recyclerView.addOnLayoutChangeListener { _, left, _, right, _, oldLeft, _, oldRight, _ ->
            if (right - left != oldRight - oldLeft) {
                updateSpanCount()
            }
        }
        updateSpanCount()
    }

    override fun getSpanSize(position: Int): Int = when {
        position < rootsAdapter.itemCount -> 1
        else -> layoutManager.spanCount
    }

    private fun updateSpanCount() {
        val minWidth = resources.getDimensionPixelSize(R.dimen.column_min_width)
        layoutManager.spanCount = resources.displayMetrics.widthPixels / minWidth
        rootsAdapter.notifyDataSetChanged()
    }
}