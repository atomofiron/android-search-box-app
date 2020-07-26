package app.atomofiron.searchboxapp.screens.finder.adapter

import androidx.recyclerview.widget.DiffUtil
import app.atomofiron.searchboxapp.screens.finder.model.FinderStateItem

class FinderDiffUtilCallback(
        private val old: List<FinderStateItem>,
        private val new: List<FinderStateItem>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = old.size

    override fun getNewListSize(): Int = new.size

    override fun areItemsTheSame(i: Int, j: Int): Boolean {
        val old = old[i]
        val new = new[j]
        return when {
            old.layoutId != new.layoutId -> false
            old.stableId != new.stableId -> false
            else -> false
        }
    }

    override fun areContentsTheSame(i: Int, j: Int): Boolean {
        val old = old[i]
        val new = new[j]
        return when (old) {
            is FinderStateItem.ProgressItem -> false
            else -> old.hashCode() == new.hashCode()
        }
    }
}