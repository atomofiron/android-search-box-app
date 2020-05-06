package ru.atomofiron.regextool.screens.finder.adapter.holder

import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.screens.finder.model.FinderStateItem

class ConfigHolder(
        parent: ViewGroup,
        id: Int,
        private val listener: OnActionListener
) : CardViewHolder(parent, id) {
    private val cbCaseSense = itemView.findViewById<CheckBox>(R.id.config_cb_case_sense)
    private val cbUseRegexp = itemView.findViewById<CheckBox>(R.id.config_cb_use_regexp)
    private val cpSearchInContent = itemView.findViewById<CheckBox>(R.id.config_cb_in_content)
    private val cbMultiline = itemView.findViewById<CheckBox>(R.id.config_cb_multiline)
    private val cbReplace = itemView.findViewById<CheckBox>(R.id.config_cb_replace)

    private var checkMultilineWhenEnabled = false

    init {
        itemView.isFocusable = false
        itemView.isClickable = false

        cbCaseSense.setOnClickListener { view ->
            view as CompoundButton
            update { it.copy(ignoreCase = !view.isChecked) }
        }
        cbUseRegexp.setOnClickListener { view ->
            view as CompoundButton
            when {
                view.isChecked && checkMultilineWhenEnabled -> {
                    checkMultilineWhenEnabled = false
                    update { it.copy(useRegex = view.isChecked, multilineSearch = true) }
                }
                !view.isChecked -> {
                    checkMultilineWhenEnabled = cbMultiline.isChecked
                    update { it.copy(useRegex = view.isChecked, multilineSearch = false) }
                }
                view.isChecked -> update { it.copy(useRegex = view.isChecked) }
            }
            cbMultiline.isEnabled = view.isChecked
        }
        cpSearchInContent.setOnClickListener { view ->
            view as CompoundButton
            update { it.copy(searchInContent = view.isChecked) }
        }
        cbMultiline.setOnClickListener { view ->
            view as CompoundButton
            update { it.copy(multilineSearch = view.isChecked) }
        }
        cbReplace.setOnClickListener { view ->
            view as CompoundButton
            update { it.copy(replaceEnabled = view.isChecked) }
        }
    }

    override fun onBind(item: FinderStateItem, position: Int) {
        item as FinderStateItem.ConfigItem
        cbCaseSense.isChecked = !item.ignoreCase
        cbUseRegexp.isChecked = item.useRegex
        cpSearchInContent.isChecked = item.searchInContent
        cbMultiline.isChecked = item.multilineSearch
        cbReplace.isChecked = item.replaceEnabled
    }

    private fun update(block: (FinderStateItem.ConfigItem) -> FinderStateItem.ConfigItem) {
        var item = item as FinderStateItem.ConfigItem
        item = block(item)
        listener.onConfigChange(item)
    }

    interface OnActionListener {
        fun onConfigChange(item: FinderStateItem.ConfigItem)
    }
}