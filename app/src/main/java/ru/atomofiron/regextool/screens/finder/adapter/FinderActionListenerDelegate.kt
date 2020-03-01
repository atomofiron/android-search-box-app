package ru.atomofiron.regextool.screens.finder.adapter

import ru.atomofiron.regextool.screens.finder.FinderViewModel
import ru.atomofiron.regextool.screens.finder.history.adapter.HistoryAdapter
import ru.atomofiron.regextool.screens.finder.model.FinderStateItem
import java.util.regex.Matcher

class FinderActionListenerDelegate(
        private val viewModel: FinderViewModel,
        private val historyAdapter: HistoryAdapter
) : FinderAdapter.OnActionListener {

    override fun onSearchClick(value: String) = historyAdapter.add(value)

    override fun onReplaceClick(value: String) {
    }

    override fun onCharacterClick(value: String) {
    }

    override fun onConfigChange(item: FinderStateItem.Config) = viewModel.onConfigChange(item)

    override fun onTextChange(): Matcher? {
        return null
    }

    override fun onItemClick(item: FinderStateItem.ProgressItem) {
    }

    override fun onItemClick(item: FinderStateItem.ResultItem) {
    }
}