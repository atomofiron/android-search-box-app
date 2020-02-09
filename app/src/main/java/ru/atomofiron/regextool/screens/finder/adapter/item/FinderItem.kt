package ru.atomofiron.regextool.screens.finder.adapter.item

sealed class FinderItem(val id: Int) {
    class SomeItem(id: Int) : FinderItem(id)
    class FieldItem(id: Int, val replace: Boolean) : FinderItem(id)
}
