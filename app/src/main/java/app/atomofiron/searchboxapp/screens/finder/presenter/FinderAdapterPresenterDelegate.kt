package app.atomofiron.searchboxapp.screens.finder.presenter

import app.atomofiron.searchboxapp.injectable.interactor.FinderInteractor
import app.atomofiron.searchboxapp.screens.finder.FinderRouter
import app.atomofiron.searchboxapp.screens.finder.FinderViewState
import app.atomofiron.searchboxapp.screens.finder.adapter.FinderAdapterOutput
import app.atomofiron.searchboxapp.screens.finder.model.FinderStateItem

class FinderAdapterPresenterDelegate(
    private val viewState: FinderViewState,
    private val router: FinderRouter,
    private val interactor: FinderInteractor
) : FinderAdapterOutput {

    override fun onConfigChange(item: FinderStateItem.ConfigItem) = viewState.updateConfig(item)

    override fun onConfigVisibilityClick() = viewState.switchConfigItemVisibility()

    override fun onHistoryClick() = viewState.showHistory()

    override fun onCharacterClick(value: String) = viewState.insertInQuery(value)

    override fun onSearchChange(value: String) = viewState.updateSearchQuery(value)

    override fun onItemClick(item: FinderStateItem.ProgressItem) {
        router.showResult(item.task.id)
    }

    override fun onProgressStopClick(item: FinderStateItem.ProgressItem) {
        interactor.stop(item.task.uuid)
    }

    override fun onProgressRemoveClick(item: FinderStateItem.ProgressItem) {
        interactor.drop(item.task)
    }

    override fun onReplaceClick(value: String) {
    }

    override fun onSearchClick(value: String) {
        if (viewState.targets.isEmpty()) {
            return
        }
        viewState.addToHistory(value)
        val config = viewState.getConfigItem()
        interactor.search(value, viewState.targets, config.ignoreCase, config.useRegex, config.excludeDirs, config.searchInContent)
    }
}