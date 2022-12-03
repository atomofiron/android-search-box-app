package app.atomofiron.searchboxapp.screens.explorer.list

import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.screens.explorer.list.util.ExplorerItemBinderImpl

interface ExplorerItemActionListener : ExplorerItemBinderImpl.ExplorerItemBinderActionListener {
    fun onItemVisible(item: Node)
}