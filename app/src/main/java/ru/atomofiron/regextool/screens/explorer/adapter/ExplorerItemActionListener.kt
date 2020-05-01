package ru.atomofiron.regextool.screens.explorer.adapter

import ru.atomofiron.regextool.model.explorer.XFile
import ru.atomofiron.regextool.screens.explorer.adapter.util.ExplorerItemBinder

interface ExplorerItemActionListener : ExplorerItemBinder.ExplorerItemBinderActionListener {
    fun onItemVisible(item: XFile)
    fun onItemInvalidate(item: XFile)
}