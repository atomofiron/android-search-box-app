package ru.atomofiron.regextool.screens.finder.adapter.holder

import android.view.ViewGroup
import ru.atomofiron.regextool.screens.finder.model.FinderStateItem

class ResultHolder(parent: ViewGroup, id: Int) : CardViewHolder(parent, id) {
    interface OnActionListener {
        fun onItemClick(item: FinderStateItem.ResultItem)
    }
}