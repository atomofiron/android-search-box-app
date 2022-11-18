package app.atomofiron.searchboxapp.screens.explorer

import android.view.Gravity
import app.atomofiron.common.util.flow.*
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.injectable.store.ExplorerStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeAction
import app.atomofiron.searchboxapp.model.explorer.NodeError
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.places.XPlace
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
    val scrollToCurrentDir = ChannelFlow<Unit>()
    val historyDrawerGravity = MutableStateFlow(Gravity.START)
    val places = MutableStateFlow<List<XPlace>>(listOf())
    val itemComposition = DeferredStateFlow<ExplorerItemComposition>()
    val items: SharedFlow<List<Node>> = explorerStore.items
    val current: StateFlow<Node?> = explorerStore.current
    val actions: Flow<NodeAction> = explorerStore.actions
    val alerts: Flow<NodeError> = explorerStore.alerts

    fun showPermissionRequiredWarning() {
        permissionRequiredWarning(scope)
    }

    fun scrollToCurrentDir() = scrollToCurrentDir.invoke(scope)
}