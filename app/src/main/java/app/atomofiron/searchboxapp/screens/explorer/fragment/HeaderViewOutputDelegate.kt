package app.atomofiron.searchboxapp.screens.explorer.fragment

import app.atomofiron.searchboxapp.model.explorer.XFile
import app.atomofiron.searchboxapp.screens.explorer.adapter.ExplorerAdapter
import app.atomofiron.searchboxapp.screens.explorer.adapter.ExplorerItemActionListener

class HeaderViewOutputDelegate(
    private val explorerAdapter: ExplorerAdapter,
    private val output: ExplorerItemActionListener
) : ExplorerItemActionListener {
    override fun onItemVisible(item: XFile) = Unit // unreachable

    override fun onItemInvalidate(item: XFile) = Unit // unreachable

    override fun onItemLongClick(item: XFile) = output.onItemLongClick(item)

    override fun onItemCheck(item: XFile, isChecked: Boolean) = output.onItemCheck(item, isChecked)

    override fun onItemClick(item: XFile) = when {
        explorerAdapter.isCurrentDirVisible() -> output.onItemClick(item)
        else -> explorerAdapter.scrollToCurrentDir()
    }
}