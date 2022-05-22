package app.atomofiron.searchboxapp.screens.result.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.adapter.util.ExplorerItemBinder

class ResultsItemHolder(itemView: View) : ResultsHolder(itemView) {
    private val binder = ExplorerItemBinder(itemView)

    init {
        itemView as ViewGroup
        LayoutInflater.from(itemView.context).inflate(R.layout.item_result_count, itemView)
    }

    private val tvCounter = itemView.findViewById<TextView>(R.id.result_tv_count)

    fun setOnItemActionListener(listener: ExplorerItemBinder.ExplorerItemBinderActionListener?) {
        binder.onItemActionListener = listener
    }

    override fun onBind(item: FinderResultItem, position: Int) {
        item as FinderResultItem.Item
        val result = item.item
        binder.onBind(result)
        tvCounter.isVisible = result.count > 0
        tvCounter.text = result.count.toString()
    }

    fun bindComposition(composition: ExplorerItemComposition) = binder.bindComposition(composition)
}