package app.atomofiron.searchboxapp.screens.finder.viewmodel

import app.atomofiron.searchboxapp.screens.finder.model.FinderStateItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.reflect.KClass

interface FinderItemsState {
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

    val searchItems: MutableStateFlow<List<FinderStateItem>>

    fun updateState(isLocal: Boolean = false)
    fun updateSearchQuery(value: String)
    fun updateConfig(item: FinderStateItem.ConfigItem)

    fun <I : FinderStateItem> getUniqueItem(kClass: KClass<I>): I
    fun <I : FinderStateItem> updateUniqueItem(item: I)
    fun <I : FinderStateItem> updateUniqueItem(kClass: KClass<I>, action: (I) -> I)
}