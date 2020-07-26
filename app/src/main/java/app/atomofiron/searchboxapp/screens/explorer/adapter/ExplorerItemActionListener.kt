package app.atomofiron.searchboxapp.screens.explorer.adapter

import app.atomofiron.searchboxapp.model.explorer.XFile
import app.atomofiron.searchboxapp.screens.explorer.adapter.util.ExplorerItemBinder

interface ExplorerItemActionListener : ExplorerItemBinder.ExplorerItemBinderActionListener {
    fun onItemVisible(item: XFile)
    fun onItemInvalidate(item: XFile)
}