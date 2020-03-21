package ru.atomofiron.regextool.screens.explorer.adapter

import ru.atomofiron.regextool.iss.service.explorer.model.XFile

interface ExplorerItemActionListener {
    fun onItemClick(item: XFile)
    fun onItemCheck(item: XFile, isChecked: Boolean)
    fun onItemVisible(item: XFile)
    fun onItemInvalidate(item: XFile)
}