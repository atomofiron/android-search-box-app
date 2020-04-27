package ru.atomofiron.regextool.screens.finder

import android.app.Application
import androidx.lifecycle.MutableLiveData
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.common.util.LateinitLiveData
import app.atomofiron.common.util.SingleLiveEvent
import ru.atomofiron.regextool.di.DaggerInjector
import ru.atomofiron.regextool.injectable.service.explorer.model.XFile
import ru.atomofiron.regextool.model.finder.FinderTaskChange
import ru.atomofiron.regextool.screens.finder.model.FinderStateItem
import kotlin.reflect.KClass

class FinderViewModel(app: Application) : BaseViewModel<FinderComponent, FinderFragment>(app) {
    /* 1 search/replace
     * characters
     * config (optional)
     * test field
     * progressItems
     * targetItems
     */
    val uniqueItems = ArrayList<FinderStateItem>()
    val progressItems = ArrayList<FinderStateItem.ProgressItem>()
    private val targetItems = ArrayList<FinderStateItem.TargetItem>()

    var configItem: FinderStateItem.ConfigItem? = FinderStateItem.ConfigItem()
        private set
    val targets = ArrayList<XFile>()

    val historyDrawerGravity = MutableLiveData<Int>()
    val state = LateinitLiveData<List<FinderStateItem>>()
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

    fun updateState() {
        val items = ArrayList<FinderStateItem>()
        items.addAll(uniqueItems)
        items.addAll(progressItems)
        items.addAll(targetItems)
        state.value = items
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

    @Suppress("UNCHECKED_CAST")
    fun <I : FinderStateItem> getUniqueItem(kClass: KClass<I>): I {
        return uniqueItems.find { it::class == kClass } as I
    }

    fun <I : FinderStateItem> updateUniqueItem(item: I) {
        val index = uniqueItems.indexOfFirst { it::class == item::class }
        uniqueItems.removeAt(index)
        uniqueItems.add(index, item)
        updateState()
    }

    fun <I : FinderStateItem> updateUniqueItem(kClass: KClass<I>, action: (I) -> I) {
        val index = uniqueItems.indexOfFirst { it::class == kClass }
        val removed = uniqueItems.removeAt(index)
        @Suppress("UNCHECKED_CAST")
        val item = action(removed as I)
        uniqueItems.add(index, item)
        updateState()
    }
}