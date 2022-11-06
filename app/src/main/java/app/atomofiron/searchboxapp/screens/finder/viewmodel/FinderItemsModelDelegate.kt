package app.atomofiron.searchboxapp.screens.finder.viewmodel

import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.screens.finder.model.FinderStateItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.reflect.KClass

class FinderItemsModelDelegate : FinderItemsModel {
    override val uniqueItems = mutableListOf<FinderStateItem>()
    override val progressItems = mutableListOf<FinderStateItem.ProgressItem>()
    override val targetItems = mutableListOf<FinderStateItem.TargetItem>()

    override val searchItems = MutableStateFlow<List<FinderStateItem>>(listOf())

    override fun updateState(isLocal: Boolean) {
        val items = ArrayList<FinderStateItem>()
        items.addAll(uniqueItems)
        items.addAll(progressItems)
        items.addAll(targetItems)
        when {
            isLocal -> Unit
            targetItems.isEmpty() -> items.add(FinderStateItem.TipItem(R.string.tip))
            else -> items.add(FinderStateItem.TipItem(R.string.search_here))
        }
        searchItems.value = items
    }

    override fun updateSearchQuery(value: String) {
        updateUniqueItem(FinderStateItem.TestItem::class) {
            it.copy(searchQuery = value)
        }
        val item = getUniqueItem(FinderStateItem.SearchAndReplaceItem::class)
        item.query = value
        // do not notify
    }

    override fun updateConfig(item: FinderStateItem.ConfigItem) {
        val oldItem = getUniqueItem(FinderStateItem.ConfigItem::class)

        val ignoreCaseChanged = oldItem.ignoreCase xor item.ignoreCase
        val replaceEnabledChanged = oldItem.replaceEnabled xor item.replaceEnabled
        val useRegexpChanged = oldItem.useRegex xor item.useRegex
        val multilineSearchChanged = oldItem.excludeDirs xor item.excludeDirs

        if (replaceEnabledChanged || useRegexpChanged) {
            updateUniqueItem(FinderStateItem.SearchAndReplaceItem::class) {
                it.copy(replaceEnabled = item.replaceEnabled, useRegex = item.useRegex)
            }
        }

        updateUniqueItem(item)

        if (ignoreCaseChanged || useRegexpChanged || multilineSearchChanged) {
            updateUniqueItem(FinderStateItem.TestItem::class) {
                it.copy(useRegex = item.useRegex, ignoreCase = item.ignoreCase)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <I : FinderStateItem> getUniqueItem(kClass: KClass<I>): I {
        return uniqueItems.find { it::class == kClass } as I
    }

    override fun <I : FinderStateItem> updateUniqueItem(item: I) {
        val index = uniqueItems.indexOfFirst { it::class == item::class }
        uniqueItems.removeAt(index)
        uniqueItems.add(index, item)
        updateState()
    }

    override fun <I : FinderStateItem> updateUniqueItem(kClass: KClass<I>, action: (I) -> I) {
        val index = uniqueItems.indexOfFirst { it::class == kClass }
        val removed = uniqueItems.removeAt(index)
        @Suppress("UNCHECKED_CAST")
        val item = action(removed as I)
        uniqueItems.add(index, item)
        updateState()
    }
}