package app.atomofiron.searchboxapp.screens.finder

import android.view.Gravity
import app.atomofiron.common.util.flow.ChannelFlow
import app.atomofiron.common.util.flow.EventFlow
import app.atomofiron.common.util.flow.invoke
import app.atomofiron.common.util.flow.set
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.finder.FinderTaskChange
import app.atomofiron.searchboxapp.screens.finder.model.FinderStateItem
import app.atomofiron.searchboxapp.screens.finder.viewmodel.FinderItemsState
import app.atomofiron.searchboxapp.screens.finder.viewmodel.FinderItemsStateDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

class FinderViewState(
    private val scope: CoroutineScope,
) : FinderItemsState by FinderItemsStateDelegate() {

    private var configItem = FinderStateItem.ConfigItem()

    val targets = ArrayList<Node>()

    val historyDrawerGravity = MutableStateFlow(Gravity.START)
    val reloadHistory = ChannelFlow<Unit>()
    val insertInQuery = ChannelFlow<String>()
    val replaceQuery = ChannelFlow<String>()
    val snackbar = ChannelFlow<String>()
    val history = ChannelFlow<String>()
    val showHistory = EventFlow<Unit>()
    val permissionRequiredWarning = ChannelFlow<Unit>()

    fun showPermissionRequiredWarning() {
        permissionRequiredWarning(scope)
    }

    fun updateTargets(currentDir: Node?, checked: List<Node>) {
        targetItems.clear()
        targets.clear()
        when {
            checked.isNotEmpty() -> {
                checked.forEach { targetItems.add(FinderStateItem.TargetItem(it)) }
                targets.addAll(checked)
            }
            currentDir != null -> {
                targetItems.add(FinderStateItem.TargetItem(currentDir))
                targets.add(currentDir)
            }
        }
        updateState()
    }

    fun onFinderTaskUpdate(change: FinderTaskChange) {
        when (change) {
            is FinderTaskChange.Add -> {
                val item = FinderStateItem.ProgressItem(change.task)
                progressItems.add(item)
            }
            is FinderTaskChange.Update -> {
                val items = change.tasks.map { FinderStateItem.ProgressItem(it) }
                progressItems.clear()
                progressItems.addAll(items)
            }
            is FinderTaskChange.Drop -> {
                val index = progressItems.indexOfFirst { it.finderTask.id == change.task.id }
                // todo wtf
                progressItems.removeAt(index)
            }
        }
        updateState()
    }

    fun setExcludeDirsValue(excludeDirs: Boolean) {
        updateConfig {
            copy(excludeDirs = excludeDirs)
        }
    }

    fun switchConfigItemVisibility() {
        var index = uniqueItems.indexOfFirst { it is FinderStateItem.ConfigItem }
        if (index < 0) {
            index = uniqueItems.indexOfFirst { it is FinderStateItem.ButtonsItem }
            when {
                uniqueItems.size >= index -> uniqueItems.add(configItem)
                else -> uniqueItems[index.inc()] = configItem
            }
        } else {
            uniqueItems.removeAt(index)
        }
        updateState()
    }

    fun reloadHistory() = reloadHistory.invoke(scope)

    fun insertInQuery(value: String) {
        insertInQuery[scope] = value
    }

    fun replaceQuery(value: String) {
        replaceQuery[scope] = value
    }

    fun showSnackbar(value: String) {
        snackbar[scope] = value
    }

    fun addToHistory(value: String) {
        history[scope] = value
    }

    fun showHistory() = showHistory.invoke(scope)

    fun getConfigItem(): FinderStateItem.ConfigItem {
        return getUniqueItem(FinderStateItem.ConfigItem::class)
    }

    private inline fun updateConfig(action: FinderStateItem.ConfigItem.() -> FinderStateItem.ConfigItem) {
        configItem = configItem.action()
        val index = uniqueItems.indexOfFirst { it is FinderStateItem.ConfigItem }
        if (index >= 0) {
            uniqueItems[index] = configItem
        }
        updateState()
    }
}