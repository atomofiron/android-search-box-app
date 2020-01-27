package ru.atomofiron.regextool.iss.service.model

sealed class Change {
    object Nothing : Change()
    class Current(val file: XFile?) : Change()
    class Update(val file: XFile) : Change()
    class Remove(val file: XFile) : Change()
    class Insert(val previous: XFile, val file: XFile) : Change()
    class RemoveRange(val files: List<XFile>) : Change()
    class InsertRange(val previous: XFile, val files: List<XFile>) : Change()
}