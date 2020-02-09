package ru.atomofiron.regextool.screens.finder.adapter.holder

import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.common.recycler.GeneralHolder
import ru.atomofiron.regextool.screens.finder.adapter.item.FinderItem

class FieldHolder(parent: ViewGroup, id: Int, onSubmitListener: (String) -> Unit) :
        GeneralHolder<FinderItem>(parent, id) {

    private val etFind = itemView.findViewById<EditText>(R.id.item_find_rt_find)
    private val btnFind = itemView.findViewById<View>(R.id.item_find_ib_find)
    private val viewReplace = itemView.findViewById<View>(R.id.item_find_i_replace)

    init {
        btnFind.setOnClickListener {
            onSubmitListener(etFind.text.toString())
        }
    }

    override fun onBind(item: FinderItem, position: Int) {
        item as FinderItem.FieldItem
        viewReplace.visibility = if (item.replace) View.VISIBLE else View.GONE
        etFind.imeOptions = if (item.replace) EditorInfo.IME_ACTION_NEXT else EditorInfo.IME_ACTION_SEARCH
    }
}