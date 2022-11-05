package app.atomofiron.searchboxapp.screens.explorer.fragment

import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.screens.explorer.adapter.ExplorerAdapter
import app.atomofiron.searchboxapp.screens.explorer.adapter.ExplorerItemActionListener

class HeaderViewOutputDelegate(
    private val explorerAdapter: ExplorerAdapter,
    private val output: ExplorerItemActionListener
) : ExplorerItemActionListener {
    override fun onItemVisible(item: Node) = Unit // unreachable

    override fun onItemLongClick(item: Node) = output.onItemLongClick(item)

    override fun onItemCheck(item: Node, isChecked: Boolean) = output.onItemCheck(item, isChecked)

    override fun onItemClick(item: Node) = when {
        explorerAdapter.isCurrentDirVisible() -> output.onItemClick(item)
        else -> explorerAdapter.scrollToCurrentDir()
    }
}