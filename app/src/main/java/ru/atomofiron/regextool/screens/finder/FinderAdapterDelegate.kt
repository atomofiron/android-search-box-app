package ru.atomofiron.regextool.screens.finder

import ru.atomofiron.regextool.screens.finder.adapter.OnFinderActionListener
import ru.atomofiron.regextool.screens.finder.history.adapter.HistoryAdapter

class FinderAdapterDelegate(
        private val adapter: HistoryAdapter,
        private val output: FinderViewModel
) : OnFinderActionListener {
    override fun onSearchClick(query: String) {
        adapter.add(query)
    }
}