package app.atomofiron.searchboxapp.screens.explorer.fragment

import android.view.View
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.ExplorerHeaderView
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.adapter.ExplorerAdapter
import app.atomofiron.searchboxapp.screens.explorer.adapter.ItemBackgroundDecorator.Companion.getExplorerItemBackground
import java.util.LinkedList
import kotlin.math.max
import kotlin.math.min

class ExplorerHeaderDelegate(
    private val recyclerView: RecyclerView,
    private val headerView: ExplorerHeaderView,
    private val adapter: ExplorerAdapter,
) : RecyclerView.OnScrollListener() {
    companion object {
        fun getHeaderBottom(recyclerView: RecyclerView, headerView: ExplorerHeaderView, adapter: ExplorerAdapter, currentDir: Node?): Int {
            val headerHeight = headerView.measuredHeight
            currentDir ?: return 0
            if (currentDir.isEmpty) return 0
            val topChildren = LinkedList<View>()
            for (i in 0 until recyclerView.childCount) {
                val child = recyclerView.getChildAt(i)
                if (child.top > headerHeight) break
                topChildren.add(child)
            }
            if (topChildren.isEmpty()) return 0
            val topItems = topChildren.map {
                val holder = recyclerView.getChildViewHolder(it)
                adapter.currentList[holder.bindingAdapterPosition]
            }
            var bottom = 0
            topItems.forEachIndexed { i, it ->
                val view = topChildren[i]
                when {
                    it.parentPath == currentDir.path -> bottom = view.bottom
                    it.path != currentDir.path -> Unit
                    view.bottom <= headerHeight -> bottom = max(headerHeight, view.bottom)
                }
            }
            return min(headerHeight, bottom)
        }
    }

    private var currentDir: Node? = null
    private var currentIndex = -1

    private var composition: ExplorerItemComposition? = null
    private var backgroundGrey = 0
    private var backgroundColor = 0

    init {
        recyclerView.addOnScrollListener(this)
        backgroundColor = headerView.context.findColorByAttr(R.attr.colorBackground)
        backgroundGrey = headerView.context.getExplorerItemBackground()
        backgroundGrey = ColorUtils.compositeColors(backgroundGrey, backgroundColor)
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (dy != 0) updateVisibility()
    }

    fun setComposition(composition: ExplorerItemComposition) {
        this.composition = composition
        tryBind()
    }

    fun setCurrentDir(item: Node?) {
        currentDir = item
        currentIndex = when (item) {
            null -> -1
            else -> adapter.currentList.indexOfFirst { it.uniqueId == item.uniqueId }
        }
        tryBind()
        updateVisibility()
    }

    private fun tryBind() {
        val composition = composition ?: return
        val currentDir = currentDir ?: return

        val color = when (currentIndex % 2) {
            0 -> backgroundGrey
            else -> backgroundColor
        }
        headerView.setComposition(composition)
        headerView.setBackgroundColor(color)
        headerView.bind(currentDir)
    }

    private fun updateVisibility() {
        val bottom = when {
            currentIndex < 0 -> 0
            else -> getHeaderBottom(recyclerView, headerView, adapter, currentDir)
        }
        headerView.move(bottom)
    }
}