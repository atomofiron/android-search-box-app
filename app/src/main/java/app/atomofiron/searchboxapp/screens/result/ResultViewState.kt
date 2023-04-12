package app.atomofiron.searchboxapp.screens.result

import app.atomofiron.common.util.flow.*
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.model.textviewer.SearchTask
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

class ResultViewState(
    private val scope: CoroutineScope,
) {
    val checked = mutableListOf<Node>()

    val oneFileOptions = listOf(R.id.menu_copy_path, R.id.menu_remove)
    val manyFilesOptions = listOf(R.id.menu_remove)

    val task = DeferredStateFlow<SearchTask>()
    val composition = DeferredStateFlow<ExplorerItemComposition>()
    val enableOptions = MutableStateFlow(false)
    val showOptions = ChannelFlow<Pair<ExplorerItemOptions, CurtainApi.Controller>>()
    val notifyTaskHasChanged = ChannelFlow<Unit>()
    val alerts = ChannelFlow<String>()

    fun sendAlert(value: String) {
        alerts[scope] = value
    }
}