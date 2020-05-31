package ru.atomofiron.regextool.injectable.channel

import app.atomofiron.common.util.KObservable
import ru.atomofiron.regextool.screens.result.adapter.FinderResultItem

class ResultChannel {
    val notifyItemChanged = KObservable<FinderResultItem.Item>(single = true)
}