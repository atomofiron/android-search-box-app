package app.atomofiron.searchboxapp.injectable.channel

import app.atomofiron.common.util.flow.ChannelFlow
import app.atomofiron.common.util.flow.invoke
import kotlinx.coroutines.CoroutineScope

class PreferenceChannel(
    private val scope: CoroutineScope,
) {
    val onHistoryImported = ChannelFlow<Unit>()

    fun notifyHistoryImported() = onHistoryImported.invoke(scope)
}