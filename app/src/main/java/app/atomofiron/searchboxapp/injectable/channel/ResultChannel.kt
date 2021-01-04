package app.atomofiron.searchboxapp.injectable.channel

import app.atomofiron.common.util.flow.DataFlow
import app.atomofiron.searchboxapp.screens.result.adapter.FinderResultItem

class ResultChannel {
    val notifyItemChanged = DataFlow<FinderResultItem.Item>(single = true)
}