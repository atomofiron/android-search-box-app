package app.atomofiron.searchboxapp.injectable.store

import app.atomofiron.common.util.flow.ChannelFlow

class ResultStore {
    val itemsShellBeDeleted = ChannelFlow<Unit>()
}