package ru.atomofiron.regextool.screens.finder.presenter

import ru.atomofiron.regextool.App
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.screens.finder.FinderViewModel
import ru.atomofiron.regextool.screens.finder.adapter.FinderAdapterOutput
import ru.atomofiron.regextool.screens.finder.model.FinderStateItem

class FinderAdapterPresenterDelegate(
        private val viewModel: FinderViewModel
) : FinderAdapterOutput {

    override fun onConfigChange(item: FinderStateItem.ConfigItem) {
        val oldItem = viewModel.getItem(FinderStateItem.ConfigItem::class)

        val ignoreCaseChanged = oldItem.ignoreCase xor item.ignoreCase
        val replaceEnabledChanged = oldItem.replaceEnabled xor item.replaceEnabled
        val useRegexpChanged = oldItem.useRegex xor item.useRegex

        if (replaceEnabledChanged || useRegexpChanged) {
            viewModel.updateItem(FinderStateItem.SearchAndReplaceItem::class) {
                it.copy(replaceEnabled = item.replaceEnabled, useRegex = item.useRegex)
            }
        }

        viewModel.updateItem(item)

        if (ignoreCaseChanged || replaceEnabledChanged || useRegexpChanged) {
            viewModel.updateItem(FinderStateItem.TestItem::class) {
                it.copy(useRegex = item.useRegex,
                        ignoreCase = item.ignoreCase,
                        multilineSearch = item.multilineSearch)
            }
        }
    }

    override fun onCharacterClick(value: String) = viewModel.insertInQuery.invoke(value)

    override fun onSearchChange(value: String) {
        viewModel.updateItem(FinderStateItem.TestItem::class) {
            it.copy(searchQuery = value)
        }
        val item = viewModel.getItem(FinderStateItem.SearchAndReplaceItem::class)
        item.query = value
        // do not notify
    }

    override fun onItemClick(item: FinderStateItem.ProgressItem) {
    }

    override fun onProgressStopClick(item: FinderStateItem.ProgressItem) {
    }

    override fun onReplaceClick(value: String) {
    }

    override fun onItemClick(item: FinderStateItem.TargetItem) {
        val context = viewModel.getApplication<App>().applicationContext
        viewModel.snackbar.invoke(context.getString(R.string.oops_not_working))
    }

    override fun onSearchClick(value: String) = viewModel.history.invoke(value)
}