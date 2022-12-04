package app.atomofiron.searchboxapp.injectable.store

import app.atomofiron.common.util.flow.DataFlow
import app.atomofiron.common.util.flow.EventFlow
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeAction
import app.atomofiron.searchboxapp.model.explorer.NodeError
import app.atomofiron.searchboxapp.model.explorer.NodeTabItems
import kotlinx.coroutines.flow.MutableStateFlow

class ExplorerStore {
    val items = DataFlow<NodeTabItems>()
    val current = MutableStateFlow<Node?>(null)
    val checked = MutableStateFlow<List<Node>>(listOf())
    val alerts = EventFlow<NodeError>()
    val actions = EventFlow<NodeAction>()
}