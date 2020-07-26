package app.atomofiron.searchboxapp.screens.finder.viewmodel

import app.atomofiron.common.util.LateinitLiveData
import app.atomofiron.searchboxapp.screens.finder.model.FinderStateItem
import kotlin.reflect.KClass

interface FinderItemsModel {
    /* 1 search/replace
     * characters
     * config (optional for finder screen)
     * test field
     * progressItems (only finder screen)
     * targetItems (only finder screen)
     */
    val uniqueItems: MutableList<FinderStateItem>
    val progressItems: MutableList<FinderStateItem.ProgressItem>
    val targetItems: MutableList<FinderStateItem.TargetItem>

    val searchItems: LateinitLiveData<List<FinderStateItem>>

    fun updateState()
    fun updateSearchQuery(value: String)
    fun updateConfig(item: FinderStateItem.ConfigItem)

    fun <I : FinderStateItem> getUniqueItem(kClass: KClass<I>): I
    fun <I : FinderStateItem> updateUniqueItem(item: I)
    fun <I : FinderStateItem> updateUniqueItem(kClass: KClass<I>, action: (I) -> I)
}