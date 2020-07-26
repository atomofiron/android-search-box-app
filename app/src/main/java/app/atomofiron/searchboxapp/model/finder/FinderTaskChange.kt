package app.atomofiron.searchboxapp.model.finder

sealed class FinderTaskChange {
    class Add(val task: FinderTask) : FinderTaskChange()
    class Update(val tasks: List<FinderTask>) : FinderTaskChange()
    class Drop(val task: FinderTask) : FinderTaskChange()
}