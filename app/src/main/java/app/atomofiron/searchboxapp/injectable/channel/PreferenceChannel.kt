package app.atomofiron.searchboxapp.injectable.channel

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel

class PreferenceChannel {
    val historyImportedEvent = BroadcastChannel<Unit>(Channel.BUFFERED)
}