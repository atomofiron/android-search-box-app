package app.atomofiron.searchboxapp.injectable.channel

import app.atomofiron.common.util.flow.sharedFlow
import app.atomofiron.searchboxapp.screens.result.adapter.FinderResultItem

class ResultChannel {
    val notifyItemChanged = sharedFlow<FinderResultItem.Item>(single = true)
}