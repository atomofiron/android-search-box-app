package app.atomofiron.searchboxapp.injectable.store

import app.atomofiron.common.util.KObservable

class ResultStore {
    val itemsShellBeDeleted = KObservable(Unit, single = true)
}