package ru.atomofiron.regextool.screens.finder.adapter.holder

import android.view.ViewGroup

class TestHolder(parent: ViewGroup, id: Int) : CardViewHolder(parent, id) {
    init {
        itemView.isFocusable = false
        itemView.isClickable= false
    }
}