package ru.atomofiron.regextool.screens.finder.adapter.holder

import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.common.recycler.GeneralHolder
import ru.atomofiron.regextool.screens.finder.model.FinderStateItem

class FieldHolder(parent: ViewGroup, id: Int, listener: OnActionListener) :
        GeneralHolder<FinderStateItem>(parent, id) {

    private val etFind = itemView.findViewById<EditText>(R.id.item_find_rt_find)
    private val btnFind = itemView.findViewById<View>(R.id.item_find_ib_find)
    private val viewReplace = itemView.findViewById<View>(R.id.item_find_i_replace)

    init {
        btnFind.setOnClickListener {
            listener.onSearchClick(etFind.text.toString())
        }
    }

    override fun onBind(item: FinderStateItem, position: Int) {
        item as FinderStateItem.SearchAndReplace
        viewReplace.visibility = if (item.replaceEnabled) View.VISIBLE else View.GONE
        etFind.imeOptions = if (item.replaceEnabled) EditorInfo.IME_ACTION_NEXT else EditorInfo.IME_ACTION_SEARCH
    }
    interface OnActionListener {
        fun onSearchClick(value: String)
        fun onReplaceClick(value: String)
    }
}