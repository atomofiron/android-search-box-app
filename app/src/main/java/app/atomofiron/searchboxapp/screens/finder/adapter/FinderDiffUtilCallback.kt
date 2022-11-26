package app.atomofiron.searchboxapp.screens.finder.adapter

import androidx.recyclerview.widget.DiffUtil
import app.atomofiron.searchboxapp.screens.finder.model.FinderStateItem

class FinderDiffUtilCallback : DiffUtil.ItemCallback<FinderStateItem>() {

    override fun areItemsTheSame(oldItem: FinderStateItem, newItem: FinderStateItem): Boolean = when {
        oldItem.layoutId != newItem.layoutId -> false
        oldItem.stableId != newItem.stableId -> false
        else -> true
    }

    override fun areContentsTheSame(oldItem: FinderStateItem, newItem: FinderStateItem): Boolean {
        return when (oldItem) {
            is FinderStateItem.ProgressItem -> false
            else -> oldItem.hashCode() == newItem.hashCode()
        }
    }
}