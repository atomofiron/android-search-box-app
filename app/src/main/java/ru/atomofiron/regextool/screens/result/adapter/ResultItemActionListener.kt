package ru.atomofiron.regextool.screens.result.adapter

import ru.atomofiron.regextool.screens.explorer.adapter.util.ExplorerItemBinder

interface ResultItemActionListener : ExplorerItemBinder.ExplorerItemBinderActionListener {
    fun onItemVisible(item: FinderResultItem.Item)
}