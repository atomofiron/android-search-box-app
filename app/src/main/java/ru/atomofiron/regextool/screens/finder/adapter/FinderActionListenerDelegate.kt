package ru.atomofiron.regextool.screens.finder.adapter

import ru.atomofiron.regextool.screens.finder.FinderViewModel
import ru.atomofiron.regextool.screens.finder.history.adapter.HistoryAdapter
import ru.atomofiron.regextool.screens.finder.model.FinderStateItem

class FinderActionListenerDelegate(
        private val viewModel: FinderViewModel,
        private val historyAdapter: HistoryAdapter
) : FinderAdapter.OnActionListener {

    override fun onSearchChange(value: String) = viewModel.onSearchChange(value)

    override fun onSearchClick(value: String) = historyAdapter.add(value)

    override fun onReplaceClick(value: String) = viewModel.onReplaceClick(value)

    override fun onCharacterClick(value: String) = viewModel.onCharacterClick(value)

    override fun onConfigChange(item: FinderStateItem.ConfigItem) = viewModel.onConfigChange(item)

    override fun onItemClick(item: FinderStateItem.ProgressItem) = viewModel.onItemClick(item)

    override fun onProgressStopClick(item: FinderStateItem.ProgressItem) = viewModel.onProgressStopClick(item)

    override fun onItemClick(item: FinderStateItem.TargetItem) = viewModel.onItemClick(item)
}