package app.atomofiron.searchboxapp.injectable.channel

import app.atomofiron.common.util.flow.DataFlow
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel

class PreferenceChannel {
    val historyImportedEvent = DataFlow(Unit, single = true)
}