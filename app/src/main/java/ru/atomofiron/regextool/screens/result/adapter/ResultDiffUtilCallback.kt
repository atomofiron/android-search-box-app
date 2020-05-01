package ru.atomofiron.regextool.screens.result.adapter

import androidx.recyclerview.widget.DiffUtil
import ru.atomofiron.regextool.model.finder.FinderResult

class ResultDiffUtilCallback(
        private val old: List<FinderResult>,
        private val new: List<FinderResult>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = old.size

    override fun getNewListSize(): Int = new.size

    override fun areItemsTheSame(i: Int, j: Int): Boolean = old[i].mHashCode == new[j].mHashCode

    override fun areContentsTheSame(i: Int, j: Int): Boolean = old[i].isDeleting == new[j].isDeleting
}