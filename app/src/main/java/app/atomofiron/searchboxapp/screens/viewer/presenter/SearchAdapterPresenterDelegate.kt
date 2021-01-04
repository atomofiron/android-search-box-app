package app.atomofiron.searchboxapp.screens.viewer.presenter

import app.atomofiron.searchboxapp.injectable.interactor.TextViewerInteractor
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.finder.adapter.FinderAdapterOutput
import app.atomofiron.searchboxapp.screens.finder.model.FinderStateItem
import app.atomofiron.searchboxapp.screens.viewer.TextViewerViewModel

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

    override fun onCharacterClick(value: String) = viewModel.insertInQuery.emit(value)

    override fun onSearchChange(value: String) = viewModel.updateSearchQuery(value)

    override fun onSearchClick(value: String) {
        val config = viewModel.getUniqueItem(FinderStateItem.ConfigItem::class)
        interactor.search(value, config.ignoreCase, config.useRegex)
        viewModel.closeBottomSheet.emit()
    }

    override fun onItemClick(item: FinderStateItem.ProgressItem) {
        if (item.finderTask.count > 0) {
            viewModel.closeBottomSheet.emit()
            interactor.showTask(item.finderTask)
        }
    }

    override fun onProgressRemoveClick(item: FinderStateItem.ProgressItem) {
        interactor.removeTask(item.finderTask)
    }

    override fun onReplaceClick(value: String) = Unit

    override fun onProgressStopClick(item: FinderStateItem.ProgressItem) = Unit
}