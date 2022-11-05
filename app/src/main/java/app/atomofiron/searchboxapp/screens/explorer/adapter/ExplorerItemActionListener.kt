package app.atomofiron.searchboxapp.screens.explorer.adapter

import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.screens.explorer.adapter.util.ExplorerItemBinderImpl

interface ExplorerItemActionListener : ExplorerItemBinderImpl.ExplorerItemBinderActionListener {
    fun onItemVisible(item: Node)
}