package app.atomofiron.searchboxapp.screens.finder

import android.view.Gravity
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.common.util.flow.ChannelFlow
import app.atomofiron.common.util.flow.invoke
import app.atomofiron.common.util.flow.set
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.finder.FinderTaskChange
import app.atomofiron.searchboxapp.screens.finder.model.FinderStateItem
import app.atomofiron.searchboxapp.screens.finder.viewmodel.FinderItemsModel
import app.atomofiron.searchboxapp.screens.finder.viewmodel.FinderItemsModelDelegate
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class FinderViewModel : BaseViewModel<FinderComponent, FinderFragment, FinderPresenter>(), FinderItemsModel by FinderItemsModelDelegate() {
    var configItem: FinderStateItem.ConfigItem? = FinderStateItem.ConfigItem()
        private set
    val targets = ArrayList<Node>()

    val historyDrawerGravity = MutableStateFlow(Gravity.START)
    val reloadHistory = ChannelFlow<Unit>()
    val insertInQuery = ChannelFlow<String>()
    val replaceQuery = ChannelFlow<String>()
    val snackbar = ChannelFlow<String>()
    val history = ChannelFlow<String>()

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

    fun reloadHistory() = reloadHistory.invoke(viewModelScope)

    fun insertInQuery(value: String) {
        insertInQuery[viewModelScope] = value
    }

    fun replaceQuery(value: String) {
        replaceQuery[viewModelScope] = value
    }

    fun showSnackbar(value: String) {
        snackbar[viewModelScope] = value
    }

    fun addToHistory(value: String) {
        history[viewModelScope] = value
    }
}