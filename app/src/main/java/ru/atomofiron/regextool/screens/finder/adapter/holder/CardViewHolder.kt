package ru.atomofiron.regextool.screens.finder.adapter.holder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.common.recycler.GeneralHolder
import ru.atomofiron.regextool.screens.finder.adapter.item.FinderItem

abstract class CardViewHolder(parent: ViewGroup, id: Int)
    : GeneralHolder<FinderItem>(wrapWithCard(parent, id)) {
    companion object {
        fun wrapWithCard(parent: ViewGroup, id: Int): View {
            val inflater = LayoutInflater.from(parent.context)
            val cardView = inflater.inflate(R.layout.layout_card, parent, false) as ViewGroup
            inflater.inflate(id, cardView, true)
            return cardView
        }
    }
}