package ru.atomofiron.regextool.screens.finder.adapter.holder

import android.view.ViewGroup
import java.util.regex.Matcher

class TestHolder(parent: ViewGroup, id: Int) : CardViewHolder(parent, id) {
    init {
        itemView.isFocusable = false
        itemView.isClickable= false
    }
    interface OnActionListener {
        fun onTextChange(): Matcher?
    }
}