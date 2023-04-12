package app.atomofiron.searchboxapp.injectable.store

import app.atomofiron.common.util.flow.DataFlow
import app.atomofiron.common.util.flow.EventFlow
import app.atomofiron.searchboxapp.model.explorer.*
import kotlinx.coroutines.flow.MutableStateFlow

class ExplorerStore {
    val items = DataFlow<NodeTabItems>()
    val current = MutableStateFlow<Node?>(null)
    val searchTargets = MutableStateFlow<List<Node>>(listOf())
    val alerts = EventFlow<NodeError>()
    val removed = EventFlow<Node>()
}