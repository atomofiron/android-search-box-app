package ru.atomofiron.regextool.screens.finder.adapter.holder

import android.view.ViewGroup
import android.widget.CheckBox
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.screens.finder.model.FinderStateItem

class ConfigHolder(
        parent: ViewGroup,
        id: Int,
        listener: OnActionListener
) : CardViewHolder(parent, id) {
    init {
        itemView.isFocusable = false
        itemView.isClickable = false

        itemView.findViewById<CheckBox>(R.id.config_cb_replace).setOnCheckedChangeListener { _, isChecked ->
            var item = item as FinderStateItem.Config
            item = item.copy(searchAndReplace = isChecked)
            listener.onConfigChange(item)
        }
    }

    interface OnActionListener {
        fun onConfigChange(item: FinderStateItem.Config)
    }
}