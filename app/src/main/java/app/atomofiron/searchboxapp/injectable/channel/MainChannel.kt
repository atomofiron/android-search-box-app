package app.atomofiron.searchboxapp.injectable.channel

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel

object MainChannel {
    val channel = BroadcastChannel<Int>(Channel.BUFFERED)
}