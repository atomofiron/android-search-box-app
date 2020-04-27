package ru.atomofiron.regextool.screens.finder.adapter

import androidx.recyclerview.widget.DiffUtil
import ru.atomofiron.regextool.screens.finder.model.FinderStateItem

class FinderDiffUtilCallback(
        private val old: List<FinderStateItem>,
        private val new: List<FinderStateItem>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = old.size

    override fun getNewListSize(): Int = new.size

    override fun areItemsTheSame(i: Int, j: Int): Boolean {
        return old[i].layoutId == new[j].layoutId && old[i].stableId == new[j].stableId
    }

    override fun areContentsTheSame(i: Int, j: Int): Boolean = old[i] === new[j]
}