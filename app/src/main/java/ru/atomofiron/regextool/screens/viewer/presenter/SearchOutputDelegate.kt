package ru.atomofiron.regextool.screens.viewer.presenter

import ru.atomofiron.regextool.injectable.store.PreferenceStore
import ru.atomofiron.regextool.screens.finder.adapter.FinderAdapterOutput
import ru.atomofiron.regextool.screens.finder.model.FinderStateItem
import ru.atomofiron.regextool.screens.viewer.TextViewerViewModel

class SearchOutputDelegate(
        private val viewModel: TextViewerViewModel,
        preferenceStore: PreferenceStore
) : FinderAdapterOutput {
    private val items = ArrayList<FinderStateItem>()

    init {
        items.add(FinderStateItem.TestItem())
        items.add(FinderStateItem.ConfigItem(isLocal = true))
        items.add(FinderStateItem.SpecialCharactersItem(preferenceStore.specialCharacters.entity))
        items.add(FinderStateItem.SearchAndReplaceItem())
        viewModel.serachItems.value = items
    }

    override fun onSearchClick(value: String) {
        // todo next
    }

    override fun onSearchChange(value: String) {
    }

    override fun onReplaceClick(value: String) = Unit

    override fun onCharacterClick(value: String) {
    }

    override fun onConfigChange(item: FinderStateItem.ConfigItem) {
    }

    override fun onItemClick(item: FinderStateItem.ProgressItem) = Unit

    override fun onProgressStopClick(item: FinderStateItem.ProgressItem) = Unit

    override fun onProgressRemoveClick(item: FinderStateItem.ProgressItem) = Unit
}