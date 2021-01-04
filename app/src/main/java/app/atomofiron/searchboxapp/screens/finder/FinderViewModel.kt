package app.atomofiron.searchboxapp.screens.finder

import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.common.util.flow.LiveDataFlow
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.model.explorer.XFile
import app.atomofiron.searchboxapp.model.finder.FinderTaskChange
import app.atomofiron.searchboxapp.screens.finder.model.FinderStateItem
import app.atomofiron.searchboxapp.screens.finder.viewmodel.FinderItemsModel
import app.atomofiron.searchboxapp.screens.finder.viewmodel.FinderItemsModelDelegate

class FinderViewModel : BaseViewModel<FinderComponent, FinderFragment>(), FinderItemsModel by FinderItemsModelDelegate() {
    var configItem: FinderStateItem.ConfigItem? = FinderStateItem.ConfigItem()
        private set
    val targets = ArrayList<XFile>()

    val historyDrawerGravity = LiveDataFlow<Int>()
    val reloadHistory = LiveDataFlow(Unit, single = true)
    val insertInQuery = LiveDataFlow<String>(single = true)
    val replaceQuery = LiveDataFlow<String>(single = true)
    val snackbar = LiveDataFlow<String>(single = true)
    val history = LiveDataFlow<String>(single = true)

    override val component = DaggerFinderComponent
            .builder()
            .bind(this)
            .bind(viewProperty)
            .dependencies(DaggerInjector.appComponent)
            .build()

    override fun inject(view: FinderFragment) {
        super.inject(view)
        component.inject(this)
        component.inject(view)
    }

    fun updateTargets(currentDir: XFile?, checked: List<XFile>) {
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
                progressItems.removeAt(index)
            }
        }
        updateState()
    }

    fun setExcludeDirsValue(excludeDirs: Boolean) {
        configItem = configItem?.copy(excludeDirs = excludeDirs)
    }

    fun switchConfigItemVisibility() {
        when (val configItem = configItem) {
            null -> {
                val item = getUniqueItem(FinderStateItem.ConfigItem::class)
                this.configItem = item
                val index = uniqueItems.indexOf(item)
                uniqueItems.removeAt(index)
            }
            else -> {
                val index = uniqueItems.indexOf(getUniqueItem(FinderStateItem.TestItem::class))
                uniqueItems.add(index, configItem)
                this.configItem = null
            }
        }
        updateState()
    }
}