package app.atomofiron.searchboxapp.injectable.channel

import app.atomofiron.common.util.flow.ChannelFlow
import app.atomofiron.searchboxapp.screens.result.adapter.FinderResultItem

class ResultChannel {
    val notifyItemChanged = ChannelFlow<FinderResultItem.Item>()
}