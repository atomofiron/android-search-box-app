package ru.atomofiron.regextool.screens.finder

import androidx.lifecycle.MutableLiveData
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.common.util.SingleLiveEvent
import ru.atomofiron.regextool.di.DaggerInjector
import ru.atomofiron.regextool.model.explorer.XFile
import ru.atomofiron.regextool.model.finder.FinderTaskChange
import ru.atomofiron.regextool.screens.finder.model.FinderStateItem
import ru.atomofiron.regextool.screens.finder.viewmodel.FinderItemsModel
import ru.atomofiron.regextool.screens.finder.viewmodel.FinderItemsModelDelegate

class FinderViewModel : BaseViewModel<FinderComponent, FinderFragment>(), FinderItemsModel by FinderItemsModelDelegate() {
    var configItem: FinderStateItem.ConfigItem? = FinderStateItem.ConfigItem()
        private set
    val targets = ArrayList<XFile>()

    val historyDrawerGravity = MutableLiveData<Int>()
    val reloadHistory = SingleLiveEvent<Unit>()
    val insertInQuery = SingleLiveEvent<String>()
    val replaceQuery = SingleLiveEvent<String>()
    val snackbar = SingleLiveEvent<String>()
    val history = SingleLiveEvent<String>()

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
                val items = progressItems.map { FinderStateItem.ProgressItem(it.finderTask) }
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