package ru.atomofiron.regextool.screens.finder.adapter.holder

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.common.recycler.GeneralHolder
import ru.atomofiron.regextool.screens.finder.model.FinderStateItem

class CharactersHolder(parent: ViewGroup, id: Int) : GeneralHolder<FinderStateItem>(parent, id) {

    override fun onBind(item: FinderStateItem, position: Int) {
        item as FinderStateItem.SpecialCharacters
        itemView as ViewGroup
        itemView.removeAllViews()

        if (item.characters.isNotEmpty() && item.characters[0].isNotEmpty()) {
            for (c in item.characters) {
                val inflater = LayoutInflater.from(itemView.context)
                val view = inflater.inflate(R.layout.button_character, itemView, false) as Button
                view.text = c
                //view.setOnClickListener(this)
                itemView.addView(view)
            }
        }
    }

    interface OnActionListener {
        fun onCharacterClick(value: String)
    }
}