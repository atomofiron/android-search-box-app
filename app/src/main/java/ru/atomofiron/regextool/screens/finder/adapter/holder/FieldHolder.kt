package ru.atomofiron.regextool.screens.finder.adapter.holder

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import app.atomofiron.common.recycler.GeneralHolder
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.screens.finder.model.FinderStateItem
import ru.atomofiron.regextool.screens.finder.model.FinderStateItem.SearchAndReplaceItem
import ru.atomofiron.regextool.view.custom.RegexInputField
import java.util.regex.Pattern

class FieldHolder(parent: ViewGroup, id: Int, private val listener: OnActionListener) :
        GeneralHolder<FinderStateItem>(parent, id) {
    private val params: SearchAndReplaceItem get() = item as SearchAndReplaceItem

    private val etFind = itemView.findViewById<RegexInputField>(R.id.item_find_rt_find)
    private val btnFind = itemView.findViewById<View>(R.id.item_find_ib_find)
    private val viewReplace = itemView.findViewById<View>(R.id.item_find_i_replace)

    init {
        btnFind.setOnClickListener {
            listener.onSearchClick(etFind.text.toString())
        }
        etFind.addTextChangedListener(TextChangeListener())
    }

    override fun onBind(item: FinderStateItem, position: Int) {
        item as SearchAndReplaceItem
        viewReplace.visibility = if (item.replaceEnabled) View.VISIBLE else View.GONE
        etFind.imeOptions = if (item.replaceEnabled) EditorInfo.IME_ACTION_NEXT else EditorInfo.IME_ACTION_SEARCH
        if (etFind.text.toString() != item.query) {
            etFind.setText(item.query)
        } else {
            "OK"
        }
        updateWarning(etFind.text.toString())
    }

    private fun updateWarning(query: String) {
        if (params.useRegex) {
            try {
                Pattern.compile(query)
            } catch (e: Exception) {
                etFind.isActivated = true
                btnFind.visibility = View.GONE
                return
            }
        }
        etFind.isActivated = false
        btnFind.visibility = View.VISIBLE
    }

    interface OnActionListener {
        fun onSearchClick(value: String)
        fun onSearchChange(value: String)
        fun onReplaceClick(value: String)
    }

    inner class TextChangeListener : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        override fun afterTextChanged(s: Editable) {
            val value = s.toString()
            updateWarning(value)
            listener.onSearchChange(value)
        }
    }
}