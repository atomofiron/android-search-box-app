package ru.atomofiron.regextool.screens.explorer.adapter

import android.view.View
import app.atomofiron.common.recycler.GeneralHolder
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.injectable.service.explorer.model.XFile
import ru.atomofiron.regextool.model.preference.ExplorerItemComposition
import ru.atomofiron.regextool.screens.explorer.adapter.util.ExplorerItemBinder
import ru.atomofiron.regextool.utils.Const
import ru.atomofiron.regextool.utils.Tool

class ExplorerHolder(itemView: View) : GeneralHolder<XFile>(itemView) {
    private val binder = ExplorerItemBinder(itemView)

    fun setOnItemActionListener(listener: ExplorerItemActionListener?) {
        binder.onItemActionListener = listener
    }

    init {
        val externalStoragePath = Tool.getExternalStorageDirectory(itemView.context)
        if (externalStoragePath != null) {
            binder.rootsAliases[externalStoragePath] = R.string.internal_storage
        }
        binder.rootsAliases[Const.SDCARD] = R.string.internal_storage
        binder.rootsAliases[Const.ROOT] = R.string.root
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