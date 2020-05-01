package ru.atomofiron.regextool.injectable.store

import app.atomofiron.common.util.KObservable

class ResultStore {
    val itemsChanged = KObservable(Unit, single = true)
}