package app.atomofiron.searchboxapp.injectable.store

import app.atomofiron.common.util.flow.ChannelFlow
import app.atomofiron.common.util.flow.dataFlow
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeError
import kotlinx.coroutines.flow.MutableStateFlow

class ExplorerStore {
    val items = dataFlow<List<Node>>(listOf())
    val current = MutableStateFlow<Node?>(null)
    val checked = dataFlow<List<Node>>(ArrayList())
    val alerts = ChannelFlow<NodeError>()
}