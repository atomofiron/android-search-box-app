package app.atomofiron.searchboxapp.screens.explorer.fragment

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.custom.view.ExplorerHeaderView
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.*
import app.atomofiron.searchboxapp.screens.explorer.fragment.roots.RootAdapter

class ExplorerListDelegate(
    private val recyclerView: RecyclerView,
    private val rootAdapter: RootAdapter,
    private val nodeAdapter: ExplorerAdapter,
    headerView: ExplorerHeaderView,
    private val output: ExplorerItemActionListener,
) {
    private var currentDir: Node? = null

    private val headerItemPosition: Int get() = when (val dir = currentDir) {
        null -> -1
        else -> nodeAdapter.currentList.indexOfFirst { it.uniqueId == dir.uniqueId }
    }
    private val headerDelegate = ExplorerHeaderDelegate(recyclerView, headerView, nodeAdapter)
    private val rootMarginDecorator = RootItemMarginDecorator()
    private val backgroundDecorator = ItemBackgroundDecorator()
    private val borderDecorator = ItemBorderDecorator(nodeAdapter, headerView, headerDelegate::onDecoratorDraw)

    init {
        recyclerView.addItemDecoration(rootMarginDecorator)
        recyclerView.addItemDecoration(backgroundDecorator)
        recyclerView.addItemDecoration(borderDecorator)
        headerView.setOnItemActionListener(HeaderListener())
    }

    private fun getFirstChild(): View? = recyclerView.getChildAt(0)

    private fun getLastChild(): View? = recyclerView.getChildAt(recyclerView.childCount.dec())

    fun isCurrentDirVisible(): Boolean = isVisible(headerItemPosition)

    fun isVisible(position: Int): Boolean {
        if (position < 0) return false
        val firstChild = getFirstChild() ?: return false
        val topItemPosition = recyclerView.getChildLayoutPosition(firstChild)
        val bottomItemPosition = recyclerView.getChildLayoutPosition(getLastChild()!!)
        return position in topItemPosition..bottomItemPosition
    }

    fun setCurrentDir(item: Node?) {
        currentDir = item
        borderDecorator.setCurrentDir(item)
        headerDelegate.setCurrentDir(item)
    }

    fun setComposition(composition: ExplorerItemComposition) {
        backgroundDecorator.enabled = composition.visibleBg
        headerDelegate.setComposition(composition)
    }

    fun scrollTo(item: Node) {
        var lastChild = getLastChild() ?: return
        var position = nodeAdapter.currentList.indexOfFirst { it.path == item.path }
        position += rootAdapter.itemCount
        val lastItemPosition = recyclerView.getChildLayoutPosition(lastChild)
        when {
            position > lastItemPosition -> {
                recyclerView.scrollToPosition(position.dec())
                recyclerView.post {
                    lastChild = getLastChild() ?: return@post
                    recyclerView.smoothScrollBy(0, lastChild.height * 2)
                }
            }
            else -> {
                recyclerView.scrollToPosition(position.inc())
                recyclerView.post {
                    val firstChild = getFirstChild() ?: return@post
                    recyclerView.smoothScrollBy(0, -firstChild.height * 3 / 2)
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
            else -> scrollTo(item)
        }
    }
}