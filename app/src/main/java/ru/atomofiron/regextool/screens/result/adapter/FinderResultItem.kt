package ru.atomofiron.regextool.screens.result.adapter

import ru.atomofiron.regextool.model.finder.FinderResult

sealed class FinderResultItem {
    class Header(var dirsCount: Int, var filesCount: Int) : FinderResultItem()
    class Item(val item: FinderResult) : FinderResultItem()
}