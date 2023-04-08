package app.atomofiron.searchboxapp.screens.result.adapter

import app.atomofiron.searchboxapp.model.finder.SearchResult

sealed class FinderResultItem {
    class Header(var dirsCount: Int, var filesCount: Int) : FinderResultItem()
    class Item(val item: SearchResult) : FinderResultItem()
}