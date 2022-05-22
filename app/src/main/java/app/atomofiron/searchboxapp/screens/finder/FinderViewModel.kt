package app.atomofiron.searchboxapp.screens.finder

import androidx.fragment.app.Fragment
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.common.util.flow.sharedFlow
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.model.explorer.XFile
import app.atomofiron.searchboxapp.model.finder.FinderTaskChange
import app.atomofiron.searchboxapp.screens.finder.model.FinderStateItem
import app.atomofiron.searchboxapp.screens.finder.viewmodel.FinderItemsModel
import app.atomofiron.searchboxapp.screens.finder.viewmodel.FinderItemsModelDelegate
import javax.inject.Inject

class FinderViewModel : BaseViewModel<FinderComponent, FinderFragment, FinderPresenter>(), FinderItemsModel by FinderItemsModelDelegate() {
    var configItem: FinderStateItem.ConfigItem? = FinderStateItem.ConfigItem()
        private set
    val targets = ArrayList<XFile>()

    val historyDrawerGravity = sharedFlow<Int>()
    val reloadHistory = sharedFlow(Unit, single = true)
    val insertInQuery = sharedFlow<String>(single = true)
    val replaceQuery = sharedFlow<String>(single = true)
    val snackbar = sharedFlow<String>(single = true)
    val history = sharedFlow<String>(single = true)

    @Inject
    override lateinit var presenter: FinderPresenter

    override fun inject(view: FinderFragment) {
        super.inject(view)
        component.inject(this)
    }

    override fun createComponent(fragmentProperty: WeakProperty<Fragment>) = DaggerFinderComponent
        .builder()
        .bind(this)
        .bind(fragmentProperty)
        .dependencies(DaggerInjector.appComponent)
        .build()

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