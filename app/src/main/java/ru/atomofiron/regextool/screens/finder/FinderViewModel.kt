package ru.atomofiron.regextool.screens.finder

import android.app.Application
import androidx.lifecycle.MutableLiveData
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.common.util.LateinitLiveData
import app.atomofiron.common.util.SingleLiveEvent
import ru.atomofiron.regextool.di.DaggerInjector
import ru.atomofiron.regextool.screens.finder.model.FinderStateItem
import ru.atomofiron.regextool.screens.finder.model.FinderStateItemUpdate
import kotlin.reflect.KClass

class FinderViewModel(app: Application) : BaseViewModel<FinderComponent, FinderFragment>(app) {
    val items = ArrayList<FinderStateItem>()
    var configItem: FinderStateItem.ConfigItem? = FinderStateItem.ConfigItem()
        private set

    val historyDrawerGravity = MutableLiveData<Int>()
    val state = LateinitLiveData<List<FinderStateItem>>()
    val reloadHistory = SingleLiveEvent<Unit>()
    val insertInQuery = SingleLiveEvent<String>()
    val replaceQuery = SingleLiveEvent<String>()
    val updateContent = SingleLiveEvent<FinderStateItemUpdate>()
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

    fun switchConfigItemVisibility() {
        when (val configItem = configItem) {
            null -> {
                val item = getItem(FinderStateItem.ConfigItem::class)
                this.configItem = item
                val index = items.indexOf(item)
                items.removeAt(index)
                updateContent.invoke(FinderStateItemUpdate.Removed(index))
            }
            else -> {
                val index = items.indexOf(getItem(FinderStateItem.TestItem::class))
                items.add(index, configItem)
                updateContent.invoke(FinderStateItemUpdate.Inserted(index, configItem))
                this.configItem = null
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <I : FinderStateItem> getItem(kClass: KClass<I>): I {
        return items.find { it::class == kClass } as I
    }

    fun <I : FinderStateItem> updateItem(item: I) {
        val index = items.indexOfFirst { it::class == item::class }
        items.removeAt(index)
        items.add(index, item)
        updateContent.invoke(FinderStateItemUpdate.Changed(index, item))
    }

    fun <I : FinderStateItem> updateItem(kClass: KClass<I>, action: (I) -> I) {
        val index = items.indexOfFirst { it::class == kClass }
        val removed = items.removeAt(index)
        @Suppress("UNCHECKED_CAST")
        val item = action(removed as I)
        items.add(index, item)
        updateContent.invoke(FinderStateItemUpdate.Changed(index, item))
    }
}