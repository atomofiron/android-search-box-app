package ru.atomofiron.regextool.screens.finder.presenter

import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.injectable.interactor.FinderInteractor
import ru.atomofiron.regextool.screens.finder.FinderRouter
import ru.atomofiron.regextool.screens.finder.FinderViewModel
import ru.atomofiron.regextool.screens.finder.adapter.FinderAdapterOutput
import ru.atomofiron.regextool.screens.finder.model.FinderStateItem

class FinderAdapterPresenterDelegate(
        private val viewModel: FinderViewModel,
        private val router: FinderRouter,
        private val interactor: FinderInteractor
) : FinderAdapterOutput {

    override fun onConfigChange(item: FinderStateItem.ConfigItem) {
        val oldItem = viewModel.getUniqueItem(FinderStateItem.ConfigItem::class)

        val ignoreCaseChanged = oldItem.ignoreCase xor item.ignoreCase
        val replaceEnabledChanged = oldItem.replaceEnabled xor item.replaceEnabled
        val useRegexpChanged = oldItem.useRegex xor item.useRegex

        if (replaceEnabledChanged || useRegexpChanged) {
            viewModel.updateUniqueItem(FinderStateItem.SearchAndReplaceItem::class) {
                it.copy(replaceEnabled = item.replaceEnabled, useRegex = item.useRegex)
            }
        }

        viewModel.updateUniqueItem(item)

        if (ignoreCaseChanged || replaceEnabledChanged || useRegexpChanged) {
            viewModel.updateUniqueItem(FinderStateItem.TestItem::class) {
                it.copy(useRegex = item.useRegex,
                        ignoreCase = item.ignoreCase,
                        multilineSearch = item.multilineSearch)
            }
        }
    }

    override fun onCharacterClick(value: String) = viewModel.insertInQuery.invoke(value)

    override fun onSearchChange(value: String) {
        viewModel.updateUniqueItem(FinderStateItem.TestItem::class) {
            it.copy(searchQuery = value)
        }
        val item = viewModel.getUniqueItem(FinderStateItem.SearchAndReplaceItem::class)
        item.query = value
        // do not notify
    }

    override fun onItemClick(item: FinderStateItem.TargetItem) {
        val context = viewModel.context
        viewModel.snackbar.invoke(context.getString(R.string.oops_not_working))
    }

    override fun onItemClick(item: FinderStateItem.ProgressItem) {
        router.showResult(item.finderTask.id)
    }

    override fun onProgressStopClick(item: FinderStateItem.ProgressItem) {
        interactor.stop(item.finderTask)
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
        viewModel.history.invoke(value)
        val config = viewModel.configItem ?: viewModel.getUniqueItem(FinderStateItem.ConfigItem::class)
        interactor.search(value, viewModel.targets, config.ignoreCase, config.useRegex, config.multilineSearch, config.searchInContent)
    }
}