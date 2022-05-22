package app.atomofiron.searchboxapp.injectable.channel

import app.atomofiron.common.util.flow.sharedFlow

class PreferenceChannel {
    val historyImportedEvent = sharedFlow(Unit, single = true)
}