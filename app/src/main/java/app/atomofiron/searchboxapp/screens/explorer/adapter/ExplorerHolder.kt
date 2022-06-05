package app.atomofiron.searchboxapp.screens.explorer.adapter

import android.view.View
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.searchboxapp.model.explorer.XFile
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.adapter.util.ExplorerItemBinder

class ExplorerHolder(itemView: View) : GeneralHolder<XFile>(itemView) {
    private val binder = ExplorerItemBinder(itemView)

    fun setOnItemActionListener(listener: ExplorerItemActionListener?) {
        binder.onItemActionListener = listener
    }

    override fun onBind(item: XFile, position: Int) = binder.onBind(item)

    fun bindComposition(composition: ExplorerItemComposition) = binder.bindComposition(composition)

    fun disableClicks() = binder.disableClicks()

    fun hideCheckBox() = binder.hideCheckBox()

    fun setGreyBackgroundColor(visible: Boolean = true) = binder.setGreyBackgroundColor(visible)
}