package app.atomofiron.searchboxapp.injectable.store

import app.atomofiron.common.util.flow.dataFlow
import app.atomofiron.common.util.flow.value
import app.atomofiron.searchboxapp.model.explorer.Change
import app.atomofiron.searchboxapp.model.explorer.MutableXFile
import app.atomofiron.searchboxapp.model.explorer.XFile
import app.atomofiron.searchboxapp.logI

class ExplorerStore {
    val items: MutableList<MutableXFile> = ArrayList()
    val store = dataFlow<List<XFile>>(items)
    val updates = dataFlow<Change>(Change.Nothing, single = true)
    val current = dataFlow<XFile?>(null)
    val alerts = dataFlow<String>(single = true)

    val checked: MutableList<MutableXFile> = ArrayList()
    val storeChecked = dataFlow<List<XFile>>(ArrayList())

    fun notifyCurrent(item: XFile?) {
        logI("notifyCurrent $item")
        current.value = item
    }

    fun notifyUpdate(item: XFile) {
        logI("notifyUpdate $item")
        updates.value = Change.Update(item)

        if (checked.contains(item)) {
            notifyChecked()
        }
    }

    fun notifyUpdateRange(items: List<XFile>) {
        logI("notifyUpdateRange ${items.size}")
        updates.value = Change.UpdateRange(items)
    }

    fun notifyRemove(item: XFile) {
        logI("notifyRemove $item")
        updates.value = Change.Remove(item)

        if (checked.remove(item)) {
            notifyChecked()
        }
    }

    fun notifyInsert(previous: XFile, item: XFile) {
        logI("notifyInsert $item after $previous")
        updates.value = Change.Insert(previous, item)
    }

    fun notifyRemoveRange(items: List<XFile>) {
        logI("notifyRemoveRange ${items.size}")
        updates.value = Change.RemoveRange(items)

        if (checked.removeAll(items)) {
            notifyChecked()
        }
    }

    fun notifyInsertRange(previous: XFile, items: List<XFile>) {
        logI("notifyInsert ${items.size} after $previous")
        updates.value = Change.InsertRange(previous, items)
    }

    fun notifyItems() {
        logI("notifyItems ${items.size}")
        store.value = items

        val size = checked.size
        checked.filter { !items.contains(it) }
                .forEach { checked.remove(it) }
        if (checked.size != size) {
            notifyChecked()
        }
    }

    fun notifyChecked() {
        logI("notifyChecked")
        storeChecked.value = ArrayList(checked)
    }
}