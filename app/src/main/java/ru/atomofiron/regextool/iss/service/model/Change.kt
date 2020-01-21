package ru.atomofiron.regextool.iss.service.model

sealed class Change(val file: XFile) {
    class Update(file: XFile) : Change(file)
    class Remove(file: XFile) : Change(file)
    class Insert(file: XFile) : Change(file)
}