package app.atomofiron.searchboxapp.injectable.store

import app.atomofiron.common.util.flow.ChannelFlow
import app.atomofiron.common.util.flow.UniqueStateFlow
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeError
import kotlinx.coroutines.flow.MutableStateFlow

class ExplorerStore {
    val items = UniqueStateFlow<List<Node>>(listOf())
    val current = MutableStateFlow<Node?>(null)
    val checked = MutableStateFlow<List<Node>>(listOf())
    val alerts = ChannelFlow<NodeError>()
}