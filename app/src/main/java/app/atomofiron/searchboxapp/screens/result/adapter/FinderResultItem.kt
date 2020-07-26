package app.atomofiron.searchboxapp.screens.result.adapter

import app.atomofiron.searchboxapp.model.finder.FinderResult

sealed class FinderResultItem {
    class Header(var dirsCount: Int, var filesCount: Int) : FinderResultItem()
    class Item(val item: FinderResult) : FinderResultItem()
}