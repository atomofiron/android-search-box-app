package app.atomofiron.searchboxapp.injectable.store

import app.atomofiron.common.util.flow.dataFlow

class ResultStore {
    val itemsShellBeDeleted = dataFlow(Unit, single = true)
}