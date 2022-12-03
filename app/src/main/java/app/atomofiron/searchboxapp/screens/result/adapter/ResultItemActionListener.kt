package app.atomofiron.searchboxapp.screens.result.adapter

import app.atomofiron.searchboxapp.screens.explorer.list.util.ExplorerItemBinderImpl

interface ResultItemActionListener : ExplorerItemBinderImpl.ExplorerItemBinderActionListener {
    fun onItemVisible(item: FinderResultItem.Item)
}