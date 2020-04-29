package ru.atomofiron.regextool.screens.result.adapter

import android.view.View
import app.atomofiron.common.recycler.GeneralHolder
import ru.atomofiron.regextool.model.finder.FinderResult
import ru.atomofiron.regextool.model.preference.ExplorerItemComposition
import ru.atomofiron.regextool.screens.explorer.adapter.util.ExplorerItemBinder

class ResultsHolder(itemView: View) : GeneralHolder<FinderResult>(itemView) {
    private val binder = ExplorerItemBinder(itemView)

    fun setOnItemActionListener(listener: ExplorerItemBinder.ExplorerItemBinderActionListener?) {
        binder.onItemActionListener = listener
    }

    override fun onBind(item: FinderResult, position: Int) {
        binder.onBind(item)
    }

    fun bindComposition(composition: ExplorerItemComposition) = binder.bindComposition(composition)
}