package app.atomofiron.searchboxapp.model.explorer

sealed class Change {
    object Nothing : Change()
    class Update(val item: XFile) : Change()
    class Remove(val item: XFile) : Change()
    class Insert(val previous: XFile, val item: XFile) : Change()
    class UpdateRange(val items: List<XFile>) : Change()
    class RemoveRange(val items: List<XFile>) : Change()
    class InsertRange(val previous: XFile, val items: List<XFile>) : Change()
}