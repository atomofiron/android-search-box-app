package app.atomofiron.searchboxapp.screens.result.adapter

import app.atomofiron.searchboxapp.model.finder.ItemMatch

sealed class ResultItem {
    class Header(var dirsCount: Int, var filesCount: Int) : ResultItem()
    class Item(val item: ItemMatch) : ResultItem()
}