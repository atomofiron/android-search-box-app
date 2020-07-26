package app.atomofiron.searchboxapp.injectable.channel

import app.atomofiron.common.util.KObservable
import app.atomofiron.searchboxapp.screens.result.adapter.FinderResultItem

class ResultChannel {
    val notifyItemChanged = KObservable<FinderResultItem.Item>(single = true)
}