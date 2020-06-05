package ru.atomofiron.regextool.screens.explorer.adapter

import android.view.View
import app.atomofiron.common.recycler.GeneralHolder
import ru.atomofiron.regextool.model.explorer.XFile
import ru.atomofiron.regextool.model.preference.ExplorerItemComposition
import ru.atomofiron.regextool.screens.explorer.adapter.util.ExplorerItemBinder

class ExplorerHolder(itemView: View) : GeneralHolder<XFile>(itemView) {
    private val binder = ExplorerItemBinder(itemView)

    fun setOnItemActionListener(listener: ExplorerItemActionListener?) {
        binder.onItemActionListener = listener
    }


    override fun onBind(item: XFile, position: Int) = binder.onBind(item)

    fun bindComposition(composition: ExplorerItemComposition) = binder.bindComposition(composition)

    fun removeBackground() {
        itemView.background = null
        itemView.isFocusable = false
        itemView.isClickable = false
    }

    fun disableCheckBox() = binder.disableCheckBox()
}