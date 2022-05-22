package app.atomofiron.searchboxapp.injectable.store

import app.atomofiron.common.util.flow.sharedFlow

class ResultStore {
    val itemsShellBeDeleted = sharedFlow(Unit, single = true)
}