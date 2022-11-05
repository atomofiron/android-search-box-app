package app.atomofiron.searchboxapp.injectable.store

import app.atomofiron.common.util.flow.dataFlow
import app.atomofiron.searchboxapp.model.explorer.Node
import kotlinx.coroutines.flow.MutableStateFlow

class ExplorerStore {
    val items = dataFlow<List<Node>>(listOf())
    val current = MutableStateFlow<Node?>(null)
    val alerts = dataFlow<String>(single = true)
    val checked = dataFlow<List<Node>>(ArrayList())
}