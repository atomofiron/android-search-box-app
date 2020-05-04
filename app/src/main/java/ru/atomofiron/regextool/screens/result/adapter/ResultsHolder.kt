package ru.atomofiron.regextool.screens.result.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import app.atomofiron.common.recycler.GeneralHolder
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.model.finder.FinderResult
import ru.atomofiron.regextool.model.preference.ExplorerItemComposition
import ru.atomofiron.regextool.screens.explorer.adapter.util.ExplorerItemBinder
import ru.atomofiron.regextool.utils.setVisibility

class ResultsHolder(itemView: View) : GeneralHolder<FinderResult>(itemView) {
    private val binder = ExplorerItemBinder(itemView)

    init {
        itemView as ViewGroup
        LayoutInflater.from(itemView.context).inflate(R.layout.item_result_count, itemView)
    }

    private val tvCounter = itemView.findViewById<TextView>(R.id.result_tv_count)

    fun setOnItemActionListener(listener: ExplorerItemBinder.ExplorerItemBinderActionListener?) {
        binder.onItemActionListener = listener
    }

    override fun onBind(item: FinderResult, position: Int) {
        binder.onBind(item)
        tvCounter.setVisibility(item.count > 0)
        tvCounter.text = item.count.toString()
    }

    fun bindComposition(composition: ExplorerItemComposition) = binder.bindComposition(composition)
}