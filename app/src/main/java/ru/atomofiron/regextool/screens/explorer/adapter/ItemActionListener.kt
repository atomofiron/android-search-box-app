package ru.atomofiron.regextool.screens.explorer.adapter

import ru.atomofiron.regextool.iss.service.model.XFile

interface ItemActionListener {
    fun onItemClick(item: XFile)
    fun onItemVisible(item: XFile)
}