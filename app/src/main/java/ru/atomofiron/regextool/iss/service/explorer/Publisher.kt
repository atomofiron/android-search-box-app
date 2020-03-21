package ru.atomofiron.regextool.iss.service.explorer

import app.atomofiron.common.util.KObservable
import ru.atomofiron.regextool.iss.service.explorer.model.Change
import ru.atomofiron.regextool.iss.service.explorer.model.MutableXFile
import ru.atomofiron.regextool.iss.service.explorer.model.XFile
import ru.atomofiron.regextool.log2

class Publisher {
    val files: MutableList<MutableXFile> = ArrayList()
    val store = KObservable<List<XFile>>(files)
    val updates = KObservable<Change>(Change.Nothing, single = true)

    fun notifyCurrent(file: XFile?) {
        log2("notifyCurrent $file")
        updates.setAndNotify(Change.Current(file))
    }

    fun notifyUpdate(file: XFile) {
        log2("notifyUpdate $file")
        updates.setAndNotify(Change.Update(file))
    }

    fun notifyRemove(file: XFile) {
        log2("notifyRemove $file")
        updates.setAndNotify(Change.Remove(file))
    }

    fun notifyInsert(previous: XFile, file: XFile) {
        log2("notifyInsert $file after $previous")
        updates.setAndNotify(Change.Insert(previous, file))
    }

    fun notifyRemoveRange(files: List<XFile>) {
        log2("notifyRemoveRange ${files.size}")
        updates.setAndNotify(Change.RemoveRange(files))
    }

    fun notifyInsertRange(previous: XFile, files: List<XFile>) {
        log2("notifyInsert ${files.size} after $previous")
        updates.setAndNotify(Change.InsertRange(previous, files))
    }

    fun notifyFiles() {
        log2("notifyFiles")
        store.setAndNotify(files)
    }
}