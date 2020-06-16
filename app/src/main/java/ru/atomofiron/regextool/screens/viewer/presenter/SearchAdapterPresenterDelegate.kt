package ru.atomofiron.regextool.screens.viewer.presenter

import ru.atomofiron.regextool.injectable.interactor.TextViewerInteractor
import ru.atomofiron.regextool.injectable.store.PreferenceStore
import ru.atomofiron.regextool.screens.finder.adapter.FinderAdapterOutput
import ru.atomofiron.regextool.screens.finder.model.FinderStateItem
import ru.atomofiron.regextool.screens.viewer.TextViewerViewModel

class SearchAdapterPresenterDelegate(
        private val viewModel: TextViewerViewModel,
        private val interactor: TextViewerInteractor,
        preferenceStore: PreferenceStore
) : FinderAdapterOutput {

    init {
        viewModel.uniqueItems.add(FinderStateItem.SearchAndReplaceItem())
        val characters = preferenceStore.specialCharacters.entity
        viewModel.uniqueItems.add(FinderStateItem.SpecialCharactersItem(characters))
        viewModel.uniqueItems.add(FinderStateItem.ConfigItem(isLocal = true))
        viewModel.uniqueItems.add(FinderStateItem.TestItem())
        viewModel.updateState()
    }

    override fun onConfigChange(item: FinderStateItem.ConfigItem) = viewModel.updateConfig(item)

    override fun onCharacterClick(value: String) = viewModel.insertInQuery.invoke(value)

    override fun onSearchChange(value: String) = viewModel.updateSearchQuery(value)

    override fun onSearchClick(value: String) {
        val config = viewModel.getUniqueItem(FinderStateItem.ConfigItem::class)
        interactor.search(value, config.ignoreCase, config.useRegex)
    }

    override fun onItemClick(item: FinderStateItem.ProgressItem) {
        viewModel.closeBottomSheet.invoke()
        interactor.showTask(item.finderTask)
    }

    override fun onProgressRemoveClick(item: FinderStateItem.ProgressItem) {
        interactor.removeTask(item.finderTask)
    }

    override fun onReplaceClick(value: String) = Unit

    override fun onProgressStopClick(item: FinderStateItem.ProgressItem) = Unit
}