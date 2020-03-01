package ru.atomofiron.regextool.screens.finder.adapter.holder

import android.view.ViewGroup
import android.widget.CheckBox
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
    private var skipUpdate = false

    init {
        itemView.isFocusable = false
        itemView.isClickable = false

        cbCaseSense.setOnCheckedChangeListener { _, isChecked ->
            update { it.copy(ignoreCase = !isChecked) }
        }
        cbUseRegexp.setOnCheckedChangeListener { _, isChecked ->
            when {
                isChecked && checkMultilineWhenEnabled -> {
                    checkMultilineWhenEnabled = false
                    update { it.copy(useRegexp = isChecked, multilineSearch = true) }
                }
                !isChecked -> {
                    checkMultilineWhenEnabled = cbMultiline.isChecked
                    update { it.copy(useRegexp = isChecked, multilineSearch = false) }
                }
                isChecked -> update { it.copy(useRegexp = isChecked) }
            }
            cbMultiline.isEnabled = isChecked
        }
        cpSearchInContent.setOnCheckedChangeListener { _, isChecked ->
            update { it.copy(searchInContent = isChecked) }
        }
        cbMultiline.setOnCheckedChangeListener { _, isChecked ->
            update { it.copy(multilineSearch = isChecked) }
        }
        cbReplace.setOnCheckedChangeListener { _, isChecked ->
            update { it.copy(searchAndReplace = isChecked) }
        }
    }

    override fun onBind(item: FinderStateItem, position: Int) {
        item as FinderStateItem.ConfigItem
        skipUpdate = true
        cbCaseSense.isChecked = !item.ignoreCase
        cbUseRegexp.isChecked = item.useRegexp
        cpSearchInContent.isChecked = item.searchInContent
        cbMultiline.isChecked = item.multilineSearch
        cbReplace.isChecked = item.replaceEnabled
        skipUpdate = false
    }

    private fun update(block: (FinderStateItem.ConfigItem) -> FinderStateItem.ConfigItem) {
        if (skipUpdate) {
            return
        }
        var item = item as FinderStateItem.ConfigItem
        item = block(item)
        listener.onConfigChange(item)
    }

    interface OnActionListener {
        fun onConfigChange(item: FinderStateItem.ConfigItem)
    }
}