package app.atomofiron.searchboxapp.screens.result.adapter

import androidx.recyclerview.widget.DiffUtil

class ResultDiffUtilCallback(
    private val old: List<FinderResultItem>,
    private val new: List<FinderResultItem>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = old.size

    override fun getNewListSize(): Int = new.size

    override fun areItemsTheSame(i: Int, j: Int): Boolean {
        if (i == 0 || j == 0) {
            return i == j
        }
        val old = old[i] as FinderResultItem.Item
        val new = new[i] as FinderResultItem.Item
        return old.item.mHashCode == new.item.mHashCode
    }

    override fun areContentsTheSame(i: Int, j: Int): Boolean {
        if (i == 0 || j == 0) {
            return i == j
        }
        val old = old[i] as FinderResultItem.Item
        val new = new[i] as FinderResultItem.Item
        return old.item.isDeleting == new.item.isDeleting
    }
}