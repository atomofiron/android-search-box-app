package ru.atomofiron.regextool.view.custom.menu

import android.content.Context
import androidx.appcompat.view.menu.MenuBuilder

class MenuImpl(context: Context) : MenuBuilder(context) {

    private var menuChangedListener: (() -> Unit)? = null

    override fun onItemsChanged(structureChanged: Boolean) {
        super.onItemsChanged(structureChanged)
        menuChangedListener?.invoke()
    }

    fun setMenuChangedListener(listener: () -> Unit) {
        menuChangedListener = listener
    }
}