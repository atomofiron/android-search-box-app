package ru.atomofiron.regextool.model.finder

sealed class FinderTaskChange {
    class Add(val task: FinderTask) : FinderTaskChange()
    object Update : FinderTaskChange()
    class Drop(val task: FinderTask) : FinderTaskChange()
}