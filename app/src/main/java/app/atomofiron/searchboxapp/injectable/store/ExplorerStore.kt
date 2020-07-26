package app.atomofiron.searchboxapp.injectable.store

import app.atomofiron.common.util.KObservable
import app.atomofiron.searchboxapp.model.explorer.Change
import app.atomofiron.searchboxapp.model.explorer.MutableXFile
import app.atomofiron.searchboxapp.model.explorer.XFile
import app.atomofiron.searchboxapp.logI

class ExplorerStore {
    val items: MutableList<MutableXFile> = ArrayList()
    val store = KObservable<List<XFile>>(items)
    val updates = KObservable<Change>(Change.Nothing, single = true)
    val current = KObservable<XFile?>(null)
    val alerts = KObservable<String>(single = true)

    val checked: MutableList<MutableXFile> = ArrayList()
    val storeChecked = KObservable<List<XFile>>(ArrayList())

    fun notifyCurrent(item: XFile?) {
        logI("notifyCurrent $item")
        current.setAndNotify(item)
    }

    fun notifyUpdate(item: XFile) {
        logI("notifyUpdate $item")
        updates.setAndNotify(Change.Update(item))

        if (checked.contains(item)) {
            notifyChecked()
        }
    }

    fun notifyUpdateRange(items: List<XFile>) {
        logI("notifyUpdateRange ${items.size}")
        updates.setAndNotify(Change.UpdateRange(items))
    }

    fun notifyRemove(item: XFile) {
        logI("notifyRemove $item")
        updates.setAndNotify(Change.Remove(item))

        if (checked.remove(item)) {
            notifyChecked()
        }
    }

    fun notifyInsert(previous: XFile, item: XFile) {
        logI("notifyInsert $item after $previous")
        updates.setAndNotify(Change.Insert(previous, item))
    }

    fun notifyRemoveRange(items: List<XFile>) {
        logI("notifyRemoveRange ${items.size}")
        updates.setAndNotify(Change.RemoveRange(items))

        if (checked.removeAll(items)) {
            notifyChecked()
        }
    }

    fun notifyInsertRange(previous: XFile, items: List<XFile>) {
        logI("notifyInsert ${items.size} after $previous")
        updates.setAndNotify(Change.InsertRange(previous, items))
    }

    fun notifyItems() {
        logI("notifyItems ${items.size}")
        store.setAndNotify(items)

        val size = checked.size
        checked.filter { !items.contains(it) }
                .forEach { checked.remove(it) }
        if (checked.size != size) {
            notifyChecked()
        }
    }

    fun notifyChecked() {
        logI("notifyChecked")
        storeChecked.setAndNotify(ArrayList(checked))
    }
}