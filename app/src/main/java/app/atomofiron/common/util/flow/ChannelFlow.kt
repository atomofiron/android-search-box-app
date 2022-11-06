package app.atomofiron.common.util.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch


class ChannelFlow<T>(channel: Channel<T> = Channel()) : Channel<T> by channel, Flow<T> by channel.receiveAsFlow()

fun <T> CoroutineScope.send(channel: Channel<T>, value: T) {
    launch {
        channel.send(value)
    }
}