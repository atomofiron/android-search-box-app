package app.atomofiron.searchboxapp.screens.explorer.adapter

import android.view.View
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.searchboxapp.model.explorer.XFile
import app.atomofiron.searchboxapp.screens.explorer.adapter.util.ExplorerItemBinder
import app.atomofiron.searchboxapp.screens.explorer.adapter.util.ExplorerItemBinderImpl

class ExplorerHolder(itemView: View) : GeneralHolder<XFile>(itemView),
    ExplorerItemBinder by ExplorerItemBinderImpl(itemView) {

    override fun onBind(item: XFile, position: Int) = onBind(item)
}