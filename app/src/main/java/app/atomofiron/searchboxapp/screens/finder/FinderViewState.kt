package app.atomofiron.searchboxapp.screens.finder

import android.view.Gravity
import app.atomofiron.common.util.flow.ChannelFlow
import app.atomofiron.common.util.flow.EventFlow
import app.atomofiron.common.util.flow.invoke
import app.atomofiron.common.util.flow.set
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.screens.finder.model.FinderStateItem
import app.atomofiron.searchboxapp.screens.finder.model.FinderStateItem.ConfigItem
import app.atomofiron.searchboxapp.screens.finder.viewmodel.FinderItemsState
import app.atomofiron.searchboxapp.screens.finder.viewmodel.FinderItemsStateDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

class FinderViewState(
    private val scope: CoroutineScope,
) : FinderItemsState by FinderItemsStateDelegate(isLocal = false) {

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
        targets.clear()
        when {
            checked.isNotEmpty() -> {
                val filtered = checked.filter { target ->
                    !checked.any { target.parentPath.startsWith(it.path) }
                }
                targets.addAll(filtered)
            }
            currentDir != null -> targets.add(currentDir)
        }
        updateState()
    }

    fun setExcludeDirsValue(excludeDirs: Boolean) {
        updateConfig {
            copy(excludeDirs = excludeDirs)
        }
    }

    fun switchConfigItemVisibility() {
        var index = uniqueItems.indexOfFirst { it is ConfigItem }
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

    private inline fun updateConfig(action: ConfigItem.() -> ConfigItem) = updateConfig(configItem.action())
}