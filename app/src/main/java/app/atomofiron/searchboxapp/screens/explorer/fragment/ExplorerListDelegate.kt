package app.atomofiron.searchboxapp.screens.explorer.fragment

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.custom.view.ExplorerHeaderView
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.adapter.*

class ExplorerListDelegate(
    private val recyclerView: RecyclerView,
    private val adapter: ExplorerAdapter,
    private val headerView: ExplorerHeaderView,
    private val output: ExplorerItemActionListener,
) {
    private var currentDir: Node? = null

    private val headerItemPosition: Int get() = when (val dir = currentDir) {
        null -> -1
        else -> adapter.currentList.indexOfFirst { it.uniqueId == dir.uniqueId }
    }
    private val gravityDecorator = ItemGravityDecorator()
    private val backgroundDecorator = ItemBackgroundDecorator()
    private val borderDecorator = ItemBorderDecorator(adapter, headerView)

    init {
        recyclerView.addItemDecoration(gravityDecorator)
        recyclerView.addItemDecoration(backgroundDecorator)
        recyclerView.addItemDecoration(borderDecorator)
        headerView.setOnItemActionListener(HeaderListener())
    }

    private fun getFirstChild(): View? = recyclerView.getChildAt(0)

    private fun getLastChild(): View? = recyclerView.getChildAt(recyclerView.childCount.dec())

    fun isCurrentDirVisible(): Boolean {
        val firstChild = getFirstChild() ?: return false
        val topItemPosition = recyclerView.getChildLayoutPosition(firstChild)
        val bottomItemPosition = recyclerView.getChildLayoutPosition(getLastChild()!!)
        return headerItemPosition in topItemPosition..bottomItemPosition
    }

    fun setCurrentDir(item: Node?) {
        currentDir = item
        borderDecorator.setCurrentDir(item)
    }

    fun setComposition(composition: ExplorerItemComposition) {
        backgroundDecorator.enabled = composition.visibleBg
    }

    fun scrollToCurrentDir(unit: Unit = Unit) {
        val dir = currentDir ?: return
        var lastChild = getLastChild() ?: return
        val position = adapter.currentList.indexOf(dir)
        val lastItemPosition = recyclerView.getChildLayoutPosition(lastChild)
        when {
            position > lastItemPosition -> {
                recyclerView.scrollToPosition(position.dec())
                recyclerView.post {
                    lastChild = getLastChild()!!
                    recyclerView.smoothScrollBy(0, lastChild.height * 3 / 2) // ItemSpaceDecorator
                }
            }
            else -> {
                recyclerView.scrollToPosition(position.inc())
                recyclerView.post {
                    val firstChild = getFirstChild()!!
                    recyclerView.smoothScrollBy(0, -firstChild.height * 5 / 2) // ItemSpaceDecorator
                }
            }
        }
    }

    private inner class HeaderListener : ExplorerItemActionListener {
        override fun onItemVisible(item: Node) = Unit // unreachable

        override fun onItemLongClick(item: Node) = output.onItemLongClick(item)

        override fun onItemCheck(item: Node, isChecked: Boolean) = output.onItemCheck(item, isChecked)

        override fun onItemClick(item: Node) = when {
            isCurrentDirVisible() -> output.onItemClick(item)
            else -> scrollToCurrentDir()
        }
    }
}