package ru.atomofiron.regextool.screens.finder.adapter.holder

import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.Util
import ru.atomofiron.regextool.common.recycler.GeneralHolder
import ru.atomofiron.regextool.screens.finder.adapter.FinderItem

class CharactersHolder(parent: ViewGroup, id: Int) : GeneralHolder<FinderItem>(parent, id) {

    init {
        val context = itemView.context
        itemView as ViewGroup
        itemView.removeAllViews()
        val sp = PreferenceManager.getDefaultSharedPreferences(context)

        val characters = sp!!.getString(Util.PREF_SPECIAL_CHARACTERS, Util.DEFAULT_SPECIAL_CHARACTERS)!!
                .trim { it <= ' ' }.split("[ ]+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (characters.isNotEmpty() && characters[0].isNotEmpty()) {
            for (c in characters) {
                val inflater = LayoutInflater.from(context)
                val view = inflater.inflate(R.layout.button_character, itemView, false) as Button
                view.text = c
                //view.setOnClickListener(this)
                itemView.addView(view)
            }
        }
    }
}