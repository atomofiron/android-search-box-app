package app.atomofiron.searchboxapp.injectable.channel

import app.atomofiron.common.util.flow.EventFlow
import app.atomofiron.searchboxapp.screens.result.adapter.FinderResultItem

class ResultChannel {
    val notifyItemChanged = EventFlow<FinderResultItem.Item>()
}