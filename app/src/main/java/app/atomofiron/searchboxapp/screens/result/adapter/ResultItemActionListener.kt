package app.atomofiron.searchboxapp.screens.result.adapter

import app.atomofiron.searchboxapp.screens.explorer.adapter.util.ExplorerItemBinder

interface ResultItemActionListener : ExplorerItemBinder.ExplorerItemBinderActionListener {
    fun onItemVisible(item: FinderResultItem.Item)
}