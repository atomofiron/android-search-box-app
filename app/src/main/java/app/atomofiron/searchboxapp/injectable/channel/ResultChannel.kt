package app.atomofiron.searchboxapp.injectable.channel

import app.atomofiron.common.util.flow.dataFlow
import app.atomofiron.searchboxapp.screens.result.adapter.FinderResultItem

class ResultChannel {
    val notifyItemChanged = dataFlow<FinderResultItem.Item>(single = true)
}