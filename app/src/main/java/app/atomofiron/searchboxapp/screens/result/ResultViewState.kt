package app.atomofiron.searchboxapp.screens.result

import app.atomofiron.common.util.flow.*
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.logI
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.finder.FinderTask
import app.atomofiron.searchboxapp.model.finder.FinderTaskChange
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import app.atomofiron.searchboxapp.screens.result.presenter.ResultPresenterParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

class ResultViewState(
    private val params: ResultPresenterParams,
    private val scope: CoroutineScope,
) {
    val checked = mutableListOf<Node>()

    val oneFileOptions = listOf(R.id.menu_copy_path, R.id.menu_remove)
    val manyFilesOptions = listOf(R.id.menu_remove)

    val task = DeferredStateFlow<FinderTask>()
    val composition = DeferredStateFlow<ExplorerItemComposition>()
    val enableOptions = MutableStateFlow(false)
    val showOptions = ChannelFlow<Pair<ExplorerItemOptions, CurtainApi.Controller>>()
    val notifyTaskHasChanged = ChannelFlow<Unit>()
    val alerts = ChannelFlow<String>()

    fun updateState(update: FinderTaskChange? = null) {
        when (update) {
            null -> notifyTaskHasChanged(scope)
            is FinderTaskChange.Update -> {
                val task = task.value
                val newTask = update.tasks.find { it.id == task.id }
                when {
                    newTask == null -> logI("[ERROR] newTask == null")
                    !task.areContentsTheSame(newTask) -> this.task.value = newTask.copyTask()
                }
            }
            is FinderTaskChange.Add -> Unit
            is FinderTaskChange.Drop -> Unit
        }
    }

    fun sendAlert(value: String) {
        alerts[scope] = value
    }
}