package app.atomofiron.searchboxapp.screens.explorer

import app.atomofiron.common.util.flow.*
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.injectable.store.ExplorerStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeAction
import app.atomofiron.searchboxapp.model.explorer.NodeError
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

class ExplorerViewState(
    private val scope: CoroutineScope,
    explorerStore: ExplorerStore,
) {
    val rootOptions = listOf(R.id.menu_create)
    val directoryOptions = listOf(R.id.menu_remove, R.id.menu_rename, R.id.menu_create)
    val oneFileOptions = listOf(R.id.menu_remove, R.id.menu_rename, R.id.menu_share, R.id.menu_open_with)
    val manyFilesOptions = listOf(R.id.menu_remove, R.id.menu_share)

    val permissionRequiredWarning = ChannelFlow<Unit>()
    val scrollTo = ChannelFlow<Node>()
    val itemComposition = DeferredStateFlow<ExplorerItemComposition>()
    val items: SharedFlow<List<Node>> = explorerStore.items
    val current: StateFlow<Node?> = explorerStore.current
    val actions: Flow<NodeAction> = explorerStore.actions
    val alerts: Flow<NodeError> = explorerStore.alerts

    fun showPermissionRequiredWarning() {
        permissionRequiredWarning(scope)
    }

    fun scrollTo(item: Node) {
        scrollTo[scope] = item
    }
}