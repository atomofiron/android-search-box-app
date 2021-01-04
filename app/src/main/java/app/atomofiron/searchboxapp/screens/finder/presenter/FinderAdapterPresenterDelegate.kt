package app.atomofiron.searchboxapp.screens.finder.presenter

import app.atomofiron.searchboxapp.injectable.interactor.FinderInteractor
import app.atomofiron.searchboxapp.screens.finder.FinderRouter
import app.atomofiron.searchboxapp.screens.finder.FinderViewModel
import app.atomofiron.searchboxapp.screens.finder.adapter.FinderAdapterOutput
import app.atomofiron.searchboxapp.screens.finder.model.FinderStateItem

class FinderAdapterPresenterDelegate(
        private val viewModel: FinderViewModel,
        private val router: FinderRouter,
        private val interactor: FinderInteractor
) : FinderAdapterOutput {

    override fun onConfigChange(item: FinderStateItem.ConfigItem) = viewModel.updateConfig(item)

    override fun onCharacterClick(value: String) = viewModel.insertInQuery.emit(value)

    override fun onSearchChange(value: String) = viewModel.updateSearchQuery(value)

    override fun onItemClick(item: FinderStateItem.ProgressItem) {
        router.showResult(item.finderTask.id)
    }

    override fun onProgressStopClick(item: FinderStateItem.ProgressItem) {
        interactor.stop(item.finderTask.uuid)
    }

    override fun onProgressRemoveClick(item: FinderStateItem.ProgressItem) {
        interactor.drop(item.finderTask)
    }

    override fun onReplaceClick(value: String) {
    }

    override fun onSearchClick(value: String) {
        if (viewModel.targets.isEmpty()) {
            return
        }
        viewModel.history.value = value
        val config = viewModel.configItem ?: viewModel.getUniqueItem(FinderStateItem.ConfigItem::class)
        interactor.search(value, viewModel.targets, config.ignoreCase, config.useRegex, config.excludeDirs, config.searchInContent)
    }
}