package ru.atomofiron.regextool.screens.explorer.adapter

import ru.atomofiron.regextool.iss.service.model.XFile

interface ExplorerItemActionListener {
    fun onItemClick(item: XFile)
    fun onItemVisible(item: XFile)
    fun onItemInvalidate(item: XFile)
}