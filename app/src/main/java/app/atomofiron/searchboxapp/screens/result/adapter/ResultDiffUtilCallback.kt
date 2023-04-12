package app.atomofiron.searchboxapp.screens.result.adapter

import androidx.recyclerview.widget.DiffUtil

class ResultDiffUtilCallback(
    private val old: List<ResultItem>,
    private val new: List<ResultItem>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = old.size

    override fun getNewListSize(): Int = new.size

    override fun areItemsTheSame(i: Int, j: Int): Boolean {
        if (i == 0 || j == 0) {
            return i == j
        }
        val old = old[i] as ResultItem.Item
        val new = new[i] as ResultItem.Item
        return old.item.item.uniqueId == new.item.item.uniqueId
    }

    override fun areContentsTheSame(i: Int, j: Int): Boolean {
        if (i == 0 || j == 0) {
            return i == j
        }
        val old = old[i] as ResultItem.Item
        val new = new[i] as ResultItem.Item
        return old.item.isDeleting == new.item.isDeleting
    }
}