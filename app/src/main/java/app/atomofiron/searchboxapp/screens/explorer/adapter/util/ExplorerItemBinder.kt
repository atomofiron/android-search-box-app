package app.atomofiron.searchboxapp.screens.explorer.adapter.util

import app.atomofiron.searchboxapp.model.explorer.MediaDirectories
import app.atomofiron.searchboxapp.model.explorer.XFile
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.adapter.ExplorerItemActionListener

interface ExplorerItemBinder {
    fun setOnItemActionListener(listener: ExplorerItemActionListener?)
    fun onBind(item: XFile)
    fun bindComposition(composition: ExplorerItemComposition)
    fun setMediaDirectories(mediaDirectories: MediaDirectories)
    fun disableClicks()
    fun hideCheckBox()
    fun setGreyBackgroundColor(visible: Boolean = true)
}