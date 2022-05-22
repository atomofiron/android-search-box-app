package app.atomofiron.searchboxapp.injectable.channel

import app.atomofiron.common.util.flow.dataFlow

class PreferenceChannel {
    val historyImportedEvent = dataFlow(Unit, single = true)
}