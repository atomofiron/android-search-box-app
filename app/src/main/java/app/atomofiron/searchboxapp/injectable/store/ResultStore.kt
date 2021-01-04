package app.atomofiron.searchboxapp.injectable.store

import app.atomofiron.common.util.flow.DataFlow

class ResultStore {
    val itemsShellBeDeleted = DataFlow(Unit, single = true)
}