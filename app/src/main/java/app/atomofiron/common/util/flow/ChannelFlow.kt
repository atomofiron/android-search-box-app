package app.atomofiron.common.util.flow

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow


class ChannelFlow<T>(channel: Channel<T> = Channel()) : Channel<T> by channel, Flow<T> by channel.receiveAsFlow()
